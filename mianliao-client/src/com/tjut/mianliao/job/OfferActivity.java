package com.tjut.mianliao.job;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.data.job.Job;
import com.tjut.mianliao.data.job.Offer;
import com.tjut.mianliao.notice.NoticeManager;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class OfferActivity extends BaseActivity implements
        OnItemClickListener, OnRefreshListener2<ListView> {

    private NoticeManager mNoticeManager;
    private PullToRefreshListView mListView;
    private OfferAdapter mAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_notice_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.job_my_jobs, null);
        mNoticeManager = NoticeManager.getInstance(this);

        mListView = (PullToRefreshListView) findViewById(R.id.ptrlv_notice);
        int padding = getResources().getDimensionPixelSize(R.dimen.card_margin_horizontal);
        mListView.getRefreshableView().setPadding(padding, 0, padding, 0);
        mListView.setOnItemClickListener(this);
        mListView.setOnRefreshListener(this);
        mListView.setMode(Mode.BOTH);

        mAdapter = new OfferAdapter(this);
        mListView.setAdapter(mAdapter);

        fetchOffers(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Offer offer = (Offer) parent.getItemAtPosition(position);
        if (offer != null && offer.job != null) {
            Intent i = new Intent(this, JobDetailActivity.class)
                    .putExtra(Job.INTENT_EXTRA_NAME, offer.job);
            startActivity(i);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchOffers(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchOffers(false);
    }

    private void fetchOffers(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new OfferTask(offset).executeLong();
    }

    private class OfferAdapter extends ArrayAdapter<Offer> {
        private int mKeyColor;

        public OfferAdapter(Context context) {
            super(context, 0);
            mKeyColor = context.getResources().getColor(R.color.txt_keyword);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_offer, parent, false);
            }
            Offer offer = getItem(position);

            ImageView ivAvatar = (ImageView) view.findViewById(R.id.av_avatar);
            NameView tvTitle = (NameView) view.findViewById(R.id.tv_user_name);
            String desc = null;
            switch (offer.status) {
                case 100:
                case 101:
                case 102:
                    ivAvatar.setImageResource(R.drawable.ic_mail_receive);
                    tvTitle.setText(getString(R.string.offer_title_receive, offer.job.corpName));
                    if (offer.status == 100) {
                        desc = getString(R.string.offer_desc_100,
                                offer.job.corpName, offer.job.title);
                    } else if (offer.status == 101) {
                        desc = getString(R.string.offer_desc_101,
                                offer.job.corpName, offer.job.title);
                    } else {
                        desc = getString(R.string.offer_desc_102,
                                offer.job.corpName, offer.job.title);
                    }
                    break;

                case 200:
                case 201:
                case 202:
                    ivAvatar.setImageResource(R.drawable.ic_mail_send);
                    tvTitle.setText(getString(R.string.offer_title_send, offer.job.corpName));
                    if (offer.status == 200) {
                        desc = getString(R.string.offer_desc_200,
                                offer.job.corpName, offer.job.title);
                    } else if (offer.status == 201) {
                        desc = getString(R.string.offer_desc_201,
                                offer.job.corpName, offer.job.title);
                    } else {
                        desc = getString(R.string.offer_desc_202,
                                offer.job.corpName, offer.job.title);
                    }
                    break;

                default:
                    break;
            }

            TextView tvDesc = (TextView) view.findViewById(R.id.tv_desc);
            tvDesc.setTextIsSelectable(false);
            tvDesc.setVisibility(View.VISIBLE);
            tvDesc.setText(Utils.getColoredText(
                    Utils.getColoredText(desc, offer.job.corpName, mKeyColor, false),
                    offer.job.title, mKeyColor, false));

            ((TextView) view.findViewById(R.id.tv_extra_info)).setText(
                    Utils.getTimeDesc(offer.uTime));

            return view;
        }
    }

    private class OfferTask extends MsTask {
        private int mOffset;

        public OfferTask(int offset) {
            super(getApplicationContext(), MsRequest.JOB_LIST_OFFER);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mListView.onRefreshComplete();
            if (MsResponse.isSuccessful(response)) {
                mAdapter.setNotifyOnChange(false);
                if (mOffset == 0) {
                    mAdapter.clear();
                }
                JSONArray ja = response.json.optJSONArray(MsResponse.PARAM_RESPONSE);
                for (int i = 0; i < ja.length(); i++) {
                    Offer offer = Offer.fromJson(ja.optJSONObject(i));
                    if (offer != null) {
                        mAdapter.add(offer);
                    }
                }
                mAdapter.notifyDataSetChanged();
                mNoticeManager.setNoticeViewed(OfferActivity.this, 0);
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.offer_tst_failed, response.code));
            }
        }
    }
}
