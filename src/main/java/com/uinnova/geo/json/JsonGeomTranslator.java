package com.uinnova.geo.json;

import cn.hutool.core.text.CharSequenceUtil;
import com.uinnova.geo.exception.GeoException;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.Geometries;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * json和几何对象、要素对象转换
 *
 * @author 蔡杨
 */
public class JsonGeomTranslator {

    private JsonGeomTranslator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将geojson对象转换为要素集合
     */
    public static FeatureCollection<FeatureType, Feature> jsonString2Features(String geojsonString, int decimals) throws IOException {

        FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(decimals));
        return featureJSON.readFeatureCollection(geojsonString);

    }

    /**
     * 将要素集合转换为geojson对象
     */
    public static String features2jsonString(FeatureCollection features, int decimals) throws GeoException {

        if (features == null) {
            return null;
        }

        try {
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(decimals));
            StringWriter w = new StringWriter();
            featureJSON.writeFeatureCollection(features, w);
            return w.toString();
        } catch (Exception exp) {

            throw new GeoException("features2jsonString error");
        }
//        return null;
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

                return JsonGeomTranslator.getGeometryType((Geometry) feature.getDefaultGeometry());
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

                return JsonGeomTranslator.getGeometryType((Geometry) feature.getDefaultGeometry());
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
     * 读json文件，得到要素集合
     */
    public static FeatureCollection<FeatureType, Feature> jsonFile2Features(String jsonPath, int decimals) {

        try {
            //String jsonStr = FileUtil.readUtf8String(jsonPath);
            //return  json2Features(jsonStr, decimals);

            FileInputStream inputStream = new FileInputStream(jsonPath);
            return json2Features(inputStream, decimals);
        } catch (Exception exp) {
            //System.out.println(exp.getMessage());
            return null;
        }
    }

    /**
     * 将geojson对象转换为要素集合
     */
    public static FeatureCollection<FeatureType, Feature> json2Features(InputStream inputStream, int decimals) {
        try {
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(decimals));
            return featureJSON.readFeatureCollection(inputStream);

        } catch (Exception exp) {
            return null;
        }
    }

    /**
     * 讲geojson字符串转为要素集合
     *
     * @param jsonStr  geojson字符串
     * @param decimals 小数位数
     * @return 要素集合
     */
    public static FeatureCollection<FeatureType, Feature> jsonStr2Features(String jsonStr, int decimals) {
        try {
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(decimals));
            return featureJSON.readFeatureCollection(jsonStr);

        } catch (Exception exp) {
            return null;
        }
    }


}
