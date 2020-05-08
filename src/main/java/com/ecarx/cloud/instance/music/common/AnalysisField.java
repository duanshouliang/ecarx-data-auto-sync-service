package com.ecarx.cloud.instance.music.common;

import java.util.*;

public class AnalysisField {
    public static final Map<String, String> ANALYSIS_FIELD = new HashMap<>();

    static {
        ANALYSIS_FIELD.put("cloud_music.cm_album", "album_name");
        ANALYSIS_FIELD.put("cloud_music.cm_music", "music_name");
        ANALYSIS_FIELD.put("cloud_music.cm_artist", "artist_name");

        ANALYSIS_FIELD.put("kuwo_music.kw_album", "album_name");
        ANALYSIS_FIELD.put("kuwo_music.kw_music", "music_name");
        ANALYSIS_FIELD.put("kuwo_music.ckw_artist", "artist_name");
    }
}
