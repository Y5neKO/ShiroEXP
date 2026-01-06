package com.y5neko.shiroexp.misc;

import com.y5neko.shiroexp.config.GlobalVariable;

import java.io.*;
import java.util.Properties;

/**
 * 配置管理工具类
 * 负责 DNSLog 域名配置的持久化存储和加载
 */
public class ConfigManager {
    private static final String CONFIG_FILE = "config/config.properties";
    private static Properties props = new Properties();

    /**
     * 加载配置文件
     * 从 config/config.properties 读取配置并同步到 GlobalVariable
     */
    public static void loadConfig() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                FileInputStream fis = new FileInputStream(configFile);
                props.load(new InputStreamReader(fis, "UTF-8"));
                fis.close();

                // 同步 DNSLog 域名配置到 GlobalVariable
                String dnslogDomain = props.getProperty("dnslog.domain");
                if (dnslogDomain != null && !dnslogDomain.isEmpty()) {
                    GlobalVariable.setDnslogDomain(dnslogDomain);
                }
            }
        } catch (IOException e) {
            // 配置文件不存在或读取失败时使用默认值
            System.err.println("加载配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 保存配置到文件
     * 将当前配置写入 config/config.properties
     */
    public static void saveConfig() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            }

            // 保存配置
            FileOutputStream fos = new FileOutputStream(configFile);
            props.store(new OutputStreamWriter(fos, "UTF-8"), "ShiroEXP Configuration");
            fos.close();
        } catch (IOException e) {
            System.err.println("保存配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取 DNSLog 域名配置
     * @return DNSLog 域名，默认返回 dnslog.cn
     */
    public static String getDnslogDomain() {
        String domain = props.getProperty("dnslog.domain");
        if (domain == null || domain.isEmpty()) {
            domain = GlobalVariable.getDnslogDomain();
        }
        return domain != null && !domain.isEmpty() ? domain : "dnslog.cn";
    }

    /**
     * 设置 DNSLog 域名配置
     * @param domain DNSLog 域名
     */
    public static void setDnslogDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return;
        }

        // 保存到 Properties
        props.setProperty("dnslog.domain", domain.trim());

        // 同步到 GlobalVariable
        GlobalVariable.setDnslogDomain(domain.trim());

        // 持久化到文件
        saveConfig();
    }

    /**
     * 清除 DNSLog 域名配置
     */
    public static void clearDnslogDomain() {
        props.remove("dnslog.domain");
        GlobalVariable.clearDnslogDomain();
        saveConfig();
    }

    // =========================== 自定义探测类配置 ===========================

    /**
     * 获取自定义探测类列表
     * @return 自定义类名数组
     */
    public static String[] getCustomClasses() {
        String classesStr = props.getProperty("custom.classes");
        if (classesStr == null || classesStr.trim().isEmpty()) {
            return new String[0];
        }

        // 按逗号分割并去除空白
        String[] classes = classesStr.split(",");
        for (int i = 0; i < classes.length; i++) {
            classes[i] = classes[i].trim();
        }

        return classes;
    }

    /**
     * 设置自定义探测类列表
     * @param classes 类名数组
     */
    public static void setCustomClasses(String[] classes) {
        if (classes == null || classes.length == 0) {
            props.remove("custom.classes");
        } else {
            // 去重并过滤空值
            java.util.Set<String> uniqueClasses = new java.util.LinkedHashSet<>();
            for (String cls : classes) {
                if (cls != null && !cls.trim().isEmpty()) {
                    uniqueClasses.add(cls.trim());
                }
            }

            // 保存为逗号分隔的字符串
            String classesStr = String.join(",", uniqueClasses);
            props.setProperty("custom.classes", classesStr);
        }

        // 持久化到文件
        saveConfig();
    }

    /**
     * 添加单个自定义类
     * @param className 类名
     */
    public static void addCustomClass(String className) {
        if (className == null || className.trim().isEmpty()) {
            return;
        }

        // 获取现有类列表
        String[] existingClasses = getCustomClasses();
        java.util.Set<String> classSet = new java.util.LinkedHashSet<>();
        classSet.addAll(java.util.Arrays.asList(existingClasses));

        // 添加新类（自动去重）
        classSet.add(className.trim());

        // 保存
        setCustomClasses(classSet.toArray(new String[0]));
    }

    /**
     * 清除所有自定义探测类配置
     */
    public static void clearCustomClasses() {
        props.remove("custom.classes");
        saveConfig();
    }

    /**
     * 加载配置文件（扩展版 - 同步自定义类配置）
     * 从 config/config.properties 读取配置并同步到 GlobalVariable
     */
    public static void loadConfigExtended() {
        // 先执行原有加载逻辑
        loadConfig();

        // 自定义类配置已在 getCustomClasses() 中实时读取，无需额外同步
    }
}
