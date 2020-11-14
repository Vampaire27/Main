package com.wwc2.main.media.driver.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Pinyin4jUtil {
	private HanyuPinyinOutputFormat format = null;
	private String[] pinyin;

	public Pinyin4jUtil() {
		format = new HanyuPinyinOutputFormat();
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		pinyin = null;
	}

	public String getCharacterPinYin(char c) {
		try {
			pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}

		if (pinyin == null)
			return null;
		return pinyin[0];

	}

	public String getStringPinYin(String str) {
		StringBuilder sb = new StringBuilder();
		String tempPinyin = null;
		for (int i = 0; i < str.length(); ++i) {
			tempPinyin = getCharacterPinYin(str.charAt(i));
			if (tempPinyin == null) {
				sb.append(str.charAt(i));
			} else {
				sb.append(tempPinyin);
			}
		}
		return sb.toString();
	}
}
