package com.tjut.mianliao.contact;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LetterBarView;
import com.tjut.mianliao.component.LetterBarView.OnLetterSelectListener;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.contact.ContactsAdapter.OnItemClickWrapperListener;
import com.tjut.mianliao.data.contact.CheckableUserEntry;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.settings.Settings;

public abstract class CheckableContactsActivity extends BaseActivity implements OnClickListener,
        OnItemClickListener, OnCheckedChangeListener, OnSearchTextListener {
	public static final String EXTRA_EXCEPT_INFOS = "extra_except_infos";
	public static final String EXTRA_IS_SET_RESULT = "extra_is_set_result";

	protected CheckBox mCbAll;
	protected ListView mLvContacts;

	protected TextView mGroupManage;
	protected CheckableContactsAdapter mAdapter;
	protected LinearLayout mMagicLinearLayout;
	private Settings mSettings;
	private ArrayList<UserInfo> mUsers;
	protected boolean isSetResult;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_checkable_contacts;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettings = Settings.getInstance(this);

		int titleResID = getTitleResID();
		if (titleResID != 0) {
			getTitleBar().showTitleText(titleResID, null);
		}
		getTitleBar().showRightText(android.R.string.ok, this);

		SearchView sv = (SearchView) findViewById(R.id.sv_search);
		mGroupManage = (TextView) findViewById(R.id.tv_groupchat_manage);
		sv.setHint(R.string.adc_input_hint);
		sv.setOnSearchTextListener(this);
		mCbAll = (CheckBox) findViewById(R.id.cb_all);
		mCbAll.setOnClickListener(this);
		mCbAll.setOnCheckedChangeListener(this);

		List<UserInfo> exceptInfos = getIntent().getParcelableArrayListExtra(
				EXTRA_EXCEPT_INFOS);
		isSetResult = getIntent().getBooleanExtra(EXTRA_IS_SET_RESULT, false);
		mAdapter = new CheckableContactsAdapter(this, exceptInfos);
		mLvContacts = (ListView) findViewById(R.id.lv_contacts);
		mLvContacts.setOnItemClickListener(mItemClickListener);
		mLvContacts.setAdapter(mAdapter);
		LetterBarView letterBar = (LetterBarView) findViewById(R.id.letter_bar);
        letterBar.setOnLetterSelectListener(new OnLetterSelectListener() {

            @Override
            public void onLetterSelect(String s) {
                if (s.equalsIgnoreCase("#")) {
                    mLvContacts.setSelection(0);
                } else {
                    if (mAdapter.containsAlpha(s)) {
                        mLvContacts.setSelection(mAdapter.getAlphaPosition(s));
                    }
                }
            }
        });
		mMagicLinearLayout = (LinearLayout) findViewById(R.id.ly_bg_checkable_contacts);
		mMagicLinearLayout.setBackgroundColor(0XFFF2F2F2);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cb_all:
			mAdapter.checkAll(mCbAll.isChecked());
			mAdapter.notifyDataSetChanged();
			break;

		case R.id.tv_right:
			final List<UserEntry> checkedUsers = mAdapter.getCheckedItems();
			if (checkedUsers == null) {
				toast(R.string.contacts_check_hint);
			} else {
				doAction(checkedUsers);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	}
	
    private OnItemClickWrapperListener<UserEntry> mItemClickListener = 
            new OnItemClickWrapperListener<UserEntry>() {

        @Override
        public void onItemClick(UserEntry ue, int position, View view, AdapterView<?> parent) {
            CheckableUserEntry cue = (CheckableUserEntry) parent.getItemAtPosition(position);
            if (cue != null) {
                mAdapter.toggle(cue);
                ((CheckBox) view.findViewById(R.id.cb_check)).setChecked(cue.checked);
                mCbAll.setChecked(mAdapter.allChecked());
            }
        }
    };

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mCbAll.setText(isChecked ? R.string.uncheck_all : R.string.check_all);
	}

	@Override
	public void onSearchTextChanged(CharSequence text) {
		mCbAll.setVisibility(TextUtils.isEmpty(text) ? View.VISIBLE : View.GONE);
		mAdapter.getFilter().filter(text);
	}

	protected abstract int getTitleResID();

	protected abstract void doAction(List<UserEntry> checkedUsers);
}
