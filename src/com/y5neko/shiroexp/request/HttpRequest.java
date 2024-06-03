package com.y5neko.shiroexp.request;

import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.payloads.TargetOBJ;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    /**
     * 获取响应对象
     * @param targetOBJ 目标请求对象
     * @param formBody POST请求体
     * @param headers 请求头
     * @param method 请求方法
     * @return 响应对象
     */
    public static ResponseOBJ httpRequest(TargetOBJ targetOBJ, FormBody formBody, Map<String, String> headers, String method) {
        // 创建OkHttp客户端和requestBuilder
        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder;
        ResponseOBJ responseOBJ = new ResponseOBJ();

        // 合并原headers
        Map<String, String> merged_headers = mergeHeaders(targetOBJ.getHeaders(), headers);

        // 判断请求方法，默认GET
        if (method.equals("GET")) {
            requestBuilder = new Request.Builder().url(targetOBJ.getUrl());
        } else if (method.equals("POST")) {
            requestBuilder = new Request.Builder().post(formBody).url(targetOBJ.getUrl());
        } else {
            requestBuilder = new Request.Builder().url(targetOBJ.getUrl());
        }

        return getResponseOBJ(merged_headers, client, requestBuilder, responseOBJ);
    }

    /**
     * 简单HTTP请求
     * @param url 目标地址
     * @param formBody POST请求体
     * @param headers 请求头
     * @param method 请求方法
     * @return 响应对象
     */
    public static ResponseOBJ httpRequest_simple(String url, FormBody formBody, Map<String, String> headers, String method) {
        // 创建OkHttp客户端和requestBuilder
        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder;
        ResponseOBJ responseOBJ = new ResponseOBJ();

        // 判断请求方法，默认GET
        if (method.equals("GET")) {
            requestBuilder = new Request.Builder().url(url);
        } else if (method.equals("POST")) {
            requestBuilder = new Request.Builder().post(formBody).url(url);
        } else {
            requestBuilder = new Request.Builder().url(url);
        }

        return getResponseOBJ(headers, client, requestBuilder, responseOBJ);
    }

    /**
     * 获取响应对象提取方法
     * @param headers 请求头
     * @param client OkHttpClient
     * @param requestBuilder Request.Builder
     * @param responseOBJ 响应对象
     * @return 处理后的响应对象
     */
    private static ResponseOBJ getResponseOBJ(Map<String, String> headers, OkHttpClient client, Request.Builder requestBuilder, ResponseOBJ responseOBJ) {
        headers.forEach(requestBuilder::header);
        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                responseOBJ.setStatusCode(response.code());
                responseOBJ.setResponse(response.body().bytes());
                responseOBJ.setHeaders(response.headers());
                return responseOBJ;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    /**
     * 向headers集合中添加一个键值对。如果键已经存在，则将值添加在原来的值后面。
     * @param headers 需要更新的集合
     * @param key 要添加的键
     * @param value 要添加的值
     */
    public static void addHeader(Map<String, String> headers, String key, String value) {
        // 检查是否已经存在该键
        if (headers.containsKey(key)) {
            // 追加新值到现有值后面
            headers.compute(key, (k, existingValue) -> existingValue + value);
        } else {
            // 添加新的键值对
            headers.put(key, value);
        }
    }

    /**
     * 将一个headers集合合并到另一个headers集合中。如果键已经存在，则将值添加在原来的值后面。
     * @param originalHeaders 需要更新的集合
     * @param newHeaders 需要合并进来的集合
     * @return 合并后的集合
     */
    public static Map<String, String> mergeHeaders(Map<String, String> originalHeaders, Map<String, String> newHeaders) {
        for (Map.Entry<String, String> entry : newHeaders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 检查是否已经存在该键
            if (originalHeaders.containsKey(key)) {
                // 追加新值到现有值后面，用逗号隔开
                originalHeaders.compute(key, (k, existingValue) -> existingValue + value);
            } else {
                // 添加新的键值对
                originalHeaders.put(key, value);
            }
        }
        return originalHeaders;
    }

    public static void main(String[] args) throws IOException {
        Map<String, String> headers = new HashMap<>();
        FormBody formBody = new FormBody.Builder().build();

        ResponseOBJ responseOBJ = httpRequest_simple("https://www.baidu.com", formBody, headers, "GET");
        if (responseOBJ != null) {
            System.out.println(Tools.bytesToString(responseOBJ.getResponse()));
            System.out.println(responseOBJ.getHeaders());
            System.out.println(responseOBJ.getStatusCode());
        }
    }
}
