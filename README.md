# STIP-generator
 ### STIP-generator mybatais代码生成插件实现功能：
 
 1. 针对Mysql批量删除、分页、批量插入和多模块生成进行优化;
 2. 可以直接生成 mapper、model、example、service类和service接口类;
 3. 插件适用于单体架构、微服务和分布式，并对数据库中java关键字进行处理;
 
 [完整使用例子](https://github.com/514687572/STIP-generator-example.git)

### 添加maven依赖
```
<dependency>
  <groupId>com.github.514687572</groupId>
  <artifactId>STIP-generator</artifactId>
  <version>2.0.1</version>
</dependency>
```
### pom文件中配置插件和配置文件地址
```
generator.properties配置文件和下面的这个XML配置文件都放在resources目录中

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN" "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>
	<properties resource="generator.properties" />

	<context id="mysqlMap" targetRuntime="MyBatis3">

		<plugin type="org.mybatis.generator.plugins.SerializablePlugin" />
		<plugin type="org.mybatis.generator.plugins.ToStringPlugin" />
		<plugin type="com.stip.mybatis.generator.plugin.EqualsHashCodePlugin" />
		<plugin type="com.stip.mybatis.generator.plugin.MysqlPaginationPlugin"/>
		<plugin type="com.stip.mybatis.generator.plugin.BooleanColumnPlugin" />

		<plugin type="com.stip.mybatis.generator.plugin.ExampleClassPlugin">
			<property name="targetProject" value="${targetProject}" />
			<property name="targetPackage" value="${targetPackage}" />
			<property name="exampleTargetPackage" value="${exampleTargetPackage}" />
			<property name="xmlTargetPackage" value="${xmlTargetPackage}" />
			<property name="modelTargetPackage" value="${modelTargetPackage}" />
			<property name="exampleTargetProject" value="${exampleTargetDir}" />
		</plugin>

		<plugin type="com.stip.mybatis.generator.plugin.ExtendXmlMapperPlugin">
			<property name="targetProject" value="${targetProject}" />
			<property name="targetPackage" value="${targetPackage}" />
			<property name="exampleTargetPackage" value="${exampleTargetPackage}" />
			<property name="xmlTargetPackage" value="${xmlTargetPackage}" />
			<property name="modelTargetPackage" value="${modelTargetPackage}" />
		</plugin>

		<plugin type="com.stip.mybatis.generator.plugin.ModelClassPlugin">
			<property name="targetProject" value="${targetProject}" />
			<property name="targetPackage" value="${targetPackage}" />
			<property name="modelTargetPackage" value="${modelTargetPackage}" />
			<property name="modelTargetProject" value="${modelTargetDir}" />
		</plugin>

		<plugin type="com.stip.mybatis.generator.plugin.MapperPlugin">
			<property name="targetProject" value="${targetProject}" />
			<property name="targetPackage" value="${targetPackage}" />
			<property name="daoTargetDir" value="${daoTargetDir}" />
			<property name="daoTargetPackage" value="${daoTargetPackage}" />
			<property name="modelTargetPackage" value="${modelTargetPackage}" />
		</plugin>
		
		<plugin type="com.stip.mybatis.generator.plugin.ServiceInterfacePlugin">
			<property name="targetProject" value="${targetProject}" />
			<property name="targetPackage" value="${targetPackage}" />
			<property name="serviceTargetDir" value="${serviceTargetDir}" />
			<property name="serviceInterfaceTargetPackage" value="${serviceInterfaceTargetPackage}" />
			<property name="modelTargetPackage" value="${modelTargetPackage}" />
		</plugin>
		
		<plugin type="com.stip.mybatis.generator.plugin.ServicePlugin">
			<property name="targetProject" value="${targetProject}" />
			<property name="targetPackage" value="${targetPackage}" />
			<property name="serviceTargetDir" value="${serviceTargetDir}" />
			<property name="serviceTargetPackage" value="${serviceTargetPackage}" />
			<property name="modelTargetPackage" value="${modelTargetPackage}" />
			<property name="serviceInterfaceTargetPackage" value="${serviceInterfaceTargetPackage}" />
		</plugin>

		<commentGenerator>
			<property name="suppressAllComments" value="true" />
		</commentGenerator>

		<jdbcConnection driverClass="${jdbc.driverClassName}" connectionURL="${jdbc.url}" userId="${jdbc.username}" password="${jdbc.password}" />
		
		<javaTypeResolver>
			<property name="forceBigDecimals" value="false" />
		</javaTypeResolver>
		
		<!--domain 代码 生成路径 -->
		<javaModelGenerator targetPackage="${targetPackage}" targetProject="${targetProject}"> 
			<property name="trimStrings" value="true" />
		</javaModelGenerator>
		
		<!--mapper 代码 生成路径 -->
		<sqlMapGenerator targetPackage="${targetPackage}" targetProject="${targetProject}">
			<property name="enableSubPackages" value="true" />
		</sqlMapGenerator>
		<table tableName="test" alias="test" mapperName="TestDao" domainObjectName="Test"></table>
	</context>
</generatorConfiguration>
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

生成方法如下
右键配置项目执行maven bulid命令 mybatis-generator:generate刷新目录即完成。

在多模块项目中可灵活修改TargetDir各个包地址来选择文件生成地址；

如有疑问可邮件联系514687572@qq.com

[插件源码地址](https://github.com/514687572/STIP-generator)

