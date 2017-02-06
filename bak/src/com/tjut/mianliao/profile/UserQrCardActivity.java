package com.tjut.mianliao.profile;

import com.tjut.mianliao.QrCardActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.scan.assist.MlParser;

public class UserQrCardActivity extends QrCardActivity {
    private UserInfo mUser;

    @Override
    protected void initData() {
        mUser = getIntent().getParcelableExtra(UserInfo.INTENT_EXTRA_INFO);
    }

    @Override
    protected String getName() {
        return mUser.getDisplayName(this);
    }

    @Override
    protected String getUri() {
        return new StringBuilder(MlParser.URI_PREFIX_USER)
                .append(mUser.guid).toString();
    }

    @Override
    protected String getImage() {
        return mUser.getAvatar();
    }

    @Override
    protected int getDefaultImageRes() {
        return mUser.defaultAvatar();
    }

    @Override
    protected String getDescription() {
        return getString(R.string.qrc_desc_user);
    }
}
