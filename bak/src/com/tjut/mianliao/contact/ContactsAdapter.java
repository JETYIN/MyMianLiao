package com.tjut.mianliao.contact;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.Utils;

public class ContactsAdapter extends BaseAdapter implements Filterable {

    protected enum ContactType{
        TYPE_ALP, TYPE_CONTACT
    }

    private Context mContext;
    protected int mResource;
    protected UserEntryManager mUserEntryManger;
    protected UserInfoManager mUserInfoManager;

    protected List<UserEntry> mContacts;
    protected List<UserEntry> mOriginalContacts;

    private ContactsFilter mFilter;
    private ContactsComparator mComparator;
    protected CharSequence mFilterConstraint;

    private boolean mIsNightMode;
    private Settings mSettings;
    
    private ArrayList<UserEntry> mOriginData;
    protected ArrayList<Object> mMergeData;
    private HashMap<String, Integer> mAlphaMap;
    protected LayoutInflater mLayoutInflater;
    
    public int peopleNum = 0;

    public int getPeopleNum() {
        return peopleNum;
    }

    public void setPeopleNum(int peopleNum) {
        this.peopleNum = peopleNum;
    }

    public ContactsAdapter(Context context) {
        this(context, 0);
    }

    public ContactsAdapter(Context context, int resource) {
        mContext = context.getApplicationContext();
        mSettings = Settings.getInstance(context);
        mIsNightMode = mSettings.isNightMode();
        mResource = resource == 0 ? R.layout.list_item_contact : resource;
        mUserEntryManger = UserEntryManager.getInstance(context);
        mUserInfoManager = UserInfoManager.getInstance(context);
        mLayoutInflater = LayoutInflater.from(context);
//        mUserInfoManager.loadUserInfo();
        mContacts = new Vector<UserEntry>();
        mComparator = new ContactsComparator();
        reset();
    }

    public void reset() {
        List<UserEntry> contacts = mOriginalContacts == null ? mContacts : mOriginalContacts;
        contacts.clear();
        Collection<UserEntry> entries = getContacts();
        contacts.addAll(entries);
        ArrayList<UserEntry> mUsers  = new ArrayList<UserEntry>();
        for (UserEntry userEntry : contacts) {
            UserInfo info = mUserInfoManager.getUserInfo(userEntry.jid);
            if (info == null) {
                mUserInfoManager.acquireUserInfo(userEntry.jid);
            } else {
                userEntry = setAlpha(info, userEntry);
                mUsers.add(userEntry);
            }
        }
        mOriginData = mUsers;
        mergeData(mUsers);
    }
    
    public int getContyactsSize() {
        List<UserEntry> contacts = mOriginalContacts == null ? mContacts : mOriginalContacts;
        contacts.clear();
        Collection<UserEntry> entries = getContacts();
        contacts.addAll(entries);
        ArrayList<UserEntry> mUsers  = new ArrayList<UserEntry>();
        for (UserEntry userEntry : contacts) {
            UserInfo info = mUserInfoManager.getUserInfo(userEntry.jid);
            if (info == null) {
                mUserInfoManager.acquireUserInfo(userEntry.jid);
            } else {
                userEntry = setAlpha(info, userEntry);
                mUsers.add(userEntry);
            }
        }
        mOriginData = mUsers;
        mergeData(mUsers);
        return mUsers.size();
    }

    private void mergeData(ArrayList<UserEntry> users) {
        Collections.sort(mOriginData, new Comparator<UserEntry>() {

            @Override
            public int compare(UserEntry lhs, UserEntry rhs) {
                return lhs.alpha.compareTo(rhs.alpha);
            }

        });
        mMergeData = new ArrayList<Object>();
        mAlphaMap = new HashMap<String, Integer>();
        final int SIZE = mOriginData.size();
        String lastAlpha = "";
        for (int i = 0; i < SIZE; i++) {
            UserEntry entry = mOriginData.get(i);
            mUserInfoManager.getUserInfo(entry.jid).getAlpha(mContext);
            if (!lastAlpha.equals(entry.alpha.substring(0, 1))) {
                lastAlpha = entry.alpha.substring(0, 1);
                mAlphaMap.put(lastAlpha, mMergeData.size());
                mMergeData.add(lastAlpha);
            }
            mMergeData.add(entry);
        }
    }
    
    public ArrayList<Object> getMergeData() {
        return mMergeData;
    }
    
    public boolean containsAlpha(String alpha) {
        return mAlphaMap.containsKey(alpha);
    }
    
    public int getAlphaPosition(String alpha) {
        return mAlphaMap.get(alpha);
    }
    
    
    @Override
    public int getItemViewType(int position) {
        if( getItem(position) instanceof UserEntry) {
            return ContactType.TYPE_CONTACT.ordinal();
        }
        return ContactType.TYPE_ALP.ordinal();
    }
    
    @Override
    public int getViewTypeCount() {
        return ContactType.values().length;
    }

    @Override
    public int getCount() {
        return mMergeData.size();
    }

    @Override
    public Object getItem(int position) {
        return mMergeData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactType type = ContactType.values()[getItemViewType(position)];
        if (convertView == null) {
            if (type == ContactType.TYPE_ALP) {
                convertView = mLayoutInflater.inflate(R.layout.item_list_alpha, parent, false);
            } else {
                convertView = mLayoutInflater.inflate(mResource, parent, false);
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
        ProImageView ivMedal = (ProImageView) convertView.findViewById(R.id.iv_medal);
        ImageView ivTypeIcon = (ImageView) convertView.findViewById(R.id.iv_type_icon);
        if (userInfo != null) {
            convertView.findViewById(R.id.iv_vip_bg).setVisibility(
                    userInfo.vip && !mIsNightMode ? View.VISIBLE : View.GONE);
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
            if (!mIsNightMode && userInfo.getLatestBadge() != null &&
                    userInfo.getLatestBadge().startsWith("http")) {
                ivMedal.setVisibility(View.VISIBLE);
                ivMedal.setImage(userInfo.getLatestBadge(), R.drawable.ic_medal_empty);
            } else {
                ivMedal.setVisibility(View.GONE);
            }
            // update type icon;it while show in day time,or it should hide
            int resIcon = userInfo.getTypeIcon();
            if (!mIsNightMode && resIcon > 0) {
                ivTypeIcon.setImageResource(resIcon);
                ivTypeIcon.setVisibility(View.VISIBLE);
            } else {
                ivTypeIcon.setVisibility(View.GONE);
            }
        } else {
            avatarId = R.drawable.chat_botton_bg_faviconboy;
            name = userInfo.name;
        }

        checkDayNightUI(tvInfo);
        
        ImageView ivAvatar = (ImageView) convertView.findViewById(R.id.iv_contact_avatar);
        Picasso.with(mContext).load(avatar).placeholder(avatarId).into(ivAvatar);
        
        tvName.setText(Utils.getColoredText(name, mFilterConstraint,
                mContext.getResources().getColor(R.color.txt_keyword)));

        tvInfo.setText(shortDesc);

        int statId = mUserEntryManger.getPresence(userInfo.jid) ?
                R.drawable.ic_status_online : R.drawable.ic_status_offline;
        convertView.findViewById(R.id.v_connection_state).setBackgroundResource(statId);

    }
    
    public static abstract class OnItemClickWrapperListener<T> implements AdapterView.OnItemClickListener {

        @SuppressWarnings("unchecked")
        @Override
        public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactsAdapter adapter = (ContactsAdapter)parent.getAdapter();
                if(adapter.getItemViewType(position) == ContactType.TYPE_ALP.ordinal() ) {
                    return;
                }
                onItemClick((T)adapter.getMergeData().get(position), position, view, parent);
        }
        
        public abstract void onItemClick(T itemData, int position, View view, AdapterView<?> parent);
         
    }

    protected void checkDayNightUI(TextView tvInfo) {
        if (mIsNightMode) {
            tvInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            if (mOriginalContacts == null) {
                mOriginalContacts = new Vector<UserEntry>(mContacts);
            }
            mFilter = new ContactsFilter();
        }
        return mFilter;
    }

    public Collection<UserEntry> getContacts() {
        ArrayList<UserEntry> contacts = new ArrayList<UserEntry>();
        for (UserEntry ue : mUserEntryManger.getFriends()) {
            if (!mUserEntryManger.isBlacklisted(ue.jid)) {
                contacts.add(ue);
            }
        }
        return contacts;
    }

    private class ContactsFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (TextUtils.isEmpty(constraint)) {
                results.values = mOriginalContacts;
                results.count = mOriginalContacts.size();
            } else {
                List<UserEntry> values = new ArrayList<UserEntry>(mOriginalContacts);
                List<UserEntry> newValues = new ArrayList<UserEntry>();
                for (int i = 0; i < values.size(); i++) {
                    UserEntry ue = values.get(i);
                    UserInfo ui = mUserInfoManager.getUserInfo(ue.jid);
                    String name = ui == null ? ue.name : ui.getDisplayName(mContext);
                    if (name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        newValues.add(ue);
                    }
                }
                results.values = new Vector<UserEntry>(newValues);
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mContacts = (List<UserEntry>) results.values;
            setPeopleNum(mContacts.size());
            mFilterConstraint = constraint;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    private class ContactsComparator implements Comparator<UserEntry> {
        private Collator mCollator = Collator.getInstance();

        @Override
        public int compare(UserEntry lhs, UserEntry rhs) {
            boolean presenceL = mUserEntryManger.getPresence(lhs.jid);
            boolean presenceR = mUserEntryManger.getPresence(rhs.jid);
            if (presenceL == presenceR) {
                UserInfo lu = mUserInfoManager.getUserInfo(lhs.jid);
                UserInfo ru = mUserInfoManager.getUserInfo(rhs.jid);
                int lt = lu == null ? UserInfo.TYPE_OTHER : lu.type;
                int rt = ru == null ? UserInfo.TYPE_OTHER : ru.type;
                if (lt == rt) {
                    String ls = lu == null ? lhs.name : lu.getDisplayName(mContext);
                    String rs = ru == null ? rhs.name : ru.getDisplayName(mContext);
                    return mCollator.compare(ls, rs);
                } else {
                    return lt > rt ? -1 : 1;
                }
            } else {
                return presenceL && !presenceR ? -1 : 1;
            }
        }
    }
    
    public UserEntry setAlpha (UserInfo user, UserEntry ue) {
        ue.alpha = user.getAlpha(mContext);
        return ue;
    }
}
