package com.y5neko.shiroexp;

import com.y5neko.shiroexp.copyright.Copyright;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.payloads.BruteKey;
import com.y5neko.shiroexp.payloads.KeyInfo;
import org.apache.commons.cli.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws ParseException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
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
            String url = cmd.getOptionValue("u");

            // 指定扫描key
            if (cmd.hasOption("sk")) {
                System.out.println("[" + Tools.color("INFO", "BLUE") + "] " + "正在进行key爆破");
                KeyInfo keyInfo;
                // 如果指定了自定义rememberMe字段
                if (cmd.hasOption("rf")) {
                    keyInfo = BruteKey.bruteKey(url, cmd.getOptionValue("rf"));
                } else {
                    keyInfo = BruteKey.bruteKey(url);
                }
                System.out.println("[" + Tools.color("SUCC", "GREEN") + "] " + "检测到key: " + keyInfo.getKey() + ", 加密算法: " + keyInfo.getType());
            }
            // 指定漏洞扫描
            else if (cmd.hasOption("s")) {
                System.out.println("指定扫描模块");
            }
            // 未指定操作
            else {
                System.out.println("[" + Tools.color("EROR", "RED") + "] " + "请指定模块");
            }
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption("h", "help", false, "打印帮助");
        options.addOption("u", "url", true, "目标地址");
        options.addOption("sk", "scan-key", false, "key爆破模块 | 爆破key");
        options.addOption("rf", "rememberme-flag", true, "key爆破模块 | 自定义rememberMe字段名");
        options.addOption("s", "scan", false, "漏洞扫描模块 | 扫描漏洞");
        options.addOption("k", "key", true, "漏洞扫描模块 | 指定key");
        options.addOption("e", "exp", true, "漏洞扫描模块 | 指定exp");
        return options;
    }
}