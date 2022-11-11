# GeoUtil

- keyword: geotools,geojson,shp

直接使用geotools比较难用，这里对常用的功能进行了封装

## 相比于直接使用geotools的优点
* 简单一些
* 稳定，且有比较完善的单元测试
* 改善了一些geotools的奇怪的地方 
    * geotools如果读取存在多种几何类型的featureCollection,所有的几何数据类型都会被转化成第一条数据的数据类型，对此进行了优化，
    例如按照geotools的逻辑，如果一个featureCollection存在LineString几何类型和MultiLineString几何类型，而第一条数据是LineString几何类型，所有的几何类型会被转换成LineString类型，这样的话MultiLineString类型的几何数据会出现异常
    * geotools在读取geojson时，如果缺失属性会报错，此代码中剔除了这个报错。
    
## 使用文档
以后有时间再完善，现在代码里有注释    