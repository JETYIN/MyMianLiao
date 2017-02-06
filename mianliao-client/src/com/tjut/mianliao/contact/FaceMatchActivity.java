package com.tjut.mianliao.contact;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.FaceInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.FaceManager;
import com.tjut.mianliao.util.FaceManager.FaceListener;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.Utils;

public class FaceMatchActivity extends BaseActivity implements
		View.OnClickListener, DialogInterface.OnClickListener, FaceListener,
		ImageResultListener {

	private static final int REQUEST_PROFILE = 100;

	private View mSetPictureView;
	private View mSetCategoryView;
	private ProImageView mIvAvatar;
	private ProImageView mIvSimilar;
	private Button mBtnSearch;
	private TextView mTvCategory;
	private TextView mTvInfo;
	private TextView mTvQuestionMark;

	private FaceManager mFaceManager;
	private GetImageHelper mGetImageHelper;
	private LightDialog mDlgSetCategory;

	private UserInfo mMyUser;
	private FaceInfo mMyFace;
	private FaceInfo mSimilarFace;
	private Bitmap mMyFaceBmp;

	private String[] mCategories;
	private String mGender;
	private AnimationDrawable mAnimation;
	private int mSimilarityColor;
	private int mSimilaritySize;
	private LinearLayout mMagicLinearLayout;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_face_match;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getTitleBar().showTitleText(R.string.adc_face_match, null);

		Resources res = getResources();
		mAnimation = (AnimationDrawable) res
				.getDrawable(R.drawable.anim_search_faces);
		mSimilarityColor = res.getColor(R.color.face_similarity);
		mSimilaritySize = res
				.getDimensionPixelSize(R.dimen.face_similarity_size);

		mSetPictureView = findViewById(R.id.ll_set_picture);
		mSetCategoryView = findViewById(R.id.ll_set_category);
		mIvAvatar = (ProImageView) findViewById(R.id.iv_avatar);
		mIvSimilar = (ProImageView) findViewById(R.id.iv_similar);
		mBtnSearch = (Button) findViewById(R.id.btn_search);
		mTvCategory = (TextView) findViewById(R.id.tv_category);
		mTvInfo = (TextView) findViewById(R.id.tv_info);
		mTvQuestionMark = (TextView) findViewById(R.id.tv_question_mark);

		mFaceManager = FaceManager.getInstance(this);
		mFaceManager.registerListener(this);
		mGetImageHelper = new GetImageHelper(this, this);

		mCategories = new String[] {
				res.getString(R.string.face_category_couple),
				res.getString(R.string.face_category_male) };
		mMyUser = new UserInfo();

		updateMyAvatar(AccountInfo.getInstance(this).getUserInfo());
		mMagicLinearLayout = (LinearLayout) findViewById(R.id.ll_bg_face_match);
	}

    @Override
	protected void onDestroy() {
		mFaceManager.unregisterListener(this);
		if (mMyFaceBmp != null) {
			mMyFaceBmp.recycle();
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_PROFILE && resultCode == RESULT_UPDATED) {
			UserInfo info = data.getParcelableExtra(UserInfo.INTENT_EXTRA_INFO);
			updateMyAvatar(info);
		} else if (resultCode == RESULT_OK) {
			mGetImageHelper.handleResult(requestCode, data);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_set_picture:
			mGetImageHelper.getImage(true, 1);
			break;

		case R.id.ll_set_category:
			showSetCategoryDialog();
			break;

		case R.id.btn_search:
			if (!Utils.isNetworkAvailable(this)) {
				mTvInfo.setText(R.string.no_network);
			} else if (mMyFace == null) {
				mTvInfo.setText(R.string.face_error_no_face);
			} else {
				setSearchStarted();
				mFaceManager.obtainSimilarFace(mMyFace, mGender);
			}
			break;

		case R.id.iv_similar:
			if (mSimilarFace != null) {
				Intent intent = new Intent(this, NewProfileActivity.class);
				intent.putExtra(NewProfileActivity.EXTRA_USER_FACE_ID,
						mSimilarFace.id);
				startActivityForResult(intent, REQUEST_PROFILE);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (dialog == mDlgSetCategory) {
			if (mMyFace != null) {
				mGender = which == 0 ? mMyFace.oppositeGender()
						: mMyFace.gender;
			}
			mTvCategory.setText(mCategories[which]);
		}
	}

	@Override
	public void onFaceErrorOccured(String errMsg) {
		getTitleBar().hideProgress();
		setSearchError(errMsg);
	}

	@Override
	public void onMyFaceObtained(FaceInfo face) {
		getTitleBar().hideProgress();
		setSearchPrepared(face);
	}

	@Override
	public void onSimilarFaceObtained(FaceInfo face) {
		setSearchSuccess(face);
	}

	@Override
	public void onImageResult(boolean success, String imageFile, Bitmap bm) {
		if (success) {
			mIvAvatar.setImageBitmap(bm);
			if (mMyFaceBmp != null) {
				mMyFaceBmp.recycle();
			}
			mMyFaceBmp = bm;
			mMyFace = null;
			obtainMyFace(bm, null);
		} else {
			setSearchError(getString(R.string.face_error_set_pic));
		}
	}

	@Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        if (success) {
            Bitmap bm = BitmapFactory.decodeFile(images.get(0));
            mIvAvatar.setImageBitmap(bm);
            if (mMyFaceBmp != null) {
                mMyFaceBmp.recycle();
            }
            mMyFaceBmp = bm;
            mMyFace = null;
            obtainMyFace(bm, null);
        } else {
            setSearchError(getString(R.string.face_error_set_pic));
        }
    }

    private void updateMyAvatar(UserInfo userInfo) {
		if (mMyUser.getAvatar() == null
				|| !mMyUser.getAvatar().equals(userInfo.getAvatar())) {
			mMyUser.copy(userInfo);
			mIvAvatar.setImage(mMyUser.getAvatar(), mMyUser.defaultAvatar());
			if (Utils.isNetworkAvailable(this)) {
				obtainMyFace(null, mMyUser.faceId);
			} else {
				setSearchEnabled(false);
				mTvInfo.setText(R.string.no_network);
			}
		}
	}

	private void obtainMyFace(Bitmap bitmap, String faceId) {
		if (bitmap != null || !TextUtils.isEmpty(faceId)) {
			setSearchEnabled(false);
			getTitleBar().showProgress();
			mFaceManager.obtainMyFace(bitmap, faceId);
		}
	}

	private void setSearchPrepared(FaceInfo face) {
		setSearchEnabled(true);
		mBtnSearch.setText(R.string.face_search_start);
		mTvCategory.setText(mCategories[0]);
		mTvInfo.setText("");
		mTvQuestionMark.setVisibility(View.VISIBLE);
		mIvSimilar.setVisibility(View.GONE);

		mGender = face.oppositeGender();
		mCategories[1] = getString(FaceInfo.MALE.equals(face.gender) ? R.string.face_category_male
				: R.string.face_category_female);
		mMyFace = face;
	}

	private void setSearchStarted() {
		setSearchEnabled(false);
		mTvInfo.setText(R.string.face_searching_desc);
		mTvQuestionMark.setVisibility(View.GONE);
		mIvSimilar.setVisibility(View.VISIBLE);
		mIvSimilar.setImageDrawable(mAnimation);
		mAnimation.start();
	}

	private void setSearchSuccess(FaceInfo face) {
		setSearchEnabled(true);
		mAnimation.stop();
		mBtnSearch.setText(R.string.face_search_continue);
		String similarity = String.format("%.2f%%", face.similarity);
		String info = getString(R.string.face_result_desc, similarity);
		mTvInfo.setText(Utils.getSizedText(
				Utils.getColoredText(info, similarity, mSimilarityColor, false),
				similarity, mSimilaritySize, false));

		mIvSimilar.setImage(face.url, 0);
		mSimilarFace = face;
	}

	private void setSearchError(String errMsg) {
		setSearchEnabled(true);
		mAnimation.stop();
		mTvInfo.setText(errMsg);
		mTvQuestionMark.setVisibility(View.VISIBLE);
		mIvSimilar.setVisibility(View.GONE);
	}

	private void setSearchEnabled(boolean enabled) {
		mSetPictureView.setEnabled(enabled);
		mSetCategoryView.setEnabled(enabled);
		mBtnSearch.setEnabled(enabled);
		mIvSimilar.setOnClickListener(enabled ? this : null);
	}

	private void showSetCategoryDialog() {
		if (mDlgSetCategory == null) {
			mDlgSetCategory = new LightDialog(this);
			mDlgSetCategory.setTitle(R.string.face_set_category);
			mDlgSetCategory.setItems(mCategories, this);
		}
		mDlgSetCategory.show();
	}
}
