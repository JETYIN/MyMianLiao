package com.tjut.mianliao.xmpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.jivesoftware.smack.PrivacyListManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.PrivacyItem;
import org.jivesoftware.smack.packet.PrivacyItem.PrivacyRule;
import org.jivesoftware.smack.packet.PrivacyItem.Type;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;

import com.tjut.mianliao.util.Utils;

public class PrivacyHelper {
    private static final String TAG = "PrivacyHelper";

    private static final String PRIVACY_LIST = "privacy_list";

    private PrivacyHelper() {}

    private static Comparator<PrivacyItem> sComparator = new Comparator<PrivacyItem>() {
        @Override
        public int compare(PrivacyItem lhs, PrivacyItem rhs) {
            return rhs.getOrder() - lhs.getOrder();
        }
    };

    private static Hashtable<Integer, PrivacyItem> sPrivacies = new Hashtable<Integer, PrivacyItem>();

    private static PrivacyListManager sPLM;

    public static void initPrivacyList(XMPPConnection connection) {
        sPLM = PrivacyListManager.getInstanceFor(connection);

        try {
            List<PrivacyItem> privacies = sPLM.getPrivacyList(PRIVACY_LIST).getItems();
            if (privacies == null) {
                createPrivacyList();
            } else {
                sPLM.setActiveListName(PRIVACY_LIST);
                sPLM.setDefaultListName(PRIVACY_LIST);
                sPrivacies.clear();
                for (PrivacyItem pi : privacies) {
                    sPrivacies.put(pi.getOrder(), pi);
                }
            }
        } catch (XMPPException e) {
            XMPPError error = e.getXMPPError();
            if (error != null && Condition.item_not_found.toString().equals(error.getCondition())) {
                createPrivacyList();
            } else {
                Utils.logW(TAG, "initPrivacyList: " + e.getMessage());
            }
        } catch (IllegalStateException e) {
            Utils.logW(TAG, "initPrivacyList: " + e.getMessage());
        }
    }

    public static boolean addToBlacklist(String jid) {
        if (jid != null) {
            PrivacyItem pi = createPrivacyItem(false, Type.jid, jid);
            pi.setFilterMessage(true);
            pi.setFilterPresence_in(true);
            pi.setFilterPresence_out(true);
            return addToPrivacyList(pi);
        }
        return false;
    }

    public static boolean removeFromBlackist(String jid) {
        if (jid != null) {
            for (PrivacyItem pi : sPrivacies.values()) {
                if (isBlacklistType(pi) && jid.equals(pi.getValue())) {
                    return removeFromPrivacyList(pi.getOrder());
                }
            }
        }
        return false;
    }

    public static Collection<String> getBlockedJids() {
        ArrayList<String> jids = new ArrayList<String>();
        for (PrivacyItem pi : sPrivacies.values()) {
            if (isBlacklistType(pi)) {
                jids.add(pi.getValue());
            }
        }
        return jids;
    }

    public static void clearPrivacies() {
        sPrivacies.clear();
        sPLM = null;
    }

    public static boolean setAllowTemporaryChat(boolean allow) {
        int order = 0;
        for (PrivacyItem pi : sPrivacies.values()) {
            if (isTemporaryChatType(pi)) {
                order = pi.getOrder();
                break;
            }
        }
        if (order == 0 && !allow) {
            PrivacyItem pi = createPrivacyItem(
                    false, Type.subscription, PrivacyRule.SUBSCRIPTION_NONE);
            pi.setFilterMessage(true);
            return addToPrivacyList(pi);
        } else if (allow) {
            return removeFromPrivacyList(order);
        }
        return true;
    }

    public static boolean isReady() {
        return sPLM != null;
    }

    public static boolean allowTemporaryChat() {
        for (PrivacyItem pi : sPrivacies.values()) {
            if (isTemporaryChatType(pi)) {
                return pi.isAllow();
            }
        }
        return true;
    }

    private static void createPrivacyList() {
        if (sPLM != null) {
            ArrayList<PrivacyItem> privacies = new ArrayList<PrivacyItem>();
            privacies.add(createPrivacyItem(true, null, null));
            try {
                sPLM.createPrivacyList(PRIVACY_LIST, privacies);
                sPLM.setActiveListName(PRIVACY_LIST);
                sPLM.setDefaultListName(PRIVACY_LIST);
            } catch (XMPPException e) {
                Utils.logW(TAG, "createPrivacyList: " + e.getMessage());
            } catch (IllegalStateException e) {
                Utils.logW(TAG, "createPrivacyList" + e.getMessage());
            }
        }
    }

    private static boolean addToPrivacyList(PrivacyItem pi) {
        if (sPLM != null) {
            sPrivacies.put(pi.getOrder(), pi);
            try {
                sPLM.updatePrivacyList(PRIVACY_LIST,
                        new ArrayList<PrivacyItem>(sPrivacies.values()));
                pi = null;
                return true;
            } catch (XMPPException e) {
                Utils.logW(TAG, "addToPrivacyList: " + e.getMessage());
            } catch (IllegalStateException e) {
                Utils.logW(TAG, "addToPrivacyList: " + e.getMessage());
            } finally {
                if (pi != null) {
                    sPrivacies.remove(pi.getOrder());
                }
            }
        }
        return false;
    }

    private static boolean removeFromPrivacyList(int order) {
        if (sPLM != null) {
            PrivacyItem pi = sPrivacies.remove(order);
            if (pi != null) {
                try {
                    sPLM.updatePrivacyList(PRIVACY_LIST,
                            new ArrayList<PrivacyItem>(sPrivacies.values()));
                    pi = null;
                    return true;
                } catch (XMPPException e) {
                    Utils.logW(TAG, "removeFromPrivacyList: " + e.getMessage());
                } catch (IllegalStateException e) {
                    Utils.logW(TAG, "removeFromPrivacyList: " + e.getMessage());
                } finally {
                    if (pi != null) {
                        sPrivacies.put(order, pi);
                    }
                }
            }
        }
        return false;
    }

    private static PrivacyItem createPrivacyItem(boolean allow, Type type, String value) {
        if (type == null) {
            return new PrivacyItem(null, allow, Integer.MAX_VALUE);
        }

        ArrayList<PrivacyItem> privacies = new ArrayList<PrivacyItem>();
        for (PrivacyItem pi : sPrivacies.values()) {
            if (pi.getType() != null) {
                privacies.add(pi);
            }
        }
        int maxOrder = 0;
        switch (privacies.size()) {
            case 0:
                break;

            case 1:
                maxOrder = privacies.get(0).getOrder();
                break;

            default:
                Collections.sort(privacies, sComparator);
                maxOrder = privacies.get(0).getOrder();

        }
        PrivacyItem pi = new PrivacyItem(type.toString(), allow, maxOrder + 1);
        pi.setValue(value);
        return pi;
    }

    private static boolean isBlacklistType(PrivacyItem pi) {
        return !pi.isAllow() && Type.jid == pi.getType()
                && pi.isFilterMessage()
                && pi.isFilterPresence_in()
                && pi.isFilterPresence_out();
    }

    private static boolean isTemporaryChatType(PrivacyItem pi) {
        return !pi.isAllow() && pi.isFilterMessage()
                && Type.subscription == pi.getType()
                && PrivacyRule.SUBSCRIPTION_NONE.equals(pi.getValue());
    }
}
