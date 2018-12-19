package com.stip.mybatis.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * 生成 service 插件类
 * 
 * @author cja
 *
 */
public class ServicePlugin extends PluginAdapter {
    /**
     * Example的基类
     */
    private String baseServiceSuperClass = "BaseService";
    private String baseServiceSuperClassName = "com.stip.mybatis.generator.plugin.BaseService";

    private ShellCallback shellCallback = null;

    private String serviceTargetDir;

    private String serviceTargetPackage;
    
    private String serviceClassName;
    
    public ServicePlugin() {
        shellCallback = new DefaultShellCallback(false);
    }

    /**
     * 验证传入的service包是否合法
     */
    public boolean validate(List<String> warnings) {
    	serviceTargetDir = properties.getProperty("serviceTargetDir");
        boolean valid = StringUtility.stringHasValue(serviceTargetDir);

        serviceTargetPackage = properties.getProperty("serviceTargetPackage");
        boolean valid2 = StringUtility.stringHasValue(serviceTargetPackage);

        return valid && valid2;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
    	// 初始化两参数为空
        serviceClassName = null;

        serviceClassName = introspectedTable.getBaseRecordType();
        String modelTargetPackage = properties.getProperty("modelTargetPackage");
        serviceClassName=introspectedTable.getBaseRecordType().replaceAll(modelTargetPackage, "");
        serviceClassName=serviceTargetPackage+serviceClassName+"Service";
        introspectedTable.setBaseServiceType(serviceClassName);
    }
    
    public boolean serviceClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        System.out.println("===============开始：修改service文件================");
        
        FullyQualifiedJavaType pkType = null;
		List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
		
		if (primaryKeyColumns.isEmpty()) {
			pkType = new FullyQualifiedJavaType("java.lang.String");
		} else {
			pkType = primaryKeyColumns.get(0).getFullyQualifiedJavaType();// TODO:默认不考虑联合主键的情况
		}
        
        // 添加基类
        FullyQualifiedJavaType superClazzType = new FullyQualifiedJavaType(baseServiceSuperClass);
        FullyQualifiedJavaType superClazzTypeName = new FullyQualifiedJavaType(baseServiceSuperClassName);
        topLevelClass.addImportedType(superClazzTypeName);
        
        FullyQualifiedJavaType subModelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        topLevelClass.addImportedType(subModelJavaType);
		FullyQualifiedJavaType subModelExampleJavaType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
		topLevelClass.addImportedType(subModelExampleJavaType);
        
        superClazzType.addTypeArgument(subModelJavaType);
        superClazzType.addTypeArgument(subModelExampleJavaType);
        superClazzType.addTypeArgument(pkType);

        topLevelClass.setSuperClass(superClazzType);
        
        topLevelClass.addImportedType("org.springframework.stereotype.Service");
        topLevelClass.addAnnotation("@Service");

        System.out.println("===============完成：修改service文件================");

        return super.serviceClassGenerated(topLevelClass, introspectedTable);
    }

}
