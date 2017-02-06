package com.tjut.mianliao.forum;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class PostActorsActivity extends BaseActivity implements
        PullToRefreshBase.OnRefreshListener2<ListView>, AdapterView.OnItemClickListener {

    private PullToRefreshListView mPtrActors;

    private CfPost mPost;
    private ActorAdapter mAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_voters;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPost = getIntent().getParcelableExtra(CfPost.INTENT_EXTRA_NAME);
        if (mPost == null || mPost.type == CfPost.TYPE_NORMAL) {
            toast(R.string.cf_post_not_exist);
            return;
        }

        switch (mPost.type) {
            case CfPost.TYPE_VOTE:
                getTitleBar().showTitleText(R.string.fv_voters, null);
                break;
            case CfPost.TYPE_EVENT:
                getTitleBar().showTitleText(R.string.fe_event_participants, null);
                break;
            default:
                break;
        }

        mPtrActors = (PullToRefreshListView) findViewById(R.id.ptrlv_voters);
        mPtrActors.getRefreshableView().addFooterView(new View(this));
        mPtrActors.setMode(Mode.BOTH);
        mPtrActors.setOnRefreshListener(this);
        mPtrActors.setOnItemClickListener(this);

        mAdapter = new ActorAdapter();
        mPtrActors.setAdapter(mAdapter);
        mPtrActors.setRefreshing(Mode.PULL_FROM_START);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchActors(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchActors(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        viewUser((Actor) parent.getItemAtPosition(position));
    }

    private void fetchActors(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new FetchActorTask(offset).executeLong();
    }

    private void viewUser(Actor actor) {
        if (actor != null) {
            Intent iProfile = new Intent(this, NewProfileActivity.class);
            iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, actor.userInfo);
            startActivity(iProfile);
        }
    }

    private class ActorAdapter extends ArrayAdapter<Actor> {

        public ActorAdapter() {
            super(getApplicationContext(), 0);
        }

        public void reset(ArrayList<Actor> actors) {
            setNotifyOnChange(false);
            clear();
            addAll(actors);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_post_actor, parent, false);
            }
            Actor actor= getItem(position);
            UserInfo user = actor.userInfo;

            ((ProImageView) view.findViewById(R.id.iv_contact_avatar))
                    .setImage(user.getAvatar(), user.defaultAvatar());
            NameView nvName = (NameView) view.findViewById(R.id.tv_contact_name);
            nvName.setText(user.getDisplayName(getContext()));
            nvName.setMedal(user.primaryBadgeImage);

            TextView tvDesc = (TextView) view.findViewById(R.id.tv_short_desc);
            if (mPost.type == CfPost.TYPE_VOTE) {
                tvDesc.setText(getString(
                        R.string.fv_voted, mPost.getVoteOpt(actor.vote[0])));
            } else {
                tvDesc.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_short_desc, 0, 0, 0);
                tvDesc.setHint(R.string.prof_no_short_desc);
                tvDesc.setText(user.shortDesc);
            }

            return view;
        }
    }

    private class FetchActorTask extends MsTask {
        private int mOffset;

        public FetchActorTask(int offset) {
            super(getApplicationContext(), MsRequest.LIST_EXTRA_ACTION_MEMBERS);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("thread_id=").append(mPost.postId)
                    .append("&offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrActors.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<Actor> actors = JsonUtil.getArray(
                        response.getJsonArray(), Actor.TRANSFORMER);
                if (mOffset == 0) {
                    mAdapter.reset(actors);
                } else {
                    mAdapter.addAll(actors);
                }
            } else {
                response.showFailInfo(getRefContext(), R.string.cf_fetch_actors_failed);
            }
        }
    }

    private static class Actor {
        UserInfo userInfo;
        int[] vote;

        private static final JsonUtil.ITransformer<Actor> TRANSFORMER =
                new JsonUtil.ITransformer<Actor>() {
                    @Override
                    public Actor transform(JSONObject json) {
                        return fromJson(json);
                    }
                };

        private static Actor fromJson(JSONObject json) {
            if (json == null) {
                return null;
            }
            Actor actor = new Actor();
            actor.userInfo = UserInfo.fromJson(json);
            actor.vote = JsonUtil.getIntArray(json.optJSONArray("vote"));
            return actor;
        }
    }
}