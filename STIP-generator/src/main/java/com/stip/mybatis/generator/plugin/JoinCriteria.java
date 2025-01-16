package com.stip.mybatis.generator.plugin;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

public class JoinCriteria {
    private String joinTable;
    private String joinCondition;
    private String joinType;
    
    public JoinCriteria(String joinTable, String joinCondition) {
        this(joinTable, joinCondition, "INNER JOIN");
    }
    
    public JoinCriteria(String joinTable, String joinCondition, String joinType) {
        this.joinTable = joinTable;
        this.joinCondition = joinCondition;
        this.joinType = joinType;
    }
    
    public String toSql() {
        return String.format("%s %s ON %s", joinType, joinTable, joinCondition);
    }
    
    public String getJoinTable() {
        return joinTable;
    }
    
    public String getJoinCondition() {
        return joinCondition;
    }
    
    public String getJoinType() {
        return joinType;
    }
}

