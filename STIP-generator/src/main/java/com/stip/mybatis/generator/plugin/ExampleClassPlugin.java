package com.stip.mybatis.generator.plugin;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 定制部分mybatis的插件，主要实现以下功能
 * <ol>
 * <li>生成Example的文件</li>
 * </ol>
 * <p>
 * 使用方法配置与在generatorConfig.xml中其中
 * baseModelNamePrefix 为新生成的类文件的前置关键字
 * baseModelPackage 为生成新的类文件的包名
 *
 * @author chenjunan
 **/
public class ExampleClassPlugin extends PluginAdapter {
    public static Log logger = LogFactory.getLog(ExampleClassPlugin.class);
    public final static String DEFAULT_BASE_MODEL_PACKAGE = "";
    public final static String DEFAULT_BASE_MODEL_NAME_PREFIX = "";

    /**
     * Example的基类
     */
    private String baseExampleSuperClass = "BaseModelExample";
    private String baseExampleSuperClassName = "com.stip.mybatis.generator.plugin.BaseModelExample";

    /**
     * Criteria的基类
     */
    private String baseCriteriaSuperClass = "BaseCriteria";
    private String baseCriteriaSuperClassName = "com.stip.mybatis.generator.plugin.BaseCriteria";

    /**
     * Model类的前缀名称
     */
    private String baseModelNamePrefix;
    /**
     * Model类的前缀名称
     */
    private String exampleTargetPackage;

    private String exampleTargetProject;

    private String baseExamplePackageName = ".example";

    /**
     * Model类文件包名
     */
    private String fullModelPackage;

    /**
     * 利用java反射获取isMergeable参数，并修改
     */
    private java.lang.reflect.Field isMergeableFid = null;

    /**
     * 两个参数用于做数据中转，否则xml文件里会用Base类，这里是为了让xml文件用标准的Model类
     */
    private String modelClassName;
    private String exampleClassName;

    /**
     * 添加新的属性
     */
    private String queryBuilderClass;

    public ExampleClassPlugin() {
        try {
            if (isMergeableFid == null) {
                isMergeableFid = GeneratedXmlFile.class.getDeclaredField("isMergeable");
                isMergeableFid.setAccessible(true);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        // 初始化两参数为空
        modelClassName = null;

        FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        modelClassName = modelJavaType.getShortName();

        exampleClassName = exampleTargetPackage + "." + modelClassName + "Example";
        introspectedTable.setExampleType(exampleClassName);
    }

    /*
     * 检查xml参数是否正确
     *
     * @see org.mybatis.generator.api.Plugin#validate(java.util.List)
     */
    public boolean validate(List<String> warnings) {
        // 获取自定义查询构建器配置
        queryBuilderClass = properties.getProperty("queryBuilderClass");
        return true;
    }

    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 添加联合查询支持
        addJoinSupport(topLevelClass);
        
        // 保留原有的查询构建器支持
        if (StringUtility.stringHasValue(queryBuilderClass)) {
            topLevelClass.addImportedType(queryBuilderClass);
            addQueryBuilderSupport(topLevelClass);
        }
        return true;
    }

    private void addQueryBuilderSupport(TopLevelClass topLevelClass) {
        // 添加查询构建器字段
        Field queryBuilderField = new Field("queryBuilder", new FullyQualifiedJavaType("CustomQueryBuilder"));
        queryBuilderField.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(queryBuilderField);

        // 添加设置查询构建器的方法
        Method setQueryBuilder = new Method("setQueryBuilder");
        setQueryBuilder.setVisibility(JavaVisibility.PUBLIC);
        setQueryBuilder.addParameter(new Parameter(new FullyQualifiedJavaType("CustomQueryBuilder"), "queryBuilder"));
        setQueryBuilder.addBodyLine("this.queryBuilder = queryBuilder;");
        topLevelClass.addMethod(setQueryBuilder);

        // 添加新的查询方法
        addCustomQueryMethods(topLevelClass);
    }

    private void addCustomQueryMethods(TopLevelClass topLevelClass) {
        // 添加分组查询方法
        Method groupBy = new Method("groupBy");
        groupBy.setVisibility(JavaVisibility.PUBLIC);
        groupBy.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "groupByClause"));
        groupBy.addBodyLine("if (queryBuilder != null) {");
        groupBy.addBodyLine("    this.groupByClause = queryBuilder.buildGroupBy(groupByClause);");
        groupBy.addBodyLine("}");
        topLevelClass.addMethod(groupBy);

        // 添加Having查询方法
        Method having = new Method("having");
        having.setVisibility(JavaVisibility.PUBLIC);
        having.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "havingClause"));
        having.addBodyLine("if (queryBuilder != null) {");
        having.addBodyLine("    this.havingClause = queryBuilder.buildHaving(havingClause);");
        having.addBodyLine("}");
        topLevelClass.addMethod(having);

        // 添加复杂条件查询方法
        Method addComplexCriteria = new Method("addComplexCriteria");
        addComplexCriteria.setVisibility(JavaVisibility.PUBLIC);
        addComplexCriteria.addParameter(new Parameter(new FullyQualifiedJavaType("Object"), "condition"));
        addComplexCriteria.addBodyLine("if (queryBuilder != null) {");
        addComplexCriteria.addBodyLine("    Criteria criteria = createCriteriaInternal();");
        addComplexCriteria.addBodyLine("    queryBuilder.addCustomCriteria(criteria, condition);");
        addComplexCriteria.addBodyLine("    oredCriteria.add(criteria);");
        addComplexCriteria.addBodyLine("}");
        topLevelClass.addMethod(addComplexCriteria);
    }

    /**
     * 清理Example的多余属性与方法，已经迁移到父类了
     *
     * @param topLevelClass
     */
    private static void clearExampleCLass(TopLevelClass topLevelClass) {
        logger.debug("开始清理Example的TopLevelCLass多余属性");

        HashSet<Field> removingFields = new HashSet<Field>();

        List<Field> fields = topLevelClass.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if ("orderByClause".equals(fieldName) || "distinct".equals(fieldName)) {// 将要删除的变量
                System.out.println("removing field:" + fieldName);
                removingFields.add(field);
            }
        }
        fields.removeAll(removingFields);

        HashSet<Method> removingMethods = new HashSet<Method>();

        List<Method> methods = topLevelClass.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();

            if ("setOrderByClause".equals(methodName) || "getOrderByClause".equals(methodName)
                    || "setDistinct".equals(methodName) || "isDistinct".equals(methodName)) {// 将要删除的方法
                System.out.println("removing method:" + methodName);
                removingMethods.add(method);
            } else if ("clear".equals(methodName)) {// 重新实现clear方法，部分实现重构到父类
                List<String> bodyLines = method.getBodyLines();
                bodyLines.clear();

                bodyLines.add("super.clear();");
                bodyLines.add("oredCriteria.clear();");
            }
        }
        methods.removeAll(removingMethods);
    }

    /**
     * 清理GeneratedCriteria的多余属性与方法，已经迁移到父类了
     *
     * @param innerClass
     */
    private static void clearGeneratedCriteriaClass(InnerClass innerClass) {
        logger.debug("开始清理GeneratedCriteriaClass的多余属性");

        HashSet<Field> removingFields = new HashSet<Field>();

        List<Field> fields = innerClass.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if ("criteria".equals(fieldName)) {// 将要删除的变量
                System.out.println("removing field:" + fieldName);
                removingFields.add(field);
            }
        }
        fields.removeAll(removingFields);

        HashSet<Method> removingMethods = new HashSet<Method>();

        List<Method> methods = innerClass.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();

            if ("GeneratedCriteria".equals(methodName) || "isValid".equals(methodName)
                    || "getAllCriteria".equals(methodName) || "getCriteria".equals(methodName)
                    || "addCriterion".equals(methodName)) {// 将要删除的方法
                System.out.println("removing method:" + methodName);
                removingMethods.add(method);
            }
        }
        methods.removeAll(removingMethods);
    }

    // 添加联合查询相关的内部类
    private static class JoinClause {
        private String joinType = "LEFT JOIN";
        private String tableName;
        private String alias;
        private String condition;
        
        public JoinClause(String tableName, String condition) {
            this.tableName = tableName;
            this.condition = condition;
        }
        
        public String toSql() {
            return String.format("%s %s %s ON %s", 
                joinType, 
                tableName, 
                alias != null ? alias : "", 
                condition);
        }
    }

    // 添加 JoinCriteria 内部类
    private static class JoinCriteria {
        private String joinType = "LEFT JOIN";
        private String tableName;
        private String condition;
        private List<String> columns;

        public JoinCriteria(String tableName, String condition) {
            this.tableName = tableName;
            this.condition = condition;
            this.columns = new ArrayList<>();
        }

        public void addColumn(String column) {
            if (columns == null) {
                columns = new ArrayList<>();
            }
            columns.add(column);
        }

        public String toSql() {
            return String.format("%s %s ON %s", joinType, tableName, condition);
        }

        public String getSelectColumns() {
            if (columns == null || columns.isEmpty()) {
                return tableName + ".*";
            }
            return String.join(",", columns.stream()
                    .map(col -> tableName + "." + col)
                    .collect(Collectors.toList()));
        }
    }

    private void addJoinSupport(TopLevelClass topLevelClass) {
        // 添加需要的导入
        topLevelClass.addImportedType("java.util.ArrayList");
        topLevelClass.addImportedType("java.util.List");
        topLevelClass.addImportedType("java.util.stream.Collectors");

        // 添加 joins 字段
        Field joinsField = new Field("joins", new FullyQualifiedJavaType("List<JoinCriteria>"));
        joinsField.setVisibility(JavaVisibility.PRIVATE);
        joinsField.setInitializationString("new ArrayList<>()");
        topLevelClass.addField(joinsField);

        // 添加 leftJoin 方法
        Method leftJoin = new Method("leftJoin");
        leftJoin.setVisibility(JavaVisibility.PUBLIC);
        leftJoin.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "tableName"));
        leftJoin.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "condition"));
        leftJoin.setReturnType(topLevelClass.getType());
        leftJoin.addBodyLine("JoinCriteria join = new JoinCriteria(tableName, condition);");
        leftJoin.addBodyLine("join.joinType = \"LEFT JOIN\";");
        leftJoin.addBodyLine("joins.add(join);");
        leftJoin.addBodyLine("return this;");
        topLevelClass.addMethod(leftJoin);

        // 添加 rightJoin 方法
        Method rightJoin = new Method("rightJoin");
        rightJoin.setVisibility(JavaVisibility.PUBLIC);
        rightJoin.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "tableName"));
        rightJoin.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "condition"));
        rightJoin.setReturnType(topLevelClass.getType());
        rightJoin.addBodyLine("JoinCriteria join = new JoinCriteria(tableName, condition);");
        rightJoin.addBodyLine("join.joinType = \"RIGHT JOIN\";");
        rightJoin.addBodyLine("joins.add(join);");
        rightJoin.addBodyLine("return this;");
        topLevelClass.addMethod(rightJoin);

        // 添加 innerJoin 方法
        Method innerJoin = new Method("innerJoin");
        innerJoin.setVisibility(JavaVisibility.PUBLIC);
        innerJoin.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "tableName"));
        innerJoin.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "condition"));
        innerJoin.setReturnType(topLevelClass.getType());
        innerJoin.addBodyLine("JoinCriteria join = new JoinCriteria(tableName, condition);");
        innerJoin.addBodyLine("join.joinType = \"INNER JOIN\";");
        innerJoin.addBodyLine("joins.add(join);");
        innerJoin.addBodyLine("return this;");
        topLevelClass.addMethod(innerJoin);

        // 添加 select 方法用于指定查询字段
        Method select = new Method("select");
        select.setVisibility(JavaVisibility.PUBLIC);
        select.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "tableName"));
        select.addParameter(new Parameter(new FullyQualifiedJavaType("String..."), "columns"));
        select.setReturnType(topLevelClass.getType());
        select.addBodyLine("joins.stream()");
        select.addBodyLine("    .filter(join -> join.tableName.equals(tableName))");
        select.addBodyLine("    .findFirst()");
        select.addBodyLine("    .ifPresent(join -> {");
        select.addBodyLine("        for (String column : columns) {");
        select.addBodyLine("            join.addColumn(column);");
        select.addBodyLine("        }");
        select.addBodyLine("    });");
        select.addBodyLine("return this;");
        topLevelClass.addMethod(select);

        // 添加获取 joins 的方法
        Method getJoins = new Method("getJoins");
        getJoins.setVisibility(JavaVisibility.PUBLIC);
        getJoins.setReturnType(new FullyQualifiedJavaType("List<JoinCriteria>"));
        getJoins.addBodyLine("return joins;");
        topLevelClass.addMethod(getJoins);
    }
}
