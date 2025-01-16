# STIP MyBatis Generator Plugin

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/stip/mybatis-generator-plugin.svg?branch=master)](https://travis-ci.org/stip/mybatis-generator-plugin)

一个强大的 MyBatis Generator 增强插件，提供了丰富的扩展功能和灵活的查询支持。

## 特性

- 🚀 强大的查询功能
  - JOIN 查询支持 (LEFT/RIGHT/INNER)
  - 动态条件构建
  - 分组和 Having 子句
  - 子查询支持
  
- 📦 代码生成增强
  - 继承体系完善
  - 自动清理冗余代码
  - XML 映射文件格式化
  - 缓存配置支持

- 🎨 优雅的 API 设计
  - 链式调用风格
  - 类型安全的查询
  - 灵活的条件组装

## 快速开始

### Maven 依赖

xml
<dependency>
    <groupId>com.github.514687572</groupId>
    <artifactId>STIP-generator</artifactId>
    <version>3.0.0</version>
</dependency>

### 配置插件

在 generatorConfig.xml 中添加:

### 基础用法
```java
// 创建查询对象
UserExample example = new UserExample();
// 添加查询条件
example.createCriteria()
.andNameLike("%张%")
.andAgeBetween(20, 30);
// 执行查询
List<User> users = userMapper.selectByExample(example);
```
### JOIN 查询

```java
UserExample example = new UserExample();
example.leftJoin("department", "department.id = user.department_id")
      .select("department", "name as deptName")
      .createCriteria()
      .andDeletedEqualTo(false);
```

### 分组统计

```java
UserExample example = new UserExample();
example.groupBy("department_id")
      .having("count(*) > 5");
```

## 插件列表

### ExampleClassPlugin
- Example 类生成增强
- 继承关系处理
- 查询条件构建

### ExtendXmlMapperPlugin  
- XML 映射文件增强
- 缓存配置支持
- 结果映射生成

### ModelClassPlugin
- 实体类生成增强
- 字段注释保留
- 类型转换优化

## 进阶使用

### 自定义查询构建器

```java
public class MyQueryBuilder implements CustomQueryBuilder {
    @Override
    public String buildGroupBy(String groupByClause) {
        return "GROUP BY " + groupByClause;
    }
    
    @Override
    public String buildHaving(String havingClause) {
        return "HAVING " + havingClause;
    }
}

// 使用自定义构建器
example.setQueryBuilder(new MyQueryBuilder());
```

## 最佳实践

1. 优先使用基础查询方法
2. 合理使用 JOIN，避免过多表关联
3. 适当使用查询构建器组装复杂条件
4. 保持生成代码的简洁性

## 版本要求

- JDK 1.8+
- MyBatis Generator 1.3.7+
- MyBatis 3.4.0+

## 贡献指南

欢迎提交 Pull Request 和 Issue。

## 许可证

[Apache License 2.0](LICENSE)