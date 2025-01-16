package com.stip.mybatis.generator.formatter;

import org.mybatis.generator.api.XmlFormatter;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.config.Context;

public class CustomXmlFormatter implements XmlFormatter {
    
    private Context context;
    
    @Override
    public void setContext(Context context) {
        this.context = context;
    }
    
    @Override
    public String getFormattedContent(Document document) {
        StringBuilder sb = new StringBuilder();
        
        // XML声明必须在最开始，不能有任何空格或换行
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("\n<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" ");
        sb.append("\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
        
        // 格式化mapper内容
        String content = document.getFormattedContent();
        content = formatMapperContent(content);
        sb.append(content);
        
        return sb.toString();
    }
    
    private String formatMapperContent(String content) {
        // 移除可能存在的XML声明
        content = content.replaceAll("<\\?xml.*\\?>\\s*", "");
        // 移除可能存在的DOCTYPE声明
        content = content.replaceAll("<!DOCTYPE.*>\\s*", "");
        
        // 缩进处理
        content = content.replaceAll("(?m)^", "  ");
        // 处理空行
        content = content.replaceAll("(?m)^\\s+$", "");
        // 处理连续空行
        content = content.replaceAll("\\n{3,}", "\n\n");
        
        return content;
    }
} 