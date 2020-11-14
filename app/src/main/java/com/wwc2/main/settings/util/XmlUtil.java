package com.wwc2.main.settings.util;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析特定的简单xml
 * @author huwei
 */
public class XmlUtil {

    /**
     * 解析XML
     * @param is        xml字节流
     * @param startName       开始位置
     * @return          返回List列表
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List getXmlList(InputStream is, String startName) {
        List list = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(is, "UTF-8");
            //事件类型
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        list = new ArrayList<Object>();
                        break;
                    case XmlPullParser.START_TAG:
                        //获得当前节点元素的名称
                        String name = parser.getName();
                        if (startName.equals(name)) {
                            String item = parser.nextText();
                            list.add(item);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e("xml pull error", e.toString());
        }
        return list;
    }


}