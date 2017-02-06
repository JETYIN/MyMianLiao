package com.tjut.mianliao.chat;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class HighChatDialog extends Dialog{
	private TextView mConfirmDialog;

	public HighChatDialog(Context context,int theme) {
		super(context, theme);
	} 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_highchat_dialog);
		mConfirmDialog = (TextView) findViewById(R.id.tv_button_confirm);
		mConfirmDialog.setOnClickListener(myDialogclickListener);
	}
	 private View.OnClickListener myDialogclickListener = new View.OnClickListener() {
         
         @Override
         public void onClick(View v) {
        	 HighChatDialog.this.dismiss();
         }
	 };
 

}
