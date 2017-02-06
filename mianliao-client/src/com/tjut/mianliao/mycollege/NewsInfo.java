package com.tjut.mianliao.mycollege;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/8/29.
 */
public class NewsInfo implements Serializable {

    public String title;

    public NewsInfo() {
    }

    public NewsInfo(String title) {
        this.title = title;
    }
}
