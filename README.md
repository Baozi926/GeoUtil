[![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green)](https://Baozi926.github.io/GeoUtil/javadoc/)
[![Maven Central](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fio%2Fgithub%2Fbaozi926%2Fgeo-util%2Fmaven-metadata.xml)](https://mvnrepository.com/artifact/io.github.baozi926/geo-util)
[![License](http://img.shields.io/:license-MIT-brightgreen.svg)](http://www.opensource.org/licenses/mit-license.php)

# GeoUtil

* 对geotools进行了封装，方便不懂GIS的人使用
* 经过踩坑，选择了相对稳定的27.1版本的geotool。


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
    <version>1.1.3</version>
</dependency>

```


## 相比于直接使用geotools
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
    



