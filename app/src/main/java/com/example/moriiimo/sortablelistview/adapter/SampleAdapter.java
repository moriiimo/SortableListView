package com.example.moriiimo.sortablelistview.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.moriiimo.sortablelistview.R;

/**
 * SampleAdapter
 * Created by moriiimo on 2016/06/08.
 */
public class SampleAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = SampleAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private String[] mNames;
    private int mDraggingPosition;
    private ImageLoader mImageLoader;

    private boolean mSortable;
    private View.OnTouchListener mOnTouchListener;

    public SampleAdapter(LayoutInflater inflater, String[] names, int draggingPosition, ImageLoader imageLoader, View.OnTouchListener listener) {
        mInflater = inflater;
        mNames = names;
        mDraggingPosition = draggingPosition;
        mImageLoader = imageLoader;
        mOnTouchListener = listener;
    }

    @Override
    public int getCount() {
        return mNames.length;
    }

    @Override
    public String getItem(int position) {
        return mNames[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder vh;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_image_with_text, null);
            vh = new ViewHolder();
            vh.title = (TextView) convertView.findViewById(R.id.title_text);
            vh.image = (NetworkImageView) convertView.findViewById(R.id.list_image);
            vh.description = (TextView) convertView.findViewById(R.id.description);
            vh.sortButtonImage = (ImageView) convertView.findViewById(R.id.sort_button_image);
            vh.sortButtonImage.setOnTouchListener(mOnTouchListener);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        vh.title.setText(mNames[position]);
        vh.image.setImageUrl("http://tetoan.com/wp-content/uploads/2015/06/%E6%8F%A1%E6%89%8B%E3%81%A7%E3%81%8A%E9%87%91%E3%82%92%E3%82%82%E3%82%89%E3%81%86%E7%8C%AB.jpg", mImageLoader);
        vh.description.setText(String.format("%sの猫さん", mNames[position]));
        vh.sortButtonImage.setVisibility(mSortable ? View.VISIBLE : View.INVISIBLE);
        convertView.setVisibility(position == mDraggingPosition ? View.INVISIBLE : View.VISIBLE);
        return convertView;
    }

    public void setDraggingPosition(int draggingPosition) {
        mDraggingPosition = draggingPosition;
    }

    public void setSortable(boolean sortable) {
        mSortable = sortable;
    }

    static class ViewHolder {
        private NetworkImageView image;
        private TextView title;
        private TextView description;
        private ImageView sortButtonImage;
    }
}
