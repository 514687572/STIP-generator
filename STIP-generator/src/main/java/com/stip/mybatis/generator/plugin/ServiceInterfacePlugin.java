package com.stip.mybatis.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

/**
 * 生成 service 插件类
 * 
 * @author chenjunan
 *
 */
public class ServiceInterfacePlugin extends PluginAdapter {
	public static Log logger = LogFactory.getLog(ServiceInterfacePlugin.class);
    private String baseServiceSuperClass = "IService";
    private String baseServiceSuperClassName = "com.stip.mybatis.generator.plugin.IService";
	private static final String DEFAULT_EXAMPLE_CLASS_NAME = "com.stip.mybatis.generator.plugin.BaseExample";

    private ShellCallback shellCallback = null;

    private String serviceTargetDir;

    private String serviceInterfaceTargetPackage;
    
    private String serviceInterfaceName;
    
    private String baseServiceInterfacePackageName=".service";
    
    public ServiceInterfacePlugin() {
        shellCallback = new DefaultShellCallback(false);
    }

    /**
     * 验证传入的service包是否合法
     */
    public boolean validate(List<String> warnings) {
    	serviceTargetDir = properties.getProperty("serviceTargetDir");
        if (!StringUtility.stringHasValue(serviceTargetDir)) {
        	serviceTargetDir = properties.getProperty("targetProject");
        	if (!StringUtility.stringHasValue(serviceTargetDir)) {
        		return false;
        	}
        }

        serviceInterfaceTargetPackage = properties.getProperty("serviceInterfaceTargetPackage");
        if (!StringUtility.stringHasValue(serviceInterfaceTargetPackage)) {
        	serviceInterfaceTargetPackage = properties.getProperty("targetPackage");
        	if (!StringUtility.stringHasValue(serviceInterfaceTargetPackage)) {
        		return false;
        	}else {
        		serviceInterfaceTargetPackage+=baseServiceInterfacePackageName;
        	}
        }

        return true;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
    	// 初始化两参数为空
    	serviceInterfaceName = null;

    	FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        serviceInterfaceName = modelJavaType.getShortName();
        serviceInterfaceName=serviceInterfaceTargetPackage+"."+serviceInterfaceName+"Service";
        introspectedTable.setBaseInterfaceType(serviceInterfaceName);
    }
    
    public boolean serviceInterfaceGenerated(Interface serviceInterface, IntrospectedTable introspectedTable) {
        logger.debug("开始：修改service文件");
        
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetPackage(serviceInterfaceTargetPackage);
        javaModelGeneratorConfiguration.setTargetProject(serviceTargetDir);
        introspectedTable.getContext().setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);
        
        FullyQualifiedJavaType superClazzType = new FullyQualifiedJavaType(baseServiceSuperClass);
        FullyQualifiedJavaType superClazzTypeName = new FullyQualifiedJavaType(baseServiceSuperClassName);
        
		serviceInterface.setVisibility(JavaVisibility.PUBLIC);
		serviceInterface.addJavaDocLine(" /**");
		serviceInterface.addJavaDocLine(" * Extensible custom interface");
		serviceInterface.addJavaDocLine(" **/");
		
		FullyQualifiedJavaType pkType = null;
		List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
		if (primaryKeyColumns.isEmpty()) {
			pkType = new FullyQualifiedJavaType("java.lang.String");
		} else {
			pkType = primaryKeyColumns.get(0).getFullyQualifiedJavaType();
			System.out.println("primaryKey Type:" + pkType);
		}
		
		FullyQualifiedJavaType subModelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		serviceInterface.addImportedType(subModelJavaType);
		FullyQualifiedJavaType subModelExampleJavaType = new FullyQualifiedJavaType(DEFAULT_EXAMPLE_CLASS_NAME);
		serviceInterface.addImportedType(subModelExampleJavaType);
		
		// 添加泛型支持
		superClazzType.addTypeArgument(subModelJavaType);
		superClazzType.addTypeArgument(subModelExampleJavaType);
		superClazzType.addTypeArgument(pkType);
		
		serviceInterface.addSuperInterface(superClazzType);
		serviceInterface.addImportedType(superClazzTypeName);
        
		logger.debug("完成：修改service文件");
		
        return super.serviceInterfaceGenerated(serviceInterface, introspectedTable);
    }

}
