package com.stip.mybatis.generator.config;

import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.XMLParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 默认配置加载器
 */
public class DefaultConfigurationLoader {
    private static final String DEFAULT_CONFIG_FILE = "/generatorConfig.xml";
    private static final String USER_PROPERTIES_FILE = "/generator.properties";
    private static final String DEFAULT_PROPERTIES_FILE = "/generator.default.properties";

    /**
     * 加载配置
     * @return Configuration 合并后的配置对象
     * @throws Exception 配置加载异常
     */
    public static Configuration loadConfiguration() throws Exception {
        Properties properties = loadProperties();
        return parseConfiguration(properties);
    }

    /**
     * 加载属性配置
     * @return Properties 合并后的属性配置
     * @throws IOException IO异常
     */
    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        // 1. 加载默认配置
        try (InputStream defaultInput = DefaultConfigurationLoader.class.getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            if (defaultInput != null) {
                properties.load(defaultInput);
            }
        }

        // 2. 加载用户配置（覆盖默认配置）
        try (InputStream userInput = DefaultConfigurationLoader.class.getResourceAsStream(USER_PROPERTIES_FILE)) {
            if (userInput != null) {
                Properties userProperties = new Properties();
                userProperties.load(userInput);
                // 合并配置，用户配置优先
                properties.putAll(userProperties);
            }
        }

        // 3. 检查必填配置项
        validateRequiredProperties(properties);

        return properties;
    }

    /**
     * 解析配置
     * @param properties 属性配置
     * @return Configuration 配置对象
     * @throws XMLParserException XML解析异常
     */
    private static Configuration parseConfiguration(Properties properties) throws XMLParserException, IOException {
        List<String> warnings = new ArrayList<>();
        InputStream configStream = DefaultConfigurationLoader.class.getResourceAsStream(DEFAULT_CONFIG_FILE);
        if (configStream == null) {
            throw new RuntimeException("Cannot find default configuration file: " + DEFAULT_CONFIG_FILE);
        }

        ConfigurationParser parser = new ConfigurationParser(properties, warnings);
        return parser.parseConfiguration(configStream);
    }

    /**
     * 验证必填配置项
     * @param properties 属性配置
     * @throws IllegalArgumentException 配置验证异常
     */
    private static void validateRequiredProperties(Properties properties) {
        String[] requiredProps = {
            "jdbc.driverClassName",
            "jdbc.url",
            "jdbc.username",
            "jdbc.password",
            "tableName"
        };

        for (String prop : requiredProps) {
            if (!properties.containsKey(prop) || properties.getProperty(prop).trim().isEmpty()) {
                throw new IllegalArgumentException("Required property is missing: " + prop);
            }
        }
    }
} 