package com.y5neko.shiroexp.config;

import com.y5neko.shiroexp.service.GitHubUpdateService;
import java.util.concurrent.TimeUnit;

/**
 * 版本更新检查缓存
 * 避免频繁调用 GitHub API
 */
public class UpdateCache {

    /**
     * 缓存条目
     */
    public static class CacheEntry {
        private GitHubUpdateService.GitHubReleaseInfo releaseInfo;
        private long timestamp;

        public CacheEntry(GitHubUpdateService.GitHubReleaseInfo releaseInfo) {
            this.releaseInfo = releaseInfo;
            this.timestamp = System.currentTimeMillis();
        }

        public GitHubUpdateService.GitHubReleaseInfo getReleaseInfo() {
            return releaseInfo;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    // 缓存有效期（1小时）
    private static final long CACHE_VALIDITY_MS = TimeUnit.HOURS.toMillis(1);

    // 缓存实例
    private static CacheEntry cache = null;

    /**
     * 获取缓存的版本信息
     * @return 缓存的版本信息，如果缓存过期或不存在则返回 null
     */
    public static GitHubUpdateService.GitHubReleaseInfo getCachedReleaseInfo() {
        if (cache != null && !isExpired()) {
            return cache.getReleaseInfo();
        }
        return null;
    }

    /**
     * 设置缓存
     * @param releaseInfo 版本信息
     */
    public static void setCache(GitHubUpdateService.GitHubReleaseInfo releaseInfo) {
        cache = new CacheEntry(releaseInfo);
    }

    /**
     * 检查缓存是否过期
     * @return true 如果缓存过期或不存在
     */
    public static boolean isExpired() {
        if (cache == null) {
            return true;
        }
        long elapsed = System.currentTimeMillis() - cache.getTimestamp();
        return elapsed > CACHE_VALIDITY_MS;
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        cache = null;
    }

    /**
     * 强制刷新（清除缓存）
     */
    public static void forceRefresh() {
        clearCache();
    }
}
