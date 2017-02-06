package me.nereo.imagechoose;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import me.nereo.imagechoose.view.ScaleImageView;
import me.nereo.multi_image_selector.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;


public class ShowActivity extends Activity {

    private ScaleImageView mScaleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_show);

        String path = getIntent().getStringExtra("path");

        mScaleImageView = (ScaleImageView) findViewById(R.id.mScaleImageView);

        Bitmap loacalBitmap = getLoacalBitmap(path);
        mScaleImageView.setImageBitmap(loacalBitmap);


    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
