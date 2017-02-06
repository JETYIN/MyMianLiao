package com.tjut.mianliao.component.news;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProgressButton;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.News.Ticket;
import com.tjut.mianliao.news.NewsManager;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class TicketView extends LinearLayout implements
        View.OnClickListener, TaskExecutionListener {

    private News mNews;
    private TextView mTvTip;
    private TextView mTvCongrat;
    private TextView mTvCode;
    private ImageView mIvCode;
    private ProgressButton mPbAction;
    private int mQrSize;

    private NewsManager mNewsManager;
    private boolean mLayoutInflated;

    public TicketView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
    }

    public void show(News news) {
        if (!news.isTicketType()) {
            return;
        }

        if (!mLayoutInflated) {
            init();
        }
        mNews = news;
        showTicket();
    }

    public void destroy() {
        if (mLayoutInflated) {
            mNewsManager.unregisterTaskListener(this);
        }
    }

    private void init() {
        mNewsManager = NewsManager.getInstance(getContext());
        mNewsManager.registerTaskListener(this);
        mLayoutInflated = true;
        mQrSize = getResources().getDimensionPixelSize(R.dimen.qrc_size);
        LayoutInflater.from(getContext()).inflate(R.layout.news_ticket_view, this, true);
        mTvTip = (TextView) findViewById(R.id.tv_tip);
        mTvCongrat = (TextView) findViewById(R.id.tv_congrat);
        mTvCode = (TextView) findViewById(R.id.tv_code);
        mIvCode = (ImageView) findViewById(R.id.iv_code);
        mPbAction = (ProgressButton) findViewById(R.id.pb_action);
        mPbAction.setOnClickListener(this);
    }

    private void showTicket() {
        Ticket ticket = (Ticket) mNews.action;
        mTvTip.setText(ticket.tip);
        if (TextUtils.isEmpty(ticket.code)) {
            mTvCongrat.setVisibility(GONE);
            mTvCode.setVisibility(GONE);
            mIvCode.setVisibility(GONE);
            mPbAction.setVisibility(VISIBLE);
            mPbAction.setEnabled(ticket.enabled);
            mPbAction.setText(ticket.button);
        } else {
            mTvCongrat.setVisibility(VISIBLE);
            mTvCode.setVisibility(VISIBLE);
            mIvCode.setVisibility(VISIBLE);
            mPbAction.setVisibility(GONE);
            mTvCode.setText(ticket.code);
            mIvCode.setImageBitmap(Utils.makeQrCodeBitmap(ticket.code, mQrSize));
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mPbAction && !mPbAction.isInProgress()) {
            mNewsManager.startNewsTicketTask(mNews);
        }
    }

    @Override
    public void onPreExecute(int type) {
        if (TaskType.NEWS_TICKET == type) {
            mPbAction.setInProgress(true);
        }
    }

    @Override
    public void onPostExecute(int type, MsResponse mr) {
        if (TaskType.NEWS_TICKET == type) {
            mPbAction.setInProgress(false);
            if (MsResponse.isSuccessful(mr) && mr.value != null) {
                showTicket();
            }
        }
    }
}
