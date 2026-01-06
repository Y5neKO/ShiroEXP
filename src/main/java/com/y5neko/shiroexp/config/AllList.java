package com.y5neko.shiroexp.config;

public class AllList {

    /**
     * 利用链列表
     * 1. CommonsCollections (CC) 系列
     * 2. CommonsBeanutils (CB) 系列
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
        "DNSLogEcho"          // DNSLog 探测 - 用于漏洞验证（通用，无依赖）
    };

    /**
     * URLDNS 类探测列表
     * 用于探测目标是否存在指定依赖类
     *
     * 注意：不能使用 java.lang 等受保护包名的类
     */
    public static String[] urlDnsClasses = {
        // ========== CommonsCollections 系列 ==========
        "org.apache.commons.collections.map.LazyMap",                     // CC1/CC3/CC6 核心类
        "org.apache.commons.collections.Transformer",                     // CC 系列接口
        "org.apache.commons.collections.functors.InvokerTransformer",     // CC1/CC3 核心利用类
        "org.apache.commons.collections4.map.LazyMap",                    // CC4/CC7 核心类
        "org.apache.commons.collections4.Transformer",                    // CC4 系列接口

        // ========== CommonsBeanutils 系列 ==========
        "org.apache.commons.beanutils.BeanComparator",                    // CB 系列核心类

        // ========== 其他常见依赖 ==========
        "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",     // TemplatesImpl 利用链
        "org.springframework.transaction.interceptor.TransactionInterceptor", // Spring 利用链
        "com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter",         // TrAXFilter 利用类（CC3）
        "org.apache.commons.collections.functors.ChainedTransformer",
        "org.apache.commons.collections4.comparators.TransformingComparator",
        "org.apache.commons.beanutils.BeanComparator",
        "com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase",
        "com.mchange.v2.c3p0.test.AlwaysFailDataSource"
    };

    /**
     * 内存马配置映射
     * 排序原则：
     * 1. 按客户端工具分组 - AntSword/Behinder/Godzilla/Suo5
     * 2. Filter 优先于 Servlet - Filter 拦截范围更广，更隐蔽
     *
     * 使用说明：
     * - memTypes: 获取所有内存马类型列表（保持顺序）
     * - getMemshellRemark(type): 获取指定类型的备注信息
     */
    private static final java.util.Map<String, String> MEMSHELL_INFO_MAP = new java.util.LinkedHashMap<String, String>() {{
        // ========== AntSword (中国蚁剑) ==========
        put("AntSwordFilter", "使用 path、password");     // Filter 型 - 拦截所有请求，更隐蔽
        put("AntSwordServlet", "使用 path、password");    // Servlet 型 - 固定路径访问

        // ========== Behinder (冰蝎) ==========
        put("BehinderFilter", "使用 path、password");     // Filter 型 - 拦截所有请求，更隐蔽
        put("BehinderServlet", "使用 path、password");    // Servlet 型 - 固定路径访问

        // ========== Godzilla (哥斯拉) ==========
        put("GodzillaFilter", "使用 path、password");     // Filter 型 - 拦截所有请求，更隐蔽
        put("GodzillaServlet", "使用 path、password");    // Servlet 型 - 固定路径访问

        // ========== Suo5 (全双工 TCP/HTTP 反向代理) ==========
        put("Suo5Filter", "使用 path、Header");           // Filter 型 - 拦截指定路径请求，支持全双工代理、SSL 绕过
        put("Suo5Servlet", "使用 path、Header");          // Servlet 型 - 固定路径访问，支持全双工代理、SSL 绕过
        put("Suo5v2Filter", "使用 path、Header");         // Filter 型 - 支持全双工代理、SSL 绕过、模板、混淆等高级特性
        put("Suo5v2Servlet", "使用 path、Header");        // Servlet 型 - 支持全双工代理、SSL 绕过、模板、混淆等高级特性
        put("Suo5Listener", "使用 Header");               // Listener 型 - 全局监听，支持全双工代理、SSL 绕过（v1版本）
        put("Suo5v2Listener", "使用 Header");             // Listener 型 - 全局监听，支持全双工代理、SSL 绕过（v2版本，支持模板、混淆等高级特性）

        // ========== 自定义内存马 ==========
        put("自定义内存马", "自定义内存马未加入验证机制，请通过响应判断或自行连接测试"); // 用户自定义 Base64 字节码
    }};

    /**
     * 获取所有内存马类型列表（保持插入顺序）
     */
    public static String[] memTypes = MEMSHELL_INFO_MAP.keySet().toArray(new String[0]);

    /**
     * 获取内存马备注信息
     * @param memshellType 内存马类型
     * @return 备注信息，如果类型不存在则返回"该内存马参数需求未知"
     */
    public static String getMemshellRemark(String memshellType) {
        String remark = MEMSHELL_INFO_MAP.get(memshellType);
        return remark != null ? remark : "该内存马参数需求未知";
    }
}
