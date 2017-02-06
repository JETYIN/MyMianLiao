package com.tjut.mianliao.component;

public interface VisibleDelay {

    public static final int DELAY_MILLS = 80;

    public void setVisibleDelayed(boolean visible);

    public void setVisible(boolean visible);

    public boolean isVisible();
}
