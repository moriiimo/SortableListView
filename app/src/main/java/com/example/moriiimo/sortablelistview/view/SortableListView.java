package com.example.moriiimo.sortablelistview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * ソート可能なリストビュー
 * via http://qiita.com/amay077/items/9f63e28db754fddb2aba
 * Created by moriiimo on 2016/06/07.
 */
public class SortableListView extends ListView {

    private static final String TAG = SortableListView.class.getSimpleName();

    private static final int SCROLL_SPEED_FAST = 25;
    private static final int SCROLL_SPEED_SLOW = 8;
    private static final Bitmap.Config DRAG_BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    private boolean mSortable;
    private boolean mDragging;
    private boolean mSortButtonImageTouched;

    private DragListener mDragListener;
    private int mBitmapBackgroundColor = Color.argb(128, 0xFF, 0xFF, 0xFF);
    private Bitmap mDragBitmap;
    private ImageView mDragImageView;
    private WindowManager.LayoutParams mLayoutParams;
    private MotionEvent mActionDownEvent;
    private int mPositionFrom = -1;

    public SortableListView(Context context) {
        super(context);
    }

    public SortableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SortableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(DragListener listener, ListAdapter adapter) {
//        setOnItemLongClickListener(this);
//        setOnItemClickListener(this);
        setDragListener(listener);
        setAdapter(adapter);
    }

    /**
     * タッチイベント処理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mSortable) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                Log.e(TAG, "action down " + event.getX() + ":" + event.getY());
                storeMotionEvent(event);
                startDrag();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (duringDrag(event)) {
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                Log.e(TAG, "action_up");
                if (stopDrag(event, true)) {
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE: {
                if (stopDrag(event, false)) {
                    return true;
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }
    /**
     * ACTION_DOWN 時の MotionEvent をプロパティに格納
     */
    private void storeMotionEvent(MotionEvent event) {
        mActionDownEvent = MotionEvent.obtain(event); // 複製しないと値が勝手に変わる
    }

    /**
     * ドラッグ開始
     */
    private boolean startDrag() {
        // イベントから position を取得
        mPositionFrom = eventToPosition(mActionDownEvent);
        Log.e(TAG, String.valueOf(mPositionFrom));

        // 取得した position が 0未満＝範囲外の場合はドラッグを開始しない
        if (mPositionFrom < 0) {
            return false;
        }

        // タップした位置がボタン画像のあたりではない場合はドラッグ中と判定しない
        if (!mSortButtonImageTouched) {
            return false;
        }

        mDragging = true;

        // View, Canvas, WindowManager の取得・生成
        final View view = getChildByIndex(mPositionFrom);
        final Canvas canvas = new Canvas();
        final WindowManager wm = getWindowManager();

        // ドラッグ対象要素の View を Canvas に描画
        mDragBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                DRAG_BITMAP_CONFIG);
        canvas.setBitmap(mDragBitmap);
        view.draw(canvas);

        // 前回使用した ImageView が残っている場合は除去（念のため？）
        if (mDragImageView != null) {
            wm.removeView(mDragImageView);
        }

        // ImageView 用の LayoutParams が未設定の場合は設定する
        if (mLayoutParams == null) {
            initLayoutParams();
        }

        // ImageView を生成し WindowManager に addChild する
        mDragImageView = new ImageView(getContext());
        mDragImageView.setBackgroundColor(mBitmapBackgroundColor);
        mDragImageView.setImageBitmap(mDragBitmap);
        wm.addView(mDragImageView, mLayoutParams);

        // ドラッグ開始
        if (mDragListener != null) {
            mPositionFrom = mDragListener.onStartDrag(mPositionFrom);
        }
        return duringDrag(mActionDownEvent);
    }

    /**
     * ドラッグ処理
     */
    private boolean duringDrag(MotionEvent event) {
        if (!mDragging || mDragImageView == null) {
            return false;
        }
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int height = getHeight();
        final int middle = height / 2;

        // スクロール速度の決定
        final int speed;
        final int fastBound = height / 9;
        final int slowBound = height / 4;
        if (event.getEventTime() - event.getDownTime() < 500) {
            // ドラッグの開始から500ミリ秒の間はスクロールしない
            speed = 0;
        } else if (y < slowBound) {
            speed = y < fastBound ? -SCROLL_SPEED_FAST : -SCROLL_SPEED_SLOW;
        } else if (y > height - slowBound) {
            speed = y > height - fastBound ? SCROLL_SPEED_FAST
                    : SCROLL_SPEED_SLOW;
        } else {
            speed = 0;
        }

        // スクロール処理
        if (speed != 0) {
            // 横方向はとりあえず考えない
            int middlePosition = pointToPosition(0, middle);
            if (middlePosition == AdapterView.INVALID_POSITION) {
                middlePosition = pointToPosition(0, middle + getDividerHeight()
                        + 64);
            }
            final View middleView = getChildByIndex(middlePosition);
            if (middleView != null) {
                setSelectionFromTop(middlePosition, middleView.getTop() - speed);
            }
        }

        // ImageView の表示や位置を更新
        if (mDragImageView.getHeight() < 0) {
            mDragImageView.setVisibility(View.INVISIBLE);
        } else {
            mDragImageView.setVisibility(View.VISIBLE);
        }
        updateLayoutParams((int) event.getRawY()); // ここだけスクリーン座標を使う
        getWindowManager().updateViewLayout(mDragImageView, mLayoutParams);
        if (mDragListener != null) {
            mPositionFrom = mDragListener.onDuringDrag(mPositionFrom,
                    pointToPosition(x, y + 100));
        }
        return true;
    }

    /**
     * ドラッグ終了
     */
    private boolean stopDrag(MotionEvent event, boolean isDrop) {
        if (!mDragging) {
            return false;
        }

        if (mSortButtonImageTouched) {
            setSortButtonImageTouched(false);
        }

        if (isDrop && mDragListener != null) {
            mDragListener.onStopDrag(mPositionFrom, eventToPosition(event));
        }

        mDragging = false;

        if (mDragImageView != null) {
            getWindowManager().removeView(mDragImageView);
            mDragImageView = null;
            // リサイクルするとたまに死ぬけどタイミング分からない by vvakame
            if (mDragBitmap != null) {
                mDragBitmap.recycle();
                mDragBitmap = null;
            }

            mActionDownEvent.recycle();
            mActionDownEvent = null;
            return true;
        }

        return false;
    }

    /**
     * 指定インデックスのView要素を取得する
     */
    private View getChildByIndex(int index) {
        return getChildAt(index - getFirstVisiblePosition());
    }

    /**
     * MotionEvent から position を取得する
     */
    private int eventToPosition(MotionEvent event) {
        return pointToPosition((int) event.getX(), (int) event.getY());
    }

    /**
     * WindowManager の取得
     */
    private WindowManager getWindowManager() {
        return (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
    }

    /**
     * ImageView 用 LayoutParams の初期化
     */
    private void initLayoutParams() {
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.windowAnimations = 0;
        mLayoutParams.x = getLeft();
        mLayoutParams.y = getTop();
    }

    /**
     * ImageView 用 LayoutParams の座標情報を更新
     */
    private void updateLayoutParams(int rawY) {
        mLayoutParams.y = rawY - 32;
    }

    public void setDragListener(DragListener listener) {
        mDragListener = listener;
    }

    public void setSortable(boolean sortable) {
        mSortable = sortable;
    }

    public void setSortButtonImageTouched(boolean sortButtonImageTouched) {
        mSortButtonImageTouched = sortButtonImageTouched;
    }

    /**
     * ソートモードの取得
     */
    public boolean getSortable() {
        return mSortable;
    }

    public interface DragListener {
        /**
         * ドラッグ開始時の処理
         */
        int onStartDrag(int position);

        /**
         * ドラッグ中の処理
         */
        int onDuringDrag(int positionFrom, int positionTo);

        /**
         * ドラッグ終了＝ドロップ時の処理
         */
        boolean onStopDrag(int positionFrom, int positionTo);
    }
}