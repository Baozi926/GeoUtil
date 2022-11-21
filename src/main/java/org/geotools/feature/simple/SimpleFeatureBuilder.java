//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.geotools.feature.simple;

import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureBuilder;
import org.geotools.feature.type.Types;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

import java.util.*;
import java.util.logging.Logger;

public class SimpleFeatureBuilder extends FeatureBuilder<FeatureType, Feature> {
    static Logger LOGGER = Logging.getLogger(SimpleFeatureBuilder.class);
    SimpleFeatureType featureType;
    FeatureFactory factory;
    Map<String, Integer> index;
    Object[] values;
    int next;
    Map<Object, Object>[] userData;
    Map<Object, Object> featureUserData;
    boolean validating;

    public SimpleFeatureBuilder(SimpleFeatureType featureType) {
        this(featureType, CommonFactoryFinder.getFeatureFactory((Hints) null));
    }

    public SimpleFeatureBuilder(SimpleFeatureType featureType, FeatureFactory factory) {
        super(featureType, factory);
        this.featureType = featureType;
        this.factory = factory;
        if (featureType instanceof SimpleFeatureTypeImpl) {
            this.index = ((SimpleFeatureTypeImpl) featureType).index;
        } else {
            this.index = SimpleFeatureTypeImpl.buildIndex(featureType);
        }

        this.reset();
    }

    public void reset() {
        this.values = new Object[this.featureType.getAttributeCount()];
        this.next = 0;
        this.userData = null;
        this.featureUserData = null;
    }

    public SimpleFeatureType getFeatureType() {
        return this.featureType;
    }

    public void init(SimpleFeature feature) {
        this.reset();
        if (feature instanceof SimpleFeatureImpl) {
            SimpleFeatureImpl impl = (SimpleFeatureImpl) feature;
            System.arraycopy(impl.values, 0, this.values, 0, impl.values.length);
            if (impl.userData != null) {
                this.featureUserData = new HashMap(impl.userData);
            }
        } else {
            Iterator var4 = feature.getAttributes().iterator();

            while (var4.hasNext()) {
                Object value = var4.next();
                this.add(value);
            }

            if (!feature.getUserData().isEmpty()) {
                this.featureUserData = new HashMap(feature.getUserData());
            }
        }

    }

    public void add(Object value) {
        this.set(this.next, value);
        ++this.next;
    }

    public void addAll(List<Object> values) {
        Iterator var2 = values.iterator();

        while (var2.hasNext()) {
            Object value = var2.next();
            this.add(value);
        }

    }

    public void addAll(Object... values) {
        this.addAll(Arrays.asList(values));
    }

    public void set(Name name, Object value) {
        this.set(name.getLocalPart(), value);
    }

    public void set(String name, Object value) {
        int index = this.featureType.indexOf(name);
        if (index == -1) {
//            throw new IllegalArgumentException("No such attribute:" + name); //CustomFix by caihuimin 如果没有的话不报错,也不做任何操作，用于读取确实属性的值
        } else {

            this.set(index, value);
        }
    }

    public void set(int index, Object value) {
        if (index >= this.values.length) {
            throw new ArrayIndexOutOfBoundsException("Can handle " + this.values.length + " attributes only, index is " + index);
        } else {
            AttributeDescriptor descriptor = this.featureType.getDescriptor(index);
            this.values[index] = this.convert(value, descriptor);
            if (this.validating) {
                Types.validate(descriptor, this.values[index]);
            }

        }
    }

    private Object convert(Object value, AttributeDescriptor descriptor) {
        if (value == null) {
            if (!descriptor.isNillable()) {
                value = descriptor.getDefaultValue();
                if (value == null) {
                    value = DataUtilities.defaultValue(descriptor.getType().getBinding());
                }
            }
        } else {
            value = super.convert(value, descriptor);
        }

        return value;
    }

    public SimpleFeature buildFeature(String id) {
        if (id == null) {
            id = createDefaultFeatureId();
        }

        Object[] values = this.values;
        Map<Object, Object>[] userData = this.userData;
        Map<Object, Object> featureUserData = this.featureUserData;
        this.reset();
        SimpleFeature sf = this.factory.createSimpleFeature(values, this.featureType, id);
        if (userData != null) {
            for (int i = 0; i < userData.length; ++i) {
                if (userData[i] != null) {
                    sf.getProperty(this.featureType.getDescriptor(i).getName()).getUserData().putAll(userData[i]);
                }
            }
        }

        if (featureUserData != null) {
            sf.getUserData().putAll(featureUserData);
        }

        return sf;
    }

    public SimpleFeature buildFeature(String id, Object... values) {
        this.addAll(values);
        return this.buildFeature(id);
    }

    public static SimpleFeature build(SimpleFeatureType type, Object[] values, String id) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        builder.addAll(values);
        return builder.buildFeature(id);
    }

    public static SimpleFeature build(SimpleFeatureType type, List<Object> values, String id) {
        return build(type, values.toArray(), id);
    }

    public static SimpleFeature copy(SimpleFeature original) {
        if (original == null) {
            return null;
        } else {
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(original.getFeatureType());
            builder.init(original);
            return builder.buildFeature(original.getID());
        }
    }

    public static SimpleFeature deep(SimpleFeature original) {
        if (original == null) {
            return null;
        } else {
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(original.getFeatureType());
            Iterator var2 = original.getProperties().iterator();

            while (var2.hasNext()) {
                Property property = (Property) var2.next();
                Object value = property.getValue();

                try {
                    Object copy = value;
                    if (value instanceof Geometry) {
                        Geometry geometry = (Geometry) value;
                        copy = geometry.copy();
                    }

                    builder.set(property.getName(), copy);
                } catch (Exception var7) {
                    throw new IllegalAttributeException((AttributeDescriptor) property.getDescriptor(), value, var7);
                }
            }

            return builder.buildFeature(original.getID());
        }
    }

    public static SimpleFeature template(SimpleFeatureType featureType, String featureId) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        Iterator var3 = featureType.getAttributeDescriptors().iterator();

        while (var3.hasNext()) {
            AttributeDescriptor ad = (AttributeDescriptor) var3.next();
            builder.add(ad.getDefaultValue());
        }

        return builder.buildFeature(featureId);
    }

    public static SimpleFeature retype(SimpleFeature feature, SimpleFeatureType featureType) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        Iterator var3 = featureType.getAttributeDescriptors().iterator();

        while (var3.hasNext()) {
            AttributeDescriptor att = (AttributeDescriptor) var3.next();
            Object value = feature.getAttribute(att.getName());
            builder.set(att.getName(), value);
        }

        return builder.buildFeature(feature.getID());
    }

    public static SimpleFeature retype(SimpleFeature feature, SimpleFeatureBuilder builder) {
        builder.reset();
        Iterator var2 = builder.getFeatureType().getAttributeDescriptors().iterator();

        while (var2.hasNext()) {
            AttributeDescriptor att = (AttributeDescriptor) var2.next();
            Object value = feature.getAttribute(att.getName());
            builder.set(att.getName(), value);
        }

        return builder.buildFeature(feature.getID());
    }

    public SimpleFeatureBuilder userData(Object key, Object value) {
        return this.setUserData(this.next, key, value);
    }

    public SimpleFeatureBuilder setUserData(int index, Object key, Object value) {
        if (this.userData == null) {
            this.userData = new Map[this.values.length];
        }

        if (this.userData[index] == null) {
            this.userData[index] = new HashMap();
        }

        this.userData[index].put(key, value);
        return this;
    }

    public SimpleFeatureBuilder featureUserData(SimpleFeature source) {
        Map<Object, Object> sourceUserData = source.getUserData();
        if (sourceUserData != null && !sourceUserData.isEmpty()) {
            if (this.featureUserData == null) {
                this.featureUserData = new HashMap();
            }

            this.featureUserData.putAll(sourceUserData);
        }

        return this;
    }

    public SimpleFeatureBuilder featureUserData(Object key, Object value) {
        if (this.featureUserData == null) {
            this.featureUserData = new HashMap();
        }

        this.featureUserData.put(key, value);
        return this;
    }

    public boolean isValidating() {
        return this.validating;
    }

    public void setValidating(boolean validating) {
        this.validating = validating;
    }
}
