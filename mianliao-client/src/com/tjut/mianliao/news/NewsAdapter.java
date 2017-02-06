package com.tjut.mianliao.news;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.image.ImageActivity;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class NewsAdapter extends ArrayAdapter<News> implements TaskExecutionListener,
        OnClickListener {

    protected Context mContext;
    protected LayoutInflater mInflater;
    private NewsManager mNewsManager;
    private boolean mSourceShown;
    private int mCountLimit = Integer.MAX_VALUE;

    public NewsAdapter(Context context) {
        super(context, 0);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mNewsManager = NewsManager.getInstance(context);
        mNewsManager.registerTaskListener(this);
    }

    public void setSourceShown(boolean shown) {
        mSourceShown = shown;
    }

    public void setCountLimit(int limit) {
        mCountLimit = limit;
    }

    public boolean hasItem(News item) {
        int position = getPosition(item);
        return position != -1 && getItem(position) == item;
    }

    public void append(List<News> newsList) {
        super.addAll(newsList);
    }

    public void reset(List<News> newsList) {
        setNotifyOnChange(false);
        clear();
        append(newsList);
        notifyDataSetChanged();
    }

    public void destroy() {
        mNewsManager.unregisterTaskListener(this);
    }

    @Override
    public int getCount() {
        int count = super.getCount();
        return count < mCountLimit ? count : mCountLimit;
    }

    @Override
    public int getViewTypeCount() {
        return News.TYPES_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_news_item, parent, false);
        } else {
            view = convertView;
        }
        News news = getItem(position);
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvTitle.setText(news.title);
        tvTitle.setCompoundDrawablesWithIntrinsicBounds(news.getTitleIcon(), 0, 0, 0);

        TextView tvSource = (TextView) view.findViewById(R.id.tv_source);
        tvSource.setVisibility(View.VISIBLE);
        tvSource.setText(news.sourceName);

        ProImageView ivThumb = (ProImageView) view.findViewById(R.id.iv_thumb);
        ivThumb.setImage(news.thumbnail, R.drawable.pic_news_list_default);
        ivThumb.setTag(TextUtils.isEmpty(news.cover) ? null : news.cover);
        ivThumb.setOnClickListener(this);

        Utils.setText(view, R.id.tv_like,
                mContext.getString(R.string.news_liked, news.likedCount));
        Utils.setText(view, R.id.tv_reply, mContext.getString(
                R.string.news_commented_count_num, news.commentedCount));

        return view;
    }

    @Override
    public void onPreExecute(int type) {
    }

    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case TaskType.NEWS_FAVORITE:
            case TaskType.NEWS_LIKE:
            case TaskType.NEWS_COMMENT:
            case TaskType.NEWS_DELETE_CMT:
            case TaskType.NEWS_TICKET:
                News value = (News) mr.value;
                int position = getPosition(value);
                if (position != -1 && MsResponse.isSuccessful(mr) && value != null) {
                    mNewsManager.updateNews(type, getItem(position), value);
                }
                notifyDataSetChanged();
                break;

            case TaskType.NEWS_QR_CODE:
                if (MsResponse.isSuccessful(mr)) {
                    notifyDataSetChanged();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_thumb:
                if (v.getTag() != null && v.getTag() instanceof String) {
                    Intent ivi = new Intent(mContext, ImageActivity.class);
                    ivi.putExtra(ImageActivity.EXTRA_IMAGE_URL, (String) v.getTag());
                    mContext.startActivity(ivi);
                }
                break;

            default:
                break;
        }
    }
}
