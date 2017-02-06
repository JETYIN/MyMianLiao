package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.ChatActivity;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class TribeChatRoomActivity extends BaseActivity implements OnRefreshListener2<ListView>,
       OnClickListener{
    
    public final static String EXT_TRIBE_DATA = "ext_tribe_data"; 
    public final static String EXT_TRIBE_ID = "ext_tribe_id";
    public final static int CHAT_REQUEST = 333;
    public final static int CHAT_INFO_REQUEST = 334;
    private final static int REQUEST_CODE = 1000;
    
    @ViewInject(R.id.ptlv_chat_rooms)
    private PullToRefreshListView mLvChatRooms;
    
    private ArrayList<TribeChatRoomInfo> mTribeRooms;
    
    private UserInfo mUserInfo;
    private ChatRoomAdadpter mAdapter;
    private TribeInfo mTribe;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_tribe_chat_room;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        getTitleBar().setTitle(getString(R.string.tribe_chat_room));
        mTribe = getIntent().getParcelableExtra(EXT_TRIBE_DATA);
        mUserInfo = AccountInfo.getInstance(this).getUserInfo();
        mTribeRooms = new ArrayList<TribeChatRoomInfo>();
        mAdapter = new ChatRoomAdadpter();
        mLvChatRooms.setAdapter(mAdapter);
        mLvChatRooms.setMode(Mode.BOTH);
        mLvChatRooms.setOnRefreshListener(this);
        mLvChatRooms.setRefreshing(Mode.PULL_FROM_START);
        fetchRooms(true);
    }
    
    private void fetchRooms(boolean refresh) {
        int size = mAdapter.getCount();
        int offset = refresh ? 0 : size;
        new GetChatRooms(offset).executeLong();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        new GetChatRooms(0).executeLong();
    }
    
    private class ChatRoomAdadpter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTribeRooms.size();
        }

        @Override
        public TribeChatRoomInfo getItem(int position) {
            return mTribeRooms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_tribe_chat_room, parent, false);
                holder  = new ViewHolder();
                ViewUtils.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            } 
            TribeChatRoomInfo roomInfo = getItem(position);
            holder.mRoomInfo = roomInfo;
            if (roomInfo.roomAvatar != null && !roomInfo.roomAvatar.equals("")) {
                Picasso.with(TribeChatRoomActivity.this)
                    .load(roomInfo.roomAvatar)
                    .placeholder(R.drawable.chat_pic_bg_wechat)
                    .into(holder.mIvRoomAvatar);
            } else {
                Picasso.with(TribeChatRoomActivity.this)
                    .load(R.drawable.chat_pic_bg_wechat)
                    .into(holder.mIvRoomAvatar);
            }
            holder.mTvRoomName.setText(roomInfo.roomName);
            holder.mTvRoomDesc.setText(roomInfo.roomDesc);
            holder.mTvPeopleNum.setText(roomInfo.peopleNum + "/100");
            convertView.setOnClickListener(TribeChatRoomActivity.this);
            return convertView;
        }
        
    }
    
    private String createTribeChatTarget(long roomId) {
        return roomId + "@groupchat." + Utils.getChatServerDomain();
    }
    
    private class ViewHolder {
        @ViewInject(R.id.iv_room_avatar)
        AvatarView mIvRoomAvatar;
        @ViewInject(R.id.tv_chat_room_name)
        TextView mTvRoomName;
        @ViewInject(R.id.tv_online_people)
        TextView mTvOnlinePeople;
        @ViewInject(R.id.tv_chat_room_desc)
        TextView mTvRoomDesc;
        @ViewInject(R.id.tv_online_people)
        TextView mTvPeopleNum;
        TribeChatRoomInfo mRoomInfo;
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchRooms(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchRooms(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_add_chat_room:
            case R.id.iv_add_chat_room:
                Intent intent = new Intent(TribeChatRoomActivity.this, CreateRoomNameActivity.class);
                intent.putExtra(EXT_TRIBE_DATA, mTribe);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.rl_tribe_chat:
                ViewHolder holder = (ViewHolder) v.getTag();
                operateData(holder);
                break;
            default:
                break;
        }
        
    }

    private void operateData(ViewHolder holder) {
        TribeChatRoomInfo mRoomInfo = holder.mRoomInfo;
        if (mRoomInfo.code == 1) {
            Intent intent = new Intent(TribeChatRoomActivity.this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CHAT_TARGET, createTribeChatTarget(mRoomInfo.chatId));
            intent.putExtra(ChatActivity.EXTRA_TRIBE_ROOM_INFO , mRoomInfo);
            intent.putExtra(ChatActivity.EXTRA_CHAT_ISGOUPCHAT, true);
            intent.putExtra(ChatActivity.EXTRA_GROUPCHAT_ID, mRoomInfo.roomId + "tribe");
            startActivityForResult(intent, CHAT_REQUEST);
        } else {
            Intent intent = new Intent(TribeChatRoomActivity.this, TribeGroupInfoActivity.class);
            intent.putExtra(TribeChatRoomInfo.INTENT_EXTRA_INFO, holder.mRoomInfo);
            startActivityForResult(intent, CHAT_INFO_REQUEST);
        }
    }   
    
    private class GetChatRooms extends MsTask {
        int mOffset;
        public GetChatRooms(int offset) {
            super(TribeChatRoomActivity.this, MsRequest.TRIBE_GET_CHAT_ROOMS);
            mOffset = offset;
        }
        
        @Override
        protected String buildParams() {
            return new StringBuilder("uid=").append(mUserInfo.userId)
                    .append("&tribe_id=").append(mTribe.tribeId)
                    .append("&offset=").append(mOffset)
                    .toString();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            mLvChatRooms.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<TribeChatRoomInfo> roomInfos = JsonUtil.getArray(
                        response.getJsonArray(), TribeChatRoomInfo.TRANSFORMER);
                if (roomInfos != null) {
                    if (mOffset <= 0) {
                        mTribeRooms.clear();
                    }
                    mTribeRooms.addAll(roomInfos);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_UPDATED) {
            mTribe = data.getParcelableExtra(EXT_TRIBE_DATA);
        }
     }
}
