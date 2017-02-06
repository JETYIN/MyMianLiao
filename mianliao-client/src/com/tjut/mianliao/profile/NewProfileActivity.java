package com.tjut.mianliao.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.contact.UserInfo;

public class NewProfileActivity extends FragmentActivity {

    public static final String EXTRA_USER_GUID = "extra_user_guid";
    public static final String EXTRA_USER_FACE_ID = "extra_user_face_id";
    public static final String EXTRA_SHOW_CHAT_BUTTON = "extra_show_chat_button";
    public static final String EXTRA_NICK_NAME = "extra_nick_name";
    public static final String EXTRA_USER_DESC = "extra_user_desc";
    private ProfileFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_new_profile);

        UserInfo info = getIntent().getParcelableExtra(UserInfo.INTENT_EXTRA_INFO);
        String guid = getIntent().getStringExtra(EXTRA_USER_GUID);
        String faceId = getIntent().getStringExtra(EXTRA_USER_FACE_ID);

        Bundle bundle = new Bundle();
        bundle.putParcelable(UserInfo.INTENT_EXTRA_INFO, info);
        bundle.putString(EXTRA_USER_GUID, guid);
        bundle.putString(EXTRA_USER_FACE_ID, faceId);

        mFragment = new ProfileFragment(getSupportFragmentManager());
        mFragment.onAttach(this);
        mFragment.setArguments(bundle);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, mFragment).commit();
        }
    }
    
    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFragment.onActivityResult(requestCode, resultCode, data);
    }
}
