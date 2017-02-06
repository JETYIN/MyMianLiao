package com.tjut.mianliao.mycollege;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.Utils;

public class PraisemeUserActivity extends BaseActivity{
    private PullToRefreshGridView mPtrgvPraiseme;
    private ArrayList<UserInfo> mPraisemeUsers = new ArrayList<UserInfo>();
     @Override
    protected int getLayoutResID() {
        return R.layout.activity_praiseme_user;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle("获赞数");
        mPtrgvPraiseme = (PullToRefreshGridView) findViewById(R.id.ptrgv_praiseme);
    }
    
    private BaseAdapter mPraisemAdapter = new BaseAdapter() {
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public Object getItem(int position) {
            return mPraisemeUsers.get(position);
        }
        
        @Override
        public int getCount() {
            return mPraisemeUsers.size();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.grid_item_pariseme_user, parent, false);
            }
            UserInfo mUser = mPraisemeUsers.get(position);
            TextView mTvName, mTvSchool;
            ProImageView mIvAvatar, mIvGender;
            mTvName = (TextView) view.findViewById(R.id.tv_name);
            mTvSchool = (TextView) view.findViewById(R.id.tv_school);
            mIvAvatar = (ProImageView) view.findViewById(R.id.iv_avatar);
            mIvGender = (ProImageView) view.findViewById(R.id.iv_gender);
            mTvName.setText(mUser.name);
            mTvSchool.setText(mUser.school);
            mIvAvatar.setImage(Utils.getImagePreviewSmall(mUser.avatarFull),
                    mUser.getDefaultAvatar(mUser.gender));
            mIvGender.setImageResource(mUser.gender == 0 ? R.drawable.pic_bg_woman
                    : R.drawable.pic_bg_man);
            view.setTag(mUser);
            return view;
        }
        
    };

}
