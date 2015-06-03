package com.jahnold.syncaudiobookplayer.Adapters;

import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 *  Base Recycler Adapter
 */
public abstract class RecyclerAdapter<T,VH extends android.support.v7.widget.RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<T> mItems;

    // clears the list
    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<T> items) {
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void add(T item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    public void remove(T item) {
        mItems.remove(item);
    }

    public T getItem(int position) {
        return mItems.get(position);
    }

    // returns the number of books in the dataset
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
