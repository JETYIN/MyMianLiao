package com.tjut.mianliao.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;

public class EduBackActivity extends BaseActivity implements
		AdapterView.OnItemClickListener {
	@Override
	protected int getLayoutResID() {
		return R.layout.activity_edu_back;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getTitleBar().showTitleText(R.string.reg_choose_edu_back, null);
		//String[] mEduBacks = new String[] { "大专", "本科", "硕士" };
		ListView mEduList = (ListView) findViewById(R.id.lv_edu_back);
		// mEduList.setAdapter(new ArrayAdapter<>(this,
		// R.layout.list_item_tv_search_result, mEduBacks));
		mEduList.setAdapter(ArrayAdapter.createFromResource(this,
				R.array.reg_academic_diplomas,
				R.layout.list_item_tv_search_result));
		mEduList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String eduBack = (String) parent.getItemAtPosition(position);
		RegInfo.getInstance().eduback = eduBack;

		if (getIntent().getBooleanExtra(RegInfo.EDIT, false)) {
			setResult(RESULT_UPDATED);
			finish();
		} else {
			Intent i = new Intent(this, EduInfoActivity.class);
			startActivity(i);
		}
	}

}
