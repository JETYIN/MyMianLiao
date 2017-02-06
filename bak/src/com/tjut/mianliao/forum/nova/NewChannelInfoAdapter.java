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

public class NewChannelInfoAdapter extends BaseAdapter implements View.OnClickListener {

    private ArrayList<ChannelInfo> mChannelInfos = new ArrayList<>();

    private Context mContext;
    private LayoutInflater mInflater;

    public NewChannelInfoAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setChannelInfo(ArrayList<ChannelInfo> channels) {
        mChannelInfos.clear();
        mChannelInfos.addAll(channels);
    }

    @Override
    public int getCount() {
        return mChannelInfos.size();
    }

    @Override
    public ChannelInfo getItem(int position) {
        return mChannelInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.list_item_channel_night, parent, false);
        }
        if (view == null) {
            return null;
        }
        ChannelInfo channel = getItem(position);
        view.setOnClickListener(this);
        view.setTag(channel);

        ProImageView piv = (ProImageView) view.findViewById(R.id.piv_channel_icon);
        TextView title = (TextView) view.findViewById(R.id.tv_channel_title);
        TextView intro = (TextView) view.findViewById(R.id.tv_channel_intro);
        Picasso.with(mContext)
	    	.load(getImagePreviewSmall(channel.icon))
	    	.placeholder(R.drawable.ic_ntc_forum)
	    	.into(piv);
        title.setText(channel.name);
        intro.setText(channel.intro);
        return view;
    }

    private String getImagePreviewSmall(String url) {
        return AliImgSpec.USER_AVATAR.makeUrl(url);
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

    private void showChannelInfoDetail(ChannelInfo channel) {
        Intent channelIntent = new Intent(mContext, ForumChannelDetailActivity.class);
        channelIntent.putExtra(ForumChannelDetailActivity.EXT_DATA, channel);
        mContext.startActivity(channelIntent);
    }

}