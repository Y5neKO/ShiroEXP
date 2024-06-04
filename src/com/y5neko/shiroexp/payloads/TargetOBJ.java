package com.y5neko.shiroexp.payloads;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象
 */
public class TargetOBJ {
    private String url;
    private String rememberMeFlag = "rememberMe";
    private String key = "kPH+bIxk5D2deZiIxcaaaA==";
    private Map<String, String> cookie = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private String gadget;
    private String echo = "AllEcho";

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

    public void resetHeaders(){
        this.headers = new HashMap<>();
    }
}
