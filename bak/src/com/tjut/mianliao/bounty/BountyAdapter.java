package com.tjut.mianliao.bounty;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.bounty.BountyTask;
import com.tjut.mianliao.data.bounty.Credits;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.Utils;

public class BountyAdapter extends BaseAdapter implements View.OnClickListener {
    private Activity mActivity;
    private ArrayList<BountyTask> mBounties;
    private boolean mShowDistance = false;

    public BountyAdapter(Activity activity) {
        mActivity = activity;
    }

    public ArrayList<BountyTask> getData() {
        return mBounties;
    }

    public void setData(ArrayList<BountyTask> bounties) {
        mBounties = bounties;
    }

    @Override
    public int getCount() {
        return mBounties == null ? 0 : mBounties.size();
    }

    @Override
    public BountyTask getItem(int position) {
        return mBounties.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mBounties.get(position).id;
    }

    public void setShowDistance(boolean show) {
        mShowDistance = show;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mActivity.getLayoutInflater().inflate(R.layout.list_item_bounty, parent, false);
        }

        view.setBackgroundResource(position == (getCount() - 1) ?
                R.drawable.selector_list_item_white_last : R.drawable.selector_list_item_white);

        BountyTask task = mBounties.get(position);
        Credits credits = task.userCredit;
        UserInfo userInfo = credits.userInfo;

        ProImageView ivAvatar = (ProImageView) view.findViewById(R.id.av_avatar);
        ivAvatar.setImage(userInfo.getAvatar(), userInfo.defaultAvatar());
        ivAvatar.setOnClickListener(this);
        ivAvatar.setTag(userInfo);

        ((ImageView) view.findViewById(R.id.iv_gender))
                .setImageResource(userInfo.getGenderIcon());

        NameView tvName = (NameView) view.findViewById(R.id.tv_user_name);
        tvName.setText(userInfo.getDisplayName(mActivity));
        tvName.setOnClickListener(this);
        tvName.setTag(userInfo);

        TextView tvCreditLevel = (TextView) view.findViewById(R.id.tv_credit_level);
        tvCreditLevel.setText(mActivity.getString(
                R.string.bty_credit_level, credits.getCreditLevel(true)));
        tvCreditLevel.setOnClickListener(this);
        tvCreditLevel.setTag(credits);

        String reward = mActivity.getString(R.string.btyct_reward, task.reward);
        Utils.setText(view, R.id.tv_reward, reward);
        Utils.setText(view, R.id.tv_school, userInfo.school);
        Utils.setText(view, R.id.tv_time, Utils.getTimeDesc(task.ctime));
        Utils.setText(view, R.id.tv_desc, Utils.getRefFriendText(task.desc, mActivity));
        Utils.setText(view, R.id.tv_status, mActivity.getString(task.getStatusDesc()));

        TextView tvPlace = (TextView) view.findViewById(R.id.tv_place);
        tvPlace.setVisibility(TextUtils.isEmpty(task.place) ? View.INVISIBLE : View.VISIBLE);
        String place = task.place;
        if (mShowDistance) {
            String distance = task.distance > 1000
                    ? mActivity.getString(R.string.around_distance_km, task.distance / 1000.0)
                    : mActivity.getString(R.string.around_distance_meter, task.distance);
            place = mActivity.getString(R.string.bty_distance_desc, distance, task.place);
        }
        tvPlace.setText(place);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_avatar:
            case R.id.tv_user_name:
                UserInfo userInfo = (UserInfo) v.getTag();
                Intent iUser = new Intent(mActivity, ProfileActivity.class);
                iUser.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
                mActivity.startActivity(iUser);
                break;

            case R.id.tv_credit_level:
                Credits credits = (Credits) v.getTag();
                Intent iCredits = new Intent(mActivity, BountyCreditsActivity.class);
                iCredits.putExtra(Credits.INTENT_EXTRA_NAME, credits);
                mActivity.startActivity(iCredits);
                break;

            default:
                break;
        }
    }
}
