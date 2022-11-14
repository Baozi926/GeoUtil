package io.github.baozi926.geo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;

import java.util.ArrayList;
import java.util.List;


/**
 * @author 蔡惠民
 * 坐标系转换的工具类
 */
public class CoordinateSystemTransformer {

    private CoordinateSystemTransformer() {
        throw new IllegalStateException("Utility class");
    }


    private static final double X_PI = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double PI = 3.1415926535897932384626;
    private static final double A = 6378245.0;
    private static final double EE = 0.00669342162296594323;


    public static Point transformPoint(Point point, String targetCoordinateSystem, String sourceCoordinateSystem) {

        if (point == null) {
            return null;
        }

        if (CharSequenceUtil.equals(sourceCoordinateSystem, GeoConstants.GCJ02)) {
            if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.WGS84)) {

                Coordinate coordinate = new Coordinate();
                Double[] res = GCJ02ToWGS84(point.getX(), point.getY());
                coordinate.setX(res[0]);
                coordinate.setY(res[1]);

                return point.getFactory().createPoint(coordinate);

            } else if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.BD09)) {

                Coordinate coordinate = new Coordinate();
                Double[] res = GCJ02ToBD09(point.getX(), point.getY());
                coordinate.setX(res[0]);
                coordinate.setY(res[1]);

                return point.getFactory().createPoint(coordinate);

            } else {
                return point;
            }
        }

        if (CharSequenceUtil.equals(sourceCoordinateSystem, GeoConstants.WGS84)) {
            if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.GCJ02)) {

                Coordinate coordinate = new Coordinate();
                Double[] res = WGS84ToGCJ02(point.getX(), point.getY());
                coordinate.setX(res[0]);
                coordinate.setY(res[1]);

                return point.getFactory().createPoint(coordinate);

            } else if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.BD09)) {

                Coordinate coordinate = new Coordinate();
                Double[] res = WGS84ToBD09(point.getX(), point.getY());
                coordinate.setX(res[0]);
                coordinate.setY(res[1]);

                return point.getFactory().createPoint(coordinate);

            } else {
                return point;
            }
        }

        if (CharSequenceUtil.equals(sourceCoordinateSystem, GeoConstants.BD09)) {
            if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.GCJ02)) {

                Coordinate coordinate = new Coordinate();
                Double[] res = BD09ToGCJ02(point.getX(), point.getY());
                coordinate.setX(res[0]);
                coordinate.setY(res[1]);

                return point.getFactory().createPoint(coordinate);

            } else if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.WGS84)) {
                Coordinate coordinate = new Coordinate();
                Double[] res = BD09ToWGS84(point.getX(), point.getY());
                coordinate.setX(res[0]);
                coordinate.setY(res[1]);
                return point.getFactory().createPoint(coordinate);

            } else {
                return point;
            }
        }

        return point;

    }


    public static MultiPoint transformMultiPoint(MultiPoint multiPoint, String targetCoordinateSystem, String sourceCoordinateSystem) {
        if (multiPoint == null) {
            return null;
        }
        Point[] points = new Point[multiPoint.getNumGeometries()];

        for (int i = 0; i < points.length; ++i) {
            points[i] = transformPoint((Point) multiPoint.getGeometryN(i), targetCoordinateSystem, sourceCoordinateSystem);
        }

        return multiPoint.getFactory().createMultiPoint(points);
    }

    public static LineString transformLineString(LineString lineString, String targetCoordinateSystem, String sourceCoordinateSystem) {

        if (lineString == null) {
            return null;
        }

        if (CharSequenceUtil.equals(sourceCoordinateSystem, GeoConstants.GCJ02)) {
            if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.WGS84)) {


                Coordinate[] coordinates = new Coordinate[lineString.getCoordinates().length];

                int length = lineString.getCoordinates().length;

                for (int i = 0; i < length; i++) {
                    Coordinate coordinate = lineString.getCoordinates()[i];
                    Coordinate transformedCoordinate = new Coordinate();
                    Double[] res = GCJ02ToWGS84(coordinate.getX(), coordinate.getY());
                    transformedCoordinate.setX(res[0]);
                    transformedCoordinate.setY(res[1]);
                    coordinates[i] = transformedCoordinate;
                }


                if (lineString instanceof LinearRing) {
                    return lineString.getFactory().createLinearRing(coordinates);
                } else {
                    return lineString.getFactory().createLineString(coordinates);
                }

            } else if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.BD09)) {

                Coordinate[] coordinates = new Coordinate[lineString.getCoordinates().length];

                int length = lineString.getCoordinates().length;

                for (int i = 0; i < length; i++) {
                    Coordinate coordinate = lineString.getCoordinates()[i];
                    Coordinate transformedCoordinate = new Coordinate();
                    Double[] res = GCJ02ToBD09(coordinate.getX(), coordinate.getY());
                    transformedCoordinate.setX(res[0]);
                    transformedCoordinate.setY(res[1]);
                    coordinates[i] = transformedCoordinate;
                }


                if (lineString instanceof LinearRing) {
                    return lineString.getFactory().createLinearRing(coordinates);
                } else {
                    return lineString.getFactory().createLineString(coordinates);
                }

            } else {
                return lineString;
            }
        }

        if (CharSequenceUtil.equals(sourceCoordinateSystem, GeoConstants.WGS84)) {
            if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.GCJ02)) {

                Coordinate[] coordinates = new Coordinate[lineString.getCoordinates().length];

                int length = lineString.getCoordinates().length;

                for (int i = 0; i < length; i++) {
                    Coordinate coordinate = lineString.getCoordinates()[i];
                    Coordinate transformedCoordinate = new Coordinate();
                    Double[] res = WGS84ToGCJ02(coordinate.getX(), coordinate.getY());
                    transformedCoordinate.setX(res[0]);
                    transformedCoordinate.setY(res[1]);
                    coordinates[i] = transformedCoordinate;
                }


                if (lineString instanceof LinearRing) {
                    return lineString.getFactory().createLinearRing(coordinates);
                } else {
                    return lineString.getFactory().createLineString(coordinates);
                }

            } else if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.BD09)) {

                Coordinate[] coordinates = new Coordinate[lineString.getCoordinates().length];

                int length = lineString.getCoordinates().length;

                for (int i = 0; i < length; i++) {
                    Coordinate coordinate = lineString.getCoordinates()[i];
                    Coordinate transformedCoordinate = new Coordinate();
                    Double[] res = WGS84ToBD09(coordinate.getX(), coordinate.getY());
                    transformedCoordinate.setX(res[0]);
                    transformedCoordinate.setY(res[1]);
                    coordinates[i] = transformedCoordinate;
                }


                if (lineString instanceof LinearRing) {
                    return lineString.getFactory().createLinearRing(coordinates);
                } else {
                    return lineString.getFactory().createLineString(coordinates);
                }

            } else {
                return lineString;
            }
        }

        if (CharSequenceUtil.equals(sourceCoordinateSystem, GeoConstants.BD09)) {
            if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.GCJ02)) {

                Coordinate[] coordinates = new Coordinate[lineString.getCoordinates().length];

                int length = lineString.getCoordinates().length;

                for (int i = 0; i < length; i++) {
                    Coordinate coordinate = lineString.getCoordinates()[i];
                    Coordinate transformedCoordinate = new Coordinate();
                    Double[] res = BD09ToGCJ02(coordinate.getX(), coordinate.getY());
                    transformedCoordinate.setX(res[0]);
                    transformedCoordinate.setY(res[1]);
                    coordinates[i] = transformedCoordinate;
                }


                if (lineString instanceof LinearRing) {
                    return lineString.getFactory().createLinearRing(coordinates);
                } else {
                    return lineString.getFactory().createLineString(coordinates);
                }

            } else if (CharSequenceUtil.equals(targetCoordinateSystem, GeoConstants.WGS84)) {
                Coordinate[] coordinates = new Coordinate[lineString.getCoordinates().length];

                int length = lineString.getCoordinates().length;

                for (int i = 0; i < length; i++) {
                    Coordinate coordinate = lineString.getCoordinates()[i];
                    Coordinate transformedCoordinate = new Coordinate();
                    Double[] res = BD09ToWGS84(coordinate.getX(), coordinate.getY());
                    transformedCoordinate.setX(res[0]);
                    transformedCoordinate.setY(res[1]);
                    coordinates[i] = transformedCoordinate;
                }


                if (lineString instanceof LinearRing) {
                    return lineString.getFactory().createLinearRing(coordinates);
                } else {
                    return lineString.getFactory().createLineString(coordinates);
                }

            } else {
                return lineString;
            }
        }

        return lineString;

    }

    public static MultiLineString transformMultiLineString(MultiLineString multiLineString, String targetCoordinateSystem, String sourceCoordinateSystem) {
        if (multiLineString == null) {
            return null;
        }

        LineString[] lineStrings = new LineString[multiLineString.getNumGeometries()];

        for (int i = 0; i < lineStrings.length; ++i) {
            lineStrings[i] = transformLineString((LineString) multiLineString.getGeometryN(i), targetCoordinateSystem, sourceCoordinateSystem);
        }

        return multiLineString.getFactory().createMultiLineString(lineStrings);
    }

    public static Polygon transformPolygon(Polygon polygon, String targetCoordinateSystem, String sourceCoordinateSystem) {
        if (polygon == null) {
            return null;
        }

        LinearRing exterior = (LinearRing) transformLineString(polygon.getExteriorRing(), targetCoordinateSystem, sourceCoordinateSystem);
        LinearRing[] interiors = new LinearRing[polygon.getNumInteriorRing()];

        for (int i = 0; i < interiors.length; ++i) {
            interiors[i] = (LinearRing) transformLineString(polygon.getInteriorRingN(i), targetCoordinateSystem, sourceCoordinateSystem);
        }

        Polygon transformed = polygon.getFactory().createPolygon(exterior, interiors);
        transformed.setUserData(polygon.getUserData());
        return transformed;
    }

    public static MultiPolygon transformMultiPolygon(MultiPolygon multiPolygon, String targetCoordinateSystem, String sourceCoordinateSystem) {

        if (multiPolygon == null) {
            return null;
        }

        Polygon[] polygons = new Polygon[multiPolygon.getNumGeometries()];

        for (int i = 0; i < polygons.length; ++i) {
            polygons[i] = transformPolygon((Polygon) multiPolygon.getGeometryN(i), targetCoordinateSystem, sourceCoordinateSystem);
        }

        return multiPolygon.getFactory().createMultiPolygon(polygons);

    }


    public static Geometry transform(Geometry g, String targetCoordinateSystem, String sourceCoordinateSystem) {
        if (CharSequenceUtil.isEmpty(targetCoordinateSystem)) {
            targetCoordinateSystem = GeoConstants.GCJ02;
        }
        if (CharSequenceUtil.isEmpty(sourceCoordinateSystem)) {
            sourceCoordinateSystem = GeoConstants.GCJ02;
        }


        GeometryFactory factory = g.getFactory();
        Geometry transformed = null;

        if (g instanceof Point) {
            transformed = transformPoint((Point) g, targetCoordinateSystem, sourceCoordinateSystem);
        } else {
            int i;
            if (g instanceof MultiPoint) {
                transformed = transformMultiPoint((MultiPoint) g, targetCoordinateSystem, sourceCoordinateSystem);
            } else if (g instanceof LineString) {
                transformed = transformLineString((LineString) g, targetCoordinateSystem, sourceCoordinateSystem);
            } else if (g instanceof MultiLineString) {
                transformed = transformMultiLineString((MultiLineString) g, targetCoordinateSystem, sourceCoordinateSystem);
            } else if (g instanceof Polygon) {
                transformed = transformPolygon((Polygon) g, targetCoordinateSystem, sourceCoordinateSystem);
            } else if (g instanceof MultiPolygon) {
                transformed = transformMultiPolygon((MultiPolygon) g, targetCoordinateSystem, sourceCoordinateSystem);
            } else {
                if (!(g instanceof GeometryCollection)) {
                    throw new IllegalArgumentException("Unsupported geometry type " + g.getClass());
                }

                GeometryCollection gc = (GeometryCollection) g;
                Geometry[] geoms = new Geometry[gc.getNumGeometries()];

                for (i = 0; i < geoms.length; ++i) {
                    geoms[i] = transform(
                            gc.getGeometryN(i),
                            targetCoordinateSystem,
                            sourceCoordinateSystem
                    );
                }

                transformed = factory.createGeometryCollection(geoms);
            }
        }

        (transformed).setUserData(g.getUserData());

        return transformed;
    }

    /**
     * 百度坐标系 (BD-09) 与 火星坐标系 (GCJ-02)的转换
     * 即 百度 转 谷歌、高德
     *
     * @param bd_lon longitude
     * @param bd_lat latitude
     * @return Double[lon, lat]
     */
    public static Double[] BD09ToGCJ02(Double bd_lon, Double bd_lat) {
        double x = bd_lon - 0.0065;
        double y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        Double[] arr = new Double[2];
        arr[0] = z * Math.cos(theta);
        arr[1] = z * Math.sin(theta);
        return arr;
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换
     * 即谷歌、高德 转 百度
     *
     * @param gcj_lon longitude
     * @param gcj_lat latitude
     * @return Double[lon, lat]
     */
    public static Double[] GCJ02ToBD09(Double gcj_lon, Double gcj_lat) {
        double z = Math.sqrt(gcj_lon * gcj_lon + gcj_lat * gcj_lat) + 0.00002 * Math.sin(gcj_lat * X_PI);
        double theta = Math.atan2(gcj_lat, gcj_lon) + 0.000003 * Math.cos(gcj_lon * X_PI);
        Double[] arr = new Double[2];
        arr[0] = z * Math.cos(theta) + 0.0065;
        arr[1] = z * Math.sin(theta) + 0.006;
        return arr;
    }

    public static Double[] WGS84ToBD09(Double lon, Double lat) {
        Double[] tmp = WGS84ToGCJ02(lon, lat);
        return GCJ02ToBD09(tmp[0], tmp[1]);
    }


    public static Double[] BD09ToWGS84(Double lon, Double lat) {
        Double[] tmp = BD09ToGCJ02(lon, lat);
        return GCJ02ToWGS84(tmp[0], tmp[1]);
    }

    /**
     * WGS84转GCJ02
     *
     * @param wgs_lon lon
     * @param wgs_lat lat
     * @return Double[lon, lat]
     */
    public static Double[] WGS84ToGCJ02(Double wgs_lon, Double wgs_lat) {
        if (outOfChina(wgs_lon, wgs_lat)) {
            return new Double[]{wgs_lon, wgs_lat};
        }
        double dlat = transformlat(wgs_lon - 105.0, wgs_lat - 35.0);
        double dlng = transformlng(wgs_lon - 105.0, wgs_lat - 35.0);
        double radlat = wgs_lat / 180.0 * PI;
        double magic = Math.sin(radlat);
        magic = 1 - EE * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((A * (1 - EE)) / (magic * sqrtmagic) * PI);
        dlng = (dlng * 180.0) / (A / sqrtmagic * Math.cos(radlat) * PI);
        Double[] arr = new Double[2];
        arr[0] = wgs_lon + dlng;
        arr[1] = wgs_lat + dlat;
        return arr;
    }

    /**
     * GCJ02转WGS84
     *
     * @param gcj_lon lon
     * @param gcj_lat lat
     * @return Double[lon, lat]
     */
    public static Double[] GCJ02ToWGS84(Double gcj_lon, Double gcj_lat) {
        if (outOfChina(gcj_lon, gcj_lat)) {
            return new Double[]{gcj_lon, gcj_lat};
        }
        double dlat = transformlat(gcj_lon - 105.0, gcj_lat - 35.0);
        double dlng = transformlng(gcj_lon - 105.0, gcj_lat - 35.0);
        double radlat = gcj_lat / 180.0 * PI;
        double magic = Math.sin(radlat);
        magic = 1 - EE * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((A * (1 - EE)) / (magic * sqrtmagic) * PI);
        dlng = (dlng * 180.0) / (A / sqrtmagic * Math.cos(radlat) * PI);
        double mglat = gcj_lat + dlat;
        double mglng = gcj_lon + dlng;
        return new Double[]{gcj_lon * 2 - mglng, gcj_lat * 2 - mglat};
    }

    private static Double transformlat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static Double transformlng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * outOfChina
     *
     * @param lng
     * @param lat
     * @return {boolean}
     * @描述: 判断是否在国内，不在国内则不做偏移
     */
    private static boolean outOfChina(Double lng, Double lat) {
        return (lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271);
    }


    /**
     * 转换featureCollection的坐标系
     *
     * @param input     input
     * @param inputCRS  inputCRS
     * @param outputCRS outputCRS
     * @return SimpleFeatureCollection
     */
    public static SimpleFeatureCollection transform(FeatureCollection input, String inputCRS, String outputCRS) {
        return transform(DataUtilities.collection(input), inputCRS, outputCRS);
    }


    /**
     * 转换featureCollection的坐标系
     * transform the coordinate system of the featureCollection
     *
     * @param simpleFeatureCollection     input
     * @param inputCRS  inputCRS
     * @param outputCRS outputCRS
     * @return SimpleFeatureCollection
     *
     */
    public static SimpleFeatureCollection transform(SimpleFeatureCollection simpleFeatureCollection, String inputCRS, String outputCRS) {

        if (StrUtil.isEmpty(inputCRS) || StrUtil.isEmpty(outputCRS)) {
            return simpleFeatureCollection;
        }
        List<SimpleFeature> features = new ArrayList<>();

        SimpleFeatureIterator featureIterator = simpleFeatureCollection.features();

        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();
            if (feature.getDefaultGeometryProperty() != null && feature.getDefaultGeometryProperty().getValue() != null) {
                Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
                Geometry transformedGeometry = CoordinateSystemTransformer.transform(geometry, outputCRS, inputCRS);
                feature.setAttribute(feature.getDefaultGeometryProperty().getName(), transformedGeometry);
                features.add(feature);
            }

        }
        return new ListFeatureCollection(simpleFeatureCollection.getSchema(), features);
    }

}

