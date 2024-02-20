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
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

/**
 * Mapper生成插件类
 * 
 * @author chenjunan
 *
 */
public class MapperPlugin extends PluginAdapter {
	public static Log logger = LogFactory.getLog(MapperPlugin.class);
    private static final String DEFAULT_DAO_SUPER_CLASS = "GenericMapper";
    private static final String DEFAULT_DAO_SUPER_CLASS_NAME = "com.stip.mybatis.generator.plugin.GenericMapper";
    private static final String DEFAULT_EXAMPLE_CLASS_NAME = "com.stip.mybatis.generator.plugin.BaseExample";

    public final static String DEFAULT_BASE_MODEL_PACKAGE = "";
    public final static String DEFAULT_BASE_MODEL_NAME_PREFIX = "";

    private ShellCallback shellCallback = null;

    private String daoTargetDir;

    private String daoTargetPackage;
    
    private String baseMapperPackageName=".dao";

    /**
     * Model基类文件包名
     */
    private String baseModelPackage;

    /**
     * Model类的前缀名称
     */
    private String baseModelNamePrefix;

    private String daoSuperClass;
    
    private String mapperClassName;

    public MapperPlugin() {
        shellCallback = new DefaultShellCallback(false);
    }

    public boolean validate(List<String> warnings) {
        daoTargetPackage = properties.getProperty("daoTargetPackage");
        if (!StringUtility.stringHasValue(daoTargetPackage)) {
        	daoTargetPackage = properties.getProperty("targetPackage");
            if (!StringUtility.stringHasValue(daoTargetPackage)) {
                return false;
            }else {
            	daoTargetPackage+=baseMapperPackageName;
            }
        }
        
        daoTargetDir = properties.getProperty("daoTargetDir");
        if (!StringUtility.stringHasValue(daoTargetDir)) {
        	daoTargetDir = properties.getProperty("targetProject");
        	if (!StringUtility.stringHasValue(daoTargetDir)) {
        		return false;
        	}
        }

        daoSuperClass = properties.getProperty("daoSuperClass");
        if (!StringUtility.stringHasValue(daoSuperClass)) {
            daoSuperClass = DEFAULT_DAO_SUPER_CLASS;
        }

        baseModelPackage = properties.getProperty("baseModelPackage");
        if (!StringUtility.stringHasValue(baseModelPackage)) {
            baseModelPackage = DEFAULT_BASE_MODEL_PACKAGE;
        }

        baseModelNamePrefix = properties.getProperty("baseModelNamePrefix");
        if (!StringUtility.stringHasValue(baseModelNamePrefix)) {
            baseModelNamePrefix = DEFAULT_BASE_MODEL_NAME_PREFIX;
        }
        
		return true;
    }
    
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        // 初始化两参数为空
        mapperClassName = null;

        FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        mapperClassName = modelJavaType.getShortName();
        mapperClassName=daoTargetPackage+"."+mapperClassName+"Dao";
        introspectedTable.setMyBatis3JavaMapperType(mapperClassName);
        introspectedTable.setMyBatis3XmlMapperPackage(daoTargetPackage);
    }

	@Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
		logger.debug("开始：生成java dao文件");
		FullyQualifiedJavaType pkType = null;
		JavaFormatter javaFormatter = context.getJavaFormatter();
		String subModelExampleType = "";
		String subModelType = "";
		
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetPackage(daoTargetPackage);
        javaModelGeneratorConfiguration.setTargetProject(daoTargetDir);
        introspectedTable.getContext().setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

		List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
		if (primaryKeyColumns.isEmpty()) {
			pkType = new FullyQualifiedJavaType("java.lang.String");
		} else {
			pkType = primaryKeyColumns.get(0).getFullyQualifiedJavaType();// TODO:默认不考虑联合主键的情况
			System.out.println("primaryKey Type:" + pkType);
		}

		List<GeneratedJavaFile> mapperJavaFiles = new ArrayList<GeneratedJavaFile>();
		
		String subExampleType = DEFAULT_EXAMPLE_CLASS_NAME;
		subModelExampleType = subExampleType;
		
		subModelType = introspectedTable.getBaseRecordType();

		Interface mapperInterface = new Interface(mapperClassName);
		mapperInterface.setVisibility(JavaVisibility.PUBLIC);
		mapperInterface.addJavaDocLine(" /**");
		mapperInterface.addJavaDocLine(" * 可添加自定义查询语句，方便后续扩展");
		mapperInterface.addJavaDocLine(" **/");

		introspectedTable.setExampleType(subExampleType);

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

		logger.debug("结束：生成Mapper文件");

		return mapperJavaFiles;
	}

}
