package com.tjut.mianliao.contact;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.Utils;

public class UserInfoAdapter extends ArrayAdapter<UserInfo> implements OnClickListener {

    private int mResource;
    private int mKeyColor;
    private String mKeyword;
    private boolean mIsShowMadel = true;

    private LayoutInflater mInflater;

    public UserInfoAdapter(Context context) {
        this(context, 0);
    }

    public UserInfoAdapter(Context context, boolean isShowMadel) {
        this(context, 0);
        mIsShowMadel = isShowMadel;
    }

    public UserInfoAdapter(Context context, int resource) {
        super(context, 0);
        mResource = resource == 0 ? R.layout.list_item_user_info : resource;
        Resources res = context.getResources();
        mKeyColor = res.getColor(R.color.txt_keyword);
        mInflater = LayoutInflater.from(context);
    }

    public int getKeyColor() {
        return mKeyColor;
    }

    public void setKeyword(String keyword) {
        mKeyword = keyword;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = getContext();
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(mResource, parent, false);
        }
        UserInfo user = getItem(position);

        ProImageView ivAvatar = (ProImageView) view.findViewById(R.id.av_avatar);
        ivAvatar.setTag(user);
        ivAvatar.setOnClickListener(this);
        ivAvatar.setImage(user.getAvatar(), user.defaultAvatar());

        NameView tvName = (NameView) view.findViewById(R.id.tv_name);
        tvName.setText(Utils.getColoredText(user.getDisplayName(getContext()),
                mKeyword, mKeyColor));
        if (mIsShowMadel) {
            tvName.setMedal(user.primaryBadgeImage);
        }

        ((ImageView) view.findViewById(R.id.iv_gender)).setImageResource(user.getGenderIcon());

        // ((TextView) view.findViewById(R.id.tv_short_desc)).setText(user
        // .getSchoolAndGrade(context));

        ((TextView) view.findViewById(R.id.tv_short_desc)).setText(user.getSchool(context));
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_avatar:
                Context context = getContext();
                UserInfo user = (UserInfo) v.getTag();
                Intent i = new Intent(context, NewProfileActivity.class);
                i.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
                context.startActivity(i);
                break;

            default:
                break;
        }
    }

}
