package com.tjut.mianliao.register;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;

public class ChooseStartYearActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static int sThisYear = Calendar.getInstance().get(Calendar.YEAR);
    private StartYear year;
    private int departmentId;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_choose_start_year;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleBar().showTitleText(R.string.reg_choose_start_year, null);

        int size = 20;
        StartYear[] years = new StartYear[size];
        for (int i = 0; i < size; i++) {
            years[i] = new StartYear(sThisYear - i,
                    getString(R.string.reg_start_year_desc, sThisYear - i));
        }
        Intent intent = getIntent();
        departmentId = intent.getIntExtra(ChooseDepartmentActivity.DEPARTMENT_ID,0);
        ListView lvYears = (ListView) findViewById(R.id.lv_start_year);
        lvYears.setAdapter(new ArrayAdapter<StartYear>(this,
                R.layout.list_item_tv_search_result, years));
        lvYears.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        year= (StartYear) parent.getItemAtPosition(position);
        RegInfo.getInstance().startYear = year.year;

        if (getIntent().getBooleanExtra(RegInfo.EDIT, false)) {
            setResult(RESULT_UPDATED);
            finish();
        } else {
            Intent i = new Intent(this, EduInfoActivity.class);
            startActivity(i);
        }
    }

    private static class StartYear {
        int year;
        String desc;

        StartYear(int year, String desc) {
            this.year = year;
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }
    }

//   private class UpdateuserinfoTask extends MsTask {
//
//    public UpdateuserinfoTask() {
//        super(ChooseStartYearActivity.this, MsRequest.USER_UPDATE_PROFILE);
//    }
//       @Override
//    protected String buildParams() {
//           StringBuilder sb = new StringBuilder();
//           sb.append("start_year=").append(year.year)
//           .append("&department_id").append(departmentId);
//        return sb.toString();
//    }
//   }

}