import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.CharsetUtil;
import io.github.baozi926.geo.*;
import io.github.baozi926.geo.exception.GeoException;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author 蔡惠民
 * @date 2022/9/28 10:12
 * 需要注意，输出的文件都需要在output文件夹下，因为这个文件夹不会上传到git
 */

class HelloWorldTest {

    /**
     * 确保测试结果的目录被创建
     */
    @BeforeAll
    static void mkOutputDirs() {

        File output = new File("." + File.separator + "output");
        FileUtil.mkdir(output);
        Assertions.assertEquals(true, output.exists());


    }


    @Test
    void readGeojson() throws GeoException {

        File input = new File("." + File.separator + "data" + File.separator + "Railway.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);

        SimpleFeatureCollection simpleFeatureCollection = GeoJsonUtil.fromJsonAsSimpleFeatureCollection(geojsonStr, 7);


        Assertions.assertTrue(simpleFeatureCollection.size() > 0);

    }

    @Test
    void mergeByField() throws Exception {


        File input = new File("." + File.separator + "data" + File.separator + "geojsonTestMerge.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "mergeByFieldResult.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);


        String resultStr = GeoJsonUtil.mergeByField(geojsonStr, "height");


        FileUtil.del(output);


        FileUtil.writeString(resultStr, output, CharsetUtil.UTF_8);


        Assertions.assertNotNull(resultStr);

    }


    @Test
    void mergeByField3() throws Exception {


        File input = new File("." + File.separator + "data" + File.separator + "merge_forceNumber.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "merge_forceNumber_mergeByFieldResult.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);


        String resultStr = GeoJsonUtil.mergeByField(geojsonStr, "number", null, true);


        FileUtil.del(output);


        FileUtil.writeString(resultStr, output, CharsetUtil.UTF_8);


        Assertions.assertNotNull(resultStr);

    }


    @Test
    void merge2() throws Exception {


        File input = new File("." + File.separator + "data" + File.separator + "NotMerge.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "mergeResult2.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);


        String resultStr = GeoJsonUtil.merge(geojsonStr);


        FileUtil.del(output);


        FileUtil.writeString(resultStr, output, CharsetUtil.UTF_8);

        Assertions.assertNotNull(resultStr);

    }

    @Test
    void merge3() throws Exception {


        File input = new File("." + File.separator + "data" + File.separator + "MergeBuilding.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "MergeBuilding_mergeResult.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);


        String resultStr = GeoJsonUtil.merge(geojsonStr);


        FileUtil.del(output);


        FileUtil.writeString(resultStr, output, CharsetUtil.UTF_8);

        Assertions.assertNotNull(resultStr);

    }


    @Test
    void merge4() throws Exception {


        File input = new File("." + File.separator + "data" + File.separator + "建筑解密_融合_慢.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "建筑解密_融合_慢_mergeResult.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);


        String resultStr = GeoJsonUtil.merge(geojsonStr);


        FileUtil.del(output);


        FileUtil.writeString(resultStr, output, CharsetUtil.UTF_8);

        Assertions.assertNotNull(resultStr);

    }


    @Test
    void merge() throws Exception {


        File input = new File("." + File.separator + "data" + File.separator + "geojsonTestMerge.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "mergeResult.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);


        String resultStr = GeoJsonUtil.merge(geojsonStr);


        FileUtil.del(output);


        FileUtil.writeString(resultStr, output, CharsetUtil.UTF_8);

        Assertions.assertNotNull(resultStr);

    }

    @Test
    void mergeLine() throws Exception {


        File input = new File("." + File.separator + "data" + File.separator + "RoadImportant.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "RoadImportant_mergeResult.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);


        String resultStr = GeoJsonUtil.merge(geojsonStr);


        FileUtil.del(output);


        FileUtil.writeString(resultStr, output, CharsetUtil.UTF_8);

        Assertions.assertNotNull(resultStr);

    }

    @Test
    void merge5() throws Exception {


        File input = new File("." + File.separator + "data" + File.separator + "融合待测数据_解密.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "融合待测数据_解密_mergeResult.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);


        String resultStr = GeoJsonUtil.merge(geojsonStr);


        FileUtil.del(output);


        FileUtil.writeString(resultStr, output, CharsetUtil.UTF_8);

        Assertions.assertNotNull(resultStr);

    }

    @Test
    void readJson() throws GeoException {
        File input = new File("." + File.separator + "data" + File.separator + "testLine.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);


        FeatureCollection featureCollection = GeoJsonUtil.fromJson(geojsonStr);

        FeatureIterator featureIterator = featureCollection.features();

        Boolean hasMulti = false;

        Boolean hasSingle = false;

        while (featureIterator.hasNext()) {
            Feature feature = featureIterator.next();

            Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();

            if (CharSequenceUtil.equals("LineString", geometry.getGeometryType())) {
                hasSingle = true;
            }

            if (CharSequenceUtil.equals("MultiLineString", geometry.getGeometryType())) {
                hasMulti = true;
            }


        }

        Assertions.assertTrue(hasMulti);
        Assertions.assertTrue(hasSingle);

    }

    @Test
    void merge6() throws Exception {

        File input = new File("." + File.separator + "data" + File.separator + "testMerge.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "testMerge_mergeResult_By_height.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);


        String resultStr = GeoJsonUtil.mergeByField(geojsonStr, "height", "Double", false);


        FileUtil.del(output);


        FileUtil.writeString(resultStr, output, CharsetUtil.UTF_8);

        Assertions.assertNotNull(resultStr);

    }

    @Test
    void merge7() throws Exception {

        File input = new File("." + File.separator + "data" + File.separator + "roads-ny-queen.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "roads-ny-queen_mergeByKind.geojson");


        SimpleFeatureCollection simpleFeatureCollection = GeoJsonUtil.fromFile(input);

        simpleFeatureCollection = FeatureCollectionUtil.mergeByField(simpleFeatureCollection, "kind", "MultiLineString", "String");


        FileUtil.del(output);


        FileUtil.writeString(GeoJsonUtil.toString(simpleFeatureCollection), output, CharsetUtil.UTF_8);


    }


    @Test
    void merge8() throws Exception {

        File input = new File("." + File.separator + "data" + File.separator + "roads-ny-queen.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "roads-ny-queen_mergeByKind.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);

        String resultStr = GeoJsonUtil.mergeByField(geojsonStr, "kind");

        FileUtil.del(output);

        FileUtil.writeString(resultStr, output, CharsetUtil.UTF_8);

    }


    @Test
    void validate() throws IOException, GeoException {

        String geojson = "{\n" +
                "        \"type\": \"FeatureCollection\",\n" +
                "        \"features\": []\n" +
                "}";

        String validateGeojson = GeoJsonUtil.validate(geojson);

        Assertions.assertNotNull(validateGeojson);
    }


    @Test
    void validate2() throws GeoException {
        File input = new File("." + File.separator + "data" + File.separator + "MergeBuilding.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "1_validate.geojson");
        FileUtil.del(output);

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);

        GeoJsonUtil.validate(geojsonStr);

        FileUtil.writeString(geojsonStr, output, CharsetUtil.UTF_8);

    }

    @Test
    void validateInvalid() throws GeoException {
        File input = new File("." + File.separator + "data" + File.separator + "invalidGeometries.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "invalidGeometries_validate.geojson");
        FileUtil.del(output);

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);

        GeoJsonUtil.validate(geojsonStr);

        FileUtil.writeString(geojsonStr, output, CharsetUtil.UTF_8);


    }


    @Test
    void validateFiles() {
        File inputDir = new File("." + File.separator + "data" + File.separator + "GEOJSON校验测试");

        //应该报错的文件
        String[] shouldErrorList = new String[]{
                "12_Position_Array_Length_L2.geojson",
                "14_Coordinates_Not_Number.geojson",
                "17_LinearRing_Points_Length_L4.geojson",
                "18_LinearRing_Points_Not_Close.geojson",
                "19_Line_Points_Length_L2.geojson",
                "1_ERROR_JSON_FORMATE.geojson",
                "3_Un_Support_Type.geojson"
        };


        for (String fileName : FileUtil.listFileNames(inputDir.getAbsolutePath())) {
            File output = new File("." + File.separator + "output" + File.separator + "geojson效验测试" + File.separator + "fromJson_" + fileName);
            FeatureCollection featureCollection = null;

            try {
                String geojsonStr = FileUtil.readString(inputDir.getAbsoluteFile() + File.separator + fileName, CharsetUtil.UTF_8);

                FileUtil.mkParentDirs(output);

                FileUtil.del(output);

                featureCollection = GeoJsonUtil.fromJson(geojsonStr);

                FileUtil.writeString(GeoJsonUtil.toString(featureCollection), output, CharsetUtil.UTF_8);

            } catch (Exception e) {
                Boolean shouldError = false;
                for (String errorFile : shouldErrorList) {

                    if (CharSequenceUtil.equals(errorFile, fileName)) {
                        shouldError = true;
                        break;
                    }
                }
                //如果不应该报错的文件报错了，那就有问题了
                Assertions.assertTrue(shouldError, () -> String.format("文件[%s]不应该报错", fileName));

            }


            if (featureCollection != null) {

                FeatureCollection finalFeatureCollection = featureCollection;
                Assertions.assertDoesNotThrow(() -> {

                    File validate = new File("." + File.separator + "output" + File.separator + "geojson矫正测试" + File.separator + "fromJson_" + fileName);
                    FileUtil.mkParentDirs(validate);
                    FileUtil.del(validate);
                    String validateStr = GeoJsonUtil.validate(finalFeatureCollection);
                    FileUtil.writeString(validateStr, validate, CharsetUtil.UTF_8);

                });

            }


        }


    }

    @Test
    void validateLine() throws GeoException {
        File input = new File("." + File.separator + "data" + File.separator + "lineWithHeight.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "lineWithHeight_validate.geojson");
        FileUtil.del(output);

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);

        geojsonStr = GeoJsonUtil.validate(geojsonStr);

        FileUtil.writeString(geojsonStr, output, CharsetUtil.UTF_8);

    }

    @Test
    void validatePolygon() throws GeoException {
        File input = new File("." + File.separator + "data" + File.separator + "polygonWithHeight.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "polygonWithHeight_validate.geojson");

        FileUtil.del(output);

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);

        geojsonStr = GeoJsonUtil.validate(geojsonStr);

        FileUtil.writeString(geojsonStr, output, CharsetUtil.UTF_8);

    }


    @Test
    void parseShp3() throws GeoException {
        File input = new File("." + File.separator + "data" + File.separator + "buildings" + File.separator + "buildings.shp");
        File output = new File("." + File.separator + "output" + File.separator + "buildings_fromShp.geojson");
        FileUtil.del(output);

        String geojsonStr = ShpUtil.parseShpAsString(input);

        FeatureCollection featureCollection = GeoJsonUtil.fromJson(GeoJsonUtil.validate(geojsonStr));

        FileUtil.writeString(GeoJsonUtil.toString(featureCollection), output, CharsetUtil.UTF_8);
    }



    @Test
    void testNestedArrayProperties() throws GeoException {
        File input = new File("." + File.separator + "data" + File.separator + "testNestedArrayProperties.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "testNestedArrayProperties_result.geojson");
        FileUtil.del(output);

        String geojsonStr = GeoJsonUtil.removeNullGeometryItem(FileUtil.readString(input, CharsetUtil.UTF_8));

        FileUtil.writeString(geojsonStr, output, CharsetUtil.UTF_8);


    }

    @Test
    void parseShp4() throws Exception {
        File input = new File("." + File.separator + "data" + File.separator + "buildings" + File.separator + "buildings.shp");
        File output = new File("." + File.separator + "output" + File.separator + "buildings_fromShp.geojson");
        FileUtil.del(output);

        String geojsonStr = ShpUtil.parseShpAsString(input);
        FileUtil.writeString(geojsonStr, output, CharsetUtil.UTF_8);

        FeatureCollection featureCollection = GeoJsonUtil.fromJson(GeoJsonUtil.validate(geojsonStr));

        FileUtil.writeString(GeoJsonUtil.toString(featureCollection), output, CharsetUtil.UTF_8);

        Assertions.assertEquals(3263, featureCollection.size());

    }

    @Test
    void parseShp2AndMerge() {


        Assertions.assertDoesNotThrow(() -> {

            File input = new File("." + File.separator + "data" + File.separator + "厦门2" + File.separator + "厦门.shp");
            File output = new File("." + File.separator + "output" + File.separator + "厦门2.geojson");
            FileUtil.del(output);
            String geojsonStr = ShpUtil.parseShpAsString(input);
            String merge = GeoJsonUtil.mergeByField(geojsonStr, "Floor");
            FileUtil.writeString(merge, output, CharsetUtil.UTF_8);

        });


    }

    @Test
    void removeNullGeometry() throws GeoException {
        File input = new File("." + File.separator + "data" + File.separator + "nullGeometry.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "nullGeometry_result.geojson");
        FileUtil.del(output);

        String geojsonStr = GeoJsonUtil.removeNullGeometryItem(FileUtil.readString(input, CharsetUtil.UTF_8));

        FileUtil.writeString(geojsonStr, output, CharsetUtil.UTF_8);


    }


    @Test
    void removeNullGeometry2() throws GeoException {

        String geojson = "{\n" +
                "        \"type\": \"FeatureCollection\",\n" +
                "        \"features\": []\n" +
                "}";

        GeoJsonUtil.removeNullGeometryItem(geojson);

    }

    @Test
    void removeNullGeometry3() {


        Assertions.assertThrows(GeoException.class, () -> {
            File input = new File("." + File.separator + "data" + File.separator + "geometryCollection.geojson");
            File output = new File("." + File.separator + "output" + File.separator + "geometryCollection_result.geojson");
            FileUtil.del(output);

            String geojsonStr = GeoJsonUtil.removeNullGeometryItem(FileUtil.readString(input, CharsetUtil.UTF_8));

            FileUtil.writeString(geojsonStr, output, CharsetUtil.UTF_8);
        });


    }


    @Test
    void convertType() throws GeoException {
        File input = new File("." + File.separator + "data" + File.separator + "MergeBuilding.geojson");
        File output = new File("." + File.separator + "output" + File.separator + "MergeBuilding_convertType.geojson");

        String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);
        FeatureCollection featureCollection = GeoJsonUtil.fromJson(geojsonStr);


        FeatureType featureType = featureCollection.getSchema();

        SimpleFeatureType simpleFeatureType = GeometryUtil.featureTypeToSimpleFeatureType(featureType);
        List<SimpleFeature> features = new ArrayList<>();

        FeatureIterator featureIterator = featureCollection.features();

        while (featureIterator.hasNext()) {

            Feature feature = featureIterator.next();

            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(simpleFeatureType);

            SimpleFeature result = featureBuilder.buildFeature(null);

            for (Property property : feature.getProperties()) {
                result.setAttribute(property.getName(), feature.getProperty(property.getName()));

            }
            result.setDefaultGeometry(feature.getProperty(feature.getDefaultGeometryProperty().getName()));

            features.add(result);
        }

        SimpleFeatureCollection simpleFeatureCollection = new ListFeatureCollection(simpleFeatureType, features);

        FileUtil.writeString(GeoJsonUtil.toString(simpleFeatureCollection), output, CharsetUtil.UTF_8);

    }


    /***
     *
     * 坐标转换
     *
     * */
    @Test
    void coordinateTransform() {


        Assertions.assertDoesNotThrow(() -> {
            File gcj02 = new File("." + File.separator + "data" + File.separator + "MergeBuilding.geojson");
            File wgs84 = new File("." + File.separator + "output" + File.separator + "MergeBuilding_wgs84.geojson");
            File bd09 = new File("." + File.separator + "output" + File.separator + "MergeBuilding_bd09.geojson");

            FileUtil.del(wgs84);
            FileUtil.del(bd09);

            FileUtil.writeString(GeoJsonUtil.coordinateTransform(FileUtil.readString(gcj02, CharsetUtil.UTF_8),
                    GeoConstants.GCJ02,
                    GeoConstants.WGS84
            ), wgs84, CharsetUtil.UTF_8);

            FileUtil.writeString(GeoJsonUtil.coordinateTransform(FileUtil.readString(gcj02, CharsetUtil.UTF_8),
                    GeoConstants.GCJ02,
                    GeoConstants.BD09)
                    , bd09, CharsetUtil.UTF_8);


            GeoJsonUtil.coordinateTransform(wgs84,
                    GeoConstants.WGS84,
                    GeoConstants.GCJ02);

            GeoJsonUtil.coordinateTransform(wgs84,
                    GeoConstants.WGS84,
                    GeoConstants.BD09);

            GeoJsonUtil.coordinateTransform(FileUtil.readString(wgs84, CharsetUtil.UTF_8),
                    GeoConstants.BD09,
                    GeoConstants.GCJ02);

            GeoJsonUtil.coordinateTransform(FileUtil.readString(wgs84, CharsetUtil.UTF_8),
                    GeoConstants.BD09,
                    GeoConstants.WGS84);


        }, () -> String.format("坐标转换失败！"));


    }


    @Test
    void geometry2Geojson() throws IOException {
        String geojson = " {\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [\n" +
                "              96.6796875,\n" +
                "              52.482780222078226\n" +
                "            ],\n" +
                "            [\n" +
                "              90,\n" +
                "              32.54681317351514\n" +
                "            ],\n" +
                "            [\n" +
                "              109.6875,\n" +
                "              26.745610382199022\n" +
                "            ],\n" +
                "            [\n" +
                "              100.1953125,\n" +
                "              40.44694705960048\n" +
                "            ],\n" +
                "            [\n" +
                "              101.6015625,\n" +
                "              44.33956524809713\n" +
                "            ],\n" +
                "            [\n" +
                "              96.6796875,\n" +
                "              52.482780222078226\n" +
                "            ]\n" +
                "          ]\n" +
                "        ]\n" +
                "      }";


        Geometry geometry = GeoJsonUtil.geoJson2Geometry(geojson);

        GeoJsonUtil.geometry2GeoJson(geometry);


    }


    @Test
    void testCopy() {

        Assertions.assertDoesNotThrow(() -> {
            String geojson = "{\n" +
                    "        \"type\": \"FeatureCollection\",\n" +
                    "        \"features\": []\n" +
                    "}";

            FeatureCollectionUtil.copy(GeoJsonUtil.fromJsonAsSimpleFeatureCollection(geojson));
        });

        Assertions.assertDoesNotThrow(() -> {
            File gcj02 = new File("." + File.separator + "data" + File.separator + "MergeBuilding.geojson");

            FeatureCollectionUtil.copy(
                    GeoJsonUtil.fromJsonAsSimpleFeatureCollection(
                            FileUtil.readString(
                                    gcj02,
                                    CharsetUtil.UTF_8)
                    )
            );
        });


    }

}
