package com.uinnova.geo.fastjson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.uinnova.geo.GeoJsonUtil;
import com.uinnova.geo.exception.GeoException;
import org.geotools.feature.FeatureCollection;

import java.io.IOException;

/**
 * @author 蔡惠民
 * @date 2021/6/29 13:53
 *
 */
public class FeatureCollectionDeserializer extends JsonDeserializer<FeatureCollection> {

    @Override
    public FeatureCollection deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        try {
            return GeoJsonUtil.fromJson(jsonParser.readValueAsTree().toString());
        } catch (GeoException e) {
            return null;
        }
    }
}
