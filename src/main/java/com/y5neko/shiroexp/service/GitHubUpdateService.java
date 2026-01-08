package com.y5neko.shiroexp.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.y5neko.shiroexp.config.GlobalVariable;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * GitHub Release 更新检查服务
 * 调用 GitHub API 获取最新版本信息
 */
public class GitHubUpdateService {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/Y5neKO/ShiroEXP/releases/latest";
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * 检查更新
     * @return GitHubReleaseInfo 版本信息
     * @throws IOException 网络请求异常
     */
    public static GitHubReleaseInfo checkForUpdates() throws IOException {
        OkHttpClient client = createOkHttpClient();

        Request request = new Request.Builder()
                .url(GITHUB_API_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API 请求失败: " + response.code() + " " + response.message());
            }

            String jsonResponse = response.body().string();
            return parseReleaseInfo(jsonResponse);
        }
    }

    /**
     * 创建 OkHttpClient（支持全局代理）
     */
    private static OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // 应用全局代理配置
        String globalProxy = GlobalVariable.getGlobalProxy();
        if (globalProxy != null && !globalProxy.isEmpty()) {
            String[] parts = globalProxy.split(":");
            if (parts.length == 2) {
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
                builder.proxy(proxy);
            }
        }

        return builder.build();
    }

    /**
     * 解析 GitHub Release API 响应
     */
    private static GitHubReleaseInfo parseReleaseInfo(String jsonResponse) {
        JSONObject json = JSON.parseObject(jsonResponse);

        GitHubReleaseInfo info = new GitHubReleaseInfo();
        info.setVersion(json.getString("tag_name"));
        info.setReleaseName(json.getString("name"));
        info.setReleaseNotes(json.getString("body"));
        info.setHtmlUrl(json.getString("html_url"));
        info.setPublishedAt(json.getString("published_at"));

        return info;
    }

    /**
     * GitHub Release 信息 DTO
     */
    public static class GitHubReleaseInfo {
        private String version;           // 版本号（如 v2.1）
        private String releaseName;       // Release 名称
        private String releaseNotes;      // Release 说明
        private String htmlUrl;           // Release 页面 URL
        private String publishedAt;       // 发布时间

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getReleaseName() {
            return releaseName;
        }

        public void setReleaseName(String releaseName) {
            this.releaseName = releaseName;
        }

        public String getReleaseNotes() {
            return releaseNotes;
        }

        public void setReleaseNotes(String releaseNotes) {
            this.releaseNotes = releaseNotes;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }

        public String getPublishedAt() {
            return publishedAt;
        }

        public void setPublishedAt(String publishedAt) {
            this.publishedAt = publishedAt;
        }

        @Override
        public String toString() {
            return "GitHubReleaseInfo{" +
                    "version='" + version + '\'' +
                    ", releaseName='" + releaseName + '\'' +
                    ", htmlUrl='" + htmlUrl + '\'' +
                    ", publishedAt='" + publishedAt + '\'' +
                    '}';
        }
    }
}
