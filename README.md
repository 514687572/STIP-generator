﻿
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
  <version>2.2.0</version>
</dependency>
```

### generator.properties配置方式如下：

#### 简洁版配置（按照默认包名在基础包上扩展）推荐
```
jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost/lottery?useUnicode=true&characterEncoding=UTF-8
jdbc.username=root
jdbc.password=123456

#\u7B80\u5316\u914D\u7F6E\u4E00(\u63A8\u8350)
targetPackage=com.stip.net
targetProject=./src/main/java
tableName=test
```
### 生成方法:
```
    public static void main(String[] args) throws Exception {
        StipGenerator generator = new StipGenerator();
        generator.generator();
    }

配置项：
需要在项目中增加扫描包com.stip.net

如有疑问可邮件联系514687572@qq.com

[插件源码地址](https://github.com/514687572/STIP-generator)

```

### 开发计划 
```
1、简化使用配置，终极目标即插即用
2、使用插件生成代码更加灵活
3、整体降低代码侵入性
4、降低对于使用者的约束，开发更灵活
5、降低代码侵入性
6、优化代码目录结构
7、性能优化
8、减少配置项，减少生成文件数量
```
