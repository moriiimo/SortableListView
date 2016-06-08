package com.example.moriiimo.sortablelistview.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.moriiimo.sortablelistview.R;

/**
 * SampleAdapter
 * Created by moriimo on 2016/06/08.
 */
public class SampleAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = SampleAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private String[] mNames;
    private int mDraggingPosition;
    private ImageLoader mImageLoader;
    private boolean mEditMode;

    public SampleAdapter(LayoutInflater inflater, String[] names, int draggingPosition, ImageLoader imageLoader) {
        mInflater = inflater;
        mNames = names;
        mDraggingPosition = draggingPosition;
        mImageLoader = imageLoader;
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
            vh.sortImageButton = (ImageButton) convertView.findViewById(R.id.sort_button);
            vh.sortImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    AdapterView.OnItemClickListener listener = ((ListView)parent).getOnItemClickListener();
                    if (listener == null) return;
                    listener.onItemClick((AdapterView<?>) parent, v, position, getItemId(position));
                }
            });
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        vh.title.setText(mNames[position]);
        vh.image.setImageUrl("http://tetoan.com/wp-content/uploads/2015/06/%E6%8F%A1%E6%89%8B%E3%81%A7%E3%81%8A%E9%87%91%E3%82%92%E3%82%82%E3%82%89%E3%81%86%E7%8C%AB.jpg", mImageLoader);
        vh.description.setText(String.format("%sの猫さん", mNames[position]));
        vh.sortImageButton.setVisibility(mEditMode ? View.VISIBLE : View.INVISIBLE);
        Log.e(TAG, mNames[position] + ";" + vh.title.getText() + ":" + mDraggingPosition);
        convertView.setVisibility(position == mDraggingPosition ? View.INVISIBLE : View.VISIBLE);
        return convertView;
    }

    /**
     * Adapter内のmDraggingPositionのアップデートをする
     * @param draggingPosition
     */
    public void updateDraggingPosition(int draggingPosition) {
        mDraggingPosition = draggingPosition;
    }

    /**
     * 編集モードの切り替え
     */
    public void toggleEditMode() {
        mEditMode = !mEditMode;
    }

    static class ViewHolder {
        private NetworkImageView image;
        private TextView title;
        private TextView description;
        private ImageButton sortImageButton;
    }
}
