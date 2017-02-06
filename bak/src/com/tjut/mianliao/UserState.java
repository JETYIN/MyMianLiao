package com.tjut.mianliao;

import java.lang.ref.WeakReference;
import java.util.Observable;

import com.tjut.mianliao.util.MsResponse;

public class UserState extends Observable {

    public static final int NORMAL = MsResponse.MS_SUCCESS;
    public static final int INACTIVE = MsResponse.MS_POLICE_USER_INACTIVE;
    public static final int INVALID_TOKEN = MsResponse.MS_USER_WRONG_TOKEN;

    private static WeakReference<UserState> sInstanceRef;

    private int mLastState;

    public static synchronized UserState getInstance() {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        UserState instance = new UserState();
        sInstanceRef = new WeakReference<UserState>(instance);
        return instance;
    }

    private UserState() {
        mLastState = NORMAL;
    }

    public void clear() {
        deleteObservers();
        sInstanceRef.clear();
    }

    public void update(int responseCode) {
        switch (responseCode) {
            case INACTIVE:
            case INVALID_TOKEN:
            case NORMAL:
                setState(responseCode);
                break;
            default:
                break;
        }
    }

    private void setState(int state) {
        if (mLastState != state) {
            setChanged();
            notifyObservers(state);
        }
        mLastState = state;
    }

    public int getCode() {
        return mLastState;
    }

    public void reset() {
        setState(NORMAL);
    }

    public boolean isActive() {
        return mLastState != INACTIVE;
    }

    public boolean isTokenValid() {
        return mLastState != INVALID_TOKEN;
    }

    public boolean isNormal() {
        return isActive() && isTokenValid();
    }
}
