package com.tjut.mianliao.contact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.CheckableUserEntry;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.Utils;

public class CheckableContactsAdapter extends ContactsAdapter {

    private int mCheckedCount;
    private Context mContext;


    public CheckableContactsAdapter(Context context, List<UserInfo> exceptInfos) {
        super(context, R.layout.list_item_checkable_contact);
        mContext = context;
        if (exceptInfos != null) {
            Iterator<UserEntry> it = mContacts.iterator();
            while (it.hasNext()) {
                UserInfo info = mUserInfoManager.getUserInfo(it.next().jid);
                if (exceptInfos.contains(info)) {
                    it.remove();
                }
            }
            
        }
    }

    public void toggle(CheckableUserEntry cue) {
        cue.checked = !cue.checked;
        if (cue.checked) {
            mCheckedCount++;
        } else {
            mCheckedCount--;
        }
    }

    public void checkAll(boolean checked) {
        List<UserEntry> contacts = mOriginalContacts == null ? mContacts : mOriginalContacts;
        for (UserEntry ue : contacts) {
            ((CheckableUserEntry) ue).checked = checked;
        }
        mCheckedCount = checked ? contacts.size() : 0;
    }

    public boolean allChecked() {
        return mOriginalContacts == null ? mCheckedCount == mContacts.size()
                : mCheckedCount == mOriginalContacts.size();
    }

    public List<UserEntry> getCheckedItems() {
        if (mCheckedCount == 0) {
            return null;
        }

        List<UserEntry> checkedItems = new ArrayList<UserEntry>();
        List<UserEntry> contacts = mOriginalContacts == null ? mContacts : mOriginalContacts;
        for (UserEntry ue : contacts) {
            if (((CheckableUserEntry) ue).checked) {
                checkedItems.add(ue);
            }
        }
        return checkedItems;
    }

    @Override
    public Object getItem(int position) {
        return  super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactType type = ContactType.values()[getItemViewType(position)];
        if (convertView == null) {
            if (type == ContactType.TYPE_ALP) {
                convertView = mLayoutInflater.inflate(R.layout.item_list_alpha, parent, false);
            } else {
                convertView = mLayoutInflater.inflate(R.layout.list_item_contact, parent, false);
            }
        }
        switch (type) {
            case TYPE_ALP:
                TextView alphaText = (TextView) convertView.findViewById(R.id.alpha);
                alphaText.setText(mMergeData.get(position).toString());
                break;
            default:
                bindOriginData(convertView, (UserEntry) mMergeData.get(position));
                break;
        }
        return convertView;
    }
    
    public void bindOriginData(View convertView, UserEntry user) {
        UserInfo userInfo = UserInfoManager.getInstance(mContext).getUserInfo(user.jid);
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_contact_name);
        TextView tvInfo = (TextView) convertView.findViewById(R.id.tv_short_desc);
        ImageView ivMedal = (ImageView) convertView.findViewById(R.id.iv_medal);
        ImageView ivTypeIcon = (ImageView) convertView.findViewById(R.id.iv_type_icon);
        if (userInfo != null) {
            convertView.findViewById(R.id.iv_vip_bg).setVisibility(
                    userInfo.vip ? View.VISIBLE : View.GONE);
        } else {
            convertView.findViewById(R.id.iv_vip_bg).setVisibility(View.GONE);
        }
        String avatar = null;
        int avatarId;
        String name;
        
        String shortDesc = null;
        if (userInfo != null) {
            avatarId = userInfo.defaultAvatar();
            name = userInfo.getDisplayName(mContext);
            avatar = userInfo.getAvatar();
            shortDesc = userInfo.shortDesc;
            if (userInfo.getLatestBadge() != null &&
                    userInfo.getLatestBadge().startsWith("http")) {
                ivMedal.setVisibility(View.VISIBLE);
                Picasso.with(mContext)
                    .load(userInfo.getLatestBadge())
                    .placeholder(R.drawable.ic_medal_empty)
                    .into(ivMedal);
            } else {
                ivMedal.setVisibility(View.GONE);
            }
            // update type icon;it while show in day time,or it should hide
            int resIcon = userInfo.getTypeIcon();
            if (resIcon > 0) {
                ivTypeIcon.setImageResource(resIcon);
                ivTypeIcon.setVisibility(View.VISIBLE);
            } else {
                ivTypeIcon.setVisibility(View.GONE);
            }
        } else {
            avatarId = R.drawable.chat_botton_bg_faviconboy;
            name = userInfo.name;
        }

        ImageView ivAvatar = (ImageView) convertView.findViewById(R.id.iv_contact_avatar);
        Picasso.with(mContext).load(avatar).placeholder(avatarId).into(ivAvatar);
        
        tvName.setText(Utils.getColoredText(name, mFilterConstraint,
                mContext.getResources().getColor(R.color.txt_keyword)));

        tvInfo.setText(shortDesc);

        CheckableUserEntry cue = (CheckableUserEntry)user;
        convertView.findViewById(R.id.view_line).setVisibility(View.INVISIBLE);
        convertView.findViewById(R.id.rl_contact).setBackgroundResource(R.drawable.selector_bg_item);
        ((CheckBox) convertView.findViewById(R.id.cb_check)).setChecked(cue.checked);

    }
    

    @Override
    public Collection<UserEntry> getContacts() {
        ArrayList<UserEntry> contacts = new ArrayList<UserEntry>();
        for (UserEntry ue : mUserEntryManger.getFriends()) {
            if (!mUserEntryManger.isBlacklisted(ue.jid)) {
                contacts.add(new CheckableUserEntry(ue));
            }
        }
        return contacts;
    }
    
}
