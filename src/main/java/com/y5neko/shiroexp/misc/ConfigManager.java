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
}
