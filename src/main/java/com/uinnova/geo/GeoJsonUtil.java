package com.uinnova.geo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.NumberUtil;
import com.uinnova.geo.exception.GeoException;
import com.uinnova.geo.json.GeojsonOptimizeUtil;
import com.uinnova.geo.json.JsonGeomTranslator;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.geojson.GeoJSONDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.util.URLs;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 蔡惠民
 * @date 2022/9/28 10:03
 */
public class GeoJsonUtil {

    private GeoJsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String geometry2GeoJson(Geometry geometry) throws IOException {
        GeometryJSON geometryJson = new GeometryJSON();
        StringWriter writer = new StringWriter();
        geometryJson.write(geometry, writer);
        writer.close();

        return writer.toString();
    }

    public static Geometry geoJson2Geometry(String geojsonString) throws IOException {
        if (geojsonString == null) {
            return null;
        }
        GeometryJSON gjson = new GeometryJSON();
        Reader reader = new StringReader(geojsonString);
        Geometry geometry = gjson.read(reader);
        geometry.setSRID(4326); //默认为4326
        return geometry;
    }


    /**
     * 将geojson对象转换为要素集合
     * 尽量使用 fromJsonAsSimpleFeatureCollection
     *
     * @param geojsonString geojson 字符串
     * @param decimals      精确到小数点后的位数
     */
    public static FeatureCollection<FeatureType, Feature> fromJson(String geojsonString, int decimals) throws GeoException {

        return (FeatureCollection) fromJsonAsSimpleFeatureCollection(geojsonString, decimals);
    }


    /**
     * 将geojson文件转换为要素集合，这个结果中是可以区分 Multi和Single的，但是fromJson那个方法会将所有 geometry类型统一成第一个要素的geometry类型，所以这个方法是优于fromJson，应优先使用
     *
     * @param inFile geojson 文件
     */
    public static SimpleFeatureCollection fromFile(File inFile) throws IOException {


        Map<String, Object> params = new HashMap<>();
        params.put(GeoJSONDataStoreFactory.URL_PARAM.key, URLs.fileToUrl(inFile));
        DataStore newDataStore = DataStoreFinder.getDataStore(params);

        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(newDataStore.getTypeNames()[0]);
        return featureSource.getFeatures();


    }

    /**
     * 将geojson对象转换为要素集合
     */
    public static FeatureCollection<FeatureType, Feature> fromJson(String geojsonString) throws GeoException {
        return fromJson(geojsonString, GeoConstants.GEOJSON_COORDINATES_ACCURACY);
    }


    /**
     * 根据属性字段合并geojson
     *
     * @param geojsonStr                  geojson 的字符串
     * @param mergeField                  合并的属性
     * @param mergeFieldType              指定合并属性的类型
     * @param forceMergeField2NumberIfCan 是否在可以将合并属性转换成数字的时候转换成数字
     * @author caihuimin
     */
    public static String mergeByField(String geojsonStr, String mergeField, String mergeFieldType, Boolean forceMergeField2NumberIfCan) throws GeoException, SchemaException {

        FeatureCollection featureCollection = fromJson(geojsonStr, GeoConstants.GEOJSON_COORDINATES_ACCURACY);


        if (BooleanUtil.isTrue(forceMergeField2NumberIfCan) && mergeFieldType == null && featureCollection.size() > 0) {
            FeatureIterator featureIterator = featureCollection.features();
            if (featureIterator.hasNext()) {
                Feature feature = featureIterator.next();

                Property val = feature.getProperty(mergeField);
                if (val != null && val.getValue() != null && NumberUtil.isNumber(val.getValue().toString())) {
                    //暂定都是Double，因为也能涵盖Integer的情况
                    mergeFieldType = "Double";
                }
            }

            featureIterator.close();

        }

        String geometryType = featureCollection.getSchema().getGeometryDescriptor().getType().getBinding().getSimpleName();
        featureCollection.getSchema().getDescriptor("mergeField");

        FeatureCollection result = GeojsonOptimizeUtil.mergeByField(featureCollection, mergeField, getMultiGeometryType(geometryType), mergeFieldType);

        return JsonGeomTranslator.features2jsonString(result, GeoConstants.GEOJSON_COORDINATES_ACCURACY);

    }

    /**
     * 根据属性字段合并geojson
     *
     * @param geojsonStr geojson 的字符串
     * @param mergeField 合并的属性
     * @author caihuimin
     */
    public static String mergeByField(String geojsonStr, String mergeField) throws GeoException, SchemaException {

        return mergeByField(geojsonStr, mergeField, null, false);
    }

    /**
     * 获取集合类型对应的 Multi类型，例如 输入Point，得到MultiPoint
     *
     * @param geometryType 几何类型
     * @author caihuimin
     */
    static String getMultiGeometryType(String geometryType) {
        if (CharSequenceUtil.startWith(geometryType, "Multi")) {
            return geometryType;
        }

        return "Multi" + geometryType;
    }


    /**
     * 粗略效验 geojson 字符串
     *
     * @param geojsonStr geojson 的字符串
     */
    public static void roughCheck(String geojsonStr) throws GeoException {
        fromJson(geojsonStr);

    }


    /**
     * 将geojson合并一条数据 （如 n Polygon -> 1 MultiPolygon）
     *
     * @param geojsonStr geojson 的字符串
     * @author caihuimin
     */
    public static String merge(String geojsonStr) throws GeoException {

        FeatureCollection featureCollection = fromJson(geojsonStr);
        if (featureCollection.getSchema() == null) {
            throw new GeoException("无法读取元数据，数据可能是空的！");
        }

        GeometryDescriptor geometryDescriptor = featureCollection.getSchema().getGeometryDescriptor();
        if (geometryDescriptor == null) {
            throw new GeoException("无法获取几何图形的元数据，可能数据是空的或者没有geometry字段！");
        }

        String geometryType = featureCollection.getSchema().getGeometryDescriptor().getType().getBinding().getSimpleName();

        FeatureCollection result = GeojsonOptimizeUtil.union(DataUtilities.collection(featureCollection), getMultiGeometryType(geometryType));

        return toString(result);

    }

    /**
     * 将featureCollection转换成字符串
     *
     * @author caihuimin
     */
    public static String toString(FeatureCollection featureCollection) throws GeoException {
        //如果是空值，就返回一个空的featureCollection
        if (featureCollection == null || featureCollection.size() == 0) {
            return "{\n" +
                    "        \"type\": \"FeatureCollection\",\n" +
                    "        \"features\": []\n" +
                    "}";
        }


        FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(GeoConstants.GEOJSON_COORDINATES_ACCURACY));

        if (featureCollection.getSchema() != null) {
            boolean geometryLess = featureCollection.getSchema().getGeometryDescriptor() == null;
            featureJSON.setEncodeFeatureCollectionBounds(!geometryLess);
            featureJSON.setEncodeFeatureCollectionCRS(!geometryLess);
        }

        StringWriter stringWriter = new StringWriter();
        try {
            featureJSON.writeFeatureCollection(featureCollection, stringWriter);
        } catch (IOException e) {
            throw new GeoException("写入geojson失败！", e);
        }
        return stringWriter.toString();
    }

    /**
     * 校验geojson featureCollection
     *
     * @param geojsonString geojson的字符串
     * @author caihuimin
     */
    public static String validate(String geojsonString) throws IOException, GeoException {
        FeatureCollection featureCollection = fromJson(geojsonString);
        FeatureCollection validateFeatures = GeoUtil.validate(featureCollection);
        return toString(validateFeatures);
    }


    /**
     * 校验geojson featureCollection
     *
     * @param featureCollection featureCollection
     * @author caihuimin
     */
    public static String validate(FeatureCollection featureCollection) throws IOException, GeoException {
        FeatureCollection validateFeatures = GeoUtil.validate(featureCollection);
        return toString(validateFeatures);
    }

    /**
     * 坐标转换geojson featureCollection
     *
     * @param geojsonFile geojson的文件
     * @param inputCRS    数据本身的坐标系
     * @param outputCRS   需要转换成的坐标系
     */
    public static String coordinateTransform(File geojsonFile, String inputCRS, String outputCRS) throws IOException, GeoException {
        return coordinateTransform(FileUtil.readString(geojsonFile, CharsetUtil.UTF_8), inputCRS, outputCRS);
    }

    /**
     * 坐标转换geojson featureCollection
     *
     * @param geojsonString geojson的字符串
     * @param inputCRS      数据本身的坐标系
     * @param outputCRS     需要转换成的坐标系
     */
    public static String coordinateTransform(String geojsonString, String inputCRS, String outputCRS) throws IOException, GeoException {

        FeatureCollection featureCollection = fromJson(geojsonString);

        if (featureCollection == null || featureCollection.size() == 0) {
            return geojsonString;
        }

        if (featureCollection.getSchema() == null) {
            throw new GeoException("schema is null");
        }

        FeatureCollection result = CoordinateSystemTransformer.transform(featureCollection, inputCRS, outputCRS);

        return toString(result);

    }

    /**
     * 将geojson字符串读取成SimpleFeatureCollection
     *
     * @param geojsonStr geojson的字符串
     * @author caihuimin
     */
    public static SimpleFeatureCollection fromJsonAsSimpleFeatureCollection(String geojsonStr) throws GeoException {
        return fromJsonAsSimpleFeatureCollection(geojsonStr, GeoConstants.GEOJSON_COORDINATES_ACCURACY);
    }

    /**
     * 将geojson字符串读取成SimpleFeatureCollection
     *
     * @param geojsonStr geojson的字符串
     * @author caihuimin
     */
    public static SimpleFeatureCollection fromJsonAsSimpleFeatureCollection(String geojsonStr, int decimals) throws GeoException {

        GeometryJSON geometryJSON = new GeometryJSON(decimals);

        FeatureJSON featureJSON = new FeatureJSON(geometryJSON);

        try {
            SimpleFeatureCollection simpleFeatureCollection = (SimpleFeatureCollection) featureJSON.readFeatureCollection(geojsonStr);
            if (simpleFeatureCollection.size() == 0) {
                GeometryCollection geometryCollection;
                try {
                    //用于判断是否是geometryCollection
                    geometryCollection = geometryJSON.readGeometryCollection(geojsonStr);
                } catch (Exception e) {
                    return simpleFeatureCollection;
                }

                if (geometryCollection != null && BooleanUtil.isFalse(geometryCollection.isEmpty())) {
                    throw new GeoException("支持读取FeatureCollection,无法读取GeometryCollection!");
                }

            }

            return simpleFeatureCollection;
        } catch (IOException e) {
            throw new GeoException("读取geojson失败！", e);
        }
    }

    /**
     * 删除geometry为null的
     *
     * @param geojsonStr geojson的字符串
     * @author caihuimin
     */
    public static String removeNullGeometryItem(String geojsonStr) throws GeoException {

        SimpleFeatureCollection simpleFeatureCollection = GeoJsonUtil.fromJsonAsSimpleFeatureCollection(geojsonStr);

        SimpleFeatureCollection featureCollectionWithGeometryNullRemoved = GeoUtil.removeNullGeometryItem(simpleFeatureCollection);

        return GeoJsonUtil.toString(featureCollectionWithGeometryNullRemoved);

    }


}
