package com.tjut.mianliao.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.Utils;

public class News implements Parcelable {
    private static final String TAG = "News";
    public static final String INTENT_EXTRA_NAME = TAG;

    public static final int TYPES_COUNT  = 3;
    public static final int TYPE_TICKET  = 1;
    public static final int TYPE_TICKET2 = 2;

    public static final String ID = "id";
    public static final String URL = "url";
    public static final String CONTENT_URL = "content_url";
    public static final String PUBLIC_URL = "public_url";
    public static final String TITLE = "title";
    public static final String SUMMARY = "summary";
    public static final String COVER = "cover";
    public static final String THUMBNAIL = "image";
    public static final String CREATE_TIME = "create_time";
    public static final String SOURCE_ID = "broadcaster_id";
    public static final String SOURCE_NAME = "broadcaster_name";
    public static final String FAVORITE = "my_fav";
    public static final String LIKED = "my_up";
    public static final String LIKED_COUNT = "up_count";
    public static final String COMMENTED_COUNT = "comment_count";
    public static final String TYPE = "type";
    public static final String ATTACHMENT = "attachment";
    public static final String ACTION = "action";

    // Members used when parsing json object
    public int id;
    public String url;
    public String contentUrl;
    public String publicUrl;
    public String title;
    public String summary;
    public String cover;
    public String thumbnail;
    public long createTime;
    public int sourceId;
    public String sourceName;
    public boolean favorite;
    public boolean liked;
    public int likedCount;
    public int commentedCount;
    public int type;
    public Attachment attachment;
    public Parcelable action;

    // Members used when interacting with user
    public String comment;

    public static final JsonUtil.ITransformer<News> TRANSFORMER =
            new JsonUtil.ITransformer<News>() {
                @Override
                public News transform(JSONObject json) {
                    return fromJson(json);
                }
            };

    public News() {
    }

    public News(News news) {
        id = news.id;
        url = news.url;
        contentUrl = news.contentUrl;
        publicUrl = news.publicUrl;
        title = news.title;
        summary = news.summary;
        cover = news.cover;
        thumbnail = news.thumbnail;
        createTime = news.createTime;
        sourceId = news.sourceId;
        sourceName = news.sourceName;
        favorite = news.favorite;
        liked = news.liked;
        likedCount = news.likedCount;
        commentedCount = news.commentedCount;
        type = news.type;
        action = news.action;
        attachment = news.attachment;
    }

    public static final News fromJson(JSONObject json) {
        if (json == null || json.optInt(ID) == 0) {
            return null;
        }
        News news = new News();
        news.id = json.optInt(ID);
        news.url = json.optString(URL);
        news.contentUrl = json.optString(CONTENT_URL);
        news.publicUrl = json.optString(PUBLIC_URL);
        news.title = json.optString(TITLE);
        news.summary = json.optString(SUMMARY);
        news.cover = json.optString(COVER);
        news.thumbnail = AliImgSpec.NEWS_THUMB.makeUrl(news.cover);
        news.createTime = json.optLong(CREATE_TIME) * 1000;
        news.sourceId = json.optInt(SOURCE_ID);
        news.sourceName = json.optString(SOURCE_NAME);
        news.favorite = json.optBoolean(FAVORITE);
        news.liked = json.optBoolean(LIKED);
        news.likedCount = json.optInt(LIKED_COUNT);
        news.commentedCount = json.optInt(COMMENTED_COUNT);
        news.type = json.optInt(TYPE);
        news.attachment = Attachment.fromJson(json.optJSONObject(ATTACHMENT));
        if (news.isTicketType()) {
            news.action = Ticket.fromJson(json.optJSONObject(ACTION));
        }
        return news;
    }

    public String getPreviewImage() {
        return AliImgSpec.NEWS_COVER.makeUrl(cover);
    }

    public int getTitleIcon() {
        return isTicketType() ? R.drawable.ic_news_ticket : 0;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(ID, id);
            json.put(URL, url);
            json.put(CONTENT_URL, contentUrl);
            json.put(PUBLIC_URL, publicUrl);
            json.put(TITLE, title);
            json.put(SUMMARY, summary);
            json.put(COVER, cover);
            json.put(THUMBNAIL, thumbnail);
            json.put(CREATE_TIME, createTime);
            json.put(SOURCE_ID, sourceId);
            json.put(SOURCE_NAME, sourceName);
            json.put(FAVORITE, favorite);
            json.put(LIKED, liked);
            json.put(LIKED_COUNT, likedCount);
            json.put(COMMENTED_COUNT, commentedCount);
            json.put(TYPE, type);
            json.put(ATTACHMENT, attachment.toJson());
            if (isTicketType()) {
                json.put(ACTION, ((Ticket) action).toJson());
            }
        } catch (JSONException e) {
            Utils.logE(TAG, "Error in toJson(): " + e.getMessage());
        }
        return json;
    }

    public boolean isTicketType() {
        return TYPE_TICKET == type || TYPE_TICKET2 == type;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof News) {
            News other = (News) o;
            return id == other.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    public static final Parcelable.Creator<News> CREATOR = new Parcelable.Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(url);
        out.writeString(contentUrl);
        out.writeString(publicUrl);
        out.writeString(title);
        out.writeString(summary);
        out.writeString(cover);
        out.writeString(thumbnail);
        out.writeLong(createTime);
        out.writeInt(sourceId);
        out.writeString(sourceName);
        out.writeInt(favorite ? 1 : 0);
        out.writeInt(liked ? 1 : 0);
        out.writeInt(likedCount);
        out.writeInt(commentedCount);
        out.writeInt(type);
        if (attachment != null) {
            attachment.writeToParcel(out);
        }
        out.writeParcelable(action, flags);
    }

    private News(Parcel in) {
        id = in.readInt();
        url = in.readString();
        contentUrl = in.readString();
        publicUrl = in.readString();
        title = in.readString();
        summary = in.readString();
        cover = in.readString();
        thumbnail = in.readString();
        createTime = in.readLong();
        sourceId = in.readInt();
        sourceName = in.readString();
        favorite = in.readInt() != 0;
        liked = in.readInt() != 0;
        likedCount = in.readInt();
        commentedCount = in.readInt();
        type = in.readInt();
        attachment = new Attachment(in);
        action = in.readParcelable(Ticket.class.getClassLoader());
    }

    public static class Ticket implements Parcelable {
        public static final String TIP = "tip";
        public static final String BUTTON = "button";
        public static final String ENABLED = "enable";
        public static final String CODE = "code";

        public String tip;
        public String button;
        public boolean enabled;
        public String code;

        private Ticket() {
        }

        public static Ticket fromJson(JSONObject json) {
            Ticket ticket = new Ticket();
            if (json != null) {
                ticket.tip = json.optString(TIP);
                ticket.button = json.optString(BUTTON);
                ticket.enabled = json.optBoolean(ENABLED);
                ticket.code = json.optString(CODE);
            }
            return ticket;
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            try {
                json.put(TIP, tip);
                json.put(BUTTON, button);
                json.put(ENABLED, enabled);
                json.put(CODE, code);
            } catch (JSONException e) {
                Utils.logE(TAG, "Error in toJson(): " + e.getMessage());
            }
            return json;
        }

        public static final Parcelable.Creator<Ticket> CREATOR = new Parcelable.Creator<Ticket>() {
            @Override
            public Ticket createFromParcel(Parcel in) {
                return new Ticket(in);
            }

            @Override
            public Ticket[] newArray(int size) {
                return new Ticket[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(tip);
            out.writeString(button);
            out.writeInt(enabled ? 1 : 0);
            out.writeString(code);
        }

        private Ticket(Parcel in) {
            tip = in.readString();
            button = in.readString();
            enabled = in.readInt() != 0;
            code = in.readString();
        }
    }
}
