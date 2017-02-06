package com.tjut.mianliao.forum.nova;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.SearchActivity;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.tribe.TribeDetailActivity;

/**
 * This adapter extends  {@link ForumPostAdapter}, and it can show the post from which tribe
 * @author YoopWu
 *
 */
public class TribePostAdapter extends ForumPostAdapter {

    public TribePostAdapter(Activity context) {
        super(context);
    }

    @Override
    public View updateFooterView(View view, CfPost post) {
        View footerView = super.updateFooterView(view, post);
        if (!mIsFromTribe) {
            return footerView;
        }
        FrameLayout mFlFooter = (FrameLayout) view.findViewById(R.id.fl_footer_content);
        if (mFlFooter != null) {
            if (mFlFooter.getChildCount() > 0) {
                mFlFooter.removeViewAt(0);
            }
            View tribeView = mInflater.inflate(R.layout.item_tribe_footer_from, mFlFooter);
            TextView tvTribeName = (TextView) mFlFooter.findViewById(R.id.tv_tribe_name);
            if (post.forumName == null || "".equals(post.forumName) || post.tribeId <= 0) {
                mFlFooter.setVisibility(View.INVISIBLE);
            } else {
                mFlFooter.setVisibility(View.VISIBLE);
                tvTribeName.setText(post.forumName);
                tvTribeName.setTag(post.tribeId);
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
                int tribeId = (int) v.getTag();
                Intent trIntent = new Intent(mContext, TribeDetailActivity.class);
                trIntent.putExtra(TribeDetailActivity.EXT_DATA_ID, tribeId);
                mContext.startActivity(trIntent);
                break;
            default:
                super.onClick(v);
                break;
        }
    }
    
}
