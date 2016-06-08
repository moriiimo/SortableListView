package com.example.moriiimo.sortablelistview.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;
import com.example.moriiimo.sortablelistview.R;

import com.example.moriiimo.sortablelistview.adapter.SampleAdapter;
import com.example.moriiimo.sortablelistview.view.SortableListView;

public class MainActivity extends AppCompatActivity {

    static final String[] PREFS = { "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県",
            "福島県", "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県", "新潟県",
            "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県", "静岡県", "愛知県", "三重県",
            "滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県", "鳥取県", "島根県", "岡山県",
            "広島県", "山口県", "徳島県", "香川県", "愛媛県", "高知県", "福岡県", "佐賀県", "長崎県",
            "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県" };

    int mDraggingPosition = -1;
    SampleAdapter mAdapter;
    SortableListView mListView;
    RequestQueue mQueue;
    ImageLoader mImageLoader;
    ImageCache mCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdapter = new SampleAdapter(getLayoutInflater(), PREFS, mDraggingPosition, getImageLoader());
        mListView = (SortableListView) findViewById(R.id.list);
        mListView.setDragListener(new DragListener());
        mListView.setSortable(true);
        mListView.setAdapter(mAdapter);
    }

    private RequestQueue getQueue() {
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mQueue;
    }

    public ImageCache getCache() {
        if (mCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
            mCache = new LoaderImageCache(cacheSize);
        }
        return mCache;
    }

    private ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(getQueue(), getCache());
        }
        return mImageLoader;
    }

    class DragListener extends SortableListView.SimpleDragListener {
        @Override
        public int onStartDrag(int position) {
            mDraggingPosition = position;
            mListView.invalidateViews();
            return position;
        }

        @Override
        public int onDuringDrag(int positionFrom, int positionTo) {
            if (positionFrom < 0 || positionTo < 0
                    || positionFrom == positionTo) {
                return positionFrom;
            }
            int i;
            if (positionFrom < positionTo) {
                final int min = positionFrom;
                final int max = positionTo;
                final String data = PREFS[min];
                i = min;
                while (i < max) {
                    PREFS[i] = PREFS[++i];
                }
                PREFS[max] = data;
            } else if (positionFrom > positionTo) {
                final int min = positionTo;
                final int max = positionFrom;
                final String data = PREFS[max];
                i = max;
                while (i > min) {
                    PREFS[i] = PREFS[--i];
                }
                PREFS[min] = data;
            }
            mDraggingPosition = positionTo;
            mListView.invalidateViews();
            return positionTo;
        }

        @Override
        public boolean onStopDrag(int positionFrom, int positionTo) {
            mDraggingPosition = -1;
            mListView.invalidateViews();
            return super.onStopDrag(positionFrom, positionTo);
        }
    }

    /**
     * 画像キャッシュクラス
     */
    private class LoaderImageCache extends LruCache<String, Bitmap> implements
            ImageCache {
        /**
         * コンストラクタ
         */
        public LoaderImageCache(final int maxSize) {
            super(maxSize);
        }

        /**
         * サイズ取得
         */
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }

        /**
         * 画像URL取得
         */
        @Override
        public Bitmap getBitmap(final String url) {
            return get(url);
        }

        /**
         * 画像URL設定
         */
        @Override
        public void putBitmap(final String url, final Bitmap bitmap) {
            put(url, bitmap);
        }
    }
}
