//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.geotools.geojson.feature;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.DelegatingHandler;
import org.geotools.geojson.IContentHandler;
import org.json.simple.parser.ParseException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class FeatureTypeHandler extends DelegatingHandler<SimpleFeatureType> implements IContentHandler<SimpleFeatureType> {
    SimpleFeatureType featureType;
    private boolean inFeatures = false;
    private Map<String, Class<?>> propertyTypes = new LinkedHashMap();
    private boolean inProperties;
    private String currentProp;
    private CoordinateReferenceSystem crs;
    private boolean nullValuesEncoded;
    private GeometryDescriptor geom;

    public FeatureTypeHandler(boolean nullValuesEncoded) {
        this.nullValuesEncoded = nullValuesEncoded;
    }

    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if ("crs".equals(key)) {
            this.delegate = new CRSHandler();
            return true;
        } else if ("features".equals(key)) {
            this.delegate = UNINITIALIZED;
            this.inFeatures = true;
            return true;
        } else {
            if (this.inFeatures && this.delegate == NULL) {
                if ("properties".equals(key)) {
                    this.inProperties = true;
                    return true;
                }

                if (this.inProperties) {
                    if (!this.propertyTypes.containsKey(key)) {
                        this.propertyTypes.put(key, Object.class);
                    }

                    this.currentProp = key;
                    return true;
                }
            }

            return super.startObjectEntry(key);
        }
    }

    public boolean startArray() throws ParseException, IOException {
        if (this.delegate == UNINITIALIZED) {
            this.delegate = new FeatureHandler((SimpleFeatureBuilder)null, new DefaultAttributeIO());
            return true;
        } else {
            return super.startArray();
        }
    }

    public boolean endObject() throws ParseException, IOException {
        super.endObject();
        if (this.delegate instanceof FeatureHandler) {
            SimpleFeature feature = ((FeatureHandler)this.delegate).getValue();
            if (feature != null) {
                this.geom = feature.getFeatureType().getGeometryDescriptor();
                List<AttributeDescriptor> attributeDescriptors = feature.getFeatureType().getAttributeDescriptors();
                Iterator var3 = attributeDescriptors.iterator();

                while(var3.hasNext()) {
                    AttributeDescriptor ad = (AttributeDescriptor)var3.next();
                    if (!ad.equals(this.geom)) {
                        this.propertyTypes.put(ad.getLocalName(), ad.getType().getBinding());
                    }
                }

                this.delegate = NULL;
                if (this.foundAllValues()) {
                    this.buildType();
                    return false;
                }
            }
        }

        return true;
    }

    public boolean primitive(Object value) throws ParseException, IOException {
        if (value != null) {
            Class<?> newType = value.getClass();
            if (this.currentProp != null) {
                Class<?> knownType = (Class)this.propertyTypes.get(this.currentProp);
                if (knownType == Object.class) {
                    this.propertyTypes.put(this.currentProp, newType);
                    if (this.foundAllValues()) {
                        this.buildType();
                        return false;
                    }
                } else if (knownType != newType) {
                    if (!Number.class.isAssignableFrom(knownType) || newType != Double.class) {
                        throw new IllegalStateException("Found conflicting types " + knownType.getSimpleName() + " and " + newType.getSimpleName() + " for property " + this.currentProp);
                    }

                    this.propertyTypes.put(this.currentProp, Double.class);
                }
            }
        }

        return super.primitive(value);
    }

    private boolean foundAllValues() {
        return this.nullValuesEncoded && this.geom != null && this.crs != null && !this.thereAreUnknownDataTypes();
    }

    private boolean thereAreUnknownDataTypes() {
        Iterator var1 = this.propertyTypes.values().iterator();

        Class clazz;
        do {
            if (!var1.hasNext()) {
                return false;
            }

            clazz = (Class)var1.next();
        } while(clazz != Object.class);

        return true;
    }

    public boolean endObjectEntry() throws ParseException, IOException {
        super.endObjectEntry();
        if (this.delegate != null && this.delegate instanceof CRSHandler) {
            this.crs = ((CRSHandler)this.delegate).getValue();
            if (this.crs != null) {
                this.delegate = NULL;
            }
        } else if (this.currentProp != null) {
            this.currentProp = null;
        } else if (this.inProperties) {
            this.inProperties = false;
        }

        return true;
    }

    public void endJSON() throws ParseException, IOException {
        this.buildType();
    }

    private void buildType() {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("feature");
        typeBuilder.setNamespaceURI("http://geotools.org");
        if (this.geom != null) {
            typeBuilder.add(this.geom.getLocalName(), this.geom.getType().getBinding(), this.crs);
        }

        if (this.propertyTypes != null) {
            Set<Entry<String, Class<?>>> entrySet = this.propertyTypes.entrySet();

            Entry entry;
            Class binding;
            for(Iterator var3 = entrySet.iterator(); var3.hasNext(); typeBuilder.add((String)entry.getKey(), binding)) {
                entry = (Entry)var3.next();
                binding = (Class)entry.getValue();
                if (binding.equals(Object.class)) {
                    binding = String.class;
                }
            }
        }

        if (this.crs != null) {
            typeBuilder.setCRS(this.crs);
        }

        this.featureType = typeBuilder.buildFeatureType();
    }

    public SimpleFeatureType getValue() {
        return this.featureType;
    }
}
