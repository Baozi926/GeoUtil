## 部署代码
```

//部署snapshot
mvn clean javadoc:jar deploy -P release

//生成javadoc
mvn clean javadoc:jar

//部署到release
mvn release:clean release:prepare
mvn release:perform

```
