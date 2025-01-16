package com.stip.mybatis.generator.plugin;

import com.stip.mybatis.generator.query.CustomQueryBuilder;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * all Example's base class and pager methord impl
 *
 * @author chenjunan
 */
public abstract class BaseExample extends BaseModelExample {
    protected List<JoinCriteria> joins = new ArrayList<>();
    protected CustomQueryBuilder queryBuilder;
    
    // 添加查询构建器支持
    public void setQueryBuilder(CustomQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }
    
    // 分组查询
    public void groupBy(String groupByClause) {
        if (queryBuilder != null) {
            this.groupByClause = queryBuilder.buildGroupBy(groupByClause);
        }
    }
    
    // Having查询
    public void having(String havingClause) {
        if (queryBuilder != null) {
            this.havingClause = queryBuilder.buildHaving(havingClause);
        }
    }
    
    // 复杂条件查询
    public void addComplexCriteria(Object condition) {
        if (queryBuilder != null) {
            Criteria criteria = createCriteriaInternal();
            queryBuilder.addCustomCriteria(criteria, condition);
            oredCriteria.add(criteria);
        }
    }
    
    // 联表查询支持
    public BaseExample leftJoin(String tableName, String condition) {
        JoinCriteria join = new JoinCriteria(tableName, condition);
        join.joinType = "LEFT JOIN";
        joins.add(join);
        return this;
    }
    
    public BaseExample rightJoin(String tableName, String condition) {
        JoinCriteria join = new JoinCriteria(tableName, condition);
        join.joinType = "RIGHT JOIN";
        joins.add(join);
        return this;
    }
    
    public BaseExample innerJoin(String tableName, String condition) {
        JoinCriteria join = new JoinCriteria(tableName, condition);
        join.joinType = "INNER JOIN";
        joins.add(join);
        return this;
    }
    
    public BaseExample select(String tableName, String... columns) {
        joins.stream()
            .filter(join -> join.tableName.equals(tableName))
            .findFirst()
            .ifPresent(join -> {
                for (String column : columns) {
                    join.addColumn(column);
                }
            });
        return this;
    }
    
    public List<JoinCriteria> getJoins() {
        return joins;
    }
    
    // JoinCriteria 内部类
    protected static class JoinCriteria {
        private String joinType;
        private String tableName;
        private List<String> columns = new ArrayList<>();
        private List<String> conditions = new ArrayList<>();
        private String alias;
        private boolean subQuery;
        private String subQuerySql;
        
        public JoinCriteria(String tableName, String condition) {
            this.tableName = tableName;
            this.conditions.add(condition);
        }
        
        public JoinCriteria(String tableName, String condition, String joinType) {
            this(tableName, condition);
            this.joinType = joinType;
        }
        
        public void addColumn(String column) {
            columns.add(column);
        }
        
        public String toSql() {
            StringBuilder sql = new StringBuilder();
            sql.append(joinType).append(" ");
            
            if (subQuery) {
                sql.append("(").append(subQuerySql).append(") ");
                sql.append(alias);
            } else {
                sql.append(tableName);
                if (StringUtility.stringHasValue(alias)) {
                    sql.append(" ").append(alias);
                }
            }
            
            sql.append(" ON ");
            if (conditions.size() == 1) {
                sql.append(conditions.get(0));
            } else {
                sql.append(String.join(" AND ", conditions));
            }
            
            return sql.toString();
        }
    }
}
