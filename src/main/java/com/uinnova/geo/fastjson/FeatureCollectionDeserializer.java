package com.uinnova.geo.fastjson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.uinnova.geo.json.JsonGeomTranslator;
import org.geotools.feature.FeatureCollection;

import java.io.IOException;

/**
 * @author 蔡惠民
 * @date 2021/6/29 13:53
 */
public class FeatureCollectionDeserializer extends JsonDeserializer<FeatureCollection> {

    @Override
    public FeatureCollection deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        return JsonGeomTranslator.jsonString2Features(jsonParser.readValueAsTree().toString(), 7);
    }
}
