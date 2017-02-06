package com.tjut.mianliao.curriculum.cell;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.tjut.mianliao.curriculum.cell.CellLayout.Cell;

public abstract class BaseCellAdapter implements Adapter {

    private DataSetObserver mObserver;

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mObserver = observer;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mObserver = null;
    }

    public void notifyDataSetChanged() {
        if (mObserver != null) {
            mObserver.onChanged();
        }
    }

    @Override
    public abstract int getCount();

    @Override
    public abstract Object getItem(int position);

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    @Override
    public final int getItemViewType(int position) {
        return 0;
    }

    @Override
    public final int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public abstract Cell getCell(int position);

    public abstract int getNumCols();

    public abstract int getNumRows();

    public abstract int getCellHeight();
}
