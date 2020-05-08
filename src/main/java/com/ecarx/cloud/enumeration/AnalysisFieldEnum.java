package com.ecarx.cloud.enumeration;

public enum AnalysisFieldEnum {
    CLOUD_MUSIC_CM_ALBUM("cloud_music.cm_album", "album_name",1),
    CLOUD_MUSIC_CM_ARTIST("cloud_music.cm_artist", "album_name",2),
    CLOUD_MUSIC_CM_MUSIC("cloud_music.cm_music", "album_name",3),

    KUWO_MUSIC_KW_ALBUM("kuwo_music.kw_album", "album_name",1),
    KUWO_MUSIC_KW_ARTIST("kuwo_music.kw_artist", "album_name",2),
    KUWO_MUSIC_KW_MUSIC("kuwo_music.kw_music", "album_name",3);

    private String table;
    private String field;
    private Integer kind;

    private AnalysisFieldEnum(String table, String field, int kind){
        this.table = table;
        this.field = field;
        this.kind = kind;
    }

    public static String getField(String table){
        for(AnalysisFieldEnum fieldEnum : AnalysisFieldEnum.values()){
            if(fieldEnum.getTable().equals(table)){
                return fieldEnum.getField();
            }
        }
        return null;
    }

    public static Integer getKind(String table){
        for(AnalysisFieldEnum fieldEnum : AnalysisFieldEnum.values()){
            if(fieldEnum.getTable().equals(table)){
                return fieldEnum.getKind();
            }
        }
        return null;
    }

    public String getTable() {
        return table;
    }

    public String getField() {
        return field;
    }

    public Integer getKind() {
        return kind;
    }
}
