package com.tjut.mianliao.mycollege;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.job.Job;
import com.tjut.mianliao.job.JobDetailActivity;
import com.tjut.mianliao.util.Utils;

public class JobAdapter extends ArrayAdapter<Job> implements OnClickListener {

    private LayoutInflater mInflater;
    private Activity mActivity;

    public JobAdapter(Activity context) {
        super(context, 0);
        mActivity = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.list_item_jobs, parent, false);
        }
        Job job = getItem(position);
        ProImageView icon = (ProImageView) view.findViewById(R.id.iv_icon);
        TextView tvName = (TextView) view.findViewById(R.id.tv_job_name);
        TextView tvSalary = (TextView) view.findViewById(R.id.tv_salary);
        TextView tvComp = (TextView) view.findViewById(R.id.tv_comp);
        TextView tvLoc = (TextView) view.findViewById(R.id.tv_loc);
        TextView tvTime = (TextView) view.findViewById(R.id.tv_time);
        view.setTag(job);
        view.setOnClickListener(this);
        tvName.setText(job.title);
        tvSalary.setText(job.salary);
        tvComp.setText(job.corpName);
        tvLoc.setText(job.locCityName);
        icon.setImage(job.corpLogo, R.drawable.ic_avatar_corp);
        tvTime.setText(Utils.getTimeDesc(job.cTime));
        if (position == getCount() - 1) {
            view.findViewById(R.id.view_divider).setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_jobs:
                Job job = (Job) v.getTag();
                showJobDetails(job);
                break;

            default:
                break;
        }
    }


    private void showJobDetails(Job job) {
        Intent intent = new Intent(mActivity, JobDetailActivity.class);
        intent.putExtra(Job.INTENT_EXTRA_NAME, job);
        mActivity.startActivity(intent);
    }
}
