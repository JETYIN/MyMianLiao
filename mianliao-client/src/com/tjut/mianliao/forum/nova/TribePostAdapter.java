package com.tjut.mianliao.forum.nova;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.tribe.TribeDetailActivity;

/**
 * This adapter extends {@link ForumPostAdapter}, and it can show the post from
 * which tribe
 * 
 * @author YoopWu
 * 
 */
public class TribePostAdapter extends ForumPostAdapter {

    private UserInfo mUserInfo;
    
    public TribePostAdapter(Activity context) {
        super(context);
        mUserInfo = AccountInfo.getInstance(context).getUserInfo();
    }
    
    @Override
    public View updateFooterView(View view, CfPost post) {
        View footerView = super.updateFooterView(view, post);
        FrameLayout mFlFooter = (FrameLayout) view.findViewById(R.id.fl_footer_content);
        if (mFlFooter != null) {
            if (mFlFooter.getChildCount() > 0) {
                mFlFooter.removeViewAt(0);
            }
            mInflater.inflate(R.layout.item_tribe_footer_from, mFlFooter);
            TextView tvSchoolName = (TextView) mFlFooter.findViewById(R.id.tv_from_where);
            TextView tvTribeName = (TextView) mFlFooter.findViewById(R.id.tv_tribe_name);
            if (post.forumName == null || "".equals(post.forumName)) {
                mFlFooter.setVisibility(View.GONE);
            } else {
                if (post.schoolId > 0) {
                    tvSchoolName.setText(R.string.tribe_footer_from_school);
                } else {
                    tvSchoolName.setText(R.string.tribe_footer_from_tribe);
                }
                mFlFooter.setVisibility(View.VISIBLE);
                tvTribeName.setText(post.forumName);
                tvTribeName.setTag(post);
                tvTribeName.setOnClickListener(this);
                tvTribeName.setBackgroundResource(R.drawable.bg_tribe_from);
            }
        }
        return footerView;
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_tribe_name:
                CfPost post = (CfPost) v.getTag();
                if (post.tribeId > 0) {
                    Intent trIntent = new Intent(mContext, TribeDetailActivity.class);
                    trIntent.putExtra(TribeDetailActivity.EXT_DATA_ID, post.tribeId);
                    mContext.startActivity(trIntent);
                } else if (post.forumName.equals(mUserInfo.school)){
                    Intent intent = new Intent();
                    intent.setClass(mContext, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_ACTIVE_TAB_INDEX, 0);
                    intent.putExtra(MainActivity.EXTRA_FRAGMENT_TAB_INDEX, 0);
                    intent.putExtra(MainActivity.EXTRA_POST_TAB_INDEX, 0);
                    mContext.startActivity(intent);
                } else if (post.tribeId <= 0 && post.schoolId <= 0){
                    Toast.makeText(mContext, R.string.tribe_not_exist, Toast.LENGTH_SHORT).show();
                }else {
                    if (DataHelper.getSchoolisUnlock(mContext, post.forumName)) {
                        Intent intent = new Intent();
                        intent.setClass(mContext, FormOtherSchoolActivity.class);
                        intent.putExtra(Forum.INTENT_EXTRA_SCHOOLID, post.schoolId);
                        intent.putExtra(Forum.INTENT_EXTRA_SCHOOLNAME, post.forumName);
                        mContext.startActivity(intent);
                    } else {
                        Toast.makeText(mContext, R.string.cf_school_is_lock, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }
    
}
