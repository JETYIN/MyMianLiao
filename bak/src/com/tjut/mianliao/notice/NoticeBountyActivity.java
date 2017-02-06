package com.tjut.mianliao.notice;

import android.content.Intent;

import com.tjut.mianliao.R;
import com.tjut.mianliao.bounty.BountyDetailsActivity;
import com.tjut.mianliao.data.bounty.BountyRating;
import com.tjut.mianliao.data.bounty.BountyTask;
import com.tjut.mianliao.data.notice.Notice;
import com.tjut.mianliao.util.Utils;

public class NoticeBountyActivity extends NoticeListActivity {

    @Override
    protected Item getItem(Notice notice) {
        Item item = super.getItem(notice);
        switch (notice.category) {
            case Notice.CAT_BOUNTY_REQUEST:
                if (notice.btyContract != null && notice.btyTask != null) {
                    item.userInfo = notice.btyContract.userCredit.userInfo;
                    item.time = notice.btyContract.createTime;
                    item.category = getString(R.string.ntc_bounty_request);
                    item.desc = notice.btyTask.desc;
                }
                break;

            case Notice.CAT_BOUNTY_AT:
                if (notice.btyTask != null) {
                    item.userInfo = notice.btyTask.userCredit.userInfo;
                    item.time = notice.btyTask.ctime;
                    item.category = getString(R.string.ntc_bounty_at);
                    item.desc = notice.btyTask.desc;
                }
                break;

            case Notice.CAT_BOUNTY_GUEST_RATED:
            case Notice.CAT_BOUNTY_HOST_RATED:
                if (notice.btyContract != null && notice.btyTask != null) {
                    item.userInfo = notice.category == Notice.CAT_BOUNTY_GUEST_RATED
                            ? notice.btyContract.userCredit.userInfo
                            : notice.btyTask.userCredit.userInfo;
                    item.time = notice.btyContract.updateTime;
                    item.category = Utils.getColoredText(
                            getString(R.string.ntc_bounty_rated, notice.btyTask.desc),
                            notice.btyTask.desc, mKeyColor, false);
                    item.desc = getString(R.string.ms_failure_template, notice.btyRatingCmt,
                            getString(BountyRating.getRatingDesc(notice.btyRating)));
                }
                break;

            default:
                break;
        }
        return item;
    }


    @Override
    protected void onItemClick(Notice notice) {
        viewTask(notice.btyTask);
    }

    private void viewTask(BountyTask task) {
        Intent intent = new Intent(this, BountyDetailsActivity.class);
        intent.putExtra(BountyTask.INTENT_EXTRA_NAME, task);
        startActivity(intent);
    }
}
