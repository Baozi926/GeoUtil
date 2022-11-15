package io.github.baozi926.geo;

import cn.hutool.core.util.BooleanUtil;
import io.github.baozi926.geo.exception.GeoException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author 蔡惠民
 */

public class GeometryUtil {

    private GeometryUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将featureType转换成 SimpleFeatureType
     *
     * @param featureType  featureType
     * @return SimpleFeatureType
     */
    public static SimpleFeatureType featureTypeToSimpleFeatureType(FeatureType featureType) {

        SimpleFeatureTypeBuilder ftBuilder = new SimpleFeatureTypeBuilder();

        //根据featureType的每个字段定义重新给simpleFeatureType
        for (PropertyDescriptor descriptor : featureType.getDescriptors()) {
            if (descriptor.getName() != null) {
                ftBuilder.add(descriptor.getName().toString(), descriptor.getType().getBinding());
            }
        }
        //scheme的名称，无用但必要
        ftBuilder.setName(featureType.getName());
        //坐标系
        ftBuilder.setCRS(featureType.getCoordinateReferenceSystem());
        //设置默认的geometry字段
        if (featureType.getGeometryDescriptor() != null && featureType.getGeometryDescriptor().getName() != null) {
            ftBuilder.setDefaultGeometry(featureType.getGeometryDescriptor().getName().toString());
        }
        return ftBuilder.buildFeatureType();
    }


    /**
     * 判断几何对象是不是合格的
     *
     * @param geometry 几何对象
     * @return is valid
     *
     */
    public Boolean isValid(Geometry geometry) {
        IsValidOp isValidOp = new IsValidOp(geometry);
        return BooleanUtil.isTrue(isValidOp.isValid());
    }

    /**
     * 融合
     * 例如，输入是多个polygon，输出是一个MultiPolygon
     * @param  geometries 几何对象
     * @return geometry
     */
    public static Geometry union(Collection<Geometry> geometries) {
        try {
            return UnaryUnionOp.union(geometries);
        } catch (Exception e) {
            //如果融合不了，就直接合并成一个多面
            return GeometryUtil.toMulti(geometries);
        }

    }

    /**
     * 转换为多[点/线/面]
     *
     * @param geometries geometries
     * @return geometry
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
    }

    /**
     * fix geometry
     * 修复几何对象
     *
     * @param geom geometry
     * @return geometry
     * @throws GeoException see in msg
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
