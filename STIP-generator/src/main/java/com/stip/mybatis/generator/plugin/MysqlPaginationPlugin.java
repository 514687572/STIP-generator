package com.stip.mybatis.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * Mysql 分页插件
 * 
 * @author chenjunan
 */
public class MysqlPaginationPlugin extends PluginAdapter {

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		XmlElement pageElement = new XmlElement("if");
		pageElement.addAttribute(new Attribute("test","fromRowNum != null and toRowNum >= 0"));
		pageElement.addElement(new TextElement("limit #{fromRowNum}, #{toRowNum}"));
		element.getElements().add(pageElement);

		return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element,introspectedTable);
	}

	public boolean validate(List<String> warnings) {

		return true;
	}

}