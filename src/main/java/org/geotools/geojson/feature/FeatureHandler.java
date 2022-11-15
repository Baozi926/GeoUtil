//用于覆盖源码
package org.geotools.geojson.feature;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.DelegatingHandler;
import org.geotools.geojson.IContentHandler;
import org.geotools.geojson.geom.GeometryCollectionHandler;
import org.geotools.geojson.geom.GeometryHandler;
import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeatureHandler extends DelegatingHandler<SimpleFeature> {
    private int fid;
    private String separator;
    String id;
    Geometry geometry;
    List<Object> values;
    List<String> properties;
    CoordinateReferenceSystem crs;
    SimpleFeatureBuilder builder;
    AttributeIO attio;
    SimpleFeature feature;
    private String baseId;
    private boolean autoFID;

    public FeatureHandler() {
        this((SimpleFeatureBuilder) null, new DefaultAttributeIO());
    }

    public FeatureHandler(SimpleFeatureBuilder builder, AttributeIO attio) {
        this.fid = 0;
        this.separator = "-";
        this.baseId = "feature";
        this.autoFID = false;
        this.builder = builder;
        this.attio = attio;
    }

    public boolean startObject() throws ParseException, IOException {
        if (this.properties == NULL_LIST) {
            this.properties = new ArrayList();
        } else if (this.properties != null) {
            this.delegate = new GeometryHandler(new GeometryFactory());
        }

        return super.startObject();
    }

    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if ("id".equals(key) && this.properties == null) {
            this.id = "";
            return true;
        } else if ("crs".equals(key) && this.properties == null) {
            this.delegate = new CRSHandler();
            return true;
        } else if ("geometry".equals(key) && this.properties == null) {
            this.delegate = new GeometryHandler(new GeometryFactory());
            return true;
        } else {
            if ("properties".equals(key) && this.delegate == NULL) {
                this.properties = NULL_LIST;
                this.values = new ArrayList();
            } else if (this.properties != null && this.delegate == NULL) {
                this.properties.add(key);
                return true;
            }

            return super.startObjectEntry(key);
        }
    }

    public boolean startArray() throws ParseException, IOException {
        if (this.properties != null && this.delegate == NULL) {
            this.delegate = new ArrayHandler();
        }

        return super.startArray();
    }

    public boolean endArray() throws ParseException, IOException {
        if (this.delegate instanceof ArrayHandler) {
            super.endArray();
            this.values.add(((ArrayHandler) this.delegate).getValue());
            this.delegate = NULL;
        }

        return super.endArray();
    }

    public boolean endObject() throws ParseException, IOException {
        if (!(this.delegate instanceof IContentHandler)) {
            if (this.delegate == UNINITIALIZED) {
                this.delegate = NULL;
                return true;
            } else if (this.properties != null) {
                if (this.builder == null) {
                    this.builder = this.createBuilder();
                }

                for (int i = 0; i < this.properties.size(); ++i) {
                    String att = (String) this.properties.get(i);
                    Object val = this.values.get(i);
                    if (val instanceof String) {
                        val = this.attio.parse(att, (String) val);
                    }

                    this.builder.set(att, val);
                }

                this.properties = null;
                this.values = null;
                return true;
            } else {
                this.feature = this.buildFeature();
                this.id = null;
                this.geometry = null;
                this.properties = null;
                this.values = null;
                return true;
            }
        } else {
            this.delegate.endObject();
            if (this.delegate instanceof GeometryHandler) {
                GeometryHandler geometryHandler = (GeometryHandler) this.delegate;
                Geometry g = (Geometry) geometryHandler.getValue();
                if (g != null || !(((GeometryHandler) this.delegate).getDelegate() instanceof GeometryCollectionHandler)) {
                    if (this.properties != null) {
                        this.values.add(g);
                    } else {
                        this.geometry = g;
                    }

                    this.delegate = NULL;
                }
            } else if (this.delegate instanceof CRSHandler) {
                this.crs = ((CRSHandler) this.delegate).getValue();
                this.delegate = UNINITIALIZED;
            }

            return true;
        }
    }

    public boolean primitive(Object value) throws ParseException, IOException {
        if (this.delegate instanceof GeometryHandler && value == null) {
            this.delegate = NULL;
            return true;
        } else if ("".equals(this.id)) {
            this.id = value.toString();
            this.setFID(this.id);
            return true;
        } else if (this.values != null && this.delegate == NULL) {
            this.values.add(value);
            return true;
        } else {
            return super.primitive(value);
        }
    }

    public SimpleFeature getValue() {
        return this.feature;
    }

    public CoordinateReferenceSystem getCRS() {
        return this.crs;
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public void init() {
        this.feature = null;
    }

    SimpleFeatureBuilder createBuilder() {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("feature");
        typeBuilder.setNamespaceURI("http://geotools.org");
        typeBuilder.setCRS(this.crs);
        if (this.properties != null) {
            for (int i = 0; i < this.properties.size(); ++i) {
                String prop = (String) this.properties.get(i);
                Object valu = this.values.get(i);
                typeBuilder.add(prop, valu != null ? valu.getClass() : Object.class);
            }
        }

        if (this.geometry != null) {
            this.addGeometryType(typeBuilder, this.geometry);
        }

        return new SimpleFeatureBuilder(typeBuilder.buildFeatureType());
    }

    void addGeometryType(SimpleFeatureTypeBuilder typeBuilder, Geometry geometry) {

        Class geometryClass = geometry != null ? geometry.getClass() : Geometry.class;


        typeBuilder.add("geometry", geometryClass);
        typeBuilder.setDefaultGeometry("geometry");
    }

    SimpleFeature buildFeature() {
        SimpleFeatureBuilder builder = this.builder != null ? this.builder : this.createBuilder();
        SimpleFeatureType featureType = builder.getFeatureType();
        SimpleFeature f = builder.buildFeature(this.getFID());
        if (this.geometry != null) {
            if (featureType.getGeometryDescriptor() == null) {
                SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
                typeBuilder.init(featureType);
                this.addGeometryType(typeBuilder, this.geometry);
                featureType = typeBuilder.buildFeatureType();
                SimpleFeatureBuilder newBuilder = new SimpleFeatureBuilder(featureType);
                newBuilder.init(f);
                f = newBuilder.buildFeature(this.getFID());
            }

            f.setAttribute(featureType.getGeometryDescriptor().getLocalName(), this.geometry);
        }
        this.builder = null; //重置builder，而不是沿用第一条数据的builder，例如，如果第一个条数据是LineString, 第二条是 MultiLineString，如果不重置builder，第二条数据会被解析为LineString，因此会丢失信息
        this.incrementFID();
        return f;
    }

    private void incrementFID() {
        ++this.fid;
    }

    private void setFID(String f) {
        int index = f.lastIndexOf(46);
        if (index < 0) {
            index = f.indexOf(45);
            if (index < 0) {
                this.autoFID = false;
                this.id = f;
                return;
            }

            this.separator = "-";
        } else {
            this.separator = ".";
        }

        this.baseId = f.substring(0, index);

        try {
            this.fid = Integer.parseInt(f.substring(index + 1));
        } catch (NumberFormatException var4) {
            this.autoFID = false;
            this.id = f;
        }

    }

    private String getFID() {
        return this.id != null && !this.autoFID ? this.id : this.baseId + this.separator + this.fid;
    }
}
