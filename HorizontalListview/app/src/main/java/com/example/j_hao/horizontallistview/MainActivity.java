package com.example.j_hao.horizontallistview;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private ListView mListView;
    private LinearLayout mLinearLayout;
    private View headerView;
    private View item;
    ArrayList<String> arrayList = new ArrayList<>();

    private String[] arr1 = {"sgsgsg",
            "sgsghshgshgshgnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
            "rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr",
            "fsfsfsfssfs",
            "ggggggggggggggggg",
            "wwwwwwwwwwwwwwwww",
            "qqqqqqqqqqqqqqqqqqqqqqqqqqq"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView list1 = (ListView)findViewById(R.id.listview);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item,arr1);

        list1.setAdapter(adapter1);
    }




}
