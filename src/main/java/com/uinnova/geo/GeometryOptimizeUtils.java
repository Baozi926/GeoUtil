package com.uinnova.geo;

import com.uinnova.geo.exception.GeoException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.*;

/**
 * @author 蔡惠民
 * @date 2021/5/7 10:42
 */

public class GeometryOptimizeUtils {

    private GeometryOptimizeUtils() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * 线融合
     */
    public static List<Geometry> mergeLines(List<Geometry> geometryList) {
        LineMerger lineMerger = new LineMerger();
        lineMerger.add(geometryList);
        Collection<Geometry> mergerLineStrings = lineMerger.getMergedLineStrings();
        return Arrays.asList(mergerLineStrings.toArray(new Geometry[]{}));
    }

    /**
     * 融合
     * 例如，输入是多个polygon，输出是一个MultiPolygon
     */
    public static Geometry union(Collection<Geometry> geometries) {
        try {
            return UnaryUnionOp.union(geometries);
        } catch (Exception e) {
            //如果融合不了，就直接合并成一个多面
            return GeometryOptimizeUtils.toMulti(geometries);
        }

    }

    /**
     * 转换为多[点/线/面]
     */
    public static Geometry toMulti(Collection<Geometry> geometries) {

        if (geometries == null) {
            return null;
        }

        if (geometries.isEmpty()) {
            return null;
        }

        GeometryFactory gf = new GeometryFactory();

        Geometry firstGeometry = geometries.iterator().next();

        if (firstGeometry == null) {
            return null;
        }

        Iterator<Geometry> iterator = geometries.iterator();


        List<Geometry> singleGeometryList = new ArrayList<>();

        while (iterator.hasNext()) {
            Geometry target = iterator.next();
            if (target != null) {
                for (int i = 0; i < target.getNumGeometries(); i++) {
                    singleGeometryList.add(target.getGeometryN(i));
                }
            }

        }

        return gf.buildGeometry(singleGeometryList);


//        if (CharSequenceUtil.equals("MultiPolygon", firstGeometry.getGeometryType())) {
//            return gf.createMultiPolygon(singleGeometryList.toArray(new Polygon[]{}));
//        } else if (CharSequenceUtil.equals("Polygon", firstGeometry.getGeometryType())) {
//            return gf.createMultiPolygon(singleGeometryList.toArray(new Polygon[]{}));
//        } else if (CharSequenceUtil.equals("MultiLineString", firstGeometry.getGeometryType())) {
//            return gf.createMultiLineString(singleGeometryList.toArray(new LineString[]{}));
//        } else if (CharSequenceUtil.equals("LineString", firstGeometry.getGeometryType())) {
//            return ;
//        } else if (CharSequenceUtil.equals("MultiPoint", firstGeometry.getGeometryType())) {
//            return gf.createMultiPoint(singleGeometryList.toArray(new Point[]{}));
//        } else if (CharSequenceUtil.equals("Point", firstGeometry.getGeometryType())) {
//            return gf.createMultiPoint(singleGeometryList.toArray(new Point[]{}));
//        } else {
//            throw new GeoException("invalid geometryType");
//        }


    }

    /**
     *
     **/
    public static Geometry validate(Geometry geom) throws GeoException {
        if (geom instanceof Polygon || geom instanceof MultiPolygon) {
            if (geom.isValid()) {
                geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
                return geom; // If the polygon is valid just return it
            }

            try {
                return geom.buffer(0);
            } catch (Exception e) {
                throw new GeoException("can not validate geometry !");
            }

        } else {
            return geom; // In my case, I only care about polygon / multipolygon geometries
        }
    }


}
