package com.ecarx.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

    public static String removeSpecialCharacter(String text) {
        // 清除掉所有特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’\"《》_-。-，、？0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        String result = null;
        try {
            result = m.replaceAll(" ").trim();
        } catch (Exception e) {
            LOGGER.error("remove special character with exception {}", e.getMessage());
        }
        return result;
    }

    public static List<String> splitByBlank(String text) {
        String[] arr = text.split(" +");
        if (null == arr || arr.length == 0) {
            return null;
        }
        return Arrays.asList(arr);
    }

    /**
     * 去除小括号及小括号内的内容
     *
     * @param text
     * @return
     */
    public static String removeParenthesisAndContent(String text) {
        while (text.contains("(")) {
            text = text.replaceAll(" *\\([^)(]*\\) *", "");
        }
        return text;
    }

    /**
     * 去除括号及括号内的内容
     *
     * @param text
     * @return
     */
    public static String removeBraketAndContent(String text) {
        while (text.contains("(")) {
            text = text.replaceAll(" *\\([^)(]*\\) *", "");
        }
        if (text.contains("[")) {
            text = text.replaceAll(" *\\[[^]\\[]*\\] *", "");
        }
        return text;
    }

    public static void main(String[] args) {
        System.out.println(removeParenthesisAndContent("duan (s(h)u)  liang"));
    }
}
