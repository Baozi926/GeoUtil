[![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green)](https://Baozi926.github.io/GeoUtil/javadoc/)

# GeoUtil

* 对geotools进行了封装，比直接用geotools要更好用,不用担心版本兼容问题
* 用的是27.1 版本的geotool，比起老一些的版本会少很多问题。

## 相比于直接使用geotools的优点
* 使用起来简单一些，例如如果你只需要处理geojson数据，使用geojsonUtil即可。
* 包含了WGS84,GCJ02,BD09的坐标转换
* 稳定，且有比较完善的单元测试
* geotool对有些模块不再维护了，对于已经存在的不合理的地方，只有通过改源码解决，我们通过覆盖源码的方式对源码进行了修改，修复的问题如下：
    * geotools如果读取存在多种几何类型的featureCollection,所有的几何数据类型都会被转化成第一条数据的数据类型，会造成部分几何数据畸形，对此进行了优化，
    例如按照geotools的逻辑，如果一个featureCollection存在LineString几何类型和MultiLineString几何类型，而第一条数据是LineString几何类型，所有的几何类型会被转换成LineString类型，这样的话MultiLineString类型的几何数据会出现异常
    * geotools在读取geojson时，如果properties缺失属性会报错，此代码中剔除了这个报错。
    * geotool无法读取有嵌套数组的属性，本工程增加了读取这种数据的能力
    ```json
      {      
        "properties": {
               "prop0": [
                 [
                   [
                     1
                   ],
                   [
                     2,
                     [
                       3,
                       4
                     ]
                   ]
                 ]
               ]
        }    
      }   
  
  
  
  
  
  
  
    ```
    

## API文档
[https://Baozi926.github.io/GeoUtil/javadoc/](https://Baozi926.github.io/GeoUtil/javadoc/)
    
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
SimpleFeatureCollection simpleFeatureCollection = ShpUtil.parseShp(input);

```



# maven地址

``` 

<dependency>
    <groupId>io.github.baozi926</groupId>
    <artifactId>geo-util</artifactId>
    <version>1.1.1</version>
</dependency>

```



