package com.stip.mybatis.generator.plugin;

import java.util.HashSet;
import java.util.List;

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

/**
 * 
 * 定制部分mybatis的插件，主要实现以下功能
 * <ol>
 * <li>生成Example的文件</li>
 * </ol>
 * <p>
 * 使用方法配置与在generatorConfig.xml中其中
 * baseModelNamePrefix 为新生成的类文件的前置关键字
 * baseModelPackage 为生成新的类文件的包名
 * 
 * @author cja
 *
 **/
public class ExampleClassPlugin extends PluginAdapter {
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

        modelClassName = introspectedTable.getBaseRecordType();
        introspectedTable.setBaseRecordType(genBaseClassName(modelClassName));
        
        String exampleTargetPackage = properties.getProperty("exampleTargetPackage");
        String modelTargetPackage = properties.getProperty("modelTargetPackage");
        exampleClassName=introspectedTable.getExampleType().replaceAll(modelTargetPackage, "");
        exampleClassName=exampleTargetPackage+exampleClassName;
        introspectedTable.setExampleType(exampleClassName);
    }

    /*
     * 检查xml参数是否正确
     * 
     * @see org.mybatis.generator.api.Plugin#validate(java.util.List)
     */
    public boolean validate(List<String> warnings) {
        System.out.println("开始：validate");

        baseModelNamePrefix = properties.getProperty("baseModelNamePrefix");
        if (!StringUtility.stringHasValue(baseModelNamePrefix)) {
            baseModelNamePrefix = DEFAULT_BASE_MODEL_NAME_PREFIX;
        }

        String exampleTargetPackage = properties.getProperty("exampleTargetPackage");
        if (!StringUtility.stringHasValue(exampleTargetPackage)) {
            return false;
        }

        String baseExamplePackage = properties.getProperty("baseExamplePackage");
        if (StringUtility.stringHasValue(baseExamplePackage)) {
            fullModelPackage = exampleTargetPackage + "." + baseExamplePackage;
        } else {
            fullModelPackage = exampleTargetPackage + "." + DEFAULT_BASE_MODEL_PACKAGE;
        }

        String xmlTargetPackage = properties.getProperty("xmlTargetPackage");
        if (!StringUtility.stringHasValue(xmlTargetPackage)) {
            return false;
        }

        String baseExampleSuperClazz = properties.getProperty("baseExampleSuperClass");
        if (StringUtility.stringHasValue(baseExampleSuperClazz)) {
            baseExampleSuperClass = baseExampleSuperClazz;
        }

        String baseCriteriaSuperClazz = properties.getProperty("baseCriteriaSuperClass");
        if (StringUtility.stringHasValue(baseCriteriaSuperClazz)) {
            baseCriteriaSuperClass = baseCriteriaSuperClazz;
        }

        return true;
    }

    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        System.out.println("===============开始：修改Example文件================");
        
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        String exampleTargetProject = properties.getProperty("exampleTargetProject");
        String targetPackage = exampleTargetPackage; //$NON-NLS-1$
        String targetProject = exampleTargetProject; //$NON-NLS-1$

        if (StringUtility.stringHasValue(targetPackage)) {
        	javaModelGeneratorConfiguration.setTargetPackage(targetPackage);
        }
        
        if (StringUtility.stringHasValue(targetProject)) {
        	javaModelGeneratorConfiguration.setTargetProject(targetProject);
        }
        
        introspectedTable.getContext().setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

        // 添加example基类
        topLevelClass.addImportedType(new FullyQualifiedJavaType(baseExampleSuperClassName));
        topLevelClass.setSuperClass(baseExampleSuperClass);
        
        clearExampleCLass(topLevelClass);

        HashSet<InnerClass> removingInnerClasses = new HashSet<InnerClass>();

        // 处理example的所有内部类
        List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
        for (InnerClass innerClass : innerClasses) {

            FullyQualifiedJavaType type = innerClass.getType();
            String innerClassName = type.getFullyQualifiedName();
            System.out.println("fullyQualifiedName:" + innerClassName);

            if ("Criterion".equals(innerClassName)) {// 删除example的Criterion静态类
                removingInnerClasses.add(innerClass);

            } else if ("GeneratedCriteria".equals(innerClassName)) {// 改造GeneratedCriteria类，添加一个基类
                // innerClass.setAbstract(false);

                // 添加Criteria基类
                topLevelClass.addImportedType(new FullyQualifiedJavaType(baseCriteriaSuperClassName));
                innerClass.setSuperClass(baseCriteriaSuperClass);

                clearGeneratedCriteriaClass(innerClass);
            }
        }

        innerClasses.removeAll(removingInnerClasses);

        System.out.println("===============完成：修改Example文件================");

        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 清理Example的多余属性与方法，已经迁移到父类了
     * 
     * @param topLevelClass
     */
    private static void clearExampleCLass(TopLevelClass topLevelClass) {
        System.out.println("开始清理Example的TopLevelCLass多余属性");

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
        System.out.println("开始清理GeneratedCriteriaClass的多余属性");

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

    /**
     * 根据Model类的全路径名称，生成Base Model类的全路径名(包括类名)。
     * 比如根据com.company.model.User，生成com.company.model.base.BaseUser
     * 
     * @param oldModelType
     * @return 新的名称
     */
    private String genBaseClassName(String oldModelType) {
        int indexOfLastDot = oldModelType.lastIndexOf('.');
        String className="";
        
        if("".equals(baseModelNamePrefix)) {
        	className=fullModelPackage + oldModelType.substring(indexOfLastDot + 1);
        }else {
        	className=fullModelPackage + "." + baseModelNamePrefix + oldModelType.substring(indexOfLastDot + 1);
        }
        
        return className;
    }

}
