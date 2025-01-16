package com.stip.mybatis.generator.plugin;

import java.util.Arrays;
import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * Lombok插件，用于生成Lombok注解
 */
public class LombokPlugin extends PluginAdapter {
    private boolean useLombokPlugin = true;
    private String lombokAnnotations = "Data,Builder,NoArgsConstructor,AllArgsConstructor,EqualsAndHashCode";

    @Override
    public boolean validate(List<String> warnings) {
        String useLombokStr = properties.getProperty("useLombokPlugin");
        if (StringUtility.stringHasValue(useLombokStr)) {
            useLombokPlugin = Boolean.parseBoolean(useLombokStr);
        }

        String annotations = properties.getProperty("lombokAnnotations");
        if (StringUtility.stringHasValue(annotations)) {
            lombokAnnotations = annotations;
        }

        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!useLombokPlugin) {
            return true;
        }

        // 添加Lombok注解
        Arrays.stream(lombokAnnotations.split(","))
              .map(String::trim)
              .forEach(annotation -> {
                  topLevelClass.addImportedType("lombok." + annotation);
                  topLevelClass.addAnnotation("@" + annotation);
              });

        return true;
    }
} 