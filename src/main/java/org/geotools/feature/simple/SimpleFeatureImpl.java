//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.geotools.feature.simple;

import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.Types;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.coordinatesequence.CoordinateSequences;
import org.geotools.util.Converters;
import org.geotools.util.SuppressFBWarnings;
import org.geotools.util.Utilities;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.*;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.*;
/**
 *
 * @exclude
 *
 * */
public class SimpleFeatureImpl implements SimpleFeature {
    protected FeatureId id;
    protected SimpleFeatureType featureType;
    protected Object[] values;
    protected Map<String, Integer> index;
    protected Map<Object, Object> userData;
    protected Map<Object, Object>[] attributeUserData;
    protected boolean validating;

    public SimpleFeatureImpl(List<Object> values, SimpleFeatureType featureType, FeatureId id) {
        this(values.toArray(), featureType, id, false, index(featureType));
    }

    public SimpleFeatureImpl(Object[] values, SimpleFeatureType featureType, FeatureId id, boolean validating) {
        this(values, featureType, id, validating, index(featureType));
    }

    public SimpleFeatureImpl(Object[] values, SimpleFeatureType featureType, FeatureId id, boolean validating, Map<String, Integer> index) {
        this.id = id;
        this.featureType = featureType;
        this.values = values;
        this.validating = validating;
        this.index = index;
        if (validating) {
            this.validate();
        }

    }

    private static Map<String, Integer> index(SimpleFeatureType featureType) {
        if (featureType instanceof SimpleFeatureTypeImpl) {
            return ((SimpleFeatureTypeImpl)featureType).index;
        } else {
            synchronized(featureType) {
                Object cache = featureType.getUserData().get("indexLookup");
                if (cache instanceof Map) {
                    return (Map)cache;
                } else {
                    Map<String, Integer> generatedIndex = SimpleFeatureTypeImpl.buildIndex(featureType);
                    featureType.getUserData().put("indexLookup", generatedIndex);
                    return generatedIndex;
                }
            }
        }
    }

    public FeatureId getIdentifier() {
        return this.id;
    }

    public String getID() {
        return this.id.getID();
    }

    public int getNumberOfAttributes() {
        return this.values.length;
    }

    public Object getAttribute(int index) throws IndexOutOfBoundsException {
        return this.values[index];
    }

    public Object getAttribute(String name) {
        Integer idx = (Integer)this.index.get(name);
        return idx != null ? this.getAttribute(idx) : null;
    }

    public Object getAttribute(Name name) {
        return this.getAttribute(name.getLocalPart());
    }

    public int getAttributeCount() {
        return this.values.length;
    }

    public List<Object> getAttributes() {
        return new ArrayList(Arrays.asList(this.values));
    }

    public Object getDefaultGeometry() {
        Integer idx = (Integer)this.index.get((Object)null);
        Object defaultGeometry = idx != null ? this.getAttribute(idx) : null;
        if (defaultGeometry == null) {
            GeometryDescriptor geometryDescriptor = this.featureType.getGeometryDescriptor();
            if (geometryDescriptor != null) {
                Integer defaultGeomIndex = (Integer)this.index.get(geometryDescriptor.getName().getLocalPart());
                defaultGeometry = this.getAttribute(defaultGeomIndex);
            }
        }

        return defaultGeometry;
    }

    public SimpleFeatureType getFeatureType() {
        return this.featureType;
    }

    public SimpleFeatureType getType() {
        return this.featureType;
    }

    public void setAttribute(int index, Object value) throws IndexOutOfBoundsException {
        Object converted = Converters.convert(value, this.getFeatureType().getDescriptor(index).getType().getBinding());
        if (this.validating) {
            Types.validate(this.featureType.getDescriptor(index), converted);
        }

        this.values[index] = converted;
    }

    public void setAttribute(String name, Object value) {
        Integer idx = (Integer)this.index.get(name);
        if (idx == null) {
//            throw new IllegalAttributeException("Unknown attribute " + name);
            //by caihuimin 不需要抛异常，继续执行即可
        } else {
            this.setAttribute(idx, value);
        }
    }

    public void setAttribute(Name name, Object value) {
        this.setAttribute(name.getLocalPart(), value);
    }

    public void setAttributes(List<Object> values) {
        for(int i = 0; i < this.values.length; ++i) {
            this.values[i] = values.get(i);
        }

    }

    public void setAttributes(Object[] values) {
        this.setAttributes(Arrays.asList(values));
    }

    public void setDefaultGeometry(Object geometry) {
        Integer geometryIndex = (Integer)this.index.get((Object)null);
        if (geometryIndex != null) {
            this.setAttribute(geometryIndex, geometry);
        }

    }

    public BoundingBox getBounds() {
        CoordinateReferenceSystem crs = this.featureType.getCoordinateReferenceSystem();
        Envelope bounds = ReferencedEnvelope.create(crs);
        Object[] var3 = this.values;
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Object o = var3[var5];
            if (o instanceof Geometry) {
                Geometry g = (Geometry)o;
                if (bounds.isNull()) {
                    bounds.init(JTS.bounds(g, crs));
                } else {
                    bounds.expandToInclude(JTS.bounds(g, crs));
                }
            }
        }

        return (BoundingBox)bounds;
    }

    public GeometryAttribute getDefaultGeometryProperty() {
        GeometryDescriptor geometryDescriptor = this.featureType.getGeometryDescriptor();
        GeometryAttribute geometryAttribute = null;
        if (geometryDescriptor != null) {
            Object defaultGeometry = this.getDefaultGeometry();
            geometryAttribute = new GeometryAttributeImpl(defaultGeometry, geometryDescriptor, (Identifier)null);
        }

        return geometryAttribute;
    }

    public void setDefaultGeometryProperty(GeometryAttribute geometryAttribute) {
        if (geometryAttribute != null) {
            this.setDefaultGeometry(geometryAttribute.getValue());
        } else {
            this.setDefaultGeometry((Object)null);
        }

    }

    public Collection<Property> getProperties() {
        return new SimpleFeatureImpl.AttributeList();
    }

    public Collection<Property> getProperties(Name name) {
        return this.getProperties(name.getLocalPart());
    }

    public Collection<Property> getProperties(String name) {
        Integer idx = (Integer)this.index.get(name);
        if (idx != null) {
            Collection<Property> c = Collections.singleton(new SimpleFeatureImpl.Attribute(idx));
            return c;
        } else {
            return Collections.emptyList();
        }
    }

    public Property getProperty(Name name) {
        return this.getProperty(name.getLocalPart());
    }

    public Property getProperty(String name) {
        Integer idx = (Integer)this.index.get(name);
        if (idx == null) {
            return null;
        } else {
            int index = idx;
            AttributeDescriptor descriptor = this.featureType.getDescriptor(index);
            return (Property)(descriptor instanceof GeometryDescriptor ? new GeometryAttributeImpl(this.values[index], (GeometryDescriptor)descriptor, (Identifier)null) : new SimpleFeatureImpl.Attribute(index));
        }
    }

    public Collection<? extends Property> getValue() {
        return this.getProperties();
    }

    public void setValue(Collection<Property> values) {
        int i = 0;

        Property p;
        for(Iterator var3 = values.iterator(); var3.hasNext(); this.values[i++] = p.getValue()) {
            p = (Property)var3.next();
        }

    }

    public void setValue(Object newValue) {
        Collection<Property> converted = (Collection)newValue;
        this.setValue(converted);
    }

    public AttributeDescriptor getDescriptor() {
        return new AttributeDescriptorImpl(this.featureType, this.featureType.getName(), 0, 2147483647, true, (Object)null);
    }

    public Name getName() {
        return this.featureType.getName();
    }

    public boolean isNillable() {
        return true;
    }

    public Map<Object, Object> getUserData() {
        if (this.userData == null) {
            this.userData = new HashMap();
        }

        return this.userData;
    }

    public boolean hasUserData() {
        return this.userData != null && !this.userData.isEmpty();
    }

    public int hashCode() {
        return this.id.hashCode() * this.featureType.hashCode();
    }

    @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH"})
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof SimpleFeatureImpl)) {
            return false;
        } else {
            SimpleFeatureImpl feat = (SimpleFeatureImpl)obj;
            if (this.id == null && feat.getIdentifier() != null) {
                return false;
            } else if (!this.id.equals(feat.getIdentifier())) {
                return false;
            } else if (!feat.getFeatureType().equals(this.featureType)) {
                return false;
            } else {
                int i = 0;

                for(int ii = this.values.length; i < ii; ++i) {
                    Object otherAtt = feat.getAttribute(i);
                    if (this.values[i] == null) {
                        if (otherAtt != null) {
                            return false;
                        }
                    } else if (this.values[i] instanceof Geometry) {
                        if (!(otherAtt instanceof Geometry)) {
                            return false;
                        }

                        if (!CoordinateSequences.equalsND((Geometry)this.values[i], (Geometry)otherAtt)) {
                            return false;
                        }
                    } else if (!this.values[i].equals(otherAtt)) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public void validate() {
        for(int i = 0; i < this.values.length; ++i) {
            AttributeDescriptor descriptor = this.getType().getDescriptor(i);
            Types.validate(descriptor, this.values[i]);
        }

    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SimpleFeatureImpl:");
        sb.append(this.getType().getName().getLocalPart());
        sb.append("=");
        sb.append(this.getValue());
        return sb.toString();
    }

    class SimpleGeometryAttribute extends SimpleFeatureImpl.Attribute implements GeometryAttribute {
        SimpleGeometryAttribute(int index) {
            super(index);
        }

        public GeometryType getType() {
            return (GeometryType)super.getType();
        }

        public GeometryDescriptor getDescriptor() {
            return (GeometryDescriptor)super.getDescriptor();
        }

        public BoundingBox getBounds() {
            ReferencedEnvelope bounds = new ReferencedEnvelope(SimpleFeatureImpl.this.featureType.getCoordinateReferenceSystem());
            Object value = SimpleFeatureImpl.this.getAttribute(this.index);
            if (value instanceof Geometry) {
                bounds.init(((Geometry)value).getEnvelopeInternal());
            }

            return bounds;
        }

        public void setBounds(BoundingBox bounds) {
        }

        public int hashCode() {
            return 17 * super.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else {
                return !(obj instanceof SimpleFeatureImpl.SimpleGeometryAttribute) ? false : super.equals(obj);
            }
        }
    }

    class Attribute implements org.opengis.feature.Attribute {
        int index;

        Attribute(int index) {
            this.index = index;
        }

        public Identifier getIdentifier() {
            return null;
        }

        public AttributeDescriptor getDescriptor() {
            return SimpleFeatureImpl.this.featureType.getDescriptor(this.index);
        }

        public AttributeType getType() {
            return SimpleFeatureImpl.this.featureType.getType(this.index);
        }

        public Name getName() {
            return this.getDescriptor().getName();
        }

        public Map<Object, Object> getUserData() {
            if (SimpleFeatureImpl.this.attributeUserData == null) {
                SimpleFeatureImpl.this.attributeUserData = new HashMap[SimpleFeatureImpl.this.values.length];
            }

            if (SimpleFeatureImpl.this.attributeUserData[this.index] == null) {
                SimpleFeatureImpl.this.attributeUserData[this.index] = new HashMap();
            }

            return SimpleFeatureImpl.this.attributeUserData[this.index];
        }

        public Object getValue() {
            return SimpleFeatureImpl.this.values[this.index];
        }

        public boolean isNillable() {
            return this.getDescriptor().isNillable();
        }

        public void setValue(Object newValue) {
            SimpleFeatureImpl.this.values[this.index] = newValue;
        }

        public int hashCode() {
            return 37 * this.getDescriptor().hashCode() + 37 * (this.getValue() == null ? 0 : this.getValue().hashCode());
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof SimpleFeatureImpl.Attribute)) {
                return false;
            } else {
                SimpleFeatureImpl.Attribute other = (SimpleFeatureImpl.Attribute)obj;
                if (!Utilities.equals(this.getDescriptor(), other.getDescriptor())) {
                    return false;
                } else {
                    return !Utilities.deepEquals(this.getValue(), other.getValue()) ? false : Utilities.equals(this.getIdentifier(), other.getIdentifier());
                }
            }
        }

        public void validate() {
            Types.validate(this.getDescriptor(), SimpleFeatureImpl.this.values[this.index]);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer("SimpleFeatureImpl.Attribute: ");
            sb.append(this.getDescriptor().getName().getLocalPart());
            if (!this.getDescriptor().getName().getLocalPart().equals(this.getDescriptor().getType().getName().getLocalPart()) || SimpleFeatureImpl.this.id != null) {
                sb.append("<");
                sb.append(this.getDescriptor().getType().getName().getLocalPart());
                if (SimpleFeatureImpl.this.id != null) {
                    sb.append(" id=");
                    sb.append(SimpleFeatureImpl.this.id);
                }

                sb.append(">");
            }

            sb.append("=");
            sb.append(SimpleFeatureImpl.this.values[this.index]);
            return sb.toString();
        }
    }

    class AttributeList extends AbstractList<Property> {
        AttributeList() {
        }

        public Property get(int index) {
            AttributeDescriptor descriptor = SimpleFeatureImpl.this.featureType.getDescriptor(index);
            return (Property)(descriptor instanceof GeometryDescriptor ? SimpleFeatureImpl.this.new SimpleGeometryAttribute(index) : SimpleFeatureImpl.this.new Attribute(index));
        }

        public SimpleFeatureImpl.Attribute set(int index, Property element) {
            SimpleFeatureImpl.this.values[index] = element.getValue();
            return null;
        }

        public int size() {
            return SimpleFeatureImpl.this.values.length;
        }
    }
}
