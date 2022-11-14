package com.uinnova.geo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.CharsetUtil;
import com.uinnova.geo.exception.GeoException;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author 蔡惠民
 * 针对shp文件的操作
 */
public class ShpUtil {

    private static final Logger LOGGER = Logging.getLogger(ShpUtil.class);

    private ShpUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String parseShpAsString(String path) throws GeoException {
        return parseShpAsString(new File(path));
    }

    /**
     * 将shp文件解析为字符串
     *
     * @param file .shp文件
     *
     * @throws  GeoException see in msg
     * @return geojson string
     */
    public static String parseShpAsString(File file) throws GeoException {

        SimpleFeatureCollection simpleFeatureCollection;
        try {
            simpleFeatureCollection = ShpUtil.parseShp(file);
        } catch (IOException | FactoryException e) {
            throw new GeoException("读取shp失败！");
        }

        return GeoJsonUtil.toString(simpleFeatureCollection);

    }

    public static SimpleFeatureCollection parseShp(String path) throws IOException, FactoryException {
        return parseShp(new File(path));
    }

    /***
     *
     * 解析shp文件，并投影转换为经纬度坐标
     * @param file .shp文件
     * @throws IOException io
     * @throws FactoryException schema
     * @return  SimpleFeatureCollection
     *
     * */
    public static SimpleFeatureCollection parseShp(File file) throws IOException, FactoryException {

        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        ShapefileDataStore dataStore = new ShapefileDataStore(file.toURI().toURL());
        dataStore.setFidIndexed(false);
        dataStore.setIndexed(false);
        dataStore.setCharset(CharsetUtil.CHARSET_UTF_8);
        SimpleFeatureSource source = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);

        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

        //如果没有坐标系，就直接返回
        if (source.getSchema() == null) {
            return source.getFeatures();
        }


        //如果本身就是wgs84，就不需要转换，在geotool 27版本，如果坐标系本身是GCS_WGS_1984，进行坐标转换会报错，所以就直接返回了
        if (CharSequenceUtil.equals("GCS_WGS_1984",
                source.getSchema().getCoordinateReferenceSystem().getCoordinateSystem().getName().getCode())) {
            return FeatureCollectionUtil.removeNullGeometryItem(source.getFeatures());
        }

        Query query = new Query();

        query.setCoordinateSystemReproject(CRS.decode("EPSG:4326", true));

        query.setFilter(filter);

        try {
            //通过此方式判断投影转换是否正常
            SimpleFeatureIterator iterator = source.getFeatures(query).features();
            if (iterator.hasNext()) {
                iterator.next();
            }

            iterator.close();

            return FeatureCollectionUtil.removeNullGeometryItem(source.getFeatures(query));
        } catch (Exception e) {
            LOGGER.warning("transformation error, will return the origin!");
            //如果投影转换异常，就直接返回没有投影的结果
            return FeatureCollectionUtil.removeNullGeometryItem(source.getFeatures());

        }


    }


}
