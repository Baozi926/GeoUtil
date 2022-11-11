package com.uinnova.geo.json;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import com.uinnova.geo.GeometryOptimizeUtils;
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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 蔡惠民
 * @date 2021/5/7 10:41
 */
public class GeojsonOptimizeUtil {

    private GeojsonOptimizeUtil() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * 根据属性合并
     *
     * @param mergeField
     * @param mergeField
     */
    public static FeatureCollection mergeByField(FeatureCollection featureCollection, String mergeField, String geometryType, String mergeFieldType) throws GeoException, SchemaException {

        if (mergeFieldType == null) {
            //当features中没数据的时候，就无法获取schema
            if (featureCollection.getSchema() == null) {
                throw new GeoException("无法获取元数据，数据可能为空！");
            }

            PropertyDescriptor propertyDescriptor = featureCollection.getSchema().getDescriptor(mergeField);
            if (propertyDescriptor == null) {
                throw new GeoException("数据中可能不存在[" + mergeField + "],或者第一条数据中，没有这个属性！");
            }

            PropertyType mergeFieldTypeInstance = propertyDescriptor.getType();
            if (mergeFieldTypeInstance == null) {
                throw new GeoException("mergeField is not valid");
            }

            mergeFieldType = featureCollection.getSchema().getDescriptor(mergeField).getType().getBinding().getCanonicalName();
        }

        Map<Object, List<Geometry>> mergeMap = new HashMap<>();

        if (featureCollection != null) {

            FeatureIterator<Feature> iterator = featureCollection.features();


            try {
                while (iterator.hasNext()) {
                    Feature feature = iterator.next();

                    Property property = feature.getProperty(mergeField);
                    if (property != null) {

                        Object key = feature.getProperty(mergeField).getValue();

                        mergeMap.computeIfAbsent(key, k -> new ArrayList<>());

                        List<Geometry> data = mergeMap.get(key);
                        //如果不做validate可能会出现自相交的问题而导致异常

                        Geometry validateGeometry = GeometryOptimizeUtils.validate((Geometry) feature.getDefaultGeometryProperty().getValue());
                        if (validateGeometry != null) {
                            data.add(validateGeometry);
                        }
                    }


                }
            } finally {
                iterator.close();
            }
        }


        final SimpleFeatureType type = DataUtilities.createType("Location", "geometry:" + geometryType + "," + mergeField + ":" + mergeFieldType);

        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureCollection collection = new ListFeatureCollection(type, features);

        Integer i = 0;

        for (Map.Entry<Object, List<Geometry>> entry : mergeMap.entrySet()) {
            Geometry geometry = GeometryOptimizeUtils.union(entry.getValue());

            if (geometry != null) {
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
                featureBuilder.add(geometry);
                SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(i)); //给一个固定的id
                feature.setAttribute(mergeField, entry.getKey());
                features.add(feature);
                i++;
            }


        }


        return collection;
    }

    /**
     * 根据属性合并
     */
    public static SimpleFeatureCollection mergeByField(SimpleFeatureCollection featureCollection, String mergeField, String geometryType, String mergeFieldType) throws GeoException, SchemaException {


        if (CharSequenceUtil.isEmpty(mergeField)) {
            return featureCollection;
        }

        if (CharSequenceUtil.isEmpty(mergeFieldType)) {
            mergeFieldType = featureCollection.getSchema().getDescriptor(mergeField).getType().getBinding().getSimpleName();
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

                    Geometry validateGeometry = GeometryOptimizeUtils.validate((Geometry) feature.getDefaultGeometry());
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

            Geometry geometry = GeometryOptimizeUtils.union(entry.getValue());
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


    public static SimpleFeatureCollection makeValid(SimpleFeatureCollection featureCollection) {


        try {
            Geometries geometryType = JsonGeomTranslator.getGeometryType(featureCollection);

            switch (geometryType) {
                case MULTIPOLYGON:
                case POLYGON: {
                    List<Geometry> results = new ArrayList<>();

                    List<Geometry> geometryList = JsonGeomTranslator.readGeometries(featureCollection);

                    for (Geometry target : geometryList) {
                        results.add(GeometryOptimizeUtils.validate(target));
                    }

                    return JsonGeomTranslator.getSimpleFeatureCollection(results);

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

        List<Geometry> geometryList = JsonGeomTranslator.readGeometries(featureCollection);
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

        return JsonGeomTranslator.getSimpleFeatureCollection(intersectionGeometryList, geometryType);
    }

    public static SimpleFeatureCollection union(SimpleFeatureCollection featureCollection) throws Exception {

        return union(featureCollection, null);


    }

    public static SimpleFeatureCollection union(SimpleFeatureCollection featureCollection, String geometryType) throws GeoException {

        List<Geometry> geometryList = JsonGeomTranslator.readGeometries(featureCollection);

        if (!CharSequenceUtil.isEmpty(geometryType)) {
            List<Geometry> geometryListAfterProcess = new ArrayList<>();

            for (Geometry geometry : geometryList) {

                if (geometry != null) {
                    if (CharSequenceUtil.endWith(geometryType, geometry.getGeometryType())) {
                        geometryListAfterProcess.add(GeometryOptimizeUtils.validate(geometry));
                    } else if (CharSequenceUtil.equals(geometry.getGeometryType(), "GeometryCollection")) {

                        String suffixGeometryType = CharSequenceUtil.removePrefix(geometryType, "Multi");

                        for (int index = 0; index < geometry.getNumGeometries(); index++) {
                            Geometry tmp = geometry.getGeometryN(0);
                            //如果是同一类型的，就加进来
                            if (CharSequenceUtil.equals(geometryType, tmp.getGeometryType()) || CharSequenceUtil.equals(suffixGeometryType, tmp.getGeometryType())) {
                                geometryListAfterProcess.add(GeometryOptimizeUtils.validate(tmp));
                            }

                        }
                    }
                }


            }
            return JsonGeomTranslator.getSimpleFeatureCollection(GeometryOptimizeUtils.union(geometryListAfterProcess), geometryType);
        }


        return JsonGeomTranslator.getSimpleFeatureCollection(GeometryOptimizeUtils.union(geometryList), geometryType);


    }


    public static SimpleFeatureCollection optimize(SimpleFeatureCollection featureCollection) {

        try {
            Geometries geometryType = JsonGeomTranslator.getGeometryType(featureCollection);

            if (geometryType == null) {
                return featureCollection;
            }

            switch (geometryType) {

                case LINESTRING:
                case MULTILINESTRING:
                    List<Geometry> geometryList = JsonGeomTranslator.readGeometries(featureCollection);
                    return JsonGeomTranslator.getSimpleFeatureCollection(GeometryOptimizeUtils.mergeLines(geometryList));
                case POLYGON:
                case MULTIPOLYGON:
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


}
