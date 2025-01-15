package com.stip.mybatis.generator;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.internal.DefaultShellCallback;

import com.stip.mybatis.generator.config.DefaultConfigurationLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * STIP代码生成器主类
 */
public class StipGenerator {
    
    /**
     * 生成代码
     * @throws Exception 生成异常
     */
    public void generate() throws Exception {
        List<String> warnings = new ArrayList<>();
        Configuration config = DefaultConfigurationLoader.loadConfiguration();
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator generator = new MyBatisGenerator(config, callback, warnings);
        generator.generate(null);
        
        // 输出警告信息
        if (!warnings.isEmpty()) {
            System.out.println("\nWarnings:");
            warnings.forEach(System.out::println);
        }
    }

    /**
     * 主方法，用于命令行方式运行生成器
     */
    public static void main(String[] args) {
        try {
            System.out.println("Starting STIP MyBatis Generator...");
            new StipGenerator().generate();
            System.out.println("Code generation completed successfully!");
        } catch (Exception e) {
            System.err.println("Error generating code: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 