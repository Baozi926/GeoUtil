package com.uinnova.geo.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uinnova.geo.fastjson.GeometryAsGeoJSONSerializer;
import com.uinnova.geo.fastjson.WGS84GeometryDeserializer;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;
//import org.locationtech.spatial4j.io.jackson.GeometryAsGeoJSONSerializer;

import java.util.List;

/**
 * @Description:
 * @author: 李淼
 * @create: 2022/2/24 15:11
 **/

@Data
public class CustomAreaParam {

	private String coordinateSystem;
	private String taskId;
	private Long projectId;
	@JsonDeserialize(contentUsing = WGS84GeometryDeserializer.class)
	@JsonSerialize(contentUsing = GeometryAsGeoJSONSerializer.class)
	private List<Geometry> geometries;
	private String userId;
	private String timeStamp;
	private String openId;
	private Long templateCode;
	private List<String> elements;
	private String dataPath;
	private String type;
	private List<Double>sizes;
	private String names;
}
