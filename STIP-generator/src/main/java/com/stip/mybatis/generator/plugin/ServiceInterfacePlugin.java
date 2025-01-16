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
        // 初始化参数
        serviceInterfaceName = null;

        // 获取实体类名和包名
        FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String shortName = modelJavaType.getShortName();
        String basePackage = modelJavaType.getPackageName();
        
        // 如果包名包含.entity，则去掉entity及之后的部分
        if (basePackage.contains(".entity")) {
            basePackage = basePackage.substring(0, basePackage.lastIndexOf(".entity"));
        }
        
        // 设置service接口包名
        serviceInterfaceTargetPackage = basePackage + ".service";
        
        // 设置完整的接口名
        serviceInterfaceName = shortName + "Service";
        
        // 设置接口的完整限定名
        String fullInterfaceName = serviceInterfaceTargetPackage + "." + serviceInterfaceName;
        introspectedTable.setBaseInterfaceType(fullInterfaceName);
    }
    
    @Override
    public boolean serviceInterfaceGenerated(Interface serviceInterface, IntrospectedTable introspectedTable) {
        // 清除默认生成的内容
        serviceInterface.getMethods().clear();
        serviceInterface.getAnnotations().clear();
        
        // 获取实体类名
        String domainObjectName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();

        // 添加类注释
        serviceInterface.addJavaDocLine("/**");
        serviceInterface.addJavaDocLine(" * Service Interface for " + domainObjectName);
        serviceInterface.addJavaDocLine(" *");
        serviceInterface.addJavaDocLine(" * @author STIP Generator");
        serviceInterface.addJavaDocLine(" */");

        // 添加必要的导入
        serviceInterface.addImportedType(new FullyQualifiedJavaType("com.stip.mybatis.generator.plugin.IService"));
        serviceInterface.addImportedType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        serviceInterface.addImportedType(new FullyQualifiedJavaType("com.stip.mybatis.generator.plugin.BaseExample"));
        
        // 获取主键类型
        FullyQualifiedJavaType pkType;
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        if (primaryKeyColumns.isEmpty()) {
            pkType = new FullyQualifiedJavaType("java.lang.String");
        } else {
            pkType = primaryKeyColumns.get(0).getFullyQualifiedJavaType();
        }
        
        // 构建泛型接口
        FullyQualifiedJavaType superInterface = new FullyQualifiedJavaType(
            String.format("IService<%s, %s, %s>",
                domainObjectName,
                "BaseExample",
                pkType.getShortName()
            )
        );
        
        serviceInterface.addSuperInterface(superInterface);
        
        return true;
    }

}
