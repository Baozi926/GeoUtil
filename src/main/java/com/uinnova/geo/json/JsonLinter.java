package com.uinnova.geo.json;

import lombok.extern.slf4j.Slf4j;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

/**
 * JSON数据检测
 * @author caiyang
 **/
@Slf4j
public class JsonLinter {

    private JsonLinter() {
        throw new IllegalStateException("JsonLinter class");
    }

    /**
     * 检测geojson文件是否符合规范。监测json格式，检测坐标点是否正确。
     * 有问题返回fasle，无问题返回true
     */
    public static boolean lintJson(String geojsonString) {
        FeatureCollection<?,?> featureCollection = JsonGeomTranslator.jsonStr2Features(geojsonString, 6);
        if (featureCollection == null) {
            return true;
        }
        return lint(featureCollection);
    }


    /**
     * 检测geojson文件是否符合规范。监测json格式，检测坐标点是否正确。
     * 有问题返回fasle，无问题返回true
     */
    public static boolean lint(String filePath) {

        FeatureCollection<?,?> featureCollection = JsonGeomTranslator.jsonFile2Features(filePath, 6);
        if (featureCollection == null) {
            return false;
        }
        return lint(featureCollection);
    }

    private static boolean lint(FeatureCollection<?, ?> featureCollection) {
        SimpleFeatureIterator iterator = (SimpleFeatureIterator) featureCollection.features();
        try {
            while( iterator.hasNext() ){
                SimpleFeature feature = iterator.next();

                org.locationtech.jts.geom.Geometry geometry = (org.locationtech.jts.geom.Geometry) feature.getDefaultGeometry();
                //Geometries geomType  = JsonGeomTranslator.getGeomtryType(geometryCom);

                //空间对象格式不正确
                if (geometry.isEmpty()){
                    iterator.close();
                    //System.out.println(geometry.toString());
                    log.info(geometry.toString());
                    return false;
                }

                //空间对象坐标点不合规范
                if (!geometry.isValid()) {
                    iterator.close();
                    //System.out.println(geometry.toString());
                    log.info(geometry.toString());
                    return false;
                }
            }
        }catch (Exception exp) {
            //System.out.println(exp.getMessage());
            log.info(exp.getMessage());
            iterator.close();
            return false;
        }
        finally {
            iterator.close();
        }
        return true;
    }


}
