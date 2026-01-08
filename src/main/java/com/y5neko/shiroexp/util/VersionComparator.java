package com.y5neko.shiroexp.util;

/**
 * 版本号比较工具类
 * 支持语义化版本号比较（Semantic Versioning）
 */
public class VersionComparator {

    /**
     * 版本比较结果
     */
    public enum CompareResult {
        NEWER,   // 远程版本更新
        EQUAL,   // 版本相同
        OLDER    // 远程版本更旧
    }

    /**
     * 比较本地版本和远程版本
     * @param localVersion 本地版本号（如 2.1）
     * @param remoteVersion 远程版本号（如 v2.2）
     * @return 比较结果
     */
    public static CompareResult compare(String localVersion, String remoteVersion) {
        if (localVersion == null || remoteVersion == null) {
            throw new IllegalArgumentException("版本号不能为 null");
        }

        String normalizedLocal = normalizeVersion(localVersion);
        String normalizedRemote = normalizeVersion(remoteVersion);

        String[] localParts = normalizedLocal.split("\\.");
        String[] remoteParts = normalizedRemote.split("\\.");

        int maxLength = Math.max(localParts.length, remoteParts.length);

        for (int i = 0; i < maxLength; i++) {
            int localPart = i < localParts.length ? Integer.parseInt(localParts[i]) : 0;
            int remotePart = i < remoteParts.length ? Integer.parseInt(remoteParts[i]) : 0;

            if (localPart < remotePart) {
                return CompareResult.NEWER;
            } else if (localPart > remotePart) {
                return CompareResult.OLDER;
            }
        }

        return CompareResult.EQUAL;
    }

    /**
     * 标准化版本号
     * - 去除 'v' 前缀
     * - 补全缺失的版本号位（2.1 -> 2.1.0）
     * @param version 原始版本号
     * @return 标准化后的版本号
     */
    private static String normalizeVersion(String version) {
        if (version == null || version.isEmpty()) {
            return "0.0.0";
        }

        // 去除 'v' 前缀
        String normalized = version.toLowerCase().startsWith("v")
            ? version.substring(1)
            : version;

        // 分割版本号
        String[] parts = normalized.split("\\.");

        // 补全缺失的位（至少需要 major.minor.patch）
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                sb.append(".");
            }
            sb.append(i < parts.length ? parts[i] : "0");
        }

        return sb.toString();
    }
}
