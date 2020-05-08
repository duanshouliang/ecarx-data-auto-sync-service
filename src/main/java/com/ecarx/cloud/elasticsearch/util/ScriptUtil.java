package com.ecarx.cloud.elasticsearch.util;

import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import java.util.Map;

public class ScriptUtil {
    public static Script buildScript(Map<String,Object> params){
        StringBuffer sb = new StringBuffer("");
        params.keySet().forEach(field ->{
            sb.append("ctx._source['"+field+"']=" + "params."+field).append(";");
        });
        return new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, sb.toString(), params);
    }
}
