//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.geotools.feature;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.FeatureIteratorImpl;
import org.geotools.feature.collection.SimpleFeatureIteratorImpl;
import org.geotools.feature.collection.SubFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.geometry.BoundingBox;
import org.opengis.util.ProgressListener;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class DefaultFeatureCollection implements SimpleFeatureCollection, Collection<SimpleFeature> {
    protected static Logger LOGGER = Logging.getLogger(DefaultFeatureCollection.class);
    private SortedMap<String, SimpleFeature> contents;
    private ReferencedEnvelope bounds;
    protected String id;
    protected SimpleFeatureType schema;

    public DefaultFeatureCollection() {
        this((String)null, (SimpleFeatureType)null);
    }

    public DefaultFeatureCollection(FeatureCollection<SimpleFeatureType, SimpleFeature> collection) {
        this(collection.getID(), (SimpleFeatureType)collection.getSchema());
        this.addAll(collection);
    }

    public DefaultFeatureCollection(String id) {
        this(id, (SimpleFeatureType)null);
    }

    public DefaultFeatureCollection(String id, SimpleFeatureType memberType) {
        this.contents = new TreeMap();
        this.bounds = null;
        this.id = id == null ? "featureCollection" : id;
        this.schema = memberType;
    }

    public ReferencedEnvelope getBounds() {
        if (this.bounds == null) {
            this.bounds = new ReferencedEnvelope();
            Iterator var1 = this.contents.values().iterator();

            while(var1.hasNext()) {
                SimpleFeature simpleFeature = (SimpleFeature)var1.next();
                BoundingBox geomBounds = simpleFeature.getBounds();
                if (!geomBounds.isEmpty()) {
                    this.bounds.include(geomBounds);
                }
            }
        }

        return this.bounds;
    }

    public boolean add(SimpleFeature o) {
        return this.add(o, true);
    }

    protected boolean add(SimpleFeature feature, boolean fire) {
        if (feature == null) {
            return false;
        } else {
            String ID = feature.getID();
            if (ID == null) {
                return false;
            } else if (this.contents.containsKey(ID)) {
                return false;
            } else {
                if (this.schema == null) {
                    this.schema = feature.getFeatureType();
                }
                //如果属性比原先的多
                if(this.schema.getTypes().size()<feature.getFeatureType().getTypes().size()){
                    this.schema = feature.getFeatureType();
                }

                SimpleFeatureType childType = this.getSchema();
                if (!feature.getFeatureType().equals(childType)) {
                    LOGGER.warning("Feature Collection contains a heterogeneous mix of features");
                }

                this.contents.put(ID, feature);
                return true;
            }
        }
    }

    public boolean addAll(Collection<? extends SimpleFeature> collection) {
        boolean changed = false;
        Iterator iterator = collection.iterator();

        boolean var9;
        try {
            while(iterator.hasNext()) {
                SimpleFeature f = (SimpleFeature)iterator.next();
                boolean added = this.add(f, false);
                changed |= added;
            }

            var9 = changed;
        } finally {
            if (iterator instanceof FeatureIterator) {
                ((FeatureIterator)iterator).close();
            }

        }

        return var9;
    }

    public boolean addAll(FeatureCollection<?, ?> collection) {
        boolean changed = false;
        FeatureIterator<?> iterator = collection.features();
        Throwable var4 = null;

        boolean var16;
        try {
            while(iterator.hasNext()) {
                SimpleFeature f = (SimpleFeature)iterator.next();
                boolean added = this.add(f, false);
                changed |= added;
            }

            var16 = changed;
        } catch (Throwable var14) {
            var4 = var14;
            throw var14;
        } finally {
            if (iterator != null) {
                if (var4 != null) {
                    try {
                        iterator.close();
                    } catch (Throwable var13) {
                        var4.addSuppressed(var13);
                    }
                } else {
                    iterator.close();
                }
            }

        }

        return var16;
    }

    public void clear() {
        this.contents.clear();
    }

    public boolean contains(Object o) {
        if (!(o instanceof SimpleFeature)) {
            return false;
        } else {
            SimpleFeature feature = (SimpleFeature)o;
            String ID = feature.getID();
            return this.contents.containsKey(ID);
        }
    }

    public boolean containsAll(Collection<?> collection) {
        Iterator iterator = collection.iterator();

        while(true) {
            boolean var4;
            try {
                if (!iterator.hasNext()) {
                    boolean var8 = true;
                    return var8;
                }

                SimpleFeature feature = (SimpleFeature)iterator.next();
                if (this.contents.containsKey(feature.getID())) {
                    continue;
                }

                var4 = false;
            } finally {
                if (iterator instanceof FeatureIterator) {
                    ((FeatureIterator)iterator).close();
                }

            }

            return var4;
        }
    }

    public boolean isEmpty() {
        return this.contents.isEmpty();
    }

    public Iterator<SimpleFeature> iterator() {
        final Iterator<SimpleFeature> iterator = this.contents.values().iterator();
        return new Iterator<SimpleFeature>() {
            SimpleFeature currFeature = null;

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public SimpleFeature next() {
                this.currFeature = (SimpleFeature)iterator.next();
                return this.currFeature;
            }

            public void remove() {
                iterator.remove();
                DefaultFeatureCollection.this.bounds = null;
            }
        };
    }

    public SimpleFeatureIterator features() {
        return new SimpleFeatureIteratorImpl(this.contents.values());
    }

    public boolean remove(Object o) {
        if (!(o instanceof SimpleFeature)) {
            return false;
        } else {
            SimpleFeature f = (SimpleFeature)o;
            boolean changed = this.contents.values().remove(f);
            return changed;
        }
    }

    public boolean removeAll(Collection<?> collection) {
        boolean changed = false;
        Iterator iterator = collection.iterator();

        boolean var9;
        try {
            while(iterator.hasNext()) {
                SimpleFeature f = (SimpleFeature)iterator.next();
                boolean removed = this.contents.values().remove(f);
                if (removed) {
                    changed = true;
                }
            }

            var9 = changed;
        } finally {
            if (iterator instanceof FeatureIterator) {
                ((FeatureIterator)iterator).close();
            }

        }

        return var9;
    }

    public boolean retainAll(Collection<?> collection) {
        boolean modified = false;
        Iterator it = this.contents.values().iterator();

        while(it.hasNext()) {
            SimpleFeature f = (SimpleFeature)it.next();
            if (!collection.contains(f)) {
                it.remove();
                modified = true;
            }
        }

        return modified;
    }

    public int size() {
        return this.contents.size();
    }

    public Object[] toArray() {
        return this.contents.values().toArray();
    }

    public <T> T[] toArray(T[] a) {
        return this.contents.values().toArray(a);
    }

    public void close(FeatureIterator<SimpleFeature> close) {
        if (close instanceof FeatureIteratorImpl) {
            FeatureIteratorImpl<SimpleFeature> wrapper = (FeatureIteratorImpl)close;
            wrapper.close();
        }

    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> reader() throws IOException {
        final SimpleFeatureIterator iterator = this.features();
        return new FeatureReader<SimpleFeatureType, SimpleFeature>() {
            public SimpleFeatureType getFeatureType() {
                return DefaultFeatureCollection.this.getSchema();
            }

            public SimpleFeature next() throws IOException, IllegalAttributeException, NoSuchElementException {
                return (SimpleFeature)iterator.next();
            }

            public boolean hasNext() throws IOException {
                return iterator.hasNext();
            }

            public void close() throws IOException {
                DefaultFeatureCollection.this.close(iterator);
            }
        };
    }

    public int getCount() throws IOException {
        return this.contents.size();
    }

    public SimpleFeatureCollection collection() throws IOException {
        DefaultFeatureCollection copy = new DefaultFeatureCollection((String)null, this.getSchema());
        List<SimpleFeature> list = new ArrayList(this.contents.size());
        SimpleFeatureIterator iterator = this.features();
        Throwable var4 = null;

        try {
            SimpleFeature duplicate;
            try {
                for(; iterator.hasNext(); list.add(duplicate)) {
                    SimpleFeature feature = (SimpleFeature)iterator.next();

                    try {
                        duplicate = SimpleFeatureBuilder.copy(feature);
                    } catch (IllegalAttributeException var16) {
                        throw new DataSourceException("Unable to copy " + feature.getID(), var16);
                    }
                }
            } catch (Throwable var17) {
                var4 = var17;
                throw var17;
            }
        } finally {
            if (iterator != null) {
                if (var4 != null) {
                    try {
                        iterator.close();
                    } catch (Throwable var15) {
                        var4.addSuppressed(var15);
                    }
                } else {
                    iterator.close();
                }
            }

        }

        copy.addAll((Collection)list);
        return copy;
    }

    public Set fids() {
        return Collections.unmodifiableSet(this.contents.keySet());
    }

    public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
        DataUtilities.visit(this, visitor, progress);
    }

    public SimpleFeatureCollection subCollection(Filter filter) {
        return (SimpleFeatureCollection)(filter == Filter.INCLUDE ? this : new SubFeatureCollection(this, filter));
    }

    public SimpleFeatureCollection sort(SortBy order) {
        return order == SortBy.NATURAL_ORDER ? this : null;
    }

    public void purge() {
    }

    public void validate() {
    }

    public String getID() {
        return this.id;
    }

    public SimpleFeatureType getSchema() {
        return this.schema;
    }
}
