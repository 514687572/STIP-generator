## STIP-generator
主要针对于Mysql批量删除、分页和多模块生成进行优化。

#1、java mybatais mapper、model和example生成工具包适用于单体架构、微服务和分布式

#2、在maven中添加依赖
```xml
<dependency>
  <groupId>com.github.514687572</groupId>
  <artifactId>STIP-generator</artifactId>
  <version>1.0.4</version>
</dependency>
```

#3、用法与用例
配置文件可参考https://github.com/514687572/STIP-generator/tree/master/STIP-generator/src/main/java/com/stip/mybatis/generator/plugin/example下的配置文件

generator.properties生成文件目录配置，java文件可以生成在不同目录中
generatorConfig.xml插件类配置

#4、在maven pom文件中添加插件
```xml
  <plugin>
	<groupId>org.mybatis.generator</groupId>
	<artifactId>mybatis-generator-maven-plugin</artifactId>
	<version>1.3.5</version>
	<dependencies>
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<version>5.1.35</version>
		</dependency>
		<dependency>
			<groupId>com.github.514687572</groupId>
			<artifactId>STIP-generator</artifactId>
			<version>1.0.3</version>
		</dependency>
	</dependencies>
	<configuration>
		<!--配置文件的路径 -->
		<configurationFile>${basedir}/src/main/resources/generatorConfig.xml</configurationFile>
		<overwrite>true</overwrite>
	</configuration>
</plugin>
```

最后右键配置项目执行maven bulid命令 mybatis-generator:generate刷新目录即完成。

如有疑问可邮件联系514687572@qq.com

https://github.com/514687572/STIP-generator.git