package com.tjut.mianliao.curriculum.cell;

import java.util.Stack;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.tjut.mianliao.R;

/**
 * A ViewGroup which act similar as GridLayout except: 1) Support lower android
 * versions. 2) Can focus a cell. 3) Each raw cell(colSpan = 1, rowSpan = 1) has
 * the same size. 4) 1px cell spacing filled with color. 5) Support only 1 type
 * of child view. 6) Full control with the code :)
 */
public class CellLayout extends ViewGroup {
    // private static final String TAG = "CellLayout";

    private static Paint mPaint;

    static {
        mPaint = new Paint();
        mPaint.setColor(0X0DFFFFFF);
    }

    private BaseCellAdapter mCellAdapter;

    private CellCutter mCellCutter;

    private Stack<View> mViewCache = new Stack<View>();

    private OnCellClickListener mOnCellClickListener;
    private OnClickListener mOnClickListener;

    private boolean mDataSetChanged = false;

    private int mTouchDownX;
    private int mTouchDownY;
    private int mTouchSlop;
    private Cell mFocusedCell = new Cell();
    private Drawable mFocusedDrawable;

    public CellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCellCutter = new CellCutter();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mFocusedDrawable = context.getResources().getDrawable(R.drawable.ic_add_course);

        setWillNotDraw(false);
        setClickable(true);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mDataSetChanged || changed) {
            defocusCell();
            mCellCutter.setTotalWidth(getWidth());
            resetContent();
            layoutChildren();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mCellCutter.getTotalHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnCellClickListener == null) {
            return false;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = (int) event.getX(0);
                mTouchDownY = (int) event.getY(0);
                break;

            case MotionEvent.ACTION_UP:
                int x = (int) event.getX(0);
                int y = (int) event.getY(0);
                if (mTouchSlop > Math.abs(x - mTouchDownX) && mTouchSlop > Math.abs(y - mTouchDownY)) {
                    onCellClicked(mCellCutter.getCol(x), mCellCutter.getRow(y));
                } else {
                    defocusCell();
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                defocusCell();
                mTouchDownX = 0;
                mTouchDownY = 0;
                break;

            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int col = mCellCutter.getNumCols();
        while (col-- > 0) {
            int pos = mCellCutter.getColPosition(col);
            canvas.drawLine(pos, 0, pos, getHeight(), mPaint);
        }

        int row = mCellCutter.getNumRows();
        while (row-- > 0) {
            int pos = mCellCutter.getRowPosition(row);
            canvas.drawLine(0, pos, getWidth(), pos, mPaint);
        }

        if (!mFocusedCell.isEmpty()) {
            mFocusedDrawable.setBounds(mCellCutter.getRect(mFocusedCell.col, mFocusedCell.rowStart,
                    mFocusedCell.getRowSpan()));
            mFocusedDrawable.draw(canvas);
        }
    }

    public void setOnCellClickListener(OnCellClickListener l) {
        mOnCellClickListener = l;
    }

    public void setOnInnerClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    private void resetContent() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(0);
            removeView(v);
            mViewCache.push(v);
        }
    }

    private void defocusCell() {
        if (!mFocusedCell.isEmpty()) {
            invalidate(mCellCutter.getRect(mFocusedCell));
            mFocusedCell.reset();
        }
    }

    private void focusCell(int col, int row) {
        if (mFocusedCell.isEmpty()) {
            mFocusedCell.col = col;
            mFocusedCell.rowStart = row;
            mFocusedCell.rowEnd = row;
            invalidate(mCellCutter.getRect(mFocusedCell));
        }
    }

    private void onCellClicked(int col, int row) {
        if (!mFocusedCell.isEmpty() && mFocusedCell.equals(col, row, row)) {
            if (mOnCellClickListener != null) {
                mOnCellClickListener.onCellClicked(col, row);
            }
            defocusCell();
            return;
        } else {
            defocusCell();
            focusCell(col, row);
            if (mOnClickListener != null) {
                mOnClickListener.onClick();
            }
        }
    }

    private void layoutChildren() {
        if (mCellAdapter != null && mCellAdapter.getCount() > 0) {
            int count = mCellAdapter.getCount();
            for (int i = 0; i < count; i++) {
                Rect rect = mCellCutter.getRect(mCellAdapter.getCell(i));
                View view = mCellAdapter.getView(i, mViewCache.size() == 0 ? null : mViewCache.pop(), this);
                LayoutParams lp = view.getLayoutParams();
                if (lp == null) {
                    lp = new LayoutParams(0, 0);
                }
                lp.width = rect.width();
                lp.height = rect.height();

                view.setOnTouchListener(mOnChildTouchListener);
                view.measure(MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY));
                view.layout(rect.left, rect.top, rect.right, rect.bottom);
                addViewInLayout(view, 0, lp);
            }
        }
    }

    public void setAdapter(BaseCellAdapter adapter) {
        if (mCellAdapter == null ? adapter == null : mCellAdapter == adapter) {
            return;
        }
        if (mCellAdapter != null) {
            mCellAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mCellAdapter = adapter;
        if (mCellAdapter != null) {
            mCellAdapter.registerDataSetObserver(mDataSetObserver);
            mCellCutter.updateAttr(mCellAdapter.getNumCols(), mCellAdapter.getNumRows(), mCellAdapter.getCellHeight());
        }
        mDataSetChanged = true;
        requestLayout();
    }

    private OnTouchListener mOnChildTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            defocusCell();
            return false;
        }
    };

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mDataSetChanged = true;
            requestLayout();
            invalidate();
        }
    };

    /**
     * A cell that has a colspan = 1;
     */
    public static class Cell {
        public int col = -1;
        public int rowStart = -1;
        public int rowEnd = -1;

        public boolean isEmpty() {
            return col <= 0 || rowStart <= 0;
        }

        public int getRowSpan() {
            return rowEnd - rowStart + 1;
        }

        public void reset() {
            col = -1;
            rowStart = -1;
            rowEnd = -1;
        }

        public void merge(int col, int rowStart, int rowEnd) {
            if (this.col == col) {
                this.rowStart = Math.min(this.rowStart, rowStart);
                this.rowEnd = Math.max(this.rowEnd, rowEnd);
            }
        }

        @Override
        public int hashCode() {
            return (col << 8) + (rowStart << 4) + rowEnd;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Cell) {
                Cell c = (Cell) o;
                return equals(c.col, c.rowStart, c.rowEnd);
            }
            return false;
        }

        public boolean equals(int col, int rowStart, int rowEnd) {
            return this.col == col && this.rowStart == rowStart && this.rowEnd == rowEnd;
        }
    }

    public interface OnItemClickListener {
        public void onItemClicked();
    }

    public interface OnCellClickListener {
        public void onCellClicked(int col, int row);
    }

    public interface OnClickListener{
        public void onClick();
    }
}
