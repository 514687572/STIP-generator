# STIP MyBatis Generator

[![Maven Central](https://img.shields.io/maven-central/v/com.github.514687572/STIP-generator.svg)](https://search.maven.org/artifact/com.github.514687572/STIP-generator)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

STIP MyBatis Generator是一个强大的代码生成器插件，基于MyBatis Generator开发，提供了丰富的功能扩展和优化，帮助开发者快速生成标准化的MyBatis相关代码。

## ✨ 特性

- 🚀 **一键生成完整代码**
  - Model实体类（支持Lombok）
  - Mapper接口（内置CRUD方法）
  - XML映射文件（完整SQL映射）
  - Service接口及实现类
  - Example查询类

- 💡 **智能化增强**
  - Lombok注解支持（@Data, @Builder等）
  - 支持Example类的继承和定制
  - 支持Model类的继承和定制
  - XML文件的扩展和覆盖控制
  - Service层代码生成

## 📦 安装

在项目的`pom.xml`中添加依赖：

```xml
<dependency>
    <groupId>com.github.514687572</groupId>
    <artifactId>STIP-generator</artifactId>
    <version>3.0.0</version>
</dependency>
```

## 🚀 快速开始

### 1. 创建配置文件

在`src/main/resources`目录下创建`generator.properties`：

```properties
# 数据库配置
jdbc.driverClassName=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/your_database?useSSL=false&serverTimezone=UTC
jdbc.username=your_username
jdbc.password=your_password

# 代码生成配置
tableName=your_table_name
basePackage=com.your.package
```

### 2. 运行生成器

```bash
mvn mybatis-generator:generate
```

## 📋 配置说明

### 基础配置

| 配置项 | 说明 | 默认值 | 是否必填 |
|--------|------|--------|----------|
| jdbc.driverClassName | 数据库驱动类名 | com.mysql.cj.jdbc.Driver | 是 |
| jdbc.url | 数据库连接URL | - | 是 |
| jdbc.username | 数据库用户名 | - | 是 |
| jdbc.password | 数据库密码 | - | 是 |
| tableName | 要生成的表名 | - | 是 |
| basePackage | 基础包名 | - | 是 |

### 高级配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| removeTablePrefix | 是否去除表前缀 | false |
| useLombokPlugin | 是否使用Lombok | true |
| useSwaggerPlugin | 是否使用Swagger | true |
| generateExampleClass | 是否生成Example类 | true |
| generateBatchInsert | 是否生成批量插入 | true |

完整配置示例请参考：[generator.properties.template](src/main/resources/generator.properties.template)

## 🎯 生成的代码结构

```
src/main/java
├── model                    # 实体类
│   └── UserModel.java
├── mapper                   # Mapper接口
│   └── UserMapper.java
├── service                  # Service接口
│   └── UserService.java
├── service.impl            # Service实现类
│   └── UserServiceImpl.java
└── xml                     # MyBatis XML文件
    └── UserMapper.xml
```

## 💡 最佳实践

### 1. 实体类生成

使用Lombok简化代码：
```properties
useLombokPlugin=true
lombokAnnotations=Data,Builder,NoArgsConstructor,AllArgsConstructor,EqualsAndHashCode
```

### 2. 表名处理

去除表前缀：
```properties
removeTablePrefix=true
tablePrefix=t_
```

### 3. API文档

启用Swagger注解：
```properties
useSwaggerPlugin=true
swaggerVersion=2.0
```

## 🔨 扩展开发

1. 创建自定义插件
```java
public class CustomPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}
```

2. 在generatorConfig.xml中注册
```xml
<plugin type="com.your.package.CustomPlugin">
    <property name="customProperty" value="customValue"/>
</plugin>
```

## 📝 更新日志

### 3.0.0 (2024-01-15)
- 新增Lombok插件支持，简化实体类代码
- 优化Example类生成逻辑
- 增强XML映射文件的可定制性
- 完善Service层代码生成
- 修复已知问题

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交改动：`git commit -m 'Add some AmazingFeature'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 提交Pull Request

## 📄 开源协议

本项目采用 [Apache 2.0 协议](LICENSE)。 

### 2. ExampleClassPlugin
- 生成增强的Example查询类
- 支持继承自定义基类
- 优化查询条件构建
- 支持自定义包名和类名前缀

### 3. ModelClassPlugin
- 实体类生成增强
- 支持继承自定义基类
- 支持自定义属性和方法
- 支持包名和类名定制

### 4. ExtendXmlMapperPlugin
- XML映射文件增强
- 支持自定义命名空间
- 控制文件覆盖行为
- 支持扩展SQL定义

## 🚀 配置示例

完整的配置示例: 
