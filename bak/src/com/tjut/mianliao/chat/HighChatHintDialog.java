package com.tjut.mianliao.chat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.tjut.mianliao.R;

public class HighChatHintDialog extends Dialog{
    
    private ImageView mIvConfim;
    
    public HighChatHintDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_highchat_hint_dialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mIvConfim = (ImageView) findViewById(R.id.iv_confim);
        mIvConfim.setOnClickListener(myDialogclickListener);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }
    
     private View.OnClickListener myDialogclickListener = new View.OnClickListener() {
         
         @Override
         public void onClick(View v) {
             HighChatHintDialog.this.dismiss();
         }
     };
 
}
