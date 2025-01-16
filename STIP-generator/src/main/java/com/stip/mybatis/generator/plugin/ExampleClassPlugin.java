package com.stip.mybatis.generator.plugin;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
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
    private String baseExampleSuperClass = "com.stip.mybatis.generator.plugin.BaseExample";

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
    public boolean validate(List<String> warnings) {
        logger.debug("开始：validate");
        exampleTargetProject = properties.getProperty("targetProject");
        exampleTargetPackage = properties.getProperty("targetPackage");
        return StringUtility.stringHasValue(exampleTargetProject) 
            && StringUtility.stringHasValue(exampleTargetPackage);
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        logger.debug("开始：初始化");
        // 获取实体类名和包名
        FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String shortName = modelJavaType.getShortName();
        
        // 设置Example类的完整包名
        String exampleType = exampleTargetPackage + ".example." + shortName + "Example";
        introspectedTable.setExampleType(exampleType);
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // Set parent class
        FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(baseExampleSuperClass);
        topLevelClass.setSuperClass(superClass);
        topLevelClass.addImportedType(superClass);

        // Clear and clean up
        topLevelClass.getFields().clear();
        topLevelClass.getMethods().clear();
        topLevelClass.getInnerClasses().clear();
        clearExampleCLass(topLevelClass);

        // Add constructor
        Method constructor = new Method(topLevelClass.getType().getShortName());
        constructor.setConstructor(true);
        constructor.setVisibility(JavaVisibility.PUBLIC);
        constructor.addBodyLine("super();");
        topLevelClass.addMethod(constructor);

        // Add Criteria class that extends BaseModelExample.GeneratedCriteria
        InnerClass criteria = new InnerClass("Criteria");
        criteria.setVisibility(JavaVisibility.PUBLIC);
        criteria.setStatic(true);
        criteria.setSuperClass(new FullyQualifiedJavaType("GeneratedCriteria"));
        
        Method criteriaConstructor = new Method("Criteria");
        criteriaConstructor.setConstructor(true);
        criteriaConstructor.setVisibility(JavaVisibility.PROTECTED);
        criteriaConstructor.addBodyLine("super();");
        criteria.addMethod(criteriaConstructor);
        
        topLevelClass.addInnerClass(criteria);

        return true;
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
}
