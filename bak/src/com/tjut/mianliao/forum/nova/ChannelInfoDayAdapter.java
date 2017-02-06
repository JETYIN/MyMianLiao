package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.util.AliImgSpec;

public class ChannelInfoDayAdapter extends BaseAdapter implements OnClickListener {

    private ArrayList<ChannelInfo> mOfficialChannelInfos = new ArrayList<>();

    private Context mContext;
    private LayoutInflater mInflater;

    public ChannelInfoDayAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setChannelInfo(ArrayList<ChannelInfo> channelInfos) {
        if (channelInfos != null && channelInfos.size() > 0) {
            mOfficialChannelInfos = channelInfos;
        }
    }

    @Override
    public int getCount() {
        return mOfficialChannelInfos.size();
    }

    @Override
    public ChannelInfo getItem(int position) {
        return mOfficialChannelInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.list_item_channel_day, parent, false);
        }

        if (view == null) {
            return null;
        }

        ChannelInfo channel = getItem(position);
        view.setOnClickListener(this);
        view.setTag(channel);

        ProImageView image = (ProImageView) view.findViewById(R.id.iv_channel_photo);
        TextView text = (TextView) view.findViewById(R.id.tv_channel_name);
        text.setText(channel.name);
        Picasso.with(mContext)
        	.load(getImagePreviewSmall(channel.bgImg))
        	.placeholder(R.drawable.loading)
        	.into(image);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_channel_day:
                showChannelInfoDetail((ChannelInfo) v.getTag());
                break;

            default:
                break;
        }
    }

    private String getImagePreviewSmall(String url) {
        return AliImgSpec.POST_THUMB_SQUARE.makeUrl(url);
    }

    private void showChannelInfoDetail(ChannelInfo channel) {
        Intent channelIntent = new Intent(mContext, ForumChannelDetailActivity.class);
        channelIntent.putExtra(ForumChannelDetailActivity.EXT_DATA, channel);
        mContext.startActivity(channelIntent);
    }

}
