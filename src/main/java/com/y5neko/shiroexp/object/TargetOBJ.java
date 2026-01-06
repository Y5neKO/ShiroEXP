package com.y5neko.shiroexp.object;

import java.util.HashMap;
import java.util.Map;

/**
 * Target类，用以生成Web请求的对象
 */
public class TargetOBJ {
    private String url;
    private String requestType = "GET";  // 请求方式：GET 或 POST
    private String rememberMeFlag = "rememberMe";
    private String key = "kPH+bIxk5D2deZiIxcaaaA==";
    private String cryptType = "CBC";    // 加密模式：CBC 或 GCM
    private Map<String, String> cookie = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private String gadget;
    private String echo = "AllEcho";
    private String proxy;

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getEcho() {
        return echo;
    }

    public void setEcho(String echo) {
        this.echo = echo;
    }

    public String getGadget() {
        return gadget;
    }

    public void setGadget(String gadget) {
        this.gadget = gadget;
    }

    public Map<String, String> getCookie() {
        return cookie;
    }

    public void setCookie(Map<String, String> cookie) {
        this.cookie = cookie;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public TargetOBJ(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRememberMeFlag() {
        return rememberMeFlag;
    }

    public void setRememberMeFlag(String rememberMeFlag) {
        this.rememberMeFlag = rememberMeFlag;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCryptType() {
        return cryptType;
    }

    public void setCryptType(String cryptType) {
        this.cryptType = cryptType;
    }

    public void resetHeaders(){
        this.headers = new HashMap<>();
    }
}
