package com.stip.mybatis.generator.plugin;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;

/**
 *
 * generate ext XML sql
 * 20180529-10:08:52
 * chenjunan
 *
 */
public class ExtendXmlMapperPlugin extends PluginAdapter {
	public final static String DEFAULT_BASE_MODEL_PACKAGE = "";
    public final static String DEFAULT_BASE_MODEL_NAME_PREFIX = "";
	private final static String DEFAULT_EXT_XML_PACKAGE = "ext";

    private ShellCallback shellCallback = null;
    
    /**
     * 两个参数用于做数据中转，否则xml文件里会用Base类，这里是为了让xml文件用标准的Model类
     */
    private String modelClassName;
    
    private String exampleClassName;
    
    /**
     * 扩展xml文件包名
     */
    private String fullExtXmlPackage;
    
    /**
     * 利用java反射获取isMergeable参数，并修改
     */
    private java.lang.reflect.Field isMergeableFid = null;

	/**
     * Model类的前缀名称
     */
    private String baseModelNamePrefix;
    
    private String baseExamplePackageName=".example";
    
    private String baseModelPackageName = ".entity";
    
    private String extDaoTargetPackage;
    
    private String DaoTargetPackageName=".dao";
    
    private String extMapperPackageName=".ext";
    
    /**
	 * 
	 */
	public ExtendXmlMapperPlugin() {
		shellCallback = new DefaultShellCallback(false);
		
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
        String exampleTargetPackage = properties.getProperty("exampleTargetPackage");
        
        if (!StringUtility.stringHasValue(exampleTargetPackage)) {
        	exampleTargetPackage = properties.getProperty("targetPackage");
            if (StringUtility.stringHasValue(exampleTargetPackage)) {
            	exampleTargetPackage+=baseExamplePackageName;
            }
        }
        
        String modelTargetPackage = properties.getProperty("modelTargetPackage");
        
        if (!StringUtility.stringHasValue(modelTargetPackage)) {
        	modelTargetPackage = properties.getProperty("basePackage");
            if (!StringUtility.stringHasValue(modelTargetPackage)) {
            	modelTargetPackage+=baseModelPackageName;
            }
        }
        
    }
    
    public void addElements(XmlElement parentElement,IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("insert"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", "batchInsert")); //$NON-NLS-1$

        answer.addAttribute(new Attribute("parameterType","java.util.List"));

        context.getCommentGenerator().addComment(answer);

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                if (gk.isJdbcStandard()) {
                    answer.addAttribute(new Attribute("useGeneratedKeys", "true")); //$NON-NLS-1$ //$NON-NLS-2$
                    answer.addAttribute(new Attribute("keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
                    answer.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName())); //$NON-NLS-1$
                } else {
                    answer.addElement(getSelectKey(introspectedColumn, gk));
                }
            }
        }

        StringBuilder insertClause = new StringBuilder();

        insertClause.append("insert into "); //$NON-NLS-1$
        insertClause.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        insertClause.append(" ("); //$NON-NLS-1$

        StringBuilder valuesClause = new StringBuilder();
        List<String> valuesClauses = new ArrayList<String>();
        valuesClauses.add("<foreach item='item' collection='list' separator=','  index=''>"); //$NON-NLS-1$
        valuesClause.append(" ("); //$NON-NLS-1$

        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn introspectedColumn = columns.get(i);

            insertClause.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            valuesClause.append(getParameterClause(introspectedColumn,"item"));
            if (i + 1 < columns.size()) {
                insertClause.append(", "); //$NON-NLS-1$
                valuesClause.append(", "); //$NON-NLS-1$
                
                if (valuesClause.length() > 60) {
                	valuesClauses.add(valuesClause.toString());
                	valuesClause.setLength(0);
                	OutputUtilities.xmlIndent(valuesClause, 1);
                }
                
                if (insertClause.length() > 60) {
                	answer.addElement(new TextElement(insertClause.toString()));
                	insertClause.setLength(0);
                	OutputUtilities.xmlIndent(insertClause, 1);
                }
            }else {
            	valuesClause.append(") "); //$NON-NLS-1$
            	insertClause.append(") values "); //$NON-NLS-1$
            }

        }

        answer.addElement(new TextElement(insertClause.toString()));

        valuesClauses.add(valuesClause.toString());
        for (String clause : valuesClauses) {
    		answer.addElement(new TextElement(clause));
        }
        
        answer.addElement(new TextElement("</foreach>"));

        if (context.getPlugins().sqlMapInsertElementGenerated(answer,introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
    
    /**
     * Gets the parameter clause.
     *
     * @param introspectedColumn
     *            the introspected column
     * @return the parameter clause
     */
    public static String getParameterClause(IntrospectedColumn introspectedColumn,String alis) {
        return getParameterClause(introspectedColumn, null,alis);
    }

    /**
     * Gets the parameter clause.
     *
     * @param introspectedColumn
     *            the introspected column
     * @param prefix
     *            the prefix
     * @return the parameter clause
     */
    public static String getParameterClause(IntrospectedColumn introspectedColumn, String prefix,String alis) {
        StringBuilder sb = new StringBuilder();

        sb.append("#{"); //$NON-NLS-1$
        sb.append(alis);
        sb.append(".");
        sb.append(introspectedColumn.getJavaProperty(prefix));
        sb.append(",jdbcType="); //$NON-NLS-1$
        sb.append(introspectedColumn.getJdbcTypeName());

        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            sb.append(",typeHandler="); //$NON-NLS-1$
            sb.append(introspectedColumn.getTypeHandler());
        }

        sb.append('}');

        return sb.toString();
    }
    
    protected XmlElement getSelectKey(IntrospectedColumn introspectedColumn,GeneratedKey generatedKey) {
        String identityColumnType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();

        XmlElement answer = new XmlElement("selectKey"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("resultType", identityColumnType)); //$NON-NLS-1$
        answer.addAttribute(new Attribute("keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("order",generatedKey.getMyBatis3Order())); 
        answer.addElement(new TextElement(generatedKey.getRuntimeSqlStatement()));

        return answer;
    }

	/*
     * 生成新的xml文件 ,覆盖原来存在文件
     * 
     * @see
     * org.mybatis.generator.api.PluginAdapter#contextGenerateAdditionalXmlFiles
     * (org.mybatis.generator.api.IntrospectedTable)
     */
    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable introspectedTable) {
        if (modelClassName != null) {
            introspectedTable.setBaseRecordType(modelClassName);
        }

        if (exampleClassName != null) {
            introspectedTable.setExampleType(exampleClassName);
        }

        List<GeneratedXmlFile> extXmlFiles = new ArrayList<GeneratedXmlFile>(1);
        List<GeneratedXmlFile> xmlFiles = introspectedTable.getGeneratedXmlFiles();

        for (GeneratedXmlFile xmlFile : xmlFiles) {
            try {
                // 将xml的isMergeabl改为false
                isMergeableFid.set(xmlFile, false);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            Document document = new Document(XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID,XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);
            XmlElement root = new XmlElement("mapper");
            document.setRootElement(root);

            // 生成新的空的xml 但是不覆盖
            root.addAttribute(new Attribute("namespace", introspectedTable.getMyBatis3FallbackSqlMapNamespace()));
            root.addElement(new TextElement("<!--"));
            StringBuilder sb = new StringBuilder();
            sb.append("  文件的生成时间： ");
            sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            sb.append('.');
            root.addElement(new TextElement(sb.toString()));
            root.addElement(new TextElement("  你应该把Mapper类的扩展方法的sql语句放在这个文件里面"));

            root.addElement(new TextElement("-->"));
            root.addElement(new TextElement(""));// 添加空白行
            
            addElements(root,introspectedTable);

            String fileName = xmlFile.getFileName();
            String targetProject = xmlFile.getTargetProject();

            try {
                File directory = shellCallback.getDirectory(targetProject, fullExtXmlPackage);

                File targetFile = new File(directory, fileName);

                if (!targetFile.exists()) {// 需要判断这个xml文件是否存在，若存在则不生成
                    GeneratedXmlFile gxf = new GeneratedXmlFile(document, fileName, fullExtXmlPackage, targetProject,true, context.getXmlFormatter());
                    extXmlFiles.add(gxf);
                }
            } catch (ShellException e) {
                e.printStackTrace();
            }

            extXmlFiles.add(xmlFile);
        }

        return extXmlFiles;
    }

	/* (non-Javadoc)
	 * @see org.mybatis.generator.api.Plugin#validate(java.util.List)
	 */
	@Override
	public boolean validate(List<String> warnings) {
        baseModelNamePrefix = properties.getProperty("baseModelNamePrefix");
        if (!StringUtility.stringHasValue(baseModelNamePrefix)) {
            baseModelNamePrefix = DEFAULT_BASE_MODEL_NAME_PREFIX;
        }

        extDaoTargetPackage = properties.getProperty("daoTargetPackage");
        if (!StringUtility.stringHasValue(extDaoTargetPackage)) {
        	extDaoTargetPackage = properties.getProperty("targetPackage");
            if (!StringUtility.stringHasValue(extDaoTargetPackage)) {
                return false;
            }else {
            	extDaoTargetPackage=extDaoTargetPackage+DaoTargetPackageName+extMapperPackageName;
            }
        }
        
        fullExtXmlPackage=extDaoTargetPackage;

        return true;
	}
	
}
