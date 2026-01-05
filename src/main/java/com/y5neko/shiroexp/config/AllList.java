package com.y5neko.shiroexp.config;

public class AllList {

    /**
     * 利用链列表
     * 排序原则：
     * 1. CommonsCollections (CC) 系列优先 - Shiro 默认自带 commons-collections，兼容性最好
     * 2. CommonsBeanutils (CB) 系列在后 - 需要额外依赖 commons-beanutils
     * 3. 同类内部按经典程度和稳定性排序
     */
    public static String[] gadgets = {
        // ========== CommonsCollections 系列 (CC) ==========
        "CommonsCollections1",     // CC1 - 最经典的利用链
        "CommonsCollections2",     // CC2 - 基于 TemplatesImpl
        "CommonsCollections3",     // CC3 - 基于 TrAXFilter
        "CommonsCollectionsK1",    // CCK1 - CC1 的 commons-collections4 版本
        "CommonsCollectionsK2",    // CCK2 - CC2 的 commons-collections4 版本

        // ========== CommonsBeanutils 系列 (CB) ==========
        "CommonsBeanutils1",                           // CB1 - 最基础的 Beanutils 利用链
        "CommonsBeanutils1_183",                       // CB1 适配 commons-beanutils 1.8.3
        "CommonsBeanutilsString",                      // 基于 String 的利用链
        "CommonsBeanutilsString_183",                  // String 版本适配 1.8.3
        "CommonsBeanutilsString_192s",                 // String 版本适配 Spring 5.2.x
        "CommonsBeanutilsAttrCompare",                 // 基于 AttributeCompare
        "CommonsBeanutilsAttrCompare_183",             // AttributeCompare 适配 1.8.3
        "CommonsBeanutilsPropertySource",              // 基于 PropertySource
        "CommonsBeanutilsPropertySource_183",          // PropertySource 适配 1.8.3
        "CommonsBeanutilsObjectToStringComparator",    // 基于 ObjectToStringComparator
        "CommonsBeanutilsObjectToStringComparator_183" // ObjectToStringComparator 适配 1.8.3
    };

    /**
     * 回显方式列表
     * 排序原则：
     * 1. 通用型回显优先 - AllEcho 兼容性最好
     * 2. 容器特定回显 - Tomcat/Spring 针对性强
     * 3. 探测型回显 - DNSLog 用于漏洞检测，非命令回显
     */
    public static String[] echoGadgets = {
        "AllEcho",            // 通用型回显 - 基于 DFS 内存搜索，兼容性最好
        "TomcatEcho",         // Tomcat 容器专用 - 性能更好
        "SpringEcho",         // Spring 框架专用 - 性能更好
        "DNSLogEcho",         // DNSLog 探测 - 用于漏洞验证（通用版）
        "DNSLogEchoSpring"    // DNSLog 探测 - 用于漏洞验证（Spring版）
    };

    /**
     * 内存马类型列表
     * 排序原则：
     * 1. 按客户端工具分组 - AntSword/Behinder/Godzilla/Suo5
     * 2. Filter 优先于 Servlet - Filter 拦截范围更广，更隐蔽
     */
    public static String[] memTypes = {
        // ========== AntSword (中国蚁剑) ==========
        "AntSwordFilter",     // Filter 型 - 拦截所有请求，更隐蔽
        "AntSwordServlet",    // Servlet 型 - 固定路径访问

        // ========== Behinder (冰蝎) ==========
        "BehinderFilter",     // Filter 型 - 拦截所有请求，更隐蔽
        "BehinderServlet",    // Servlet 型 - 固定路径访问

        // ========== Godzilla (哥斯拉) ==========
        "GodzillaFilter",     // Filter 型 - 拦截所有请求，更隐蔽
        "GodzillaServlet",    // Servlet 型 - 固定路径访问

        // ========== Suo5 (全双工 TCP/HTTP 反向代理) ==========
        "Suo5Filter",         // Filter 型 - 拦截指定路径请求，支持全双工代理、SSL 绕过
        "Suo5Servlet",        // Servlet 型 - 固定路径访问，支持全双工代理、SSL 绕过
        "Suo5Listener",       // Listener 型 - 全局监听所有请求，支持全双工代理、SSL 绕过

        // ========== 自定义内存马 ==========
        "自定义内存马"        // 用户自定义 Base64 字节码
    };

    /**
     * 内存马备注信息列表
     * 说明各内存马需要使用的参数，其他参数为工具内部传递使用
     */
    public static String[] memRemarks = {
        // AntSword (中国蚁剑)
        "使用 path、password",
        "使用 path、password",

        // Behinder (冰蝎)
        "使用 path、password",
        "使用 path、password",

        // Godzilla (哥斯拉)
        "使用 path、password",
        "使用 path、password",

        // Suo5 (全双工 TCP/HTTP 反向代理)
        "使用 path、Header",
        "使用 path、Header",
        "使用 Header",

        // 自定义内存马
        "请自行验证"
    };
}
