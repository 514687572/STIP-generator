package com.stip.mybatis.generator.query;

public interface CustomQueryBuilder {
    /**
     * 添加自定义查询条件
     * @param criteria 查询条件对象
     * @param condition 条件值
     */
    void addCustomCriteria(Object criteria, Object condition);
    
    /**
     * 构建排序条件
     * @param orderByClause 排序语句
     */
    String buildOrderBy(String orderByClause);
    
    /**
     * 构建分组条件
     * @param groupByClause 分组语句
     */
    String buildGroupBy(String groupByClause);
    
    /**
     * 构建Having条件
     * @param havingClause Having语句
     */
    String buildHaving(String havingClause);
} 