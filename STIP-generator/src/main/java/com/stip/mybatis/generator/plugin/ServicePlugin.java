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
 * @author chenjunan
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
        // 初始化参数
        serviceClassName = null;

        // 获取实体类名和包名
        FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String shortName = modelJavaType.getShortName();
        String basePackage = modelJavaType.getPackageName();
        
        // 如果包名包含.entity，则去掉entity及之后的部分
        if (basePackage.contains(".entity")) {
            basePackage = basePackage.substring(0, basePackage.lastIndexOf(".entity"));
        }
        
        // 设置service实现类包名
        serviceTargetPackage = basePackage + ".service.impl";
        
        // 设置service接口包名
        serviceInterFacePackage = basePackage + ".service";
        
        // 设置完整的类名
        modelClassName = shortName;
        superServiceUrl = serviceInterFacePackage + "." + modelClassName + "Service";
        serviceClassName = serviceTargetPackage + "." + modelClassName + "ServiceImpl";
        
        // 设置实现类的完整限定名
        introspectedTable.setBaseServiceType(serviceClassName);
    }
    
    @Override
    public boolean serviceClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 清除默认生成的内容
        topLevelClass.getMethods().clear();
        topLevelClass.getFields().clear();
        
        // 添加类注释
        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine(" * Service Implementation for " + introspectedTable.getFullyQualifiedTable().getDomainObjectName());
        topLevelClass.addJavaDocLine(" *");
        topLevelClass.addJavaDocLine(" * @author STIP Generator");
        topLevelClass.addJavaDocLine(" */");
        
        // 添加必要的导入
        topLevelClass.addImportedType("org.springframework.stereotype.Service");
        topLevelClass.addImportedType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        topLevelClass.addImportedType(new FullyQualifiedJavaType(baseServiceSuperClassName));
        topLevelClass.addImportedType(superServiceUrl);
        topLevelClass.addImportedType(new FullyQualifiedJavaType("com.stip.mybatis.generator.plugin.BaseExample"));
        
        // 获取主键类型
        FullyQualifiedJavaType pkType;
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        if (primaryKeyColumns.isEmpty()) {
            pkType = new FullyQualifiedJavaType("java.lang.String");
        } else {
            pkType = primaryKeyColumns.get(0).getFullyQualifiedJavaType();
        }
        
        // 设置父类
        FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(
            String.format("BaseService<%s, %s, %s>",
                introspectedTable.getFullyQualifiedTable().getDomainObjectName(),
                "BaseExample",
                pkType.getShortName()
            )
        );
        topLevelClass.setSuperClass(superClass);
        
        // 添加接口实现
        FullyQualifiedJavaType interfaceType = new FullyQualifiedJavaType(superServiceUrl);
        topLevelClass.addSuperInterface(interfaceType);
        
        // 添加Service注解
        topLevelClass.addAnnotation("@Service");
        
        return true;
    }

}
