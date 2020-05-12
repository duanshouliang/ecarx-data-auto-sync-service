package com.ecarx.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ChineseDetectUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChineseDetectUtil.class);

    public static final String REG = "[\\u4e00-\\u9fa5]+";

    public static boolean containChinese(String text) {
        char[] charArray = text.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if ((charArray[i] >= 0x4e00) && (charArray[i] <= 0x9fbb)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isFullChinese(String text) {
        boolean result = false;
        try {
            result = text.matches(REG);
        } catch (Exception e) {
            LOGGER.error("Detect chinese with exception {}", e.getMessage());
        }
        return result;
    }
}
