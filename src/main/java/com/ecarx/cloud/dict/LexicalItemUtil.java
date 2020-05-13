package com.ecarx.cloud.dict;

import com.ecarx.cloud.enumeration.AnalysisFieldEnum;
import com.ecarx.cloud.util.ChineseDetectUtil;
import com.ecarx.cloud.util.CommonUtil;
import com.ecarx.cloud.util.ZhConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 词项工具
 */
public class LexicalItemUtil {

    /**
     * 获取词项
     *
     * @param word
     * @return
     */
    public static Set<String> getLexicalItems(String word, Integer kind){
        word = word.trim();
        if (!ChineseDetectUtil.containChinese(word)) {
            return null;
        }
        Set<String> lexicalItems = new HashSet<>();
        String wordWithoutSpecialCharacter = CommonUtil.removeSpecialCharacter(word);
        if (StringUtils.isNotBlank(wordWithoutSpecialCharacter)) {
            List<String> list = CommonUtil.splitByBlank(wordWithoutSpecialCharacter);
            list.forEach(item -> {
                if (StringUtils.isNotBlank(item)) {
                    lexicalItems.add(item.trim());
                }
            });
        }

        if (kind == AnalysisFieldEnum.ALBUM.getKind()) {
            word = CommonUtil.removeParenthesisAndContent(word);
        } else if (kind == AnalysisFieldEnum.ARTIST.getKind()) {
            word = CommonUtil.removeBraketAndContent(word);
        }
        if (StringUtils.isNotBlank(word.trim())) {
            lexicalItems.add(ZhConverter.convert(word, ZhConverter.SIMPLIFIED));
        }
        return lexicalItems;
    }
}
