package com.tjut.mianliao.register;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CnArrayAdapter;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class ChooseDepartmentActivity extends BaseActivity implements
        SearchView.OnSearchTextListener, AdapterView.OnItemClickListener {

    private static final int OTHER_DEPARTMENT_ID = 0;
    public static final String DEPARTMENT_ID = "department_id";

    private CnArrayAdapter<Department> mAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_search_generic;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.reg_choose_department, null);
        ((TextView) findViewById(R.id.tv_search_hint)).setText(R.string.reg_choose_department_hint);

        SearchView svDepartment = (SearchView) findViewById(R.id.sv_search);
        svDepartment.setHint(R.string.reg_choose_department_search_hint);
        svDepartment.setOnSearchTextListener(this);

        getTitleBar().showProgress();
        new AdvAsyncTask<Void, Void, MsResponse>() {

            @Override
            protected MsResponse doInBackground(Void... params) {
                return HttpUtil.msRequest(getApplicationContext(), MsRequest.LIST_DEPARTMENT,
                        "school_id=" + RegInfo.getInstance().schoolId);
            }

            @Override
            protected void onPostExecute(MsResponse response) {
                super.onPostExecute(response);
                getTitleBar().hideProgress();
                Utils.logD(getTag(), response.response);
                if (MsResponse.isSuccessful(response)) {
                    JSONArray ja = response.json.optJSONArray(MsResponse.PARAM_RESPONSE);
                    initData(JsonUtil.getArray(ja, Department.TRANSFORMER));
                }
            }
        }.executeLong();
    }

    private void initData(ArrayList<Department> departments) {
        mAdapter = new CnArrayAdapter<Department>(getApplicationContext(),
                R.layout.list_item_tv_search_result, departments);
        mAdapter.setExtraItem(new Department(OTHER_DEPARTMENT_ID,
                getString(R.string.reg_other_department)), 5);

        PullToRefreshListView ptrlvDepartments = (PullToRefreshListView) findViewById(R.id.lv_search_result);
        ptrlvDepartments.setMode(PullToRefreshBase.Mode.DISABLED);
        ptrlvDepartments.getRefreshableView().addFooterView(new View(this));
        ptrlvDepartments.setAdapter(mAdapter);
        ptrlvDepartments.setOnItemClickListener(this);
    }

    @Override
    public void onSearchTextChanged(CharSequence text) {
        if (mAdapter != null) {
            mAdapter.getFilter().filter(text);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Department department = (Department) parent.getItemAtPosition(position);
        RegInfo info = RegInfo.getInstance();
        info.departmentId = department.id;
        info.departmentName = department.name;
        if (getIntent().getBooleanExtra(RegInfo.EDIT, false)) {
            setResult(RESULT_UPDATED);
            finish();
        } else {
            Intent i = new Intent(this, ChooseStartYearActivity.class);
            i.putExtra(DEPARTMENT_ID,department.id);
            startActivity(i);
        }
    }

    private static class Department {
        int id;
        String name;

        Department(int id, String name) {
            this.id = id;
            this.name = name;
        }

        static final JsonUtil.ITransformer<Department> TRANSFORMER
                = new JsonUtil.ITransformer<Department>() {
            @Override
            public Department transform(JSONObject json) {
                return json == null ?
                        null : new Department(json.optInt("id"), json.optString("name"));
            }
        };

        @Override
        public String toString() {
            return name;
        }
    }
}