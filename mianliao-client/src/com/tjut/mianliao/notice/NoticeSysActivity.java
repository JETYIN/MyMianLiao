package com.tjut.mianliao.notice;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.notice.Notice;

public class NoticeSysActivity extends NoticeListActivity {

    @Override
    protected Item getItem(Notice notice) {
        Item item = super.getItem(notice);
        switch (notice.category) {
            case Notice.CAT_SYS_MSG:
                if (notice.sysMsg != null) {
                    item.defaultAvatar = R.drawable.ic_ntc_sys_msg;
                    item.title = notice.sysMsg.title;
                    item.time = notice.time;
                    item.desc = notice.sysMsg.content;
                }
                break;

            case Notice.CAT_SYS_MEDAL:
                if (notice.sysMedal != null) {
                    item.avatar = notice.sysMedal.imageUrl;
                    item.defaultAvatar = R.drawable.ic_medal_default;
                    item.title = getString(R.string.ntc_sys_medal, notice.sysMedal.name);
                    item.time = notice.sysMedal.conferDate;
                    item.desc = notice.sysMedal.description;
                }
                break;

            default:
                break;
        }
        return item;
    }
}