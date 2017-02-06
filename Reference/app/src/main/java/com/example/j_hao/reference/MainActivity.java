package com.example.j_hao.reference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        show();

    }

    private void show() {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String inviteString = "邀请儿子";
        intent.putExtra(Intent.EXTRA_SUBJECT, inviteString);
        intent.putExtra(Intent.EXTRA_TEXT, "快来吧，我的跳儿子，赶快下载吧，点击就送草泥马" + "http://t.cn/8sBdKIS");
        startActivity(Intent.createChooser(intent, inviteString));
    }
}
