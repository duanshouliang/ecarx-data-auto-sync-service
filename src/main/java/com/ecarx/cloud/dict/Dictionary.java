package com.ecarx.cloud.dict;

import com.ecarx.cloud.util.ChineseDetectUtil;
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
    private static String ALBUM_DICT = "album.dic";
    private static String ARTIST_DICT = "artist.dic";
    private static String MUSIC_DICT = "music.dic";
    private static String DICTIONARY_CONFIG = "dictionary.properties";
    private static Set<String> _MainDict;
    private static Set<String> _AlbumDict;
    private static Set<String> _ArtistDict;
    private static Set<String> _MusicDict;
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
        this.loadAlbumDict();
        this.loadArtistDict();
        this.loadMusicDict();
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
                addAlbumWord(words);
                break;
            case 2:
                addArtistWord(words);
                break;
            case 3:
                addMusicWord(words);
                break;
            default:
                addMainWord(words);
                break;

        }
    }

    public void  addMainWord(Set<String> words){
        Set<String> newWords = this.filter(_MainDict, words);
        if(newWords.size() == 0){
            LOGGER.info("Words already exist of main dict...");
            return;
        }
        this.addWords(_MainDict, newWords, props.getProperty(MAIN_DICT));
    }

    public void  addAlbumWord(Set<String> words){

        Set<String> newWords = this.filter(_AlbumDict, words);
        if(newWords.size() == 0){
            LOGGER.info("Words already exist  of album dict...");
            return;
        }
        this.addWords(_AlbumDict, newWords, props.getProperty(ALBUM_DICT));
    }

    public void  addArtistWord(Set<String> words){
        Set<String> newWords = this.filter(_ArtistDict, words);
        if(newWords.size() == 0){
            LOGGER.info("Words already exist  of artist dict...");
            return;
        }
        this.addWords(_ArtistDict, newWords, props.getProperty(ARTIST_DICT));
    }

    public void  addMusicWord(Set<String> words){
        Set<String> newWords = this.filter(_MusicDict, words);
        if(newWords.size() == 0){
            LOGGER.info("All Words already exist  of music dict...");
            return;
        }
        this.addWords(_MusicDict, newWords, props.getProperty(MUSIC_DICT));
    }

    private void loadMainDict(){
        _MainDict = new ConcurrentSkipListSet<>();
        this.loadDict(props.getProperty(MAIN_DICT), _MainDict);
    }

    private void loadAlbumDict(){
        _AlbumDict = new ConcurrentSkipListSet<>();
        this.loadDict(props.getProperty(ALBUM_DICT), _AlbumDict);
    }

    private void loadArtistDict(){
        _ArtistDict = new ConcurrentSkipListSet<>();
        this.loadDict(props.getProperty(ARTIST_DICT), _ArtistDict);
    }

    private void loadMusicDict(){
        _MusicDict = new ConcurrentSkipListSet<>();
        this.loadDict(props.getProperty(MUSIC_DICT), _MusicDict);
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
                if(StringUtils.isNotBlank(theWord) && ChineseDetectUtil.isFullChinese(theWord.trim())){
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
                if(ChineseDetectUtil.isFullChinese(word)) {
                    dict.add(word);
                    bw.write(word + "\r\n");
                }
            }
            bw.flush();
            LOGGER.info("Add words " + words + "to " + path + " completed");
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

    private Set<String> filter(Set<String> cache, Set<String> words){
        Set<String> result = new ConcurrentSkipListSet();
        for(String word : words){
            if(!cache.contains(word)){
                result.add(word);
            }
        }
        return result;
    }
}
