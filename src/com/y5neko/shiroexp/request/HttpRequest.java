package com.y5neko.shiroexp.request;

import com.y5neko.shiroexp.misc.Tools;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    /**
     * 获取响应对象
     * @param url 目标地址
     * @param formBody POST请求体
     * @param headers 请求头
     * @param method 请求方法
     * @return 响应对象
     */
    public static ResponseOBJ httpRequest(String url, FormBody formBody, Map<String, String> headers, String method) {
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



    public static void main(String[] args) throws IOException {
        Map<String, String> headers = new HashMap<>();
        FormBody formBody = new FormBody.Builder().build();

        ResponseOBJ responseOBJ = httpRequest("https://www.baidu.com", formBody, headers, "GET");
        if (responseOBJ != null) {
            System.out.println(Tools.bytesToString(responseOBJ.getResponse()));
            System.out.println(responseOBJ.getHeaders());
            System.out.println(responseOBJ.getStatusCode());
        }
    }
}
