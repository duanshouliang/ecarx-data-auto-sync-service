package com.ecarx.cloud.dict;

import com.ecarx.cloud.util.LanguageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class Dictionary {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dictionary.class);

    private static Properties props;
    private static String MAIN_DICT = "main.dic";
    private static String DICTIONARY_CONFIG = "dictionary.properties";
    private static Set<String> _MainDict;
    private static Object lock = new Object();
    private static Dictionary dictionary;
    private static boolean loadFinish;

    public static Dictionary getInstance(){
        if(null == dictionary){
            synchronized (Dictionary.class){
                if(null == dictionary){
                    dictionary = new Dictionary();
                }
            }
        }
        return dictionary;
    }
    private Dictionary(){
        this.loadDicConfig();
        this.loadMainDict();
        loadFinish = true;
    }

    private void loadDicConfig(){
        if(null == props){
            synchronized (lock){
                if(null == props){
                    InputStream inputStream = null;
                    props = new Properties();
                    try {
                        inputStream = this.getClass()
                                .getClassLoader()
                                .getResourceAsStream(DICTIONARY_CONFIG);
                        props.load(inputStream);
                    } catch (IOException e) {
                       LOGGER.error("Load dictionary configurations with exception {}, stack {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                    }finally {
                        if(null != inputStream){
                            try {
                                inputStream.close();
                                inputStream = null;
                            } catch (IOException e) {
                                LOGGER.error("Close resource when load dictionary configurations with exception {}, stack {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                            }
                        }
                    }
                }
            }
        }
    }

    public synchronized void addWordTasks(Map<Integer, Set<String>> wordTasks){
        if(null == wordTasks || wordTasks.size() == 0){
            return;
        }
        for(Map.Entry<Integer, Set<String>> entry : wordTasks.entrySet()){
            this.addWords(entry);
        }
    }
    public synchronized void addWords(Map.Entry<Integer, Set<String>> cpWords){
        Integer kind = cpWords.getKey();
        Set<String> words = cpWords.getValue();

        switch (kind){
            case 1:
                addAlbumWords(words);
                break;
            case 2:
                addArtistWords(words);
                break;
            case 3:
                addMusicWords(words);
                break;
            default:
                addMainWord(words);
                break;

        }
    }

    private void addAlbumWords(Set<String> words) {
        System.out.println(words);
    }

    private void addArtistWords(Set<String> words) {
        System.out.println(words);
    }

    private void addMusicWords(Set<String> words) {
        System.out.println(words);
    }

    public void  addMainWord(Set<String> words){
        this.addWords(_MainDict, words, props.getProperty(MAIN_DICT));
    }

    private void loadMainDict(){
        _MainDict = new ConcurrentSkipListSet<>();
        this.loadDict(props.getProperty(MAIN_DICT), _MainDict);
    }

    private void loadDict(String path, Set<String> dict){
        InputStream is = null;
        try {
            is = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(is == null){
            throw new RuntimeException("Dictionary: "+path+" not found!!!");
        }
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(is , "UTF-8"), 512);
            String theWord = null;
            do {
                theWord = br.readLine();
                if(StringUtils.isNotBlank(theWord) && LanguageUtil.isChinese(theWord.trim())){
                    dict.add(theWord.trim());
                }
            } while (theWord != null);

        } catch (IOException ioe) {
            LOGGER.error("Loading dictionary {} exception with exception {}, stack {}", path, ioe.getMessage(), Arrays.toString(ioe.getStackTrace()));
        }finally{
            try {
                if(is != null){
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                LOGGER.error("Close resource when loading dictionary {} exception with exception {}, stack {}", path, e.getMessage(), Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void addWords(Set<String> dict, Set<String> words, String path){
        FileWriter fileWriter = null;
        BufferedWriter bw = null;

        try {
            if (null == words || words.size() == 0) {
                return;
            }
            fileWriter = new FileWriter(path, true);
            bw = new BufferedWriter(fileWriter);
            for (String word : words) {
                if(LanguageUtil.isChinese(word)) {
                    dict.add(word);
                    bw.write(word + "\r\n");
                }
            }
            bw.flush();
        } catch (IOException e) {
            LOGGER.error("Add words to {} with exception {}, stack {}", path, e.getMessage(), Arrays.toString(e.getStackTrace()));
        } finally {
            try {
                if (null != bw) {
                    bw.close();
                    bw = null;
                }
                if (null != fileWriter) {
                    fileWriter.close();
                    fileWriter = null;
                }
            } catch (IOException e) {
                LOGGER.error("Close resource when add words {} with exception {}, stack {}", words, e.getMessage(), Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public static boolean isLoadFinish(){
        return loadFinish;
    }
}
