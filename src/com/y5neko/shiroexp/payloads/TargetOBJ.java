package com.y5neko.shiroexp.payloads;

import java.util.HashMap;
import java.util.Map;

public class TargetOBJ {
    private String url;
    private String rememberMeFlag = "rememberMe";
    private String key = "kPH+bIxk5D2deZiIxcaaaA==";
    private Map<String, String> cookie = new HashMap<>();

    public Map<String, String> getCookie() {
        return cookie;
    }

    public void setCookie(Map<String, String> cookie) {
        this.cookie = cookie;
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
}
