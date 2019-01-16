package com.stip.mybatis.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
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
public class ServiceInterfacePlugin extends PluginAdapter {
	public static Log logger = LogFactory.getLog(ServiceInterfacePlugin.class);
    private String baseServiceSuperClass = "IService";
    private String baseServiceSuperClassName = "com.stip.mybatis.generator.plugin.IService";

    private ShellCallback shellCallback = null;

    private String serviceTargetDir;

    private String serviceInterfaceTargetPackage;
    
    private String serviceInterfaceName;
    
    public ServiceInterfacePlugin() {
        shellCallback = new DefaultShellCallback(false);
    }

    /**
     * 验证传入的service包是否合法
     */
    public boolean validate(List<String> warnings) {
    	serviceTargetDir = properties.getProperty("serviceTargetDir");
        boolean valid = StringUtility.stringHasValue(serviceTargetDir);

        serviceInterfaceTargetPackage = properties.getProperty("serviceInterfaceTargetPackage");
        boolean valid2 = StringUtility.stringHasValue(serviceInterfaceTargetPackage);

        return valid && valid2;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
    	// 初始化两参数为空
    	serviceInterfaceName = null;

        serviceInterfaceName = introspectedTable.getBaseRecordType();
        String modelTargetPackage = properties.getProperty("modelTargetPackage");
        serviceInterfaceName=introspectedTable.getBaseRecordType().replaceAll(modelTargetPackage, "");
        serviceInterfaceName=serviceInterfaceTargetPackage+serviceInterfaceName+"Service";
        introspectedTable.setBaseInterfaceType(serviceInterfaceName);
    }
    
    public boolean serviceInterfaceGenerated(Interface serviceInterface, IntrospectedTable introspectedTable) {
        logger.debug("开始：修改service文件");
        
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
			pkType = primaryKeyColumns.get(0).getFullyQualifiedJavaType();// TODO:默认不考虑联合主键的情况
			System.out.println("primaryKey Type:" + pkType);
		}
		
		FullyQualifiedJavaType subModelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		serviceInterface.addImportedType(subModelJavaType);
		FullyQualifiedJavaType subModelExampleJavaType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
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
