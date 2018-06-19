package com.stip.mybatis.generator.plugin;

import java.util.HashSet;
import java.util.List;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * 
 * 定制部分mybatis的插件，主要实现以下功能
 * <ol>
 * <li>生成Model文件</li>
 * </ol>
 * <p>
 * 使用方法配置与在generatorConfig.xml中其中
 * baseModelNamePrefix 为新生成的类文件的前置关键字
 * baseModelPackage 为生成新的类文件的包名
 * extXmlPackage 包名
 * 
 * @author cja
 *
 **/
public class ModelClassPlugin extends PluginAdapter {

    public final static String DEFAULT_BASE_MODEL_PACKAGE = "";
    public final static String DEFAULT_BASE_MODEL_NAME_PREFIX = "";

    /**
     * Model的基类
     */
    private String baseModelSuperClass = "BaseModel";
    private String baseModelSuperClassName = "com.stip.mybatis.generator.plugin.BaseModel";

    /**
     * Model类的前缀名称
     */
    private String baseModelNamePrefix;

    /**
     * Model类文件包名
     */
    private String fullModelPackage;

    /**
     * 类的主键字段名, 默认为sid
     */
    private String modelPKColumnName = "sid";

    /**
     * 利用java反射获取isMergeable参数，并修改
     */
    private java.lang.reflect.Field isMergeableFid = null;

    /**
     * 两个参数用于做数据中转，否则xml文件里会用Base类，这里是为了让xml文件用标准的Model类
     */
    private String modelClassName;

    public ModelClassPlugin() {
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

        String modelTargetPackage = properties.getProperty("modelTargetPackage");
        if (!StringUtility.stringHasValue(modelTargetPackage)) {
            return false;
        }

        String baseModelPackage = properties.getProperty("baseModelPackage");
        if (StringUtility.stringHasValue(baseModelPackage)) {
            fullModelPackage = modelTargetPackage + "." + baseModelPackage;
        } else {
            fullModelPackage = modelTargetPackage + "." + DEFAULT_BASE_MODEL_PACKAGE;
        }

        String baseModelSuperClazz = properties.getProperty("baseModelSuperClass");
        if (StringUtility.stringHasValue(baseModelSuperClazz)) {
            baseModelSuperClass = baseModelSuperClazz;
        }

        String pkColumnName = properties.getProperty("pkColumnName");
        if (StringUtility.stringHasValue(pkColumnName)) {
            modelPKColumnName = pkColumnName;
        }

        return true;
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        System.out.println("===============开始：修改Model文件================");
        
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();

        String modelTargetPackage = properties.getProperty("modelTargetPackage");
        String modelTargetProject = properties.getProperty("modelTargetProject");
        String targetPackage = modelTargetPackage; //$NON-NLS-1$
        String targetProject = modelTargetProject; //$NON-NLS-1$

        javaModelGeneratorConfiguration.setTargetPackage(targetPackage);
        javaModelGeneratorConfiguration.setTargetProject(targetProject);
        
        introspectedTable.getContext().setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

        // 添加基类
        FullyQualifiedJavaType superClazzType = new FullyQualifiedJavaType(baseModelSuperClass);
        FullyQualifiedJavaType superClazzTypeName = new FullyQualifiedJavaType(baseModelSuperClassName);
        topLevelClass.addImportedType(superClazzTypeName);

        FullyQualifiedJavaType pkType = null;
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        if (primaryKeyColumns.isEmpty()) {
            pkType = new FullyQualifiedJavaType("java.lang.String");//没有主键的表，默认使用字符串作为主键类型
        } else {
            pkType = primaryKeyColumns.get(0).getFullyQualifiedJavaType();//TODO:默认不考虑联合主键的情况
            System.out.println("primaryKey Type:" + pkType);
        }

        superClazzType.addTypeArgument(pkType);
        System.out.println("Model基类：" + superClazzType.toString());
        topLevelClass.setSuperClass(superClazzType);

        clearModelCLass(topLevelClass);

        System.out.println("===============完成：修改Model文件================");

        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 清理Model的多余属性与方法，已经迁移到父类了
     * 
     * @param topLevelClass
     */
    private void clearModelCLass(TopLevelClass topLevelClass) {

        System.out.println("开始清理Model的TopLevelCLass多余属性");

        HashSet<Field> removingFields = new HashSet<Field>();

        List<Field> fields = topLevelClass.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (modelPKColumnName.equals(fieldName)) {// 将要删除的变量
                System.out.println("removing field:" + fieldName);
                removingFields.add(field);
            }
        }
        fields.removeAll(removingFields);

        HashSet<Method> removingMethods = new HashSet<Method>();

        String pkSetter = "set" + capitalize(modelPKColumnName);
        String pkGetter = "get" + capitalize(modelPKColumnName);
        
        List<Method> methods = topLevelClass.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();

            if (pkSetter.equals(methodName) || pkGetter.equals(methodName)) {// 将要删除的方法
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

    public static String capitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        char firstChar = str.charAt(0);
        if (Character.isTitleCase(firstChar)) {
            // already capitalized
            return str;
        }

        return new StringBuilder(strLen)
            .append(Character.toTitleCase(firstChar))
            .append(str.substring(1))
            .toString();
    }

}
