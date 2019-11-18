package com.stip.mybatis.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

/**
 * 生成 service 插件类
 * 
 * @author cja
 *
 */
public class ServicePlugin extends PluginAdapter {
	public static Log logger = LogFactory.getLog(ServicePlugin.class);
    private String baseServiceSuperClass = "BaseService";
    private String baseServiceSuperClassName = "com.stip.mybatis.generator.plugin.BaseService";
    private static final String DEFAULT_EXAMPLE_CLASS_NAME = "com.stip.mybatis.generator.plugin.BaseExample";

    private ShellCallback shellCallback = null;

    private String serviceTargetDir;

    private String serviceTargetPackage;
    
    private String serviceInterFacePackage;
    
    private String serviceClassName;
    
    private String superServiceUrl;
    
    private String modelClassName;
    
    private String baseServicePackageName=".service.impl";
    
    private String baseServiceInterfacePackageName=".service";
    
    public ServicePlugin() {
        shellCallback = new DefaultShellCallback(false);
    }

    /**
     * 验证传入的service包是否合法
     */
    public boolean validate(List<String> warnings) {
        serviceTargetPackage = properties.getProperty("serviceTargetPackage");
        if (!StringUtility.stringHasValue(serviceTargetPackage)) {
        	serviceTargetPackage = properties.getProperty("targetPackage");
            if (!StringUtility.stringHasValue(serviceTargetPackage)) {
                return false;
            }else {
            	serviceTargetPackage+=baseServicePackageName;
            }
        }
        
        serviceInterFacePackage = properties.getProperty("serviceInterfaceTargetPackage");
        if (!StringUtility.stringHasValue(serviceInterFacePackage)) {
        	serviceInterFacePackage = properties.getProperty("targetPackage");
        	if (!StringUtility.stringHasValue(serviceInterFacePackage)) {
        		return false;
        	}else {
        		serviceInterFacePackage+=baseServiceInterfacePackageName;
        	}
        }
        
        serviceTargetDir = properties.getProperty("serviceTargetDir");
        if (!StringUtility.stringHasValue(serviceTargetDir)) {
        	serviceTargetDir = properties.getProperty("targetProject");
        	if (!StringUtility.stringHasValue(serviceTargetDir)) {
        		return false;
        	}
        }
        
		return true;

    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
    	// 初始化两参数为空
        serviceClassName = null;

        FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        modelClassName = modelJavaType.getShortName();
        superServiceUrl=serviceInterFacePackage+"."+modelClassName+"Service";
        serviceClassName=serviceTargetPackage+"."+modelClassName+"ServiceImpl";
        introspectedTable.setBaseServiceType(serviceClassName);
    }
    
    public boolean serviceClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        logger.debug("开始：修改serviceImpl文件");
        FullyQualifiedJavaType pkType = null;
		List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
		
		if (primaryKeyColumns.isEmpty()) {
			pkType = new FullyQualifiedJavaType("java.lang.String");
		} else {
			pkType = primaryKeyColumns.get(0).getFullyQualifiedJavaType();// TODO:默认不考虑联合主键的情况
		}
		
		JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetPackage(serviceTargetPackage);
        javaModelGeneratorConfiguration.setTargetProject(serviceTargetDir);
        introspectedTable.getContext().setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);
        
        // 添加基类
        FullyQualifiedJavaType superClazzType = new FullyQualifiedJavaType(baseServiceSuperClass);
        FullyQualifiedJavaType superClazzTypeName = new FullyQualifiedJavaType(baseServiceSuperClassName);
        topLevelClass.addImportedType(superClazzTypeName);
        
        FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        topLevelClass.addImportedType(modelJavaType);
		FullyQualifiedJavaType exampleJavaType = new FullyQualifiedJavaType(DEFAULT_EXAMPLE_CLASS_NAME);
		topLevelClass.addImportedType(exampleJavaType);
        
        superClazzType.addTypeArgument(modelJavaType);
        superClazzType.addTypeArgument(exampleJavaType);
        superClazzType.addTypeArgument(pkType);

        topLevelClass.setSuperClass(superClazzType);
        
        FullyQualifiedJavaType superInterface = new FullyQualifiedJavaType((modelJavaType.getShortName()+"Service"));
        topLevelClass.addSuperInterface(superInterface);
        topLevelClass.addImportedType(superServiceUrl);
        
        topLevelClass.addImportedType("org.springframework.stereotype.Service");
        topLevelClass.addAnnotation("@Service");
        
        logger.debug("完成：修改serviceImpl文件");

        return super.serviceClassGenerated(topLevelClass, introspectedTable);
    }

}
