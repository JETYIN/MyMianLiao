package com.tjut.mianliao.mycollege;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.Utils;

public class MyVisitorActivity extends BaseActivity {
    private TextView mTvVisitorNum, mTvShowMore;
    private PullToRefreshListView mPtrlvVisitor;
    private ArrayList<UserInfo> mVisiors;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_my_visitor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTvVisitorNum = (TextView) findViewById(R.id.tv_num_visitor);
        mTvShowMore = (TextView) findViewById(R.id.tv_show_more);
        mPtrlvVisitor = (PullToRefreshListView) findViewById(R.id.ptrlv_my_visitor);
    }

    private BaseAdapter mPraisemAdapter = new BaseAdapter() {

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mVisiors.get(position);
        }

        @Override
        public int getCount() {
            return mVisiors.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.grid_item_pariseme_user, parent, false);
            }
            UserInfo mUser = mVisiors.get(position);
            TextView mTvName, mTvSchool, mTvVisitTime;
            ProImageView mIvAvatar, mIvGender;
            mTvName = (TextView) view.findViewById(R.id.tv_name);
            mTvSchool = (TextView) view.findViewById(R.id.tv_school);
            mIvAvatar = (ProImageView) view.findViewById(R.id.iv_avatar);
            mIvGender = (ProImageView) view.findViewById(R.id.iv_gender);
            mTvVisitTime = (TextView) view.findViewById(R.id.tv_visit_time);
            mTvName.setText(mUser.name);
            mTvSchool.setText(mUser.school);
            mIvAvatar.setImage(Utils.getImagePreviewSmall(mUser.avatarFull), mUser.getDefaultAvatar(mUser.gender));
            mIvGender.setImageResource(mUser.gender == 0 ? R.drawable.pic_bg_woman : R.drawable.pic_bg_man);
            view.setTag(mUser);
            return view;
        }

    };

}
