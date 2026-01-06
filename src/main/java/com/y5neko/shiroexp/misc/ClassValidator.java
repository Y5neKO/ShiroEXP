package com.y5neko.shiroexp.misc;

import com.y5neko.shiroexp.config.AllList;

import java.util.*;

/**
 * 类名验证器
 * 负责自定义探测类的验证和去重逻辑
 */
public class ClassValidator {

    /**
     * 验证结果类
     * 包含去重后的列表、重复项信息等
     */
    public static class ValidationResult {
        // 重复的类名列表
        private List<String> duplicates = new ArrayList<>();
        // 去重后的类名列表
        private List<String> deduplicatedList = new ArrayList<>();
        // 重复项来源映射 (类名 -> 来源说明)
        private Map<String, String> duplicateSources = new HashMap<>();
        // 成功添加的类名列表
        private List<String> addedClasses = new ArrayList<>();

        /**
         * 添加重复项
         * @param className 类名
         * @param source 重复来源 (预置类/自定义类)
         */
        public void addDuplicate(String className, String source) {
            if (!duplicates.contains(className)) {
                duplicates.add(className);
                duplicateSources.put(className, source);
            }
        }

        /**
         * 设置去重后的列表
         * @param list 去重后的列表
         */
        public void setDeduplicatedList(List<String> list) {
            this.deduplicatedList = list;
        }

        /**
         * 添加成功添加的类
         * @param className 类名
         */
        public void addClass(String className) {
            addedClasses.add(className);
        }

        /**
         * 检查是否存在重复
         * @return true if has duplicates
         */
        public boolean hasDuplicates() {
            return !duplicates.isEmpty();
        }

        /**
         * 获取去重后的数组
         * @return 去重后的类名数组
         */
        public String[] getDeduplicatedArray() {
            return deduplicatedList.toArray(new String[0]);
        }

        /**
         * 获取去重后的列表
         * @return 去重后的类名列表
         */
        public List<String> getDeduplicatedList() {
            return new ArrayList<>(deduplicatedList);
        }

        /**
         * 获取重复信息字符串
         * @return 重复信息描述
         */
        public String getDuplicateInfo() {
            if (duplicates.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            for (String dup : duplicates) {
                sb.append("• ")
                  .append(dup)
                  .append(" (与")
                  .append(duplicateSources.get(dup))
                  .append("重复)\n");
            }
            return sb.toString().trim();
        }

        /**
         * 获取重复项数量
         * @return 重复项数量
         */
        public int getDuplicateCount() {
            return duplicates.size();
        }

        /**
         * 获取成功添加的数量
         * @return 成功添加的类数量
         */
        public int getAddedCount() {
            return addedClasses.size();
        }

        /**
         * 获取去重后的总数
         * @return 去重后的类总数
         */
        public int getTotalCount() {
            return deduplicatedList.size();
        }
    }

    /**
     * 验证并去重类名列表
     * @param inputClasses 用户输入的类名列表
     * @return 验证结果对象
     */
    public static ValidationResult validateAndDeduplicate(List<String> inputClasses) {
        ValidationResult result = new ValidationResult();
        Set<String> seenCustomClasses = new HashSet<>();
        List<String> deduplicated = new ArrayList<>();

        // 1. 遍历输入的类名
        for (String inputClass : inputClasses) {
            String trimmedClass = inputClass.trim();

            // 跳过空行
            if (trimmedClass.isEmpty()) {
                continue;
            }

            // 2. 检查是否与预置类重复
            if (isPredefinedClass(trimmedClass)) {
                result.addDuplicate(trimmedClass, "预置类");
                continue;
            }

            // 3. 检查是否与已处理的自定义类重复
            if (seenCustomClasses.contains(trimmedClass)) {
                result.addDuplicate(trimmedClass, "自定义类");
                continue;
            }

            // 4. 通过所有检查，添加到去重列表
            seenCustomClasses.add(trimmedClass);
            deduplicated.add(trimmedClass);
            result.addClass(trimmedClass);
        }

        result.setDeduplicatedList(deduplicated);
        return result;
    }

    /**
     * 检查是否为预置类
     * @param className 类名
     * @return true if is predefined class
     */
    public static boolean isPredefinedClass(String className) {
        if (className == null || className.trim().isEmpty()) {
            return false;
        }

        String trimmed = className.trim();

        // 检查是否在预置类列表中
        for (String predefined : AllList.urlDnsClasses) {
            if (predefined.equals(trimmed)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 验证单个类名格式
     * @param className 类名
     * @return true if format is valid
     */
    public static boolean isValidClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            return false;
        }

        // 简单的Java类名格式验证
        // 允许：包名.类名 格式，如 com.example.MyClass
        String trimmed = className.trim();
        return trimmed.matches("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");
    }

    /**
     * 获取完整的探测类列表（预置+自定义）
     * @return 完整的类名数组
     */
    public static String[] getAllClasses(String[] customClasses) {
        Set<String> allClasses = new LinkedHashSet<>();

        // 1. 添加预置类
        allClasses.addAll(Arrays.asList(AllList.urlDnsClasses));

        // 2. 添加自定义类（自动去重）
        if (customClasses != null) {
            for (String customClass : customClasses) {
                if (customClass != null && !customClass.trim().isEmpty()) {
                    allClasses.add(customClass.trim());
                }
            }
        }

        return allClasses.toArray(new String[0]);
    }
}
