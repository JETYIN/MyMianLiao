package com.tjut.mianliao.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

import com.tjut.mianliao.data.Emotion;

public class PullParseService {
    public static List<Emotion> getEmoInfo(InputStream inputStream) throws Exception {
        List<Emotion> emos = null;
        Emotion emo = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, "UTF-8");

        int event = parser.getEventType();// 产生第一个事件
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_DOCUMENT:// 判断当前事件是否是文档开始事件
                    emos = new ArrayList<Emotion>();// 初始化books集合
                    break;
                case XmlPullParser.START_TAG:// 判断当前事件是否是标签元素开始事件
                    if ("string".equals(parser.getName())) {// 判断开始标签元素是否是book

                        String content = parser.nextText();

                        if (emo == null) {
                            emo = Emotion.fromString(null, 0);
                            emo.setParseText(content);
                        } else {
                            emo.setImageName(content);
                            emos.add(emo);
                            emo = null;
                        }
                    }

                    break;

            }
            event = parser.next();// 进入下一个元素并触发相应事件
        }// end while
        return emos;
    }

}