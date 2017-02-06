package com.tjut.mianliao.scan.assist;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.tjut.mianliao.news.wicket.WicketHelper;

public class MlParser {

    public static final String URI_PREFIX_USER = "ml:u:";
    public static final String URI_PREFIX_NEWS_SOURCE = "ml:ns:";
    public static final String URI_PREFIX_USA = "ml:usa:";
    public static final String URI_PREFIX_FORUM = "ml:f:";

    private MlParser() {}

    public static Type check(ParsedResult result) {
        String info = result.getDisplayResult();
        Type type = Type.UNKNOWN;
        if (info.startsWith(URI_PREFIX_USER)) {
            type = Type.USER;
        } else if (info.startsWith(URI_PREFIX_NEWS_SOURCE)) {
            type = Type.NEWS_SOURCE;
        } else if (info.startsWith(URI_PREFIX_USA)) {
            type = Type.USA;
        } else if (info.startsWith(URI_PREFIX_FORUM)) {
            type = Type.FORUM;
        } else if (result.getType() == ParsedResultType.TEXT && info.matches(WicketHelper.TICKET_REGEX)) {
            type = Type.TICKET;
        }

        return type;
    }

    public enum Type {
        USER,
        NEWS_SOURCE,
        USA,
        TICKET,
        FORUM,
        UNKNOWN
    }
}


