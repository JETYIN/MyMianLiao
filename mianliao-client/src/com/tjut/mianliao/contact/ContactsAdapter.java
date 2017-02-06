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
import com.tjut.mianliao.data.FocusUserInfo;
import com.tjut.mianliao.data.RadMenInfo;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.Utils;

public class ContactsAdapter extends BaseAdapter implements Filterable {

    protected enum ContactType{
        TYPE_ALP, TYPE_CONTACT
    }

    private Context mContext;
    protected UserEntryManager mUserEntryManger;
    protected UserInfoManager mUserInfoManager;

    protected List<UserEntry> mContacts;
    protected List<UserEntry> mOriginalContacts;

    private ContactsFilter mFilter;
    private ContactsComparator mComparator;
    protected CharSequence mFilterConstraint;
    private ArrayList<RadMenInfo> mOriginData;
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
        mUserEntryManger = UserEntryManager.getInstance(context);
        mUserInfoManager = UserInfoManager.getInstance(context);
        mLayoutInflater = LayoutInflater.from(context);
        mContacts = new Vector<UserEntry>();
        mComparator = new ContactsComparator();
        mMergeData = new ArrayList<Object>();
    }

    public void reset(ArrayList<RadMenInfo> users) {
//        List<UserEntry> contacts = mOriginalContacts == null ? mContacts : mOriginalContacts;
//        contacts.clear();
//        Collection<UserEntry> entries = getContacts();
//        contacts.addAll(entries);
        ArrayList<RadMenInfo> mUsers  = new ArrayList<>();
        for (RadMenInfo user : users) {
//            UserInfo info = mUserInfoManager.getUserInfo(userEntry.jid);
//            if (info == null) {
//                mUserInfoManager.acquireUserInfo(userEntry.jid);
//            } else {
                user = setAlpha(user, user);
                mUsers.add(user);
//            }
        }
        mOriginData = mUsers;
        System.out.println("------------------mOriginData.Size = " + mOriginData.size());
        mergeData();
    }
    
//    public int getContyactsSize() {
//        List<UserEntry> contacts = mOriginalContacts == null ? mContacts : mOriginalContacts;
//        contacts.clear();
//        Collection<UserEntry> entries = getContacts();
//        contacts.addAll(entries);
//        ArrayList<UserEntry> mUsers  = new ArrayList<UserEntry>();
//        for (UserEntry userEntry : contacts) {
//            UserInfo info = mUserInfoManager.getUserInfo(userEntry.jid);
//            if (info == null) {
//                mUserInfoManager.acquireUserInfo(userEntry.jid);
//            } else {
//                userEntry = setAlpha(info, userEntry);
//                mUsers.add(userEntry);
//            }
//        }
//        mOriginData = mUsers;
//        mergeData(mUsers);
//        return mUsers.size();
//    }

    private void mergeData() {
        Collections.sort(mOriginData, new Comparator<RadMenInfo>() {

            @Override
            public int compare(RadMenInfo lhs, RadMenInfo rhs) {
                return lhs.alpha.compareTo(rhs.alpha);
            }

        });
        
        mAlphaMap = new HashMap<String, Integer>();
        final int SIZE = mOriginData.size();
        String lastAlpha = "";
        System.out.println("-----------------mMergeData.size = " + mMergeData.size());
        mMergeData.clear();
        for (int i = 0; i < SIZE; i++) {
            RadMenInfo user = mOriginData.get(i);
            if (!lastAlpha.equals(user.alpha.substring(0, 1))) {
                lastAlpha = user.alpha.substring(0, 1);
                mAlphaMap.put(lastAlpha, mMergeData.size());
                mMergeData.add(lastAlpha);
            }
            mMergeData.add(user);
        }
        System.out.println("-----------------mMergeData.size END = " + mMergeData.size());
        notifyDataSetChanged();
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
        if( getItem(position) instanceof RadMenInfo) {
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
                convertView = mLayoutInflater.inflate(R.layout.list_item_contact, parent, false);
            }
        }
        switch (type) {
            case TYPE_ALP:
                TextView alphaText = (TextView) convertView.findViewById(R.id.alpha);
                alphaText.setText(mMergeData.get(position).toString());
                break;
            default:
                bindOriginData(convertView, (RadMenInfo) mMergeData.get(position));
                System.out.println("--------radmenuid = " +((RadMenInfo) mMergeData.get(position)).uid);
                break;
        }
        return convertView;
    }
    
    public void bindOriginData(View convertView, RadMenInfo user) {

        UserInfo userInfo = UserInfoManager.getInstance(mContext).getUserInfo(user.uid);

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
            name = "kdjfhdsfh";
            System.out.println("------------userInfo  = " + userInfo == null);
        }

        ImageView ivAvatar = (ImageView) convertView.findViewById(R.id.iv_contact_avatar);
        Picasso.with(mContext).load(avatar).placeholder(avatarId).into(ivAvatar);
        
        tvName.setText(Utils.getColoredText(name, mFilterConstraint,
                mContext.getResources().getColor(R.color.txt_keyword)));

        tvInfo.setText(shortDesc);

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
            return 1;
        }
    }
    
    public RadMenInfo setAlpha (RadMenInfo user, RadMenInfo ue) {
        ue.alpha = user.getAlpha(mContext);
        return ue;
    }
}
