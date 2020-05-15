package com.ecarx.cloud.util;

import java.io.*;
import java.util.*;

/**
 *
 */
public class ZhConverter {
    private Properties charMap = new Properties();
    private Set<String> conflictingSets = new HashSet<String>();
    public static final int TRADITIONAL = 0;
    public static final int SIMPLIFIED = 1;
    private static final int NUM_OF_CONVERTERS = 2;
    private static final ZhConverter[] converters = new ZhConverter[NUM_OF_CONVERTERS];
    private static final String[] propertyFiles = new String[2];

    static {
        propertyFiles[TRADITIONAL] = "zh2Hant.properties"; //附件下载
        propertyFiles[SIMPLIFIED] = "zh2Hans.properties";  //附件下载
    }
    /**
     * @param converterType 0 for traditional and 1 for simplified
     * @return
     */
    public static ZhConverter getInstance(int converterType) {
        if (converterType >= 0 && converterType < NUM_OF_CONVERTERS) {
            if (converters[converterType] == null) {
                synchronized (ZhConverter.class) {
                    if (converters[converterType] == null) {
                        converters[converterType] = new ZhConverter(propertyFiles[converterType]);
                    }
                }
            }
            return converters[converterType];
        } else {
            return null;
        }
    }

    public static String convert(String text, int converterType) {
        ZhConverter instance = getInstance(converterType);
        return instance.convert(text);
    }


    private ZhConverter(String propertyFile) {
        InputStream is = null;
//        is = getClass().getResourceAsStream(propertyFile);

        is = this.getClass()
                .getClassLoader()
                .getResourceAsStream(propertyFile);
        if (is != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(is));
                charMap.load(reader);
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null)
                        reader.close();
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                }
            }
        }
        initializeHelper();
    }

    @SuppressWarnings("rawtypes")
    private void initializeHelper() {
        Map<String, Integer> stringPossibilities = new HashMap<String, Integer>();
        Iterator iter = charMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (key.length() >= 1) {
                for (int i = 0; i < (key.length()); i++) {
                    String keySubstring = key.substring(0, i + 1);
                    if (stringPossibilities.containsKey(keySubstring)) {
                        Integer integer = (Integer) (stringPossibilities.get(keySubstring));
                        stringPossibilities.put(keySubstring, new Integer(
                                integer.intValue() + 1));
                    } else {
                        stringPossibilities.put(keySubstring, new Integer(1));
                    }
                }
            }
        }
        iter = stringPossibilities.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (((Integer) (stringPossibilities.get(key))).intValue() > 1) {
                conflictingSets.add(key);
            }
        }
    }

    public String convert(String in) {
        StringBuilder outString = new StringBuilder();
        StringBuilder stackString = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            String key = "" + c;
            stackString.append(key);
            if (conflictingSets.contains(stackString.toString())) {
            } else if (charMap.containsKey(stackString.toString())) {
                outString.append(charMap.get(stackString.toString()));
                stackString.setLength(0);
            } else {
                CharSequence sequence = stackString.subSequence(0, stackString.length() - 1);
                stackString.delete(0, stackString.length() - 1);
                flushStack(outString, new StringBuilder(sequence));
            }
        }
        flushStack(outString, stackString);
        return outString.toString();
    }

    private void flushStack(StringBuilder outString, StringBuilder stackString) {
        while (stackString.length() > 0) {
            if (charMap.containsKey(stackString.toString())) {
                outString.append(charMap.get(stackString.toString()));
                stackString.setLength(0);
            } else {
                outString.append("" + stackString.charAt(0));
                stackString.delete(0, 1);
            }
        }
    }

    String parseOneChar(String c) {
        if (charMap.containsKey(c)) {
            return (String) charMap.get(c);

        }
        return c;
    }
}
