package com.tjut.mianliao.component;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.Utils;

public class ChatExtPicker extends GridView implements VisibleDelay {

    private boolean mVisible;
    private ChatExtListener mListener;
    private ChatExtAdapter mAdapter;

    public ChatExtPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setChatExtListener(ChatExtListener listener) {
        mListener = listener;
    }

    public void updateForGroupChat() {
        int avatarPos = mAdapter.getCount() - 1;
        mAdapter.remove(mAdapter.getItem(avatarPos));
    }

    @Override
    public void setVisibleDelayed(boolean visible) {
        mVisible = visible;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(mVisible ? VISIBLE : GONE);
            }
        }, DELAY_MILLS);
    }

    @Override
    public void setVisible(boolean visible) {
        mVisible = visible;
        setVisibility(visible ? VISIBLE : GONE);
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    private void init() {
        ArrayList<ChatExtItem> items = new ArrayList<ChatExtItem>();
        TypedArray ta = getResources().obtainTypedArray(R.array.chat_ext_menu);
        for (int i = 0; i < ta.length(); i += 2) {
            ChatExtItem item =  new ChatExtItem();
            item.title = ta.getResourceId(i, 0);
            item.image = ta.getResourceId(i + 1, 0);
            items.add(item);
        }
        ta.recycle();

        mAdapter = new ChatExtAdapter(getContext(), items);
        setAdapter(mAdapter);
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatExtItem item = (ChatExtItem) parent.getItemAtPosition(position);
                if (item != null && mListener != null) {
                    mListener.onChatExtClicked(item);
                }
            }
        });
    }

    private static class ChatExtAdapter extends ArrayAdapter<ChatExtItem> {

        public ChatExtAdapter(Context context, ArrayList<ChatExtItem> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = LayoutInflater.from(getContext()).inflate(
                        R.layout.list_item_chat_ext, parent, false);
            }
            ChatExtItem ceItem = getItem(position);

            Utils.setImage(view, R.id.iv_icon, ceItem.image);
            Utils.setText(view, R.id.tv_title, ceItem.title);

            return view;
        }
    }

    public static class ChatExtItem {
        public int title;
        public int image;
    }

    public interface ChatExtListener {
        public void onChatExtClicked(ChatExtItem item);
    }
}
