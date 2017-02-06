package com.tjut.mianliao.news;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.NewsSource;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class NewsSourceAdapter extends ArrayAdapter<NewsSource> implements
        TaskExecutionListener, OnClickListener {

    private NewsManager mNewsManager;

    private boolean mActionEnabled;
    private String mKeywords;

    public NewsSourceAdapter(Context context) {
        super(context, 0);
        mNewsManager = NewsManager.getInstance(context);
        mNewsManager.registerTaskListener(this);
    }

    public void setActionEnabled(boolean enabled) {
        mActionEnabled = enabled;
    }

    public void setKeywords(String keywords) {
        mKeywords = keywords;
    }

    public void append(List<NewsSource> sourceList) {
        super.addAll(sourceList);
    }

    public void reset(List<NewsSource> sourceList) {
        setNotifyOnChange(false);
        clear();
        append(sourceList);
        notifyDataSetChanged();
    }

    public void update(NewsSource source) {
        int position = getPosition(source);
        if (position != -1) {
            getItem(position).copy(source);
        }
        notifyDataSetChanged();
    }

    public void destroy() {
        mNewsManager.unregisterTaskListener(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_news_source, parent, false);
        }
        NewsSource source = getItem(position);

        ((ProImageView) view.findViewById(R.id.av_avatar))
                .setImage(source.avatar, R.drawable.ic_news_source_avatar);

        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        int color = getContext().getResources().getColor(R.color.txt_keyword);
        tvName.setText(Utils.getColoredText(source.name, mKeywords, color));

        TextView tvDesc = (TextView) view.findViewById(R.id.tv_desc);
        tvDesc.setText(source.description);

        ImageView ivFollow = (ImageView) view.findViewById(R.id.iv_follow);
        ivFollow.setTag(source);
        ivFollow.setOnClickListener(this);
        View pbFollow = view.findViewById(R.id.pb_follow);
        if (mActionEnabled) {
            ivFollow.setVisibility(View.VISIBLE);
            if (source.following) {
                pbFollow.setVisibility(View.VISIBLE);
                ivFollow.setImageDrawable(null);
            } else {
                pbFollow.setVisibility(View.GONE);
                if (source.followed) {
                    ivFollow.setBackgroundResource(R.drawable.selector_btn_red);
                    ivFollow.setImageResource(R.drawable.btn_news_source_unfollow);
                } else {
                    ivFollow.setBackgroundResource(R.drawable.selector_btn_blue);
                    ivFollow.setImageResource(R.drawable.btn_news_source_follow);
                }
            }
        } else {
            ivFollow.setVisibility(View.GONE);
            pbFollow.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onPreExecute(int type) {
    }

    @Override
    public void onPostExecute(int type, MsResponse mr) {
        if (TaskType.NEWS_SOURCE_FOLLOW == type) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_follow && v.getTag() != null) {
            NewsSource source = (NewsSource) v.getTag();
            if (!source.following) {
                source.following = true;
                notifyDataSetChanged();
                mNewsManager.startSourceFollowTask(source);
            }
        }
    }
}
