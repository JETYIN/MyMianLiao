package com.tjut.mianliao.data;

public class MenuItem {
    public int id;
    public String title;
    public int order;

    public MenuItem(int id, String title) {
        this.title = title;
        this.id = id;
    }

    public MenuItem(int id, String title, int order) {
        this.title = title;
        this.id = id;
        this.order = order;
    }

    @Override
    public String toString() {
        return title;
    }
}
