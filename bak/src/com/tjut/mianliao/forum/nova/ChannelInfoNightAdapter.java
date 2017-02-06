package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.util.AliImgSpec;

public class ChannelInfoNightAdapter extends BaseAdapter implements View.OnClickListener {

    private ArrayList<ChannelInfo> mChannelInfos = new ArrayList<>();
    private ArrayList<ChannelInfo> mOfficialChannelInfos = new ArrayList<>();
    private ArrayList<ChannelInfo> mUserChannelInfos = new ArrayList<>();

    private Context mContext;
    private LayoutInflater mInflater;

    private enum ViewType {
        OFFCIAL, USR, CHANNEL_INFO
    }

    public ChannelInfoNightAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setChannelInfo(ArrayList<ChannelInfo> ocInfo, ArrayList<ChannelInfo> ucInfo) {
        mOfficialChannelInfos.clear();
        mUserChannelInfos.clear();
        mChannelInfos.clear();
        mOfficialChannelInfos.addAll(ocInfo);
        mUserChannelInfos.addAll(ucInfo);
        mChannelInfos.addAll(ocInfo);
        mChannelInfos.addAll(ucInfo);
    }

    @Override
    public int getCount() {
        if (mChannelInfos != null) {
            return mChannelInfos.size() + 2;
        }
        return 0;
    }

    @Override
    public ChannelInfo getItem(int position) {
        if (position <= 0) {
            return null;
        } else if (position <= mOfficialChannelInfos.size()) {
            return mChannelInfos.get(position - 1);
        } else if (position == mOfficialChannelInfos.size() + 1) {
            return null;
        } else {
            if (position >= 2) {
                return mChannelInfos.get(position - 2);
            } else {
                return null;
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ViewType.OFFCIAL.ordinal();
        } else if (position == mOfficialChannelInfos.size() + 1) {
            return ViewType.USR.ordinal();
        } else {
            return ViewType.CHANNEL_INFO.ordinal();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewType type = ViewType.values()[getItemViewType(position)];
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = inflateView(type, parent);
        }
        if (view == null) {
            return null;
        }
        ChannelInfo channel = getItem(position);
        view.setOnClickListener(this);
        view.setTag(channel);
        ViewType nextType = null;
        if (position < getCount() - 1) {
            nextType = ViewType.values()[getItemViewType(position + 1)];
        }

        if (type.equals(ViewType.CHANNEL_INFO)) {
            ProImageView piv = (ProImageView) view.findViewById(R.id.piv_channel_icon);
            TextView title = (TextView) view.findViewById(R.id.tv_channel_title);
            TextView intro = (TextView) view.findViewById(R.id.tv_channel_intro);
            Picasso.with(mContext)
	        	.load(getImagePreviewSmall(channel.icon))
	        	.placeholder(R.drawable.ic_ntc_forum)
	        	.into(piv);
            title.setText(channel.name);
            intro.setText(channel.intro);
            if ((position == getCount() - 1) ||
                    (nextType != null && !nextType.equals(ViewType.CHANNEL_INFO))) {
                view.findViewById(R.id.view_split).setVisibility(View.GONE);
            }
        }
        return view;
    }

    private View inflateView(ViewType type, ViewGroup parent) {
        switch (type) {
            case OFFCIAL:
                return mInflater.inflate(R.layout.channel_night_item_offcial, parent, false);
            case CHANNEL_INFO:
                return mInflater.inflate(R.layout.list_item_channel_night, parent, false);
            case USR:
                return mInflater.inflate(R.layout.channel_night_item_user, parent, false);
            default:
                return mInflater.inflate(R.layout.list_item_channel_night, parent, false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_channel_neight:
                showChannelInfoDetail((ChannelInfo) v.getTag());
                break;

            default:
                break;
        }
    }

    private String getImagePreviewSmall(String url) {
        return AliImgSpec.USER_AVATAR.makeUrl(url);
    }

    private void showChannelInfoDetail(ChannelInfo channel) {
        Intent channelIntent = new Intent(mContext, ForumChannelDetailActivity.class);
        channelIntent.putExtra(ForumChannelDetailActivity.EXT_DATA, channel);
        mContext.startActivity(channelIntent);
    }

}