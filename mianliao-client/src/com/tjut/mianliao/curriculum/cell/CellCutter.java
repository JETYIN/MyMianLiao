package com.tjut.mianliao.curriculum.cell;

import android.graphics.Rect;

import com.tjut.mianliao.curriculum.cell.CellLayout.Cell;

/**
 * The purpose of this class it to help calculate the cell position/size for
 * cells of a CellLayout.
 */
public class CellCutter {

    private int mTotalWidth;

    private int mNumCols = 1;
    private int mNumRows = 1;

    private float mCellWidth;
    private int mCellHeight;

    private Rect mRect = new Rect();

    public CellCutter() {}

    public void updateAttr(int numCols, int numRows, int cellHeight) {
        mNumCols = numCols;
        mNumRows = numRows;
        mCellWidth = ((float) mTotalWidth) / numCols - 1;
        mCellHeight = cellHeight;
    }

    public void setTotalWidth(int totalWidth) {
        mTotalWidth = totalWidth;
        mCellWidth = ((float) mTotalWidth) / mNumCols - 1;
    }

    public Rect getRect(Cell cell) {
        return getRect(cell.col, cell.rowStart, cell.getRowSpan());
    }

    /**
     * @return The rect info for the specified area. If you need to modify it,
     *         make a copy.
     */
    public Rect getRect(int col, int row, int rowSpan) {
        float left = (col - 1) * mCellWidth + col - 1;
        mRect.left = (int) left;
        mRect.top = (row - 1) * mCellHeight + row - 1;
        if (col == mNumCols) {
            mRect.right = mTotalWidth;
        } else {
            mRect.right = (int) (mCellWidth + left);
        }
        mRect.bottom = mRect.top + rowSpan * mCellHeight + rowSpan - 1;
        return mRect;
    }

    public int getTotalHeight() {
        return mCellHeight > 0 ? mNumRows * mCellHeight + mNumRows - 1 : 0;
    }

    public int getCol(int x) {
        int col = 1;

        while (mCellWidth < x && col < mNumCols) {
            x = (int) (x - mCellWidth - 1);
            col++;
        }

        return col;
    }

    public int getRow(int y) {
        int row = 1;

        while (mCellHeight < y) {
            y = y - mCellHeight - 1;
            row++;
        }

        return row;
    }

    /**
     * @param col
     *            Starts from 1
     */
    public int getColPosition(int col) {
        return (int) (mCellWidth * col + col - 1);
    }

    /**
     * @param row
     *            Starts from 1
     */
    public int getRowPosition(int row) {
        return mCellHeight * row + row - 1;
    }

    public float getCellWidth() {
        return mCellWidth;
    }

    public int getCellHeight() {
        return mCellHeight;
    }

    public int getNumCols() {
        return mNumCols;
    }

    public int getNumRows() {
        return mNumRows;
    }

}
