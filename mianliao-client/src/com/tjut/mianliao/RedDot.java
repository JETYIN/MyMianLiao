package com.tjut.mianliao;

import java.lang.ref.WeakReference;
import java.util.Observable;

/**
 * This is a notice center for red dot in order to simplify the RedDot notice display.
 * Chat notice is from none-ui thread, so please take care of the ui change in ui thread.
 */
public class RedDot extends Observable {

    public enum RedDotType {
        CHAT, EXPLORE, MY_COLLEGE
    }

    private static WeakReference<RedDot> sInstanceRef;

    private int[] mCounts;

    public static synchronized RedDot getInstance() {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        RedDot instance = new RedDot();
        sInstanceRef = new WeakReference<RedDot>(instance);
        return instance;
    }

    private RedDot() {
        mCounts = new int[RedDotType.values().length];
    }

    public void clear() {
        deleteObservers();
        sInstanceRef.clear();
    }

    public boolean hasNew(RedDotType type) {
        return getCount(type) > 0;
    }

    public int getCount(RedDotType type) {
        return mCounts[type.ordinal()];
    }

    public void update(RedDotType type, int number) {
        mCounts[type.ordinal()] = number;
        setChanged();
        /**将参数传回**/
        notifyObservers(type);
    }
}
