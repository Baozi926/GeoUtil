package com.uinnova.geo;

import cn.hutool.core.util.BooleanUtil;
import com.uinnova.geo.exception.GeoException;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author 蔡惠民
 * @date 2022/10/28 11:24
 * 几何相关的辅助操作
 */
public class GeoUtil {

    private static final Logger LOGGER = Logging.getLogger(GeoUtil.class);

    private GeoUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将featureType转换成 SimpleFeatureType
     * @param featureType
     *
     * */
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
     * 拷贝 SimpleFeatureCollection
     * @author caihuimin
     */
    public static SimpleFeatureCollection copy(SimpleFeatureCollection simpleFeatureCollection) {


        List<SimpleFeature> features = new ArrayList<>();

        SimpleFeatureIterator featureIterator = simpleFeatureCollection.features();

        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();
            if (feature.getDefaultGeometryProperty() != null && feature.getDefaultGeometryProperty().getValue() != null) {
                Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
                feature.setAttribute(feature.getDefaultGeometryProperty().getName(), geometry.copy());
                features.add(feature);
            }

        }
        return new ListFeatureCollection(simpleFeatureCollection.getSchema(), features);
    }


    /**
     * 去除空值
     *
     * @author caihuimin
     */
    public static SimpleFeatureCollection removeNullGeometryItem(SimpleFeatureCollection featureCollection) {

        List<SimpleFeature> features = new ArrayList<>();

        SimpleFeatureIterator featureIterator = featureCollection.features();

        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();
            if (feature.getDefaultGeometryProperty() != null && feature.getDefaultGeometryProperty().getValue() != null) {
                Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
                if ((geometry.getCoordinate() != null && geometry.getCoordinates() != null)) {
                    features.add(feature);
                } else {
                    LOGGER.info("存在空geometry数据");
                }
            } else {
                LOGGER.info("存在空geometry数据");
            }

        }

        featureIterator.close();

        return new ListFeatureCollection(featureCollection.getSchema(), features);


    }


    /**
     * featureCollection
     *
     * @param featureCollection geojson的字符串
     * @author caihuimin
     */
    public static FeatureCollection validate(FeatureCollection featureCollection) throws GeoException {

        if (featureCollection == null || featureCollection.size() == 0) {
            return featureCollection;
        }

        if (featureCollection.getSchema() == null) {
            return featureCollection;
        }

        ListFeatureCollection validateFeatures = new ListFeatureCollection((SimpleFeatureType) featureCollection.getSchema());
        FeatureIterator<SimpleFeature> features = featureCollection.features();
        while (features.hasNext()) {
            SimpleFeature feature = features.next();
            feature.setDefaultGeometry(GeometryOptimizeUtils.validate((Geometry) feature.getDefaultGeometry()));
            validateFeatures.add(feature);

        }

        return validateFeatures;
    }

    /**
     * 判断几何对象是不是合格的
     *
     * @param geometry 几何对象
     * @author caihuimin
     */
    public Boolean isValid(Geometry geometry) {
        IsValidOp isValidOp = new IsValidOp(geometry);
        return BooleanUtil.isTrue(isValidOp.isValid());
    }

}
