package com.y5neko.shiroexp.request.shiro721;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求信息封装类
 * 用于封装 Padding Oracle 攻击所需的 HTTP 请求信息
 */
public class HttpRequestInfo {
    private String requestMethod;
    private String requestURL;
    private String rememberMeCookie;  // 存储为十六进制字符串
    private String rememberMeCookieName = "rememberMe";  // Cookie 名称，默认为 rememberMe
    private String requestBody;
    private Map<String, String> headers = new HashMap<>();
    private String original;

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public String getRememberMeCookie() {
        return rememberMeCookie;
    }

    public void setRememberMeCookie(String rememberMeCookie) {
        // 如果是 Base64 编码的 Cookie，转换为十六进制
        try {
            byte[] decoded = Base64.decodeBase64(rememberMeCookie);
            this.rememberMeCookie = Hex.encodeHexString(decoded);
        } catch (Exception e) {
            // 如果转换失败，直接使用原始值
            this.rememberMeCookie = rememberMeCookie;
        }
    }

    public String getRememberMeCookieName() {
        return rememberMeCookieName;
    }

    public void setRememberMeCookieName(String rememberMeCookieName) {
        this.rememberMeCookieName = rememberMeCookieName;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "HttpRequestInfo{" +
                "requestMethod='" + requestMethod + '\'' +
                ", requestURL='" + requestURL + '\'' +
                ", rememberMeCookieName='" + rememberMeCookieName + '\'' +
                ", rememberMeCookie='" + rememberMeCookie + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", headers=" + headers +
                ", original='" + original + '\'' +
                '}';
    }
}
