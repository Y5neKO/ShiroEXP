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
     * 获取自定义探测类列表（不包含注释）
     * @return 自定义类名数组
     */
    public static String[] getCustomClasses() {
        String classesStr = props.getProperty("custom.classes");
        if (classesStr == null || classesStr.trim().isEmpty()) {
            return new String[0];
        }

        // 使用 Pattern.quote() 对分隔符进行转义，避免被当作正则表达式
        String delimiter = "|||";
        String[] lines = classesStr.split(java.util.regex.Pattern.quote(delimiter));
        java.util.List<String> classes = new java.util.ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            // 跳过空行和注释行
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                classes.add(trimmed);
            }
        }

        return classes.toArray(new String[0]);
    }

    /**
     * 获取自定义探测类的原始内容（包含注释）
     * 用于在UI中显示完整内容
     * @return 原始内容（多行字符串）
     */
    public static String getCustomClassesRaw() {
        String classesStr = props.getProperty("custom.classes");
        if (classesStr == null || classesStr.trim().isEmpty()) {
            return "";
        }

        // 将分隔符替换为换行符（使用 literal 替换，不需要转义）
        String delimiter = "|||";
        StringBuilder result = new StringBuilder();
        String[] parts = classesStr.split(java.util.regex.Pattern.quote(delimiter));
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                result.append("\n");
            }
            result.append(parts[i]);
        }
        return result.toString();
    }

    /**
     * 设置自定义探测类列表（支持注释）
     * @param content 多行内容（包含注释和类名）
     */
    public static void setCustomClasses(String content) {
        if (content == null || content.trim().isEmpty()) {
            props.remove("custom.classes");
        } else {
            // 将换行符替换为特殊的分隔符（三个竖线）
            String escapedContent = content.replace("\n", "|||");
            props.setProperty("custom.classes", escapedContent);
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

        // 获取现有内容
        StringBuilder content = new StringBuilder();
        String existingContent = getCustomClassesRaw();
        if (!existingContent.isEmpty()) {
            content.append(existingContent);
            if (!existingContent.endsWith("\n")) {
                content.append("\n");
            }
        }

        // 添加新类（自动去重由UI层处理）
        content.append(className.trim());

        // 保存
        setCustomClasses(content.toString());
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
