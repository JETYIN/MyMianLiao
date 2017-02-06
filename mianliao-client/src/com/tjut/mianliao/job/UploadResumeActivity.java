package com.tjut.mianliao.job;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.ResumeAlt;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MenuHelper;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class UploadResumeActivity extends BaseActivity implements
        DialogInterface.OnClickListener, AdapterView.OnItemClickListener {

    private static final int MAX_RESUME_COUNT = 3;
    private static final int MAX_RESUME_SIZE = 5 * 1024 * 1024;

    private ListView mListView;
    private ResumeAdapter mResumeAdapter;

    private LightDialog mUploadingDialog;
    private LightDialog mMenuDialog;
    private MenuHelper mMenuHelper;
    private ResumeAlt mResume;

    private ArrayList<UploadResumeTask> mUploadTasks;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_upload_resume;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.rsm_upload_resume_title));
        mUploadTasks = new ArrayList<UploadResumeTask>();

        mListView = (ListView) findViewById(R.id.lv_resume);
        mListView.setOnItemClickListener(this);

        new LoadResumeTask().executeLong();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getIdentity() && data != null) {
            Uri uri = data.getData();
            String path = Utils.getPath(this, uri);
            if (TextUtils.isEmpty(path)) {
                toast(R.string.att_error_invalid);
                return;
            }

            File file = new File(path);
            if (file.isFile()) {
                if (file.length() > MAX_RESUME_SIZE) {
                    toast(R.string.att_error_reach_limit);
                } else {
                    ResumeAlt resume = new ResumeAlt(file);
                    mResumeAdapter.add(resume);
                    uploadResume(resume);
                }
            } else {
                toast(R.string.att_error_invalid);
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (mUploadTasks.isEmpty()) {
            super.onBackPressed();
        } else {
            showUploadingDialog();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mUploadingDialog) {
            for (UploadResumeTask task : mUploadTasks) {
                task.cancel(true);
            }
            finish();
        } else if (dialog == mMenuDialog) {
            switch (mMenuHelper.get(which).id) {
                case R.integer.mi_rsm_download:
                    downloadResume(mResume);
                    break;
                case R.integer.mi_rsm_upload:
                    uploadResume(mResume);
                    break;
                case R.integer.mi_rsm_delete:
                    deleteResume(mResume);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mResume = (ResumeAlt) parent.getItemAtPosition(position);
        if (mResume == null) {
            Utils.pickAttachment(this, getIdentity());
        } else {
            if (mResume.status == ResumeAlt.STATUS_UPLOADING) {
                toast(R.string.rsm_tst_resume_uploading);
            } else {
                showMenuDialog();
            }
        }
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuHelper = new MenuHelper(this, R.array.menu_resume_item);
            mMenuDialog = new LightDialog(this)
                    .setTitleLd(R.string.please_choose)
                    .setItems(mMenuHelper.getMenu(), this);
        }
        mMenuHelper.setEnabled(R.integer.mi_rsm_download,
                mResume.status == ResumeAlt.STATUS_UPLOADED);
        mMenuHelper.setEnabled(R.integer.mi_rsm_upload,
                mResume.status == ResumeAlt.STATUS_UPLOAD_FAILED);
        mMenuDialog.show();
    }

    private void showUploadingDialog() {
        if (mUploadingDialog == null) {
            mUploadingDialog = new LightDialog(this)
                    .setTitleLd(R.string.qa_upload_title)
                    .setMessage(R.string.qa_upload_message)
                    .setNegativeButton(R.string.qa_upload_wait, null)
                    .setPositiveButton(R.string.qa_upload_quit, this)
                    .setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                            R.drawable.selector_btn_red);
        }
        mUploadingDialog.show();
    }

    private void uploadResume(ResumeAlt resume) {
        if (resume.status != ResumeAlt.STATUS_UPLOADING) {
            resume.status = ResumeAlt.STATUS_UPLOADING;
            mResumeAdapter.notifyDataSetChanged();
        }
        HashMap<String, String> files = new HashMap<String, String>();
        files.put("attachment", resume.att.url);
        new UploadResumeTask(resume, files).executeLong();
    }

    private void downloadResume(ResumeAlt resume) {
        Utils.downloadFile(this, resume.att.url, resume.att.name);
    }

    private void deleteResume(ResumeAlt resume) {
        if (resume.status == ResumeAlt.STATUS_UPLOADED) {
            new DeleteResumeTask(resume).executeLong();
        } else {
            mResumeAdapter.remove(resume);
        }
    }

    private class LoadResumeTask extends MsTask {

        public LoadResumeTask() {
            super(getApplicationContext(), MsRequest.JOB_MY_RESUME_ALT);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mResumeAdapter = new ResumeAdapter();
            if (response.isSuccessful()) {
                mResumeAdapter.addAll(JsonUtil.getArray(
                        response.getJsonArray(), ResumeAlt.TRANSFORMER));
            } else {
                response.showFailInfo(getRefContext(), R.string.rsm_tst_load_failed);
            }
            mListView.setAdapter(mResumeAdapter);
        }
    }

    private class UploadResumeTask extends MsMhpTask {
        private ResumeAlt mResume;

        public UploadResumeTask(ResumeAlt resume, HashMap<String, String> files) {
            super(getApplicationContext(), MsRequest.JOB_CREATE_RESUME_ALT, null, files);
            mResume = resume;
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            mUploadTasks.add(this);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mUploadTasks.remove(this);
            if (mUploadTasks.isEmpty()) {
                getTitleBar().hideProgress();
            }

            if (response.isSuccessful()) {
                mResume.copy(ResumeAlt.fromJson(response.getJsonObject()));
            } else {
                mResume.status = ResumeAlt.STATUS_UPLOAD_FAILED;
                response.showFailInfo(getRefContext(), R.string.rsm_tst_upload_failed);
            }
            mResumeAdapter.notifyDataSetChanged();
        }
    }

    private class DeleteResumeTask extends MsTask {
        private ResumeAlt mResume;

        public DeleteResumeTask(ResumeAlt resume) {
            super(getApplicationContext(), MsRequest.JOB_DELETE_RESUME_ALT);
            mResume = resume;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("resume_alt_ids=").append(mResume.id).toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                mResumeAdapter.remove(mResume);
            } else {
                response.showFailInfo(getRefContext(), R.string.rsm_tst_delete_failed);
            }
        }
    }

    private class ResumeAdapter extends ArrayAdapter<ResumeAlt> {

        public ResumeAdapter() {
            super(getApplicationContext(), 0);
        }

        @Override
        public int getCount() {
            return Math.min(super.getCount() + 1, MAX_RESUME_COUNT);
        }

        @Override
        public ResumeAlt getItem(int position) {
            return position < super.getCount() ? super.getItem(position) : null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_resume_att, parent, false);
            } else {
                view = convertView;
            }
            ResumeAlt resume = getItem(position);

            ImageView ivIcon = (ImageView) view.findViewById(R.id.resume_icon);
            if (resume == null) {
                ivIcon.setImageResource(R.drawable.resume_add);
                Utils.setText(view, R.id.resume_lable, R.string.rsm_add_resume_lable);
                Utils.setText(view, R.id.resume_info, R.string.rsm_upload_resume_hint);
            } else {
                switch (resume.status) {
                    case ResumeAlt.STATUS_UPLOAD_FAILED:
                        ivIcon.setImageResource(R.drawable.resume_failed);
                        break;
                    case ResumeAlt.STATUS_UPLOADED:
                        ivIcon.setImageResource(R.drawable.resume_uploaded);
                        break;
                    case ResumeAlt.STATUS_UPLOADING:
                    default:
                        ivIcon.setImageResource(R.drawable.resume_uploading);
                        break;
                }

                Utils.setText(view, R.id.resume_lable, R.string.rsm_uploaded_resume_lable);

                String info = getString(R.string.att_information, resume.att.name,
                        Utils.getAttSizeString(getContext(), resume.att.size));
                Utils.setText(view, R.id.resume_info, Html.fromHtml(info));
            }

            return view;
        }
    }
}
