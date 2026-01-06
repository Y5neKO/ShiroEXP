package com.y5neko.shiroexp.misc;

import com.y5neko.shiroexp.config.AllList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DNSLog结果解析器
 * 用于解析DNSLog平台返回的结果，提取类名并匹配完整类路径
 */
public class DNSLogParser {

    /**
     * DNSLog类名匹配结果
     */
    public static class ClassMatchResult {
        private final String shortClassName;    // DNSLog中的短类名（如 TemplatesImpl2）
        private final String baseClassName;     // 基础类名（如 TemplatesImpl）
        private final int index;                // 索引（2表示第2个）
        private final String fullClassName;     // 完整类名（如果匹配成功）
        private final boolean matched;          // 是否匹配成功

        public ClassMatchResult(String shortClassName, String baseClassName, int index, String fullClassName) {
            this.shortClassName = shortClassName;
            this.baseClassName = baseClassName;
            this.index = index;
            this.fullClassName = fullClassName;
            this.matched = (fullClassName != null);
        }

        public String getShortClassName() {
            return shortClassName;
        }

        public String getBaseClassName() {
            return baseClassName;
        }

        public int getIndex() {
            return index;
        }

        public String getFullClassName() {
            return fullClassName;
        }

        public boolean isMatched() {
            return matched;
        }

        /**
         * 格式化显示
         */
        public String format() {
            StringBuilder sb = new StringBuilder();
            if (matched) {
                sb.append("[+] ").append(shortClassName)
                  .append("\n   → ").append(fullClassName);
            } else {
                sb.append("[-] ").append(shortClassName)
                  .append("\n   → 未在预置类列表中找到匹配");
            }
            return sb.toString();
        }
    }

    /**
     * DNSLog解析结果
     */
    public static class DNSLogParseResult {
        private final List<ClassMatchResult> matches;
        private int matchedCount = 0;
        private int unknownCount = 0;

        public DNSLogParseResult(List<ClassMatchResult> matches) {
            this.matches = matches;
            for (ClassMatchResult match : matches) {
                if (match.isMatched()) {
                    matchedCount++;
                } else {
                    unknownCount++;
                }
            }
        }

        public List<ClassMatchResult> getMatches() {
            return matches;
        }

        public int getMatchedCount() {
            return matchedCount;
        }

        public int getUnknownCount() {
            return unknownCount;
        }

        public int getTotalCount() {
            return matches.size();
        }

        /**
         * 格式化所有结果
         */
        public String formatAll() {
            StringBuilder sb = new StringBuilder();
            sb.append("检测结果：\n\n");

            for (ClassMatchResult match : matches) {
                sb.append(match.format()).append("\n\n");
            }

            sb.append("统计：找到 ").append(matchedCount)
              .append(" 个有效依赖，").append(unknownCount)
              .append(" 个未知类");

            return sb.toString();
        }

        /**
         * 获取CSV格式（仅包含匹配成功的完整类名）
         */
        public String toCSV() {
            StringBuilder sb = new StringBuilder();
            for (ClassMatchResult match : matches) {
                if (match.isMatched()) {
                    sb.append(match.getFullClassName()).append("\n");
                }
            }
            return sb.toString().trim();
        }
    }

    /**
     * 解析DNSLog文本
     * @param dnslogText DNSLog平台的原始输出
     * @return 解析结果
     */
    public static DNSLogParseResult parse(String dnslogText) {
        if (dnslogText == null || dnslogText.trim().isEmpty()) {
            return new DNSLogParseResult(new ArrayList<>());
        }

        // 1. 提取类名并去重
        List<String> shortClassNames = extractClassNames(dnslogText);

        // 2. 匹配完整类名
        List<ClassMatchResult> matches = new ArrayList<>();
        for (String shortName : shortClassNames) {
            ClassMatchResult match = matchFullClassName(shortName);
            matches.add(match);
        }

        return new DNSLogParseResult(matches);
    }

    /**
     * 从DNSLog文本中提取类名
     * 格式：38    TCPEndpoint-W9C175Ce.a96605e270.ddns.1433.eu.org.
     * 提取：TCPEndpoint
     *
     * @param text DNSLog文本
     * @return 类名列表（已去重）
     */
    private static List<String> extractClassNames(String text) {
        List<String> classNames = new ArrayList<>();
        Map<String, String> seenNames = new LinkedHashMap<>();

        // 正则：匹配类名-随机值.域名 格式
        // 类名规则：以字母开头，包含字母、数字、下划线、美元符号
        Pattern pattern = Pattern.compile("([A-Za-z_$][\\w$]*)-[A-Za-z0-9]+\\.");

        String[] lines = text.split("\n");
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String className = matcher.group(1);
                if (!seenNames.containsKey(className)) {
                    seenNames.put(className, className);
                    classNames.add(className);
                }
            }
        }

        return classNames;
    }

    /**
     * 匹配完整类名
     * 支持数字后缀机制：TemplatesImpl2 → 第2个以TemplatesImpl结尾的类
     *
     * @param shortClassName 短类名（可能带数字后缀，如 TemplatesImpl2）
     * @return 匹配结果
     */
    private static ClassMatchResult matchFullClassName(String shortClassName) {
        // 1. 解析基础类名和索引
        String baseClassName;
        int index = 1;

        // 检查是否以数字结尾
        Pattern digitPattern = Pattern.compile("^(.*?)(\\d+)$");
        Matcher digitMatcher = digitPattern.matcher(shortClassName);

        if (digitMatcher.matches()) {
            baseClassName = digitMatcher.group(1);
            index = Integer.parseInt(digitMatcher.group(2));
        } else {
            baseClassName = shortClassName;
            index = 1;
        }

        // 2. 在预置类和自定义类中查找
        String fullClassName = findFullClassName(baseClassName, index);

        return new ClassMatchResult(shortClassName, baseClassName, index, fullClassName);
    }

    /**
     * 根据基础类名和索引查找完整类名
     * 例如：baseClassName="TemplatesImpl", index=2 → 找第2个以TemplatesImpl结尾的类
     *
     * @param baseClassName 基础类名（不含数字后缀）
     * @param index 索引（1表示第1个，2表示第2个）
     * @return 完整类名，如果未找到返回null
     */
    private static String findFullClassName(String baseClassName, int index) {
        // 获取所有类（预置+自定义）
        List<String> allClasses = new ArrayList<>();
        for (String cls : AllList.urlDnsClasses) {
            allClasses.add(cls);
        }

        String[] customClasses = com.y5neko.shiroexp.misc.ConfigManager.getCustomClasses();
        for (String cls : customClasses) {
            allClasses.add(cls);
        }

        // 查找所有以baseClassName结尾的类
        List<String> matchedClasses = new ArrayList<>();
        for (String fullClass : allClasses) {
            if (fullClass.endsWith("." + baseClassName)) {
                matchedClasses.add(fullClass);
            }
        }

        // 根据索引返回
        if (index > 0 && index <= matchedClasses.size()) {
            return matchedClasses.get(index - 1);
        }

        return null;
    }
}
