
# STIP-generator
 ### STIP-generator mybatais代码生成插件实现功能：
 
 1. 针对Mysql批量删除、分页、批量插入和多模块生成进行优化；
 2. 可以直接生成 mapper、model、service类和service接口类；
 3. 插件适用于单体架构、微服务和分布式；
 4. 并对数据库中java关键字进行处理；
 5. 不再针对每个表生成example，提供baseExample拼接查询条件；
 6. 针对时间字段增加@UpdateTime，@CreateTime注解自动填充时间支持目前只支持java.util.Date类型；
 
 
 [完整使用例子](https://github.com/514687572/STIP-generator-example.git)

### 添加maven依赖
```
<dependency>
  <groupId>com.github.514687572</groupId>
  <artifactId>STIP-generator</artifactId>
  <version>2.1.3</version>
</dependency>
```
### pom文件中配置插件和配置文件地址
```
generator.properties配置文件和下面的这个XML配置文件都放在resources目录中

[配置文件例子](https://github.com/514687572/STIP-generator/tree/master/STIP-generator/src/main/java/com/stip/mybatis/generator/example/generatorConfig.xml)
```

### generator.properties两种配置方式如下：

#### 简洁版配置（按照默认包名在基础包上扩展）推荐
```
jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost/lottery?useUnicode=true&characterEncoding=UTF-8
jdbc.username=root
jdbc.password=123456

#\u7B80\u5316\u914D\u7F6E\u4E00(\u63A8\u8350)
targetPackage=com.stip.net
targetProject=./src/main/java
```

#### 详细版配置（自定生成包结构和输出项目路径）

```
jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost/lottery?useUnicode=true&characterEncoding=UTF-8
jdbc.username=root
jdbc.password=123456
targetPackage=com.stip.net
targetProject=./src/main/java

modelTargetDir=./src/main/java
modelTargetPackage=com.stip.net.entity

exampleTargetDir=./src/main/java
exampleTargetPackage=com.stip.net.example

serviceTargetDir=./src/main/java
serviceTargetPackage=com.stip.net.service.impl
serviceInterfaceTargetPackage=com.stip.net.service

daoTargetDir=./src/main/java
daoTargetPackage=com.stip.net.dao

xmlTargetDir=./src/main/java
xmlTargetPackage=com.stip.net.dao
```

生成方法:
右键配置项目执行maven bulid命令 mybatis-generator:generate刷新目录即完成。
在多模块项目中可灵活修改TargetDir各个包地址来选择文件生成地址;

配置项：
需要在项目中增加扫描包com.stip.net

如有疑问可邮件联系514687572@qq.com

[插件源码地址](https://github.com/514687572/STIP-generator)



开发计划
1、简化使用配置，目标即插即用
2、让代码生成更灵活
