//用于覆盖源码
package org.geotools.geojson.feature;

import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONStreamAware;
import org.json.simple.parser.JSONParser;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class FeatureJSON {
    GeometryJSON gjson;
    SimpleFeatureType featureType;
    AttributeIO attio;
    boolean encodeFeatureBounds;
    boolean encodeFeatureCollectionBounds;
    boolean encodeFeatureCRS;
    boolean encodeFeatureCollectionCRS;
    boolean encodeNullValues;

    public FeatureJSON() {
        this(new GeometryJSON());
    }

    public FeatureJSON(GeometryJSON gjson) {
        this.encodeFeatureBounds = false;
        this.encodeFeatureCollectionBounds = false;
        this.encodeFeatureCRS = false;
        this.encodeFeatureCollectionCRS = false;
        this.encodeNullValues = false;
        this.gjson = gjson;
        this.attio = new DefaultAttributeIO();
    }

    public void setFeatureType(SimpleFeatureType featureType) {
        this.featureType = featureType;
        this.attio = new FeatureTypeAttributeIO(featureType);
    }

    public void setEncodeFeatureBounds(boolean encodeFeatureBounds) {
        this.encodeFeatureBounds = encodeFeatureBounds;
    }

    public boolean isEncodeFeatureBounds() {
        return this.encodeFeatureBounds;
    }

    public void setEncodeFeatureCollectionBounds(boolean encodeFeatureCollectionBounds) {
        this.encodeFeatureCollectionBounds = encodeFeatureCollectionBounds;
    }

    public boolean isEncodeFeatureCollectionBounds() {
        return this.encodeFeatureCollectionBounds;
    }

    public void setEncodeFeatureCRS(boolean encodeFeatureCRS) {
        this.encodeFeatureCRS = encodeFeatureCRS;
    }

    public boolean isEncodeFeatureCRS() {
        return this.encodeFeatureCRS;
    }

    public void setEncodeFeatureCollectionCRS(boolean encodeFeatureCollectionCRS) {
        this.encodeFeatureCollectionCRS = encodeFeatureCollectionCRS;
    }

    public boolean isEncodeFeatureCollectionCRS() {
        return this.encodeFeatureCollectionCRS;
    }

    public void setEncodeNullValues(boolean encodeNullValues) {
        this.encodeNullValues = encodeNullValues;
    }

    public boolean isEncodeNullValues() {
        return this.encodeNullValues;
    }

    public void writeFeature(SimpleFeature feature, Object output) throws IOException {
        GeoJSONUtil.encode((new FeatureJSON.FeatureEncoder(feature)).toJSONString(), output);
    }

    public void writeFeature(SimpleFeature feature, OutputStream output) throws IOException {
        this.writeFeature(feature, (Object)output);
    }

    public String toString(SimpleFeature feature) throws IOException {
        StringWriter w = new StringWriter();
        this.writeFeature(feature, (Object)w);
        return w.toString();
    }

    public SimpleFeature readFeature(Object input) throws IOException {
        return (SimpleFeature)GeoJSONUtil.parse(new FeatureHandler(this.featureType != null ? new SimpleFeatureBuilder(this.featureType) : null, this.attio), input, false);
    }

    public SimpleFeature readFeature(InputStream input) throws IOException {
        return this.readFeature((Object)input);
    }

    public void writeFeatureCollection(FeatureCollection features, Object output) throws IOException {
        LinkedHashMap<String, Object> obj = new LinkedHashMap();
        obj.put("type", "FeatureCollection");
        if (features.getSchema().getGeometryDescriptor() != null) {
            ReferencedEnvelope bounds = features.getBounds();
            CoordinateReferenceSystem crs = bounds != null ? bounds.getCoordinateReferenceSystem() : null;
            if (bounds != null && this.encodeFeatureCollectionBounds) {
                JSONStreamAware writer = (out) -> {
                    JSONArray.writeJSONString(Arrays.asList(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()), out);
                };
                obj.put("bbox", writer);
            }

            if (crs != null && (this.encodeFeatureCollectionCRS || !this.isStandardCRS(crs))) {
                obj.put("crs", this.createCRS(crs));
            }
        }

        obj.put("features", new FeatureJSON.FeatureCollectionEncoder(features, this.gjson));
        GeoJSONUtil.encode(obj, output);
    }

    private boolean isStandardCRS(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return true;
        } else {
            try {
                CoordinateReferenceSystem standardCRS = CRS.decode("EPSG:4326");
                return CRS.equalsIgnoreMetadata(crs, standardCRS);
            } catch (Exception var3) {
                return false;
            }
        }
    }

    public void writeFeatureCollection(FeatureCollection features, OutputStream output) throws IOException {
        this.writeFeatureCollection(features, (Object)output);
    }

    public FeatureCollection readFeatureCollection(Object input) throws IOException {
        DefaultFeatureCollection features = new DefaultFeatureCollection((String)null, (SimpleFeatureType)null);
        FeatureJSON.FeatureCollectionIterator it = (FeatureJSON.FeatureCollectionIterator)this.streamFeatureCollection(input);
        Throwable var4 = null;

        DefaultFeatureCollection var5;
        try {
            while(it.hasNext()) {
                features.add(it.next());
            }

            if (features.getSchema() != null && features.getSchema().getCoordinateReferenceSystem() == null && it.getHandler().getCRS() != null) {
                try {
                    ForceCoordinateSystemFeatureResults var19 = new ForceCoordinateSystemFeatureResults(features, it.getHandler().getCRS());
                    return var19;
                } catch (SchemaException var16) {
                    throw (IOException)(new IOException()).initCause(var16);
                }
            }

            var5 = features;
        } catch (Throwable var17) {
            var4 = var17;
            throw var17;
        } finally {
            if (it != null) {
                if (var4 != null) {
                    try {
                        it.close();
                    } catch (Throwable var15) {
                        var4.addSuppressed(var15);
                    }
                } else {
                    it.close();
                }
            }

        }

        return var5;
    }

    public FeatureCollection readFeatureCollection(InputStream input) throws IOException {
        return this.readFeatureCollection((Object)input);
    }

    public FeatureIterator<SimpleFeature> streamFeatureCollection(Object input) throws IOException {
        return new FeatureJSON.FeatureCollectionIterator(input);
    }

    public String toString(FeatureCollection features) throws IOException {
        StringWriter w = new StringWriter();
        this.writeFeatureCollection(features, (Object)w);
        return w.toString();
    }

    public void writeCRS(CoordinateReferenceSystem crs, Object output) throws IOException {
        GeoJSONUtil.encode(this.createCRS(crs), output);
    }

    public void writeCRS(CoordinateReferenceSystem crs, OutputStream output) throws IOException {
        this.writeCRS(crs, (Object)output);
    }

    Map<String, Object> createCRS(CoordinateReferenceSystem crs) throws IOException {
        Map<String, Object> obj = new LinkedHashMap();
        obj.put("type", "name");
        Map<String, Object> props = new LinkedHashMap();
        if (crs == null) {
            props.put("name", "EPSG:4326");
        } else {
            try {
                String identifier = CRS.lookupIdentifier(crs, true);
                props.put("name", identifier);
            } catch (FactoryException var5) {
                throw (IOException)(new IOException("Error looking up crs identifier")).initCause(var5);
            }
        }

        obj.put("properties", props);
        return obj;
    }

    public CoordinateReferenceSystem readCRS(Object input) throws IOException {
        return (CoordinateReferenceSystem)GeoJSONUtil.parse(new CRSHandler(), input, false);
    }

    public CoordinateReferenceSystem readCRS(InputStream input) throws IOException {
        return this.readCRS((Object)input);
    }

    public SimpleFeatureType readFeatureCollectionSchema(Object input, boolean nullValuesEncoded) throws IOException {
        return (SimpleFeatureType)GeoJSONUtil.parse(new FeatureTypeHandler(nullValuesEncoded), input, false);
    }

    public SimpleFeatureType readFeatureCollectionSchema(InputStream input, boolean nullValuesEncoded) throws IOException {
        return this.readFeatureCollectionSchema((Object)input, false);
    }

    public String toString(CoordinateReferenceSystem crs) throws IOException {
        StringWriter writer = new StringWriter();
        this.writeCRS(crs, (Object)writer);
        return writer.toString();
    }

    class FeatureCollectionIterator implements FeatureIterator<SimpleFeature> {
        Reader reader;
        FeatureCollectionHandler handler;
        JSONParser parser;
        SimpleFeature next;

        FeatureCollectionIterator(Object input) {
            try {
                this.reader = GeoJSONUtil.toReader(input);
            } catch (IOException var4) {
                throw new RuntimeException(var4);
            }

            this.parser = new JSONParser();
        }

        FeatureCollectionHandler getHandler() {
            return this.handler;
        }

        public boolean hasNext() {
            if (this.next != null) {
                return true;
            } else {
                if (this.handler == null) {
                    this.handler = new FeatureCollectionHandler(FeatureJSON.this.featureType, FeatureJSON.this.attio);
                }

                this.next = this.readNext();
                return this.next != null;
            }
        }

        public SimpleFeature next() {
            SimpleFeature feature = this.next;
            this.next = null;
            return feature;
        }

        SimpleFeature readNext() {
            try {
                this.parser.parse(this.reader, this.handler, true);
                return this.handler.getValue();
            } catch (Exception var2) {
                throw new RuntimeException(var2);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void close() {
            if (this.reader != null) {
                try {
                    this.reader.close();
                } catch (IOException var2) {
                }

                this.reader = null;
            }

            this.parser = null;
            this.handler = null;
        }
    }

    class FeatureCollectionEncoder implements JSONStreamAware {
        FeatureCollection features;
        GeometryJSON gjson;

        public FeatureCollectionEncoder(FeatureCollection features, GeometryJSON gjson) {
            this.features = features;
            this.gjson = gjson;
        }

        public void writeJSONString(Writer out) throws IOException {
            FeatureJSON.FeatureEncoder featureEncoder = FeatureJSON.this.new FeatureEncoder((SimpleFeatureType)this.features.getSchema());
            out.write("[");
            FeatureIterator i = this.features.features();
            Throwable var4 = null;

            try {
                if (i.hasNext()) {
                    SimpleFeature f = (SimpleFeature)i.next();
                    out.write(featureEncoder.toJSONString(f));

                    while(i.hasNext()) {
                        out.write(",");
                        f = (SimpleFeature)i.next();
                        out.write(featureEncoder.toJSONString(f));
                    }
                }
            } catch (Throwable var13) {
                var4 = var13;
                throw var13;
            } finally {
                if (i != null) {
                    if (var4 != null) {
                        try {
                            i.close();
                        } catch (Throwable var12) {
                            var4.addSuppressed(var12);
                        }
                    } else {
                        i.close();
                    }
                }

            }

            out.write("]");
            out.flush();
        }
    }

    class FeatureEncoder implements JSONAware {
        SimpleFeatureType featureType;
        SimpleFeature feature;

        public FeatureEncoder(SimpleFeature feature) {
            this((SimpleFeatureType)feature.getType());
            this.feature = feature;
        }

        public FeatureEncoder(SimpleFeatureType featureType) {
            this.featureType = featureType;
        }

        public String toJSONString(SimpleFeature feature) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            GeoJSONUtil.entry("type", "Feature", sb);
            sb.append(",");
            if (FeatureJSON.this.encodeFeatureCRS) {
                CoordinateReferenceSystem crs = feature.getFeatureType().getCoordinateReferenceSystem();
                if (crs != null) {
                    try {
                        GeoJSONUtil.string("crs", sb).append(":");
                        sb.append(FeatureJSON.this.toString(crs)).append(",");
                    } catch (IOException var8) {
                        throw new RuntimeException(var8);
                    }
                }
            }

            if (FeatureJSON.this.encodeFeatureBounds) {
                BoundingBox bbox = feature.getBounds();
                GeoJSONUtil.string("bbox", sb).append(":");
                sb.append(FeatureJSON.this.gjson.toString(bbox)).append(",");
            }

            if (feature.getDefaultGeometry() != null) {
                GeoJSONUtil.string("geometry", sb).append(":").append(FeatureJSON.this.gjson.toString((Geometry)feature.getDefaultGeometry()));
                sb.append(",");
            }

            int gindex = this.featureType.getGeometryDescriptor() != null ? this.featureType.indexOf(this.featureType.getGeometryDescriptor().getLocalName()) : -1;
            GeoJSONUtil.string("properties", sb).append(":").append("{");
            boolean attributesWritten = false;

            for(int i = 0; i < this.featureType.getAttributeCount(); ++i) {
                AttributeDescriptor ad = this.featureType.getDescriptor(i);
                if (i != gindex) {
                    Object value = feature.getAttribute(i);
                    if (FeatureJSON.this.encodeNullValues || value != null) {
                        attributesWritten = true;
                        if (value instanceof Envelope) {
                            GeoJSONUtil.array(ad.getLocalName(), FeatureJSON.this.gjson.toString((Envelope)value), sb);
                        } else if (value instanceof BoundingBox) {
                            GeoJSONUtil.array(ad.getLocalName(), FeatureJSON.this.gjson.toString((BoundingBox)value), sb);
                        } else if (value instanceof Geometry) {
                            GeoJSONUtil.string(ad.getLocalName(), sb).append(":").append(FeatureJSON.this.gjson.toString((Geometry)value));
                        } else {
                            GeoJSONUtil.entry(ad.getLocalName(), value, sb);
                        }

                        sb.append(",");
                    }
                }
            }

            if (attributesWritten) {
                sb.setLength(sb.length() - 1);
            }

            sb.append("},");
            GeoJSONUtil.entry("id", feature.getID(), sb);
            sb.append("}");
            return sb.toString();
        }

        public String toJSONString() {
            return this.toJSONString(this.feature);
        }
    }
}
