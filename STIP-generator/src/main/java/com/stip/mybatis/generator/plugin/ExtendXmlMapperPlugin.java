package com.stip.mybatis.generator.plugin;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.*;
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
    
    private String mapperClassName;
    
    private Set<String> generatedMethods = new HashSet<>();
    
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
        generatedMethods.clear();
        super.initialized(introspectedTable);
        
        // 初始化两参数为空
        modelClassName = null;
        mapperClassName=null;
        
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
        
        extDaoTargetPackage = properties.getProperty("daoTargetPackage");
        if (!StringUtility.stringHasValue(extDaoTargetPackage)) {
        	extDaoTargetPackage = properties.getProperty("targetPackage");
            if (StringUtility.stringHasValue(extDaoTargetPackage)) {
            	extDaoTargetPackage=extDaoTargetPackage+DaoTargetPackageName+extMapperPackageName;
            }
        }else {
        	extDaoTargetPackage=extDaoTargetPackage+DaoTargetPackageName+extMapperPackageName;
        }
        
        fullExtXmlPackage=extDaoTargetPackage;
        
        FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        mapperClassName = modelJavaType.getShortName();
        mapperClassName=extDaoTargetPackage+"."+mapperClassName+"Dao";
        introspectedTable.setMyBatis3JavaMapperType(mapperClassName);
        
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

        processColumns(introspectedTable, answer);

        if (context.getPlugins().sqlMapInsertElementGenerated(answer,introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
    
    private void processColumns(IntrospectedTable introspectedTable, XmlElement answer) {
        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();
        List<String> valuesClauses = new ArrayList<>();
        
        // 获取所有列,包括BLOB列
        List<IntrospectedColumn> allColumns = introspectedTable.getAllColumns();
        
        // 过滤掉不需要的列
        List<IntrospectedColumn> columns = allColumns.stream()
            .filter(col -> !col.isIdentity()) // 过滤自增列
            .filter(col -> !col.isGeneratedAlways()) // 过滤生成列
            .collect(Collectors.toList());
            
        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn column = columns.get(i);
            
            // 处理列名,进行转义
            String escapedColumnName = MyBatis3FormattingUtilities.getEscapedColumnName(column);
            insertClause.append(escapedColumnName);
            
            // 处理参数,根据JDBC类型生成
            String parameterClause = getParameterClause(column, "item");
            valuesClause.append(parameterClause);
            
            // 处理分隔符
            if (i + 1 < columns.size()) {
                insertClause.append(", ");
                valuesClause.append(", ");
                
                // 处理长度超过限制的情况
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
            }
        }
        
        // 添加最后的列
        if (insertClause.length() > 0) {
            answer.addElement(new TextElement(insertClause.toString()));
        }
        
        // 添加最后的值
        if (valuesClause.length() > 0) {
            valuesClauses.add(valuesClause.toString());
        }
        
        // 生成values子句
        for (String clause : valuesClauses) {
            answer.addElement(new TextElement(clause));
        }
    }

    private String getParameterClause(IntrospectedColumn column, String prefix) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("#{");
        sb.append(prefix);
        sb.append('.');
        sb.append(column.getJavaProperty());
        
        // 添加JDBC类型
        if (StringUtility.stringHasValue(column.getTypeHandler())) {
            sb.append(",typeHandler=");
            sb.append(column.getTypeHandler());
        } else {
            sb.append(",jdbcType=");
            sb.append(column.getJdbcTypeName());
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
        
        String edtp = properties.getProperty("daoTargetPackage");
        if (!StringUtility.stringHasValue(edtp)) {
        	edtp = properties.getProperty("targetPackage");
            if (StringUtility.stringHasValue(edtp)) {
            	edtp=edtp+DaoTargetPackageName;
            }
        }else {
        	edtp=edtp+DaoTargetPackageName;
        }
        
        FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        mapperClassName = modelJavaType.getShortName();
        mapperClassName=edtp+"."+mapperClassName+"Dao";
        introspectedTable.setMyBatis3JavaMapperType(mapperClassName);
        introspectedTable.setMyBatis3FallbackSqlMapNamespace(mapperClassName);

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
            root.addAttribute(new Attribute("namespace", mapperClassName));
            root.addElement(new TextElement("<!--"));
            StringBuilder sb = new StringBuilder();
            sb.append("  文件的生成时间： ");
            sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            sb.append('.');
            root.addElement(new TextElement(sb.toString()));
            root.addElement(new TextElement("  自定义sql写在这个文件中  "));

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
        
        return true;
	}
	
	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
	    // 添加联合查询支持
	    XmlElement joinElement = new XmlElement("if");
	    joinElement.addAttribute(new Attribute("test", "joins != null and joins.size() > 0"));
	    
	    StringBuilder joinSql = new StringBuilder();
	    joinSql.append("\n");
	    joinSql.append("      <foreach collection=\"joins\" item=\"join\" separator=\" \">\n");
	    joinSql.append("        ${join.toSql}\n");
	    joinSql.append("      </foreach>\n");
	    
	    joinElement.addElement(new TextElement(joinSql.toString()));
	    
	    // 在 FROM 子句后面插入联合查询语句
	    List<Element> elements = element.getElements();
	    for (int i = 0; i < elements.size(); i++) {
	        Element e = elements.get(i);
	        if (e instanceof TextElement) {
	            String content = ((TextElement) e).getContent();
	            if (content.contains("from")) {
	                elements.add(i + 1, joinElement);
	                break;
	            }
	        }
	    }
	    
	    return true;
	}

	@Override
	public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
	    // 添加联合查询支持
	    XmlElement joinElement = new XmlElement("if");
	    joinElement.addAttribute(new Attribute("test", "joins != null and joins.size() > 0"));
	    
	    StringBuilder joinSql = new StringBuilder();
	    joinSql.append("\n");
	    joinSql.append("      <foreach collection=\"joins\" item=\"join\" separator=\" \">\n");
	    joinSql.append("        ${join.toSql}\n");
	    joinSql.append("      </foreach>\n");
	    
	    joinElement.addElement(new TextElement(joinSql.toString()));
	    
	    // 在 FROM 子句后面插入联合查询语句
	    List<Element> elements = element.getElements();
	    for (int i = 0; i < elements.size(); i++) {
	        Element e = elements.get(i);
	        if (e instanceof TextElement) {
	            String content = ((TextElement) e).getContent();
	            if (content.contains("from")) {
	                elements.add(i + 1, joinElement);
	                break;
	            }
	        }
	    }
	    
	    return true;
	}

	@Override
	public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        String methodName = element.getAttributes().stream()
            .filter(attr -> "id".equals(attr.getName()))
            .findFirst()
            .map(Attribute::getValue)
            .orElse("");
            
        if (generatedMethods.contains(methodName)) {
            return false; // 跳过重复生成
        }
        
        generatedMethods.add(methodName);
        return true;
    }

    private void generateXmlStructure(XmlElement root, IntrospectedTable introspectedTable) {
        // 1. 添加文件头注释
        addFileHeader(root);
        
        // 2. 添加缓存配置
        addCacheConfig(root, introspectedTable);
        
        // 3. 添加公共SQL片段
        addCommonSqlSnippets(root, introspectedTable);
        
        // 4. 添加基础结果映射
        addResultMaps(root, introspectedTable);
        
        // 5. 添加自定义SQL占位
        addCustomSqlPlaceholder(root);
    }
    
    private void addFileHeader(XmlElement root) {
        root.addElement(new TextElement("<!--"));
        root.addElement(new TextElement("  This file was generated by STIP MyBatis Generator."));
        root.addElement(new TextElement("  Generated time: " + 
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        root.addElement(new TextElement("  Do not modify this file manually."));
        root.addElement(new TextElement("-->"));
        root.addElement(new TextElement(""));
    }
    
    private void addCacheConfig(XmlElement root, IntrospectedTable introspectedTable) {
        if (introspectedTable.hasBLOBColumns()) {
            XmlElement cache = new XmlElement("cache");
            cache.addAttribute(new Attribute("type", "org.mybatis.caches.ehcache.EhcacheCache"));
            cache.addAttribute(new Attribute("eviction", "LRU"));
            cache.addAttribute(new Attribute("flushInterval", "60000"));
            cache.addAttribute(new Attribute("size", "512"));
            cache.addAttribute(new Attribute("readOnly", "true"));
            root.addElement(cache);
        }
    }
    
    private void addCommonSqlSnippets(XmlElement root, IntrospectedTable introspectedTable) {
        // Base_Column_List
        XmlElement baseColumnList = new XmlElement("sql");
        baseColumnList.addAttribute(new Attribute("id", "Base_Column_List"));
        baseColumnList.addElement(new TextElement(getBaseColumnListText(introspectedTable)));
        root.addElement(baseColumnList);
        
        // Example_Where_Clause
        XmlElement whereClause = new XmlElement("sql");
        whereClause.addAttribute(new Attribute("id", "Example_Where_Clause"));
        whereClause.addElement(getExampleWhereClauseText());
        root.addElement(whereClause);
    }
    
    private void addResultMaps(XmlElement root, IntrospectedTable introspectedTable) {
        // BaseResultMap
        XmlElement resultMap = new XmlElement("resultMap");
        resultMap.addAttribute(new Attribute("id", "BaseResultMap"));
        resultMap.addAttribute(new Attribute("type", introspectedTable.getBaseRecordType()));
        addResultMapColumns(resultMap, introspectedTable);
        root.addElement(resultMap);
    }
    
    private void addCustomSqlPlaceholder(XmlElement root) {
        root.addElement(new TextElement(""));
        root.addElement(new TextElement("  <!-- 自定义SQL请写在这里 -->"));
        root.addElement(new TextElement(""));
    }

    private String getBaseColumnListText(IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        // 获取非BLOB列
        List<IntrospectedColumn> columns = introspectedTable.getNonBLOBColumns();
        
        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn column = columns.get(i);
            sb.append(column.getActualColumnName());
            if (i + 1 < columns.size()) {
                sb.append(", ");
                // 每3个字段换一行，提高可读性
                if ((i + 1) % 3 == 0) {
                    sb.append("\n        ");
                }
            }
        }
        return sb.toString();
    }

    private XmlElement getExampleWhereClauseText() {
        XmlElement whereElement = new XmlElement("where");
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "oredCriteria"));
        foreachElement.addAttribute(new Attribute("item", "criteria"));
        foreachElement.addAttribute(new Attribute("separator", "or"));
        
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "criteria.valid"));
        
        XmlElement trimElement = new XmlElement("trim");
        trimElement.addAttribute(new Attribute("prefix", "("));
        trimElement.addAttribute(new Attribute("suffix", ")"));
        trimElement.addAttribute(new Attribute("prefixOverrides", "and"));
        
        XmlElement foreachCondition = new XmlElement("foreach");
        foreachCondition.addAttribute(new Attribute("collection", "criteria.criteria"));
        foreachCondition.addAttribute(new Attribute("item", "criterion"));
        
        XmlElement chooseElement = new XmlElement("choose");
        XmlElement when1 = new XmlElement("when");
        when1.addAttribute(new Attribute("test", "criterion.noValue"));
        when1.addElement(new TextElement("and ${criterion.condition}"));
        
        XmlElement when2 = new XmlElement("when");
        when2.addAttribute(new Attribute("test", "criterion.singleValue"));
        when2.addElement(new TextElement("and ${criterion.condition} #{criterion.value}"));
        
        XmlElement when3 = new XmlElement("when");
        when3.addAttribute(new Attribute("test", "criterion.betweenValue"));
        when3.addElement(new TextElement("and ${criterion.condition} #{criterion.value} and #{criterion.secondValue}"));
        
        XmlElement when4 = new XmlElement("when");
        when4.addAttribute(new Attribute("test", "criterion.listValue"));
        when4.addElement(new TextElement("and ${criterion.condition}"));
        
        XmlElement foreachList = new XmlElement("foreach");
        foreachList.addAttribute(new Attribute("collection", "criterion.value"));
        foreachList.addAttribute(new Attribute("item", "listItem"));
        foreachList.addAttribute(new Attribute("open", "("));
        foreachList.addAttribute(new Attribute("close", ")"));
        foreachList.addAttribute(new Attribute("separator", ","));
        foreachList.addElement(new TextElement("#{listItem}"));
        
        when4.addElement(foreachList);
        chooseElement.addElement(when1);
        chooseElement.addElement(when2);
        chooseElement.addElement(when3);
        chooseElement.addElement(when4);
        foreachCondition.addElement(chooseElement);
        trimElement.addElement(foreachCondition);
        ifElement.addElement(trimElement);
        foreachElement.addElement(ifElement);
        whereElement.addElement(foreachElement);
        
        return whereElement;
    }

    private void addResultMapColumns(XmlElement resultMap, IntrospectedTable introspectedTable) {
        // 添加主键映射
        for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
            XmlElement id = new XmlElement("id");
            id.addAttribute(new Attribute("column", column.getActualColumnName()));
            id.addAttribute(new Attribute("property", column.getJavaProperty()));
            id.addAttribute(new Attribute("jdbcType", column.getJdbcTypeName()));
            resultMap.addElement(id);
        }
        
        // 添加非主键列映射
        for (IntrospectedColumn column : introspectedTable.getNonPrimaryKeyColumns()) {
            XmlElement result = new XmlElement("result");
            result.addAttribute(new Attribute("column", column.getActualColumnName()));
            result.addAttribute(new Attribute("property", column.getJavaProperty()));
            result.addAttribute(new Attribute("jdbcType", column.getJdbcTypeName()));
            
            // 如果有TypeHandler，添加typeHandler属性
            if (StringUtility.stringHasValue(column.getTypeHandler())) {
                result.addAttribute(new Attribute("typeHandler", column.getTypeHandler()));
            }
            
            resultMap.addElement(result);
        }
    }

    private void addJoinSupport(XmlElement element) {
        XmlElement joinElement = new XmlElement("if");
        joinElement.addAttribute(new Attribute("test", "joins != null and joins.size() > 0"));
        
        StringBuilder joinSql = new StringBuilder();
        joinSql.append("\n");
        joinSql.append("      <foreach collection=\"joins\" item=\"join\" separator=\" \">\n");
        joinSql.append("        ${join.toSql}\n");
        joinSql.append("      </foreach>\n");
        
        // 添加联表字段选择
        StringBuilder selectColumns = new StringBuilder();
        selectColumns.append("      <if test=\"join.columns != null and join.columns.size() > 0\">\n");
        selectColumns.append("        ,${join.getSelectColumns}\n");
        selectColumns.append("      </if>\n");
        
        joinElement.addElement(new TextElement(joinSql.toString()));
        joinElement.addElement(new TextElement(selectColumns.toString()));
        
        insertJoinElement(element, joinElement);
    }

    private void insertJoinElement(XmlElement parentElement, XmlElement joinElement) {
        // 在 FROM 子句后面插入联合查询语句
        List<Element> elements = parentElement.getElements();
        boolean inserted = false;
        
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element instanceof TextElement) {
                String content = ((TextElement) element).getContent().toLowerCase();
                if (content.contains("from")) {
                    // 在FROM后的下一个元素位置插入
                    elements.add(i + 1, joinElement);
                    inserted = true;
                    break;
                }
            }
        }
        
        // 如果没找到合适的位置，就添加到最后
        if (!inserted) {
            elements.add(joinElement);
        }
    }
}
