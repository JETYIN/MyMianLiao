package com.tjut.mianliao.mycollege;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/8/29.
 */
public class Notes implements Serializable {

    public String title;
    public int type;
    public String content;

    public Notes() {
    }

    public Notes(String title, int type, String content) {
        this.title = title;
        this.type = type;
        this.content = content;
    }
}
