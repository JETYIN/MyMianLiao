package com.tjut.mianliao.contact;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * If there's a entity want to gets update when user info is updated,
 * register a observer here.
 */
/**目标对象Object**/
public class ContactUpdateCenter {

    private static Collection<ContactObserver> sObservers = new CopyOnWriteArrayList<ContactObserver>();

    private ContactUpdateCenter() {
    }

    public static void registerObserver(ContactObserver observer) {
        if (!sObservers.contains(observer)) {
            sObservers.add(observer);
        }
    }

    public static void removeObserver(ContactObserver observer) {
        if (sObservers.contains(observer)) {
            sObservers.remove(observer);
        }
    }

    public static void clearObservers() {
        sObservers.clear();
    }

    /* package */
    static void notifyContactsUpdated(UpdateType type) {
        notifyContactsUpdated(type, null);
    }

    /* package */
    static void notifyContactsUpdated(UpdateType type, Object data) {
        for (ContactObserver ob : sObservers) {
            ob.onContactsUpdated(type, data);
        }
    }

    public enum UpdateType {
        Presence, UserEntry, UserInfo, Subscription, Unsubscribe, Blacklist
    }

    /**
     * 观察者oberser
     **/
    public interface ContactObserver {
        /**
         * This call is mostly from a none main thread, so treat it carefully.
         */
        void onContactsUpdated(UpdateType type, Object data);
    }
}
