package com.stip.mybatis.generator.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.JavaFormatter;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * 生成 Mapper 类
 * 
 * @author cja
 *
 */
public class MapperPlugin extends PluginAdapter {

    private static final String DEFAULT_DAO_SUPER_CLASS = "GenericMapper";
    private static final String DEFAULT_DAO_SUPER_CLASS_NAME = "com.stip.mybatis.generator.plugin.GenericMapper";

    private ShellCallback shellCallback = null;

    private String daoTargetDir;

    private String daoTargetPackage;

    /**
     * Model基类文件包名
     */
    private String baseModelPackage;

    /**
     * Model类的前缀名称
     */
    private String baseModelNamePrefix;

    private String daoSuperClass;

    public MapperPlugin() {
        shellCallback = new DefaultShellCallback(false);
    }

    public boolean validate(List<String> warnings) {
        daoTargetDir = properties.getProperty("daoTargetDir");
        boolean valid = StringUtility.stringHasValue(daoTargetDir);

        daoTargetPackage = properties.getProperty("daoTargetPackage");
        boolean valid2 = StringUtility.stringHasValue(daoTargetPackage);

        daoSuperClass = properties.getProperty("daoSuperClass");
        if (!StringUtility.stringHasValue(daoSuperClass)) {
            daoSuperClass = DEFAULT_DAO_SUPER_CLASS;
        }

        baseModelPackage = properties.getProperty("baseModelPackage");
        if (!StringUtility.stringHasValue(baseModelPackage)) {
            baseModelPackage = ModelAndExampleBaseClassPlugin.DEFAULT_BASE_MODEL_PACKAGE;
        }

        baseModelNamePrefix = properties.getProperty("baseModelNamePrefix");
        if (!StringUtility.stringHasValue(baseModelNamePrefix)) {
            baseModelNamePrefix = ModelAndExampleBaseClassPlugin.DEFAULT_BASE_MODEL_NAME_PREFIX;
        }

        return valid && valid2;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        System.out.println("===============开始：生成Mapper文件================");

        JavaFormatter javaFormatter = context.getJavaFormatter();
        
        FullyQualifiedJavaType pkType = null;
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        if (primaryKeyColumns.isEmpty()) {
            pkType = new FullyQualifiedJavaType("java.lang.String");
        } else {
            pkType = primaryKeyColumns.get(0).getFullyQualifiedJavaType();//TODO:默认不考虑联合主键的情况
            System.out.println("primaryKey Type:" + pkType);
        }

        List<GeneratedJavaFile> mapperJavaFiles = new ArrayList<GeneratedJavaFile>();
        for (GeneratedJavaFile javaFile : introspectedTable.getGeneratedJavaFiles()) {

            CompilationUnit unit = javaFile.getCompilationUnit();
            FullyQualifiedJavaType baseModelJavaType = unit.getType();

            String shortName = baseModelJavaType.getShortName();

            if (shortName.endsWith("Example")) {// 针对Example类不要生成Mapper
                continue;
            }

            String subModelType = getSubModelType(baseModelJavaType);
            String subModelExampleType = subModelType + "Example";

            System.out.println("shortName:" + shortName);

            String subModelName = shortName.replace(baseModelNamePrefix, "");

            Interface mapperInterface = new Interface(daoTargetPackage + "." + subModelName + "Dao");

            mapperInterface.setVisibility(JavaVisibility.PUBLIC);
            mapperInterface.addJavaDocLine(" /**");
            mapperInterface.addJavaDocLine(" * generator XML");
            mapperInterface.addJavaDocLine(" **/");

            FullyQualifiedJavaType subModelJavaType = new FullyQualifiedJavaType(subModelType);
            mapperInterface.addImportedType(subModelJavaType);
            FullyQualifiedJavaType subModelExampleJavaType = new FullyQualifiedJavaType(subModelExampleType);
            mapperInterface.addImportedType(subModelExampleJavaType);

            FullyQualifiedJavaType daoSuperType = new FullyQualifiedJavaType(daoSuperClass);
            FullyQualifiedJavaType daoSuperTypeName = new FullyQualifiedJavaType(DEFAULT_DAO_SUPER_CLASS_NAME);
            // 添加泛型支持
            daoSuperType.addTypeArgument(subModelJavaType);
            daoSuperType.addTypeArgument(subModelExampleJavaType);
            daoSuperType.addTypeArgument(pkType);
            mapperInterface.addImportedType(daoSuperTypeName);
            mapperInterface.addSuperInterface(daoSuperType);

            try {
                GeneratedJavaFile mapperJavafile = new GeneratedJavaFile(mapperInterface, daoTargetDir, javaFormatter);

                File mapperDir = shellCallback.getDirectory(daoTargetDir, daoTargetPackage);

                File mapperFile = new File(mapperDir, mapperJavafile.getFileName());

                // 文件不存在
                if (!mapperFile.exists()) {

                    mapperJavaFiles.add(mapperJavafile);
                }
            } catch (ShellException e) {
                e.printStackTrace();
            }

        }

        System.out.println("===============结束：生成Mapper文件================");

        return mapperJavaFiles;
    }

    private String getSubModelType(FullyQualifiedJavaType fullyQualifiedJavaType) {
        String type = fullyQualifiedJavaType.getFullyQualifiedName();
        String defaultPrefix = baseModelPackage + "." + baseModelNamePrefix;
        String newType = type.replace(defaultPrefix, "");
        return newType;
    }
}
