package com.tjut.mianliao.notice;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.notice.Notice;
import com.tjut.mianliao.forum.ReplyActivity;
import com.tjut.mianliao.news.NewsDetailsActivity;
import com.tjut.mianliao.news.NewsManager;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class NoticeNewsActivity extends NoticeListActivity implements
        OnClickListener, TaskExecutionListener {

    private static final int REQUEST_REPLY = 101;

    private NewsManager mNewsManager;

    private LightDialog mMenuDialog;

    private Notice mNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNewsManager = NewsManager.getInstance(this);
        mNewsManager.registerTaskListener(this);
    }

    @Override
    protected void onDestroy() {
        mNewsManager.unregisterTaskListener(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REPLY && resultCode == RESULT_OK) {
            doReply(data == null ? null : data.getStringExtra(ReplyActivity.EXTRA_RESULT));
        }
    }

    @Override
    protected Item getItem(Notice notice) {
        Item item = super.getItem(notice);
        switch (notice.category) {
            case Notice.CAT_NEWS_CMT_REPLY:
            case Notice.CAT_NEWS_CMT_AT:
                if (notice.news != null && notice.newsComment != null) {
                    item.userInfo = notice.newsComment.userInfo;
                    item.time = notice.newsComment.time;
                    int resId = notice.category == Notice.CAT_NEWS_CMT_REPLY
                            ? R.string.ntc_comment_reply : R.string.ntc_comment_at;
                    item.category = Utils.getColoredText(
                            getString(resId, notice.news.title),
                            notice.news.title, mKeyColor, false);
                    item.desc = notice.newsComment.content;
                }
                break;

            default:
                break;
        }
        return item;
    }

    @Override
    protected void onItemClick(Notice notice) {
        mNotice = notice;
        showMenuDialog();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0:
                showReplyActivity();
                break;

            case 1:
                viewNews(mNotice.news);
                break;

            default:
                break;
        }
    }

    @Override
    public void onPreExecute(int type) {
        if (TaskType.NEWS_COMMENT == type) {
            getTitleBar().showProgress();
        }
    }

    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case TaskType.NEWS_LIKE:
            case TaskType.NEWS_COMMENT:
                getTitleBar().hideProgress();
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    News value = (News) mr.value;
                    mNewsManager.updateNews(type, mNotice.news, value);
                }
                break;

            default:
                break;
        }
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this);
            mMenuDialog.setTitle(R.string.please_choose);
            mMenuDialog.setItems(R.array.ntc_news_menu, this);
        }
        mMenuDialog.show();
    }

    private void showReplyActivity() {
        Intent intent = new Intent(this, ReplyActivity.class);
        startActivityForResult(intent, REQUEST_REPLY);
    }

    private void viewNews(News news) {
        Intent intent = new Intent(this, NewsDetailsActivity.class);
        intent.putExtra(News.INTENT_EXTRA_NAME, new News(news));
        startActivity(intent);
    }

    private void doReply(String reply) {
        if (!TextUtils.isEmpty(reply)) {
            mNotice.news.comment = reply;
            mNewsManager.startNewsCommentTask(mNotice.news, mNotice.newsComment);
        }
    }
}
