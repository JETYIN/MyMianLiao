package com.example.j_hao.my_frament;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**此方法默认是否保存Fragment的状态。但是默认状态下当Activity被Destroy后Fragment的状态会被保存在bundle中，这会引发错误**/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        }
    }

