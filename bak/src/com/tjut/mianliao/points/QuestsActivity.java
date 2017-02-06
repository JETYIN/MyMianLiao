package com.tjut.mianliao.points;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class QuestsActivity extends BaseActivity {

    private static final String POINTS_URL = Utils.getServerAddress()
            + "assets/pages/points/info.html";

    private ArrayList<Quest> mQuests = new ArrayList<Quest>();

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_quests;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleBar().showTitleText(R.string.points_my_quests, null);

        ListView lvQuests = (ListView) findViewById(R.id.lv_quests);
        View header = getLayoutInflater().inflate(R.layout.list_header_quest, lvQuests, false);
        int pointsColor = getResources().getColor(R.color.txt_color_red);
        int pointsSize = getResources().getDimensionPixelSize(R.dimen.title_text_size);
        String points = String.valueOf(AccountInfo.getInstance(this).getUserInfo().points);
        String desc = getString(R.string.points_count, points);
        ((TextView) header.findViewById(R.id.tv_points_count)).setText(
                Utils.getSizedText(Utils.getColoredText(desc, points, pointsColor, false),
                        points, pointsSize, false));
        ((TextView) header.findViewById(R.id.tv_get_points))
                .setText(R.string.points_see_points_intro);
        lvQuests.addHeaderView(header);
        lvQuests.setAdapter(mAdapter);

        new GetQuestsTask().executeLong();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_points_card:
                Intent iPoints = new Intent(this, BrowserActivity.class);
                iPoints.putExtra(BrowserActivity.URL, POINTS_URL);
                iPoints.putExtra(BrowserActivity.TITLE, getString(R.string.points_info));
                startActivity(iPoints);
                break;
            default:
                break;
        }
    }

    private BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mQuests.size();
        }

        @Override
        public Object getItem(int position) {
            return mQuests.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        /**
         * 0, first; 1, middle; 2, foot
         */
        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else if (position == mQuests.size() - 1) {
                return 2;
            } else {
                return 1;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                int viewType = getItemViewType(position);
                int resId = viewType == 0 ? R.layout.list_item_quest_first :
                        R.layout.list_item_quest;
                view = getLayoutInflater().inflate(resId, parent, false);
                if (viewType == 2) {
                    view.setBackgroundResource(R.drawable.bg_card_bottom_in_list);
                }
            }
            Quest quest = mQuests.get(position);
            ((ImageView) view.findViewById(R.id.iv_quest_icon))
                    .setImageResource(quest.getQuestIcon());
            ((TextView) view.findViewById(R.id.tv_name)).setText(
                    getString(R.string.points_quest_desc, quest.event, quest.done, quest.quota));
            ((TextView) view.findViewById(R.id.tv_points)).setText(
                    getString(R.string.points_quest_desc_points, quest.done * quest.score));

            return view;
        }
    };

    private class GetQuestsTask extends MsTask {

        public GetQuestsTask() {
            super(getApplicationContext(), MsRequest.LIST_SCORE_TASK);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonObject().optJSONArray("task");
                mQuests.clear();
                mQuests.addAll(JsonUtil.getArray(ja, Quest.TRANSFORMER));
                mAdapter.notifyDataSetChanged();
            } else {
                response.showFailInfo(getRefContext(), R.string.points_get_quests_failed);
            }
        }
    }
}