package com.y5neko.shiroexp.config;

import javafx.scene.image.Image;

public class GlobalVariable {
    public static Image icon = new Image("img/icon.png");

    // 全局代理配置
    private static String globalProxy = null;

    /**
     * 获取全局代理
     * @return 代理地址，格式：ip:port
     */
    public static String getGlobalProxy() {
        return globalProxy;
    }

    /**
     * 设置全局代理
     * @param proxy 代理地址，格式：ip:port
     */
    public static void setGlobalProxy(String proxy) {
        globalProxy = proxy;
    }

    /**
     * 清除全局代理
     */
    public static void clearGlobalProxy() {
        globalProxy = null;
    }

    // DNSLog 域名配置
    private static String dnslogDomain = null;

    /**
     * 获取 DNSLog 域名
     * @return DNSLog 域名
     */
    public static String getDnslogDomain() {
        return dnslogDomain;
    }

    /**
     * 设置 DNSLog 域名
     * @param domain DNSLog 域名
     */
    public static void setDnslogDomain(String domain) {
        dnslogDomain = domain;
    }

    /**
     * 清除 DNSLog 域名
     */
    public static void clearDnslogDomain() {
        dnslogDomain = null;
    }
}
