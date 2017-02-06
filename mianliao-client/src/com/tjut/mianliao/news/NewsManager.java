package com.tjut.mianliao.news;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;
import android.widget.Toast;

import com.tjut.mianliao.BaseTask;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.RedDot;
import com.tjut.mianliao.RedDot.RedDotType;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.NewsComment;
import com.tjut.mianliao.data.NewsSource;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class NewsManager implements TaskExecutionListener {
    private static final String TAG = "NewsManager";

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_SCHOOL = 1;
    public static final int TYPE_RECOMMEND = 2;
    public static final int TYPE_TICKET = 3;
    public static final int TYPE_MY_FAV = 4;

    private static final int LATEST_NEWS_COUNT_LIMIT = 20;

    private static final String SHARED_PREF_NAME = "news";
    private static final String SP_LATEST_NEWS = "latest_news";
    private static final String SP_FAVORITE_NEWS = "favorite_news";
    private static final String SP_LAST_LOAD_SUGGESTED = "last_load_suggested";

    private static WeakReference<NewsManager> sInstanceRef;

    private Context mContext;
    private SharedPreferences mSharedPrefs;

    private List<TaskExecutionListener> mListeners;
    private NewsTask mLatestNewsTask;
    private long mLastLoadSuggested;

    public static synchronized NewsManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        NewsManager instance = new NewsManager(context);
        sInstanceRef = new WeakReference<NewsManager>(instance);
        return instance;
    }

    private NewsManager(Context context) {
        mContext = context.getApplicationContext();
        mSharedPrefs = context.getSharedPreferences(SHARED_PREF_NAME, 0);
        mLastLoadSuggested = mSharedPrefs.getLong(SP_LAST_LOAD_SUGGESTED, 0);
        mListeners = new ArrayList<TaskExecutionListener>();
    }

    @Override
    public void onPreExecute(int type) {
        for (TaskExecutionListener listener : mListeners) {
            listener.onPreExecute(type);
        }
    }

    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case TaskType.NEWS_FETCH_LATEST:
            case TaskType.NEWS_FETCH_HOT:
            case TaskType.NEWS_FETCH_FAV:
                if (!MsResponse.isSuccessful(mr) || mr.value == null) {
                    toast(MsResponse.getFailureDesc(mContext,
                            R.string.news_tst_fetch_list_failed, mr.code));
                }
                if (TaskType.NEWS_FETCH_LATEST == type) {
                    mLatestNewsTask = null;
                }
                break;

            case TaskType.NEWS_FETCH_SUGGESTED:
                if (MsResponse.isSuccessful(mr)) {
                    updateRedDot(0);
                    setLoadSuggestedTime();
                } else {
                    toast(MsResponse.getFailureDesc(mContext,
                            R.string.news_tst_fetch_list_failed, mr.code));
                }
                break;

            case TaskType.NEWS_FETCH_SUGGESTED_COUNT:
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    updateRedDot((Integer) mr.value);
                }
                break;

            case TaskType.NEWS_FETCH_CMT:
                if (!MsResponse.isSuccessful(mr)) {
                    toast(MsResponse.getFailureDesc(mContext,
                            R.string.news_tst_fetch_cmt_failed, mr.code));
                }
                break;

            case TaskType.NEWS_FAVORITE:
                if (mr.value != null) {
                    News news = (News) mr.value;
                    if (MsResponse.isSuccessful(mr)) {
                        toast(news.favorite ? R.string.news_tst_unfavorite_success
                                : R.string.news_tst_favorite_success);
                        news.favorite = !news.favorite;
                    } else {
                        int resId = news.favorite ? R.string.news_tst_unfavorite_failed :
                                R.string.news_tst_favorite_failed;
                        toast(MsResponse.getFailureDesc(mContext, resId, mr.code));
                    }
                }
                break;

            case TaskType.NEWS_LIKE:
                if (mr.value != null) {
                    News news = (News) mr.value;
                    if (MsResponse.isSuccessful(mr)) {
                        toast(news.liked ? R.string.news_tst_unlike_success
                                : R.string.news_tst_like_success);
                        news.liked = !news.liked;
                    } else {
                        int resId = news.liked ? R.string.news_tst_unlike_failed :
                                R.string.news_tst_like_failed;
                        toast(MsResponse.getFailureDesc(mContext, resId, mr.code));
                    }
                }
                break;

            case TaskType.NEWS_COMMENT:
                if (mr.value != null) {
                    if (MsResponse.isSuccessful(mr)) {
                        toast(R.string.news_tst_comment_success);
                    } else {
                        toast(MsResponse.getFailureDesc(mContext,
                                R.string.news_tst_comment_failed, mr.code));
                    }
                }
                break;

            case TaskType.NEWS_DELETE_CMT:
                if (!MsResponse.isSuccessful(mr)) {
                    toast(MsResponse.getFailureDesc(
                            mContext, R.string.cf_delete_failed, mr.code));
                }
                break;

            case TaskType.NEWS_TICKET:
                if (mr.value != null) {
                    if (!MsResponse.isSuccessful(mr)) {
                        toast(MsResponse.getFailureDesc(mContext,
                                R.string.news_tst_ticket_failed, mr.code));
                    }
                }
                break;

            default:
                break;
        }

        for (TaskExecutionListener listener : mListeners) {
            listener.onPostExecute(type, mr);
        }
    }

    public List<News> loadLatestNews() {
        return loadNews(SP_LATEST_NEWS);
    }

    public List<News> loadFavoriteNews() {
        return loadNews(SP_FAVORITE_NEWS);
    }

    public void setLoadSuggestedTime() {
        mLastLoadSuggested = System.currentTimeMillis() / 1000 + (Utils.isDebug() ? 0 : 120);
        mSharedPrefs.edit().putLong(SP_LAST_LOAD_SUGGESTED, mLastLoadSuggested).commit();
    }

    public void saveLatestNews(List<News> newsList) {
        if (newsList != null) {
            saveNews(SP_LATEST_NEWS, newsList, LATEST_NEWS_COUNT_LIMIT);
        }
    }

    public void saveFavoriteNews(List<News> newsList) {
        if (newsList != null) {
            saveNews(SP_FAVORITE_NEWS, newsList, 0);
        }
    }

    public void registerTaskListener(TaskExecutionListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(0, listener);
        }
    }

    public void unregisterTaskListener(TaskExecutionListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    public void startNewsFetchLatestTask(long time, int listType) {
        startNewsFetchLatestTask(time, listType, null, 0);
    }
    
    public void startNewsFetchLatestTask(long time, int listType, int limit) {
        startNewsFetchLatestTask(time, listType, null, limit);
    }

    public void startNewsFetchLatestTask(long time, int listType, int[] filterTypes) {
        startNewsFetchLatestTask(time, listType, filterTypes, 0);
    }
    
    public void startNewsFetchLatestTask(long time, int listType, int[] filterTypes, int limit) {
        if (mLatestNewsTask != null) {
            mLatestNewsTask.cancel(true);
        }
        mLatestNewsTask = new NewsTask(mContext, TaskType.NEWS_FETCH_LATEST);
        mLatestNewsTask.setTime(time).setListType(listType).setTypeFilters(filterTypes)
                .setListener(this).setLimit(limit).executeLong();
    }

    public void startNewsFetchSuggestedTask() {
        new NewsTask(mContext, TaskType.NEWS_FETCH_SUGGESTED)
                .setListener(this).executeLong();
    }

    public void startNewsSuggestedCountTask() {
        new NewsTask(mContext, TaskType.NEWS_FETCH_SUGGESTED_COUNT).setTime(mLastLoadSuggested)
                .setListener(this).executeLong();
    }

    public void startNewsFetchCommentTask(News news, int offset) {
        new NewsTask(mContext, TaskType.NEWS_FETCH_CMT).setNews(news)
                .setOffset(offset).setListener(this).executeLong();
    }

    public void startFetchFavoriteTask(int offset) {
        new NewsTask(mContext, TaskType.NEWS_FETCH_MY_FAV).setOffset(offset)
                .setListener(this).executeLong();
    }

    public void startNewsFavoriteTask(News news) {
        new NewsTask(mContext, TaskType.NEWS_FAVORITE).setNews(news)
                .setListener(this).executeLong();
    }

    public void startNewsLikeTask(News news) {
        new NewsTask(mContext, TaskType.NEWS_LIKE).setNews(news)
                .setListener(this).executeLong();
    }

    public void startNewsCommentTask(News news, NewsComment target) {
        new NewsTask(mContext, TaskType.NEWS_COMMENT).setNews(news)
                .setTarget(target).setListener(this).executeLong();
    }

    public void startNewsDeleteCommentTask(News news, NewsComment target) {
        new NewsTask(mContext, TaskType.NEWS_DELETE_CMT).setNews(news)
                .setTarget(target).setListener(this).executeLong();
    }

    public void startNewsTicketTask(News news) {
        new NewsTask(mContext, TaskType.NEWS_TICKET).setNews(news)
                .setListener(this).executeLong();
    }

    public void startSourceProfileTask(NewsSource source) {
        new NewsSourceTask(mContext, TaskType.NEWS_SOURCE_PROFILE).setSource(source)
                .setListener(this).executeLong();
    }

    public void startSourceFollowTask(NewsSource source) {
        new NewsSourceTask(mContext, TaskType.NEWS_SOURCE_FOLLOW).setSource(source)
                .setListener(this).executeLong();
    }

    public void startSourceNewsTask(NewsSource source, int offset) {
        new NewsSourceTask(mContext, TaskType.NEWS_SOURCE_NEWS).setSource(source)
                .setOffset(offset).setListener(this).executeLong();
    }

    public void startSourcesFollowedTask(int offset) {
        new NewsSourceTask(mContext, TaskType.NEWS_SOURCES_FOLLOWED).setOffset(offset)
                .setListener(this).executeLong();
    }

    public void startSourcesSearchTask(String keyword, int offset) {
        new NewsSourceTask(mContext, TaskType.NEWS_SOURCES_SEARCH).setKeyword(keyword)
                .setOffset(offset).setListener(this).executeLong();
    }

    public boolean isFetchingLatestNews() {
        return mLatestNewsTask != null;
    }

    public void clear() {
        mSharedPrefs.edit().clear().commit();
        mListeners.clear();
        sInstanceRef.clear();
    }

    public void updateNews(int type, News news, News value) {
        if (TaskType.NEWS_FAVORITE == type) {
            news.favorite = value.favorite;
        } else if (TaskType.NEWS_LIKE == type) {
            news.liked = value.liked;
            if (news.liked) {
                news.likedCount++;
            } else {
                news.likedCount--;
            }
        } else if (TaskType.NEWS_COMMENT == type) {
            news.commentedCount++;
        } else if (TaskType.NEWS_DELETE_CMT == type) {
            news.commentedCount--;
        } else if (TaskType.NEWS_TICKET == type) {
            news.action = value.action;
        }
    }

    public String getQrCodeFileName(String code) {
        String filename = getCacheFileName(code);
        if (new File(filename).isFile()) {
            return filename;
        } else {
            new NewsQrCodeTask(mContext).setCode(code)
                    .setListener(this).executeQuick();
        }
        return null;
    }

    private String getCacheFileName(String name) {
        return new StringBuilder().append(mContext.getCacheDir().getAbsolutePath())
                .append(name).toString();
    }

    private List<News> loadNews(String key) {
        List<News> newsList = new ArrayList<News>();
        String newsString = mSharedPrefs.getString(key, null);
        if (!TextUtils.isEmpty(newsString)) {
            try {
                newsList.addAll(toNewsList(new JSONArray(newsString)));
            } catch (JSONException e) {
                Utils.logE(TAG, "Error in loadNews(" + key + "): " + e.getMessage());
            }
        }
        return newsList;
    }

    private void saveNews(String key, List<News> newsList, int limit) {
        JSONArray newsArray = new JSONArray();
        int count = 0;
        for (News news : newsList) {
            newsArray.put(news.toJson());
            count++;
            if (limit > 0 && count == limit) {
                break;
            }
        }
        mSharedPrefs.edit().putString(key, newsArray.toString()).commit();
    }

    private List<News> toNewsList(JSONArray array) {
        List<News> list = new ArrayList<News>();
        for (int i = 0; i < array.length(); i++) {
            News news = News.fromJson(array.optJSONObject(i));
            if (news != null) {
                list.add(news);
            }
        }
        return list;
    }

    private List<NewsComment> toNewsCommentList(JSONArray array) {
        List<NewsComment> list = new ArrayList<NewsComment>();
        for (int i = 0; i < array.length(); i++) {
            NewsComment comment = NewsComment.fromJson(array.optJSONObject(i));
            if (comment != null) {
                list.add(comment);
            }
        }
        return list;
    }

    private List<NewsSource> toNewsSourceList(JSONArray array) {
        List<NewsSource> list = new ArrayList<NewsSource>();
        for (int i = 0; i < array.length(); i++) {
            NewsSource source = NewsSource.fromJson(array.optJSONObject(i));
            if (source != null) {
                list.add(source);
            }
        }
        return list;
    }

    private void toast(CharSequence text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    private void toast(int resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
    }

    private void updateRedDot(int count) {
        RedDot.getInstance().update(RedDotType.MY_COLLEGE, count);
    }

    private class NewsTask extends BaseTask {
        private static final String API_NEWS = "api/broadcast";
        private static final String API_TICKET = "api/ticket";

        private static final String ACTION_FETCH_LATEST = "broadcast_latest";
        private static final String ACTION_FETCH_HOT = "broadcast_hot";
        private static final String ACTION_FETCH_FAV = "broadcast_fav";
        private static final String ACTION_FETCH_MY_FAV = "list_my_fav";
        private static final String ACTION_FETCH_SUGGESTED = "broadcast_suggested";
        private static final String ACTION_FETCH_COUNT = "count_new";
        private static final String ACTION_FETCH_SUGGESTED_COUNT = "count_new_suggested";
        private static final String ACTION_FETCH_CMT = "list_comment";
        private static final String ACTION_FAVORITE = "fav";
        private static final String ACTION_LIKE = "like";
        private static final String ACTION_COMMENT = "comment";
        private static final String ACTION_DELETE_CMT = "delete_comment";
        private static final String ACTION_TICKET = "grab";

        private static final String PARAM_TIME = "time=";
        private static final String PARAM_ID = "bid=";
        private static final String PARAM_COMMENT = "content=";
        private static final String PARAM_TARGET = "target_id=";
        private static final String PARAM_COMMENT_ID = "comment_id=";

        private News mNews;
        private long mTime;
        private int mListType;
        private int[] mTypeFilters;
        private NewsComment mTarget;
        private int mLimit;
        
        public NewsTask(Context context, int type) {
            super(context, type);
        }

        public NewsTask setNews(News news) {
            mNews = news;
            return this;
        }

        public NewsTask setTime(long time) {
            mTime = time;
            return this;
        }

        public NewsTask setListType(int listType) {
            mListType = listType;
            return this;
        }
        public NewsTask setTypeFilters(int[] filters) {
            mTypeFilters = filters;
            return this;
        }

        public NewsTask setTarget(NewsComment target) {
            mTarget = target;
            return this;
        }

        public NewsTask setLimit(int limit) {
            mLimit = limit;
            return this;
        }
        
        @Override
        protected boolean isGet() {
            switch (mType) {
                case TaskType.NEWS_FAVORITE:
                case TaskType.NEWS_LIKE:
                case TaskType.NEWS_COMMENT:
                case TaskType.NEWS_DELETE_CMT:
                case TaskType.NEWS_TICKET:
                    return false;

                default:
                    return true;
            }
        }

        @Override
        protected String getApi() {
            switch (mType) {
                case TaskType.NEWS_TICKET:
                    return API_TICKET;

                default:
                    return API_NEWS;
            }
        }

        @Override
        protected String getAction() {
            switch (mType) {
                case TaskType.NEWS_FETCH_LATEST:
                    return ACTION_FETCH_LATEST;

                case TaskType.NEWS_FETCH_HOT:
                    return ACTION_FETCH_HOT;

                case TaskType.NEWS_FETCH_FAV:
                    return ACTION_FETCH_FAV;

                case TaskType.NEWS_FETCH_MY_FAV:
                    return ACTION_FETCH_MY_FAV;

                case TaskType.NEWS_FETCH_SUGGESTED:
                    return ACTION_FETCH_SUGGESTED;

                case TaskType.NEWS_FETCH_COUNT:
                    return ACTION_FETCH_COUNT;

                case TaskType.NEWS_FETCH_SUGGESTED_COUNT:
                    return ACTION_FETCH_SUGGESTED_COUNT;

                case TaskType.NEWS_FETCH_CMT:
                    return ACTION_FETCH_CMT;

                case TaskType.NEWS_FAVORITE:
                    return ACTION_FAVORITE;

                case TaskType.NEWS_LIKE:
                    return ACTION_LIKE;

                case TaskType.NEWS_COMMENT:
                    return ACTION_COMMENT;

                case TaskType.NEWS_DELETE_CMT:
                    return ACTION_DELETE_CMT;

                case TaskType.NEWS_TICKET:
                    return ACTION_TICKET;

                default:
                    return null;
            }
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            switch (mType) {
                case TaskType.NEWS_FETCH_LATEST:
                    sb.append(PARAM_TIME).append(mTime / 1000);
                    if (mListType > 0) {
                        sb.append("&type=").append(mListType);
                    }
                    if (mTypeFilters != null && mTypeFilters.length > 0) {
                        sb.append("&filter_types=").append(
                                Utils.join(Utils.COMMA_DELIMITER, mTypeFilters));
                    }
                    break;

                case TaskType.NEWS_FETCH_SUGGESTED_COUNT:
                    sb.append(PARAM_TIME).append(mTime);
                    break;

                case TaskType.NEWS_FETCH_HOT:
                case TaskType.NEWS_FETCH_FAV:
                case TaskType.NEWS_FETCH_MY_FAV:
                    sb.append(PARAM_OFFSET).append(mOffset);
                    break;

                case TaskType.NEWS_FETCH_CMT:
                    sb.append(PARAM_ID).append(mNews == null ? 0 : mNews.id)
                            .append("&").append(PARAM_OFFSET).append(mOffset);
                    break;

                case TaskType.NEWS_LIKE:
                case TaskType.NEWS_TICKET:
                    sb.append(PARAM_ID).append(mNews == null ? 0 : mNews.id);
                    break;

                case TaskType.NEWS_FAVORITE:
                    sb.append(PARAM_ID).append(mNews == null ? 0 : mNews.id).append(
                            "&fav=" + ((mNews != null && mNews.favorite) ? 0 : 1));
                    break;

                case TaskType.NEWS_COMMENT:
                    sb.append(PARAM_ID).append(mNews == null ? 0 : mNews.id)
                            .append("&").append(PARAM_TARGET)
                            .append(mTarget == null ? 0 : mTarget.id)
                            .append("&").append(PARAM_COMMENT)
                            .append(mNews == null ? "" : Utils.urlEncode(mNews.comment));
                    break;

                case TaskType.NEWS_DELETE_CMT:
                    sb.append(PARAM_COMMENT_ID).append(mTarget == null ? 0 : mTarget.id);
                    break;

                default:
                    break;
            }
            if (mLimit > 0) {
                sb.append("&limit=").append(mLimit);
            }
            return sb.toString();
        }

        @Override
        protected Object getResponseValue(MsResponse mr) throws Exception {
            switch (mType) {
                case TaskType.NEWS_FETCH_LATEST:
                case TaskType.NEWS_FETCH_HOT:
                case TaskType.NEWS_FETCH_FAV:
                case TaskType.NEWS_FETCH_MY_FAV:
                    if (MsResponse.isSuccessful(mr)) {
                        return toNewsList(new JSONArray(mr.response));
                    }
                    break;

                case TaskType.NEWS_FETCH_SUGGESTED_COUNT:
                    if (MsResponse.isSuccessful(mr)) {
                        return Integer.valueOf(mr.response);
                    }
                    break;

                case TaskType.NEWS_FETCH_CMT:
                    if (MsResponse.isSuccessful(mr)) {
                        return toNewsCommentList(new JSONArray(mr.response));
                    }
                    break;

                case TaskType.NEWS_FAVORITE:
                case TaskType.NEWS_LIKE:
                case TaskType.NEWS_COMMENT:
                    return mNews;

                case TaskType.NEWS_DELETE_CMT:
                    if (MsResponse.isSuccessful(mr)) {
                        mNews.action = mTarget;
                    }
                    return mNews;

                case TaskType.NEWS_TICKET:
                    if (MsResponse.isSuccessful(mr)) {
                        mNews.action = News.Ticket.fromJson(new JSONObject(mr.response));
                    }
                    return mNews;

                default:
                    break;
            }
            return null;
        }
    }

    private class NewsSourceTask extends BaseTask {
        private static final String API_NEWS = "api/broadcast";

        private static final String ACTION_PROFILE = "broadcaster_profile";
        private static final String ACTION_FOLLOW = "follow_broadcaster";
        private static final String ACTION_NEWS = "list_by_broadcaster";
        private static final String ACTION_FOLLOWED = "broadcaster_followed";
        private static final String ACTION_SEARCH = "broadcaster_search";

        private static final String PARAM_ID = "broadcaster_id=";
        private static final String PARAM_GUID = "guid=";
        private static final String PARAM_FOLLOW = "follow=";
        private static final String PARAM_KEYWORDS = "keywords=";

        private NewsSource mSource;
        private String mKeyword;

        public NewsSourceTask(Context context, int type) {
            super(context, type);
        }

        public NewsSourceTask setSource(NewsSource source) {
            mSource = source;
            return this;
        }

        public NewsSourceTask setKeyword(String keyword) {
            mKeyword = keyword;
            return this;
        }

        @Override
        protected boolean isGet() {
            switch (mType) {
                case TaskType.NEWS_SOURCE_FOLLOW:
                    return false;

                default:
                    return true;
            }
        }

        @Override
        protected String getApi() {
            return API_NEWS;
        }

        @Override
        protected String getAction() {
            switch (mType) {
                case TaskType.NEWS_SOURCE_PROFILE:
                    return ACTION_PROFILE;

                case TaskType.NEWS_SOURCE_FOLLOW:
                    return ACTION_FOLLOW;

                case TaskType.NEWS_SOURCE_NEWS:
                    return ACTION_NEWS;

                case TaskType.NEWS_SOURCES_FOLLOWED:
                    return ACTION_FOLLOWED;

                case TaskType.NEWS_SOURCES_SEARCH:
                    return ACTION_SEARCH;

                default:
                    return null;
            }
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            switch (mType) {
                case TaskType.NEWS_SOURCE_PROFILE:
                    sb.append(PARAM_ID).append(mSource == null ? 0 : mSource.id)
                            .append("&").append(PARAM_GUID)
                            .append(mSource == null ? 0 : mSource.guid);
                    break;

                case TaskType.NEWS_SOURCE_FOLLOW:
                    sb.append(PARAM_ID).append(mSource == null ? 0 : mSource.id)
                            .append("&").append(PARAM_FOLLOW)
                            .append(mSource == null || mSource.followed ? 0 : 1);
                    break;

                case TaskType.NEWS_SOURCE_NEWS:
                    sb.append(PARAM_ID).append(mSource == null ? 0 : mSource.id)
                            .append("&").append(PARAM_OFFSET).append(mOffset);
                    break;

                case TaskType.NEWS_SOURCES_FOLLOWED:
                    sb.append(PARAM_OFFSET).append(mOffset);
                    break;

                case TaskType.NEWS_SOURCES_SEARCH:
                    sb.append(PARAM_OFFSET).append(mOffset)
                            .append("&").append(PARAM_KEYWORDS).append(Utils.urlEncode(mKeyword));
                    break;

                default:
                    break;
            }
            return sb.toString();
        }

        @Override
        protected Object getResponseValue(MsResponse mr) throws Exception {
            switch (mType) {
                case TaskType.NEWS_SOURCE_PROFILE:
                    if (MsResponse.isSuccessful(mr)) {
                        return NewsSource.fromJson(new JSONObject(mr.response));
                    }
                    break;

                case TaskType.NEWS_SOURCE_FOLLOW:
                    return mSource;

                case TaskType.NEWS_SOURCE_NEWS:
                    if (MsResponse.isSuccessful(mr)) {
                        return toNewsList(new JSONArray(mr.response));
                    }
                    break;

                case TaskType.NEWS_SOURCES_FOLLOWED:
                case TaskType.NEWS_SOURCES_SEARCH:
                    if (MsResponse.isSuccessful(mr)) {
                        return toNewsSourceList(new JSONArray(mr.response));
                    }
                    break;

                default:
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(MsResponse mr) {
            switch (mType) {
                case TaskType.NEWS_SOURCE_PROFILE:
                    if (mSource != null) {
                        if (MsResponse.isSuccessful(mr) && mr.value != null) {
                            mSource.copy((NewsSource) mr.value);
                        } else {
                            toast(MsResponse.getFailureDesc(mContext,
                                    R.string.news_source_tst_profile_failed, mr.code));
                        }
                    }
                    break;

                case TaskType.NEWS_SOURCE_FOLLOW:
                    if (mSource != null) {
                        mSource.following = false;
                        if (MsResponse.isSuccessful(mr)) {
                            mSource.followed = !mSource.followed;
                            if (mSource.followed) {
                                mSource.followerCount++;
                            } else {
                                mSource.followerCount--;
                            }
                        } else {
                            toast(MsResponse.getFailureDesc(mContext,
                                    R.string.news_source_tst_follow_failed, mr.code));
                        }
                    }
                    break;

                case TaskType.NEWS_SOURCE_NEWS:
                    if (!MsResponse.isSuccessful(mr)) {
                        toast(MsResponse.getFailureDesc(mContext,
                                R.string.news_source_tst_news_failed, mr.code));
                    }
                    break;

                case TaskType.NEWS_SOURCES_FOLLOWED:
                case TaskType.NEWS_SOURCES_SEARCH:
                    if (!MsResponse.isSuccessful(mr)) {
                        toast(MsResponse.getFailureDesc(mContext,
                                R.string.news_sources_tst_fetch_error, mr.code));
                    }
                    break;

                default:
                    break;
            }
            super.onPostExecute(mr);
        }
    }

    private class NewsQrCodeTask extends BaseTask {
        private String mCode;

        public NewsQrCodeTask(Context context) {
            super(context, TaskType.NEWS_QR_CODE);
        }

        public NewsQrCodeTask setCode(String code) {
            mCode = code;
            return this;
        }

        @Override
        protected boolean isGet() {
            return false;
        }

        @Override
        protected String getApi() {
            return null;
        }

        @Override
        protected String getAction() {
            return null;
        }

        @Override
        protected String buildParams() {
            return null;
        }

        @Override
        protected Object getResponseValue(MsResponse mr) throws Exception {
            return null;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            int size = mContext.getResources().getDimensionPixelSize(R.dimen.qrc_size);
            Bitmap bmp = Utils.makeQrCodeBitmap(mCode, size);
            if (bmp != null) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(getCacheFileName(mCode));
                    bmp.compress(CompressFormat.PNG, 100, fos);
                    MsResponse mr = new MsResponse();
                    mr.code = MsResponse.MS_SUCCESS;
                    return mr;
                } catch (IOException e) {
                    Utils.logE(TAG, "Error in create Qr code file: " + e.getMessage());
                } finally {
                    bmp.recycle();
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            return null;
        }
    }
}
