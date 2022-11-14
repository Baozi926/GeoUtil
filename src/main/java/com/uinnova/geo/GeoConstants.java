package com.uinnova.geo;

/**
 * @author 蔡惠民
 */
public class GeoConstants {


    private GeoConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final int GEOJSON_COORDINATES_ACCURACY = 6; //读写geojson时的精度，精确到小数点后6位， 精度约为0.1米

    public static final String GCJ02 = "GCJ02";
    public static final String WGS84 = "WGS84";
    public static final String BD09 = "BD09";

}
