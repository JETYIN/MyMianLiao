package com.tjut.mianliao.tribe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;

public class CreateRoomNameActivity extends BaseActivity implements OnClickListener{
    
    private static final int REQUEST_CODE = 1001;
    
    @ViewInject(R.id.et_chat_room_name)
    private EditText mEtChatRoomName;
    
    private String mRoomName;
    private TitleBar mTitleBar;
    private TribeChatRoomInfo mRoomInfo;
    private TribeInfo mTribe;    

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_create_room_name;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mTribe = getIntent().getParcelableExtra(TribeChatRoomActivity.EXT_TRIBE_DATA);
        if (mTribe == null) {
            toast(R.string.tribe_room_name_data_error);
            finish();
            return;
        }
        mTitleBar = getTitleBar();
        mRoomInfo = new TribeChatRoomInfo();
        mRoomInfo.tribeId = mTribe.tribeId;
        mRoomInfo.tribeName = mTribe.tribeName;
        mTitleBar.setTitle(getString(R.string.tribe_full_chat_room_name));
        mTitleBar.showRightText(R.string.reg_next, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                mRoomName = mEtChatRoomName.getText().toString();
                mRoomInfo.roomName = mRoomName;
                if (mRoomName == null || !isGroupNameOk(mRoomName)){
                    toast(R.string.tribe_group_name_length_toast);
                    return;
                }
                Intent intent = new Intent( CreateRoomNameActivity.this, ChooseRoomAvatarActivity.class);
                intent.putExtra(ChooseRoomAvatarActivity.EXT_CHAT_ROOM_DATA, mRoomInfo);
                startActivityForResult(intent, REQUEST_CODE);
                break;

            default:
                break;
        }
    }
    
    private boolean isGroupNameOk(String name) {
        return name.length() >=2 && name.length() <=10;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_UPDATED) {
            setResult(RESULT_UPDATED, data);
            finish();
        }
    }
}
