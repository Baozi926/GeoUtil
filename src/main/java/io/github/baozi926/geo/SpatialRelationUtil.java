package io.github.baozi926.geo;

import io.github.baozi926.geo.exception.GeoException;
import org.locationtech.jts.geom.Geometry;

/**
 * 空间关系相关的计算
 *
 * @author 蔡惠民
 * @date 2022/12/1 17:45
 */
public class SpatialRelationUtil {

    private SpatialRelationUtil() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * 制作缓冲区
     *
     * @param geometry geometry
     * @param distance distance
     */
    public static Geometry buffer(Geometry geometry, Double distance) throws GeoException {

        if (distance == null) {
            throw new GeoException("distance should not be null");
        }

        return geometry.buffer(distance);
    }

    /**
     * 判断是否相交
     *
     * @param a geometry a
     * @param b geometry b
     **/
    public static Boolean intersect(Geometry a, Geometry b) throws GeoException {
        if (a == null || b == null) {
            throw new GeoException("intersect geometry should not be null");
        }

        return a.intersects(b);
    }

    /**
     * 计算相交的部分
     *
     * @param a geometry a
     * @param b geometry b
     **/
    public static Geometry intersection(Geometry a, Geometry b) throws GeoException {
        if (a == null || b == null) {
            throw new GeoException("intersect geometry should not be null");
        }
        return a.intersection(b);
    }

}
