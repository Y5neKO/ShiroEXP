package com.y5neko.shiroexp.misc;

import com.alibaba.fastjson.JSONObject;
import com.y5neko.shiroexp.request.HttpRequest;
import okhttp3.FormBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DnslogConfig {
    public static String[] getDnslogDomain(){
        Map<String, String> headers = new HashMap<>();

        String response = Tools.bytesToString(Objects.requireNonNull(HttpRequest.httpRequest_simple("https://dnslog.org/new_gen", new FormBody.Builder().add("domain", "log.dnslog.sbs.").build(), headers, "POST")).getResponse());
        JSONObject jsonObject = JSONObject.parseObject(response);
        return new String[]{(String) jsonObject.get("domain"), (String) jsonObject.get("key"), (String) jsonObject.get("token")};
    }

    public static String getDnslogRecord(String[] dnslogInfo){
        Map<String, String> headers = new HashMap<>();

        return Tools.bytesToString(Objects.requireNonNull(HttpRequest.httpRequest_simple("https://dnslog.org/" + dnslogInfo[2], new FormBody.Builder().add("domain", "log.dnslog.sbs.").build(), headers, "POST")).getResponse());
    }

    public static void main(String[] args) {
        System.out.println(getDnslogRecord(new String[]{"e2d71c72.dns.su18.org.", "e2d71c72", "nnahtjzrbgxc"}));
    }
}
