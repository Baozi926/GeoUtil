# GeoUtil

* 对geotools进行了封装，比直接使用geotools要更好用
* 根据数据类型进行了分类，例如处理geojson数据，在GeoJsonUtil中找方法即可

## 相比于直接使用geotools的优点
* 使用起来简单一些，例如如果你只需要处理geojson数据，使用geojsonUtil即可。
* 包含了WGS84,GCJ02,BD09的坐标转换
* 稳定，且有比较完善的单元测试
* 改善了一些geotools的奇怪的地方 
    * geotools如果读取存在多种几何类型的featureCollection,所有的几何数据类型都会被转化成第一条数据的数据类型，对此进行了优化，
    例如按照geotools的逻辑，如果一个featureCollection存在LineString几何类型和MultiLineString几何类型，而第一条数据是LineString几何类型，所有的几何类型会被转换成LineString类型，这样的话MultiLineString类型的几何数据会出现异常
    * geotools在读取geojson时，如果缺失属性会报错，此代码中剔除了这个报错。
    
## 使用示例

### geojson

```java

//geojson
File input = new File("." + File.separator + "data" + File.separator + "MergeBuilding.geojson");
String geojsonStr = FileUtil.readString(input, CharsetUtil.UTF_8);
//读取geojson
SimpleFeatureCollection simpleFeatureCollection = GeoJsonUtil.fromJsonAsSimpleFeatureCollection(geojsonStr, 7);

//根据height合并
simpleFeatureCollection = FeatureCollectionUtil.mergeByField(simpleFeatureCollection, "height", "MultiLineString", "String");

//删除没有geometry的数据项
simpleFeatureCollection = FeatureCollectionUtil.removeNullGeometryItem(simpleFeatureCollection)

//将simpleFeatureCollection转化为geojson字符串
String geojsonString = GeoJsonUtil.toString(simpleFeatureCollection)



```

### shpfile

```java

//shpfile
File input = new File("." + File.separator + "data" + File.separator + "杭州市城市建筑" + File.separator + "12杭州市建筑.shp");
//将shpfile读取成geojson string
String geojsonStr = ShpUtil.parseShpAsString(input);
//将shpfile读取成SimpleFeatureCollection,便于进行其他处理
String geojsonStr = ShpUtil.parseShpAs(input);

```

# maven地址

``` 

<dependency>
    <groupId>io.github.baozi926</groupId>
    <artifactId>geo-util</artifactId>
    <version>1.1</version>
</dependency>

```



