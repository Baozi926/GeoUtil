package com.uinnova.geo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import com.uinnova.geo.exception.GeoException;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.Geometries;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author 蔡惠民
 * 用于处理featureCollection的工具类
 */
public class FeatureCollectionUtil {

    private FeatureCollectionUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOGGER = Logging.getLogger(FeatureCollectionUtil.class);

    public static SimpleFeatureCollection makeValid(SimpleFeatureCollection featureCollection) {


        try {
            Geometries geometryType = getGeometryType(featureCollection);

            switch (geometryType) {
                case MULTIPOLYGON:
                case POLYGON: {
                    List<Geometry> results = new ArrayList<>();

                    List<Geometry> geometryList = readGeometries(featureCollection);

                    for (Geometry target : geometryList) {
                        results.add(GeometryUtil.validate(target));
                    }

                    return getSimpleFeatureCollection(results);

                }
                case POINT:
                case MULTIPOINT:
                default:
                    break;
            }
            return featureCollection;
        } catch (Exception exp) {
            return featureCollection;
        }

    }

    // 剪裁不应该丢失属性
    public static SimpleFeatureCollection intersection(SimpleFeatureCollection featureCollection, Geometry intersectionGeometry, String geometryType) {

        if (intersectionGeometry == null) {
            return featureCollection;
        }

        List<Geometry> geometryList = readGeometries(featureCollection);
        List<Geometry> intersectionGeometryList = new ArrayList<>();
        for (Geometry geometry : geometryList) {
            if (geometry != null) {
                IsValidOp isValidOp = new IsValidOp(geometry);
                if (BooleanUtil.isTrue(isValidOp.isValid())) {

                    Geometry intersectionResultGeometry = geometry.intersection(intersectionGeometry);
                    if (intersectionResultGeometry.isValid()) {
                        intersectionGeometryList.add(intersectionResultGeometry);
                    }
                }
            }


        }

        return getSimpleFeatureCollection(intersectionGeometryList, geometryType);
    }

    public static SimpleFeatureCollection union(SimpleFeatureCollection featureCollection) throws Exception {

        return union(featureCollection, null);


    }

    public static SimpleFeatureCollection union(SimpleFeatureCollection featureCollection, String geometryType) throws GeoException {

        List<Geometry> geometryList = readGeometries(featureCollection);

        if (!CharSequenceUtil.isEmpty(geometryType)) {
            List<Geometry> geometryListAfterProcess = new ArrayList<>();

            for (Geometry geometry : geometryList) {

                if (geometry != null) {
                    if (CharSequenceUtil.endWith(geometryType, geometry.getGeometryType())) {
                        geometryListAfterProcess.add(GeometryUtil.validate(geometry));
                    } else if (CharSequenceUtil.equals(geometry.getGeometryType(), "GeometryCollection")) {

                        String suffixGeometryType = CharSequenceUtil.removePrefix(geometryType, "Multi");

                        for (int index = 0; index < geometry.getNumGeometries(); index++) {
                            Geometry tmp = geometry.getGeometryN(0);
                            //如果是同一类型的，就加进来
                            if (CharSequenceUtil.equals(geometryType, tmp.getGeometryType()) || CharSequenceUtil.equals(suffixGeometryType, tmp.getGeometryType())) {
                                geometryListAfterProcess.add(GeometryUtil.validate(tmp));
                            }

                        }
                    }
                }


            }
            return getSimpleFeatureCollection(GeometryUtil.union(geometryListAfterProcess), geometryType);
        }


        return getSimpleFeatureCollection(GeometryUtil.union(geometryList), geometryType);


    }


    /**
     * 根据属性合并
     *
     * @param featureCollection featureCollection
     * @param mergeField        mergeField
     * @param geometryType      geometryType
     * @param mergeFieldType    mergeFieldType
     * @return SimpleFeatureCollection
     * @throws GeoException    GeoException
     * @throws SchemaException SchemaException
     */
    public static FeatureCollection mergeByField(FeatureCollection featureCollection, String mergeField, String geometryType, String mergeFieldType) throws GeoException, SchemaException {
        return mergeByField(DataUtilities.collection(featureCollection), mergeField, geometryType, mergeFieldType);
    }

    /**
     * 根据属性合并
     *
     * @param featureCollection featureCollection
     * @param mergeField        mergeField
     * @param geometryType      geometryType
     * @param mergeFieldType    mergeFieldType
     * @return SimpleFeatureCollection
     * @throws GeoException    GeoException
     * @throws SchemaException SchemaException
     */
    public static SimpleFeatureCollection mergeByField(SimpleFeatureCollection featureCollection,
                                                       String mergeField,
                                                       String geometryType,
                                                       String mergeFieldType) throws GeoException, SchemaException {


        if (CharSequenceUtil.isEmpty(mergeField)) {
            return featureCollection;
        }

        if (CharSequenceUtil.isEmpty(mergeFieldType)) {
            mergeFieldType = featureCollection.getSchema().getDescriptor(mergeField).getType().getBinding().getSimpleName();

            //Long 会报错。。
            if (CharSequenceUtil.equals(mergeFieldType, "Long")) {
                mergeFieldType = "Integer";
            }
        }


        Map<Object, List<Geometry>> mergeMap = new HashMap<>();

        if (featureCollection != null) {

            SimpleFeatureIterator iterator = featureCollection.features();


            try {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();

                    Object key = feature.getAttribute(mergeField);

                    mergeMap.computeIfAbsent(key, k -> new ArrayList<>());

                    List<Geometry> data = mergeMap.get(key);
                    //如果不做validate可能会出现自相交的问题而导致异常

                    Geometry validateGeometry = GeometryUtil.validate((Geometry) feature.getDefaultGeometry());
                    if (validateGeometry != null) {
                        data.add(validateGeometry);
                    }


                }
            } finally {
                iterator.close();
            }
        }


        final SimpleFeatureType type = DataUtilities.createType("Location", "geometry:" + geometryType + "," + mergeField + ":" + mergeFieldType);

        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureCollection collection = new ListFeatureCollection(type, features);

        int id = 0;

        for (Map.Entry<Object, List<Geometry>> entry : mergeMap.entrySet()) {

            Geometry geometry = GeometryUtil.union(entry.getValue());
            if (geometry != null) {
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
                featureBuilder.add(geometry);
                SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(id));
                feature.setAttribute(mergeField, entry.getKey());
                features.add(feature);
                id++;
            }

        }


        return collection;
    }


    // 在百万级数据的时候性能极差，
    public static Geometries getGeometryType(FeatureCollection<FeatureType, SimpleFeature> featureCollection) {

        try {
            if (featureCollection != null) {

                SimpleFeatureIterator iterator = (SimpleFeatureIterator) featureCollection.features();
                SimpleFeature feature = null;
                if (iterator.hasNext()) {
                    feature = iterator.next();

                }
                iterator.close();

                if (feature == null) {
                    return null;
                }

                return getGeometryType((Geometry) feature.getDefaultGeometry());
            }

            return null;

        } catch (Exception exp) {
            return null;
        }
    }


    // 在百万级数据的时候性能极差，
    public static Geometries getGeometryType(SimpleFeatureCollection featureCollection) {

        try {
            if (featureCollection != null) {

                SimpleFeatureIterator iterator = featureCollection.features();
                SimpleFeature feature = null;
                if (iterator.hasNext()) {
                    feature = iterator.next();

                }
                iterator.close();

                if (feature == null) {
                    return null;
                }

                return getGeometryType((Geometry) feature.getDefaultGeometry());
            }

            return null;

        } catch (Exception exp) {
            return null;
        }
    }


    public static Geometries getGeometryType(Geometry geometry) {

        Geometries geomType = Geometries.get(geometry);

        //如果是GEOMETRYCOLLECTION 需要单独判断
        if (Geometries.GEOMETRYCOLLECTION == geomType && geometry.getNumGeometries() > 0) {

            Geometries subGeometryType = Geometries.get(geometry.getGeometryN(0));
            if (subGeometryType == Geometries.POLYGON) {
                return Geometries.MULTIPOLYGON;
            } else if (subGeometryType == Geometries.POINT) {
                return Geometries.MULTIPOINT;
            } else if (subGeometryType == Geometries.LINESTRING) {
                return Geometries.MULTILINESTRING;
            }

            return Geometries.get(geometry.getGeometryN(0));

        }

        return geomType;
    }

    public static SimpleFeatureCollection getSimpleFeatureCollection(Geometry geometry) {
        List<Geometry> tmp = new ArrayList<>();
        tmp.add(geometry);
        return getSimpleFeatureCollection(tmp);

    }

    public static SimpleFeatureCollection getSimpleFeatureCollection(Geometry geometry, String geometryType) {
        List<Geometry> tmp = new ArrayList<>();
        if (geometry != null) {
            tmp.add(geometry);
        }

        return getSimpleFeatureCollection(tmp, geometryType);

    }

    public static SimpleFeatureCollection getSimpleFeatureCollection(List<Geometry> geometries) {
        return getSimpleFeatureCollection(geometries, null);
    }

    public static SimpleFeatureCollection getSimpleFeatureCollection(List<Geometry> geometries, String geometryType) {

        try {

            if (geometries == null || geometries.isEmpty()) {
                return null;
            }

            //如果geometryType为空就自己判断
            if (CharSequenceUtil.isEmpty(geometryType)) {
                geometryType = geometries.get(0).getGeometryType();


                //因为几何里面可能存在 单几何和多几何（Polygon，Multipolygon）情况，这里统一按照多几何的情况处理，因为如果按照单几何的情况处理，多几何就会出现错误
                if (CharSequenceUtil.equals(geometryType, "Polygon")) {
                    geometryType = "MultiPolygon";
                } else if (CharSequenceUtil.equals(geometryType, "LingString")) {
                    geometryType = "MultiLingString";
                } else if (CharSequenceUtil.equals(geometryType, "Point")) {
                    geometryType = "MultiPoint";
                }
            }


            final SimpleFeatureType featureType = DataUtilities.createType("Location", "geometry:" + geometryType);

            List<SimpleFeature> features = new ArrayList<>();
            SimpleFeatureCollection collection = new ListFeatureCollection(featureType, features);

            int i = 0;

            for (Geometry geometry : geometries
            ) {
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
                featureBuilder.add(geometry);
                SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(i));
                features.add(feature);
                i++;
            }

            return collection;
        } catch (Exception exp) {

            return null;
        }
    }


    public static List<Geometry> readGeometries(FeatureCollection<FeatureType, SimpleFeature> featureCollection) {

        List<Geometry> geometryList = new ArrayList<>();

        if (featureCollection != null) {

            SimpleFeatureIterator iterator = (SimpleFeatureIterator) featureCollection.features();
            try {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    geometryList.add((Geometry) feature.getDefaultGeometry());
                }
            } finally {
                iterator.close();
            }
        }

        return geometryList;
    }


    public static List<Geometry> readGeometries(SimpleFeatureCollection featureCollection) {

        List<Geometry> geometryList = new ArrayList<>();

        if (featureCollection != null) {

            SimpleFeatureIterator iterator = featureCollection.features();
            try {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    geometryList.add((Geometry) feature.getDefaultGeometry());
                }
            } finally {
                iterator.close();
            }
        }

        return geometryList;
    }


    /**
     * 拷贝 SimpleFeatureCollection
     *
     * @param simpleFeatureCollection simpleFeatureCollection
     * @return simpleFeatureCollection
     * @author caihuimin
     */
    public static SimpleFeatureCollection copy(SimpleFeatureCollection simpleFeatureCollection) {


        List<SimpleFeature> features = new ArrayList<>();

        SimpleFeatureIterator featureIterator = simpleFeatureCollection.features();

        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();
            if (feature.getDefaultGeometryProperty() != null && feature.getDefaultGeometryProperty().getValue() != null) {
                Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
                feature.setAttribute(feature.getDefaultGeometryProperty().getName(), geometry.copy());
                features.add(feature);
            }

        }
        return new ListFeatureCollection(simpleFeatureCollection.getSchema(), features);
    }


    /**
     * 去除空值
     *
     * @param featureCollection featureCollection
     * @return simpleFeatureCollection
     * @author caihuimin
     */
    public static SimpleFeatureCollection removeNullGeometryItem(SimpleFeatureCollection featureCollection) {

        List<SimpleFeature> features = new ArrayList<>();

        SimpleFeatureIterator featureIterator = featureCollection.features();

        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();
            if (feature.getDefaultGeometryProperty() != null && feature.getDefaultGeometryProperty().getValue() != null) {
                Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
                if ((geometry.getCoordinate() != null && geometry.getCoordinates() != null)) {
                    features.add(feature);
                } else {
                    LOGGER.info("存在空geometry数据");
                }
            } else {
                LOGGER.info("存在空geometry数据");
            }

        }

        featureIterator.close();

        return new ListFeatureCollection(featureCollection.getSchema(), features);


    }


    /**
     * featureCollection
     *
     * @param featureCollection geojson的字符串
     * @return FeatureCollection
     * @throws GeoException GeoException
     * @author caihuimin
     */
    public static FeatureCollection validate(FeatureCollection featureCollection) throws GeoException {

        if (featureCollection == null || featureCollection.size() == 0) {
            return featureCollection;
        }

        if (featureCollection.getSchema() == null) {
            return featureCollection;
        }

        ListFeatureCollection validateFeatures = new ListFeatureCollection((SimpleFeatureType) featureCollection.getSchema());
        FeatureIterator<SimpleFeature> features = featureCollection.features();
        while (features.hasNext()) {
            SimpleFeature feature = features.next();
            feature.setDefaultGeometry(GeometryUtil.validate((Geometry) feature.getDefaultGeometry()));
            validateFeatures.add(feature);

        }

        return validateFeatures;
    }


}
