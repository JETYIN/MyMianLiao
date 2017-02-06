package com.tjut.mianliao.scan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.tjut.mianliao.R;

public class ScanCoverView extends View {

    private static final int COVER_COLOR = 0x99000000;
    private static final int FRAME_COLOR = 0xFF38D3A9;
    private static final int FRAME_OFFSET = 6;

    private boolean mIsCoverActive = true;

    // Draw a cover over the preview to indicate the scan area.
    private RectF mRect;
    private Path mCoverPath;
    private static Paint sCoverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Draw a frame to over the scan area to make it obvious.
    private Drawable mFrameDrawable;
    private static Paint sFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        sCoverPaint.setColor(COVER_COLOR);
        sFramePaint.setColor(FRAME_COLOR);
    }

    public ScanCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFrameDrawable = getResources().getDrawable(R.drawable.bg_scan_frame);
    }

    public void setScanRect(RectF rect) {
        mRect = new RectF(rect.left, rect.top, rect.right, rect.bottom);
        mCoverPath = new Path();
        mCoverPath.setFillType(Path.FillType.INVERSE_WINDING);
        mCoverPath.addRect(mRect, Path.Direction.CW);

        // Some stupid android devices don't draw correct shape without the following two lines.
        mCoverPath.addCircle(0, 0, 1, Path.Direction.CW);
        mCoverPath.addCircle(getWidth(), getHeight(), 1, Path.Direction.CW);

        mFrameDrawable.setBounds((int) mRect.left - FRAME_OFFSET, (int) mRect.top - FRAME_OFFSET,
                (int) mRect.right + FRAME_OFFSET, (int) mRect.bottom + FRAME_OFFSET);

        invalidate();
    }

    public void setCoverActive(boolean active) {
        if (mIsCoverActive == active) {
            return;
        }
        mIsCoverActive = active;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsCoverActive && mRect != null) {
            canvas.drawPath(mCoverPath, sCoverPaint);
        } else {
            canvas.drawColor(COVER_COLOR);
        }

        if (mRect != null) {
            mFrameDrawable.draw(canvas);
        }
    }
}
