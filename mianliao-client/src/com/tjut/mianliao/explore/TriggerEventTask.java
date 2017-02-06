package com.tjut.mianliao.explore;

import android.content.Context;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class TriggerEventTask extends MsTask {

	private Context mContext;
	private String mEvent;
	
	public TriggerEventTask(Context context, String event) {
		super(context, MsRequest.TRIGGER_EVENT);
		mContext = context;
		mEvent = event;
	}
	
	@Override
	protected String buildParams() {
		return "event=" + mEvent;
	}

	@Override
	protected void onPostExecute(MsResponse response) {
		if (response.isSuccessful()) {
			Toast.makeText(mContext, R.string.explor_mission_success, Toast.LENGTH_SHORT).show();
		}
	}
	
}
