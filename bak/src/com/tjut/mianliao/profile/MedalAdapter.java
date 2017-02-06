package com.tjut.mianliao.profile;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.Medal;

public class MedalAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Medal> mMedals;
    private boolean mActionEnabled;

    public MedalAdapter(Context context) {
        mContext = context;
        mMedals = new ArrayList<Medal>();
    }

    public void setActionEnabled(boolean actionEnabled) {
        mActionEnabled = actionEnabled;
    }

    public void addAll(ArrayList<Medal> medals) {
        if (medals != null && !medals.isEmpty()) {
            mMedals.addAll(medals);
            notifyDataSetChanged();
        }
    }

    public void reset(ArrayList<Medal> medals) {
        mMedals.clear();
        if (medals != null && !medals.isEmpty()) {
            mMedals.addAll(medals);
//            Collections.sort(mMedals, Medal.PRIMARY_COMPARATOR);
        }
        
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMedals.size();
    }

    @Override
    public Medal getItem(int position) {
        return mMedals.get(position);
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
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item_medal, parent, false);
        }
        Medal medal = getItem(position);

        ((ProImageView) view.findViewById(R.id.iv_medal)).setImage(
                medal.imageUrl, R.drawable.ic_medal_default);
        ((TextView) view.findViewById(R.id.tv_medal_name)).setText(medal.name);
        ((TextView) view.findViewById(R.id.tv_desc)).setText(medal.description);

        View ivShare = view.findViewById(R.id.iv_share);
        View ivPick = view.findViewById(R.id.iv_pick);
        if (mActionEnabled) {
            ivShare.setVisibility(View.VISIBLE);
            ivShare.setTag(medal);
            ivPick.setVisibility(View.VISIBLE);
            ivPick.setTag(medal);
            ivPick.setBackgroundResource(medal.isPrimary()
                    ? R.drawable.selector_btn_red : R.drawable.selector_btn_green);
        } else {
            ivShare.setVisibility(View.GONE);
            ivPick.setVisibility(View.GONE);
        }

        return view;
    }

}
