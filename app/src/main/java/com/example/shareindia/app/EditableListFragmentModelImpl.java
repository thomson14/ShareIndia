package com.example.shareindia.app;

import com.example.shareindia.widget.EditableListAdapter;

/**
 * created by: veli
 * date: 8/24/18 1:36 PM
 */
public interface EditableListFragmentModelImpl<V extends EditableListAdapter.EditableViewHolder>
{
    void setLayoutClickListener(EditableListFragment.LayoutClickListener<V> clickListener);
}
