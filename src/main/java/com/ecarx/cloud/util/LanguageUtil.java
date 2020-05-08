package com.ecarx.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class LanguageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageUtil.class);

    public static boolean isChinese(String text){
        boolean res = false;
        try {
            res = text.matches("[\\u4e00-\\u9fa5]+");
        }catch (Exception e){
            LOGGER.error("Check text {} language with exception {}, stack {}", text, e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
        return res;
    }
}
