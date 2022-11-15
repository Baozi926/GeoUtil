package io.github.baozi926.geo.fastjson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.baozi926.geo.GeoConstants;
import io.github.baozi926.geo.GeoJsonUtil;
import io.github.baozi926.geo.exception.GeoException;
import org.geotools.feature.FeatureCollection;
import org.geotools.util.logging.Logging;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 蔡惠民
 * 用于序列化 FeatureCollection
 */
public class FeatureCollectionSerializer extends JsonSerializer<FeatureCollection> {

    private static final Logger LOGGER = Logging.getLogger(FeatureCollectionSerializer.class);

    @Override
    public void serialize(FeatureCollection featureCollection, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        try {
            jsonGenerator.writeRawValue(GeoJsonUtil.toString(featureCollection, GeoConstants.GEOJSON_COORDINATES_ACCURACY));
        } catch (GeoException e) {
            LOGGER.log(Level.WARNING, "FeatureCollectionSerializer parse error", e);
        }
    }

}
