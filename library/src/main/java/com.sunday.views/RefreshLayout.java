package com.sunday.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.sunday.views.assist.ClassicsHeaderView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by zhongfei.sun on 2017/10/20.
 */

public class RefreshLayout extends ViewGroup {

    private HeaderView mHeaderView;
    private FootView mFootView;
    private float mLastX;
    private float mLastY;
    private float mLastRawX;
    private float mLastRawY;
    private int mHeadViewHeight;
    private int mFootViewHeight;
    private Scroller mScroller;
    private RefreshListener mRefreshListener;
    //是否可以拉动超出HeaderView的高度
    private boolean isFullPull = true;
    //是否可以拉动超出FooterView的高度
    private boolean isFullPush = true;
    //把HeaderView拉出的最大高度，isFullPull = true时 生效
    private int maxPullHeight;
    //把FooterView拉出的最大高度，isFullPush = true时 生效
    private int maxPushHeight;
    //滑动差值，值越小，滑动速度越慢
    private float mMoveRate = 0.3f;
    //headerView或者footerView 在可全屏滑动下，松手后滑动进入刷新/加载状态的时间
    private int mOutRangeScrollTime = 800;
    //headerView或者footerView 在刷新/加载结束后，隐藏的滑动时间
    public int hideHeadFootViewTime = 800;//ms
    //是否处于刷新状态
    private boolean isRefresh;
    //是否处于加载状态
    private boolean isLoadMore;
    //是否允许刷新
    private boolean isCanRefresh = true;
    //是否允许加载
    private boolean isCanLoadMore = true;
    //用于计算子View是否到底/到顶，轻松解决嵌套/组合 可滑动控件
    private Set<View> mChildCalcList;
    //覆盖mBaseView的错误提示view;
    private View mErrorView;
    private View mBaseView;
    private Runnable finishRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };



    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mChildCalcList = new HashSet<>();
        mScroller = new Scroller(getContext());
        mHeaderView = new ClassicsHeaderView(getContext());
        setHeadView(mHeaderView);
    }

    public void setRefreshListener(RefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    public void showErrorView(View view) {
        showErrorView(view, null);
    }

    /**
     * @param view     需要显示的view
     * @param baseView 被覆盖的view
     */
    public void showErrorView(View view, View baseView) {
        if (mErrorView == view) {
            mErrorView.setVisibility(VISIBLE);
        } else {
            if (mErrorView != null) {
                removeView(mErrorView);
            }
            mBaseView = baseView;
            mErrorView = view;
            LayoutParams layoutParams = new LayoutParams(-1, -1);
            mErrorView.setLayoutParams(layoutParams);
            addView(mErrorView);
        }
    }

    public void hideErrorView() {
        if (mErrorView != null) {
            mErrorView.setVisibility(GONE);
        }
    }

    public void setHeadView(HeaderView headerView) {
        View headView = getChildAt(0);
        if (mHeaderView != null && headView == mHeaderView.getView()) {
            removeView(headView);
        }
        mHeaderView = headerView;
        RefreshLayout.LayoutParams layoutParams = new LayoutParams(-1, -2);
        mHeaderView.getView().setLayoutParams(layoutParams);
        if (mHeaderView.getView().getParent() != null) {
            ((ViewGroup) mHeaderView.getView().getParent()).removeAllViews();
        }
        addView(mHeaderView.getView(), 0);
    }

    public void setFootView(FootView footView) {
        int index = getChildCount() - 1;
        View child = getChildAt(index);
        if (mFootView != null && child == mFootView.getView()) {
            removeView(child);
        }
        mFootView = footView;
        LayoutParams layoutParams = new LayoutParams(-1, -2);
        mFootView.getView().setLayoutParams(layoutParams);
        if (mFootView.getView().getParent() != null) {
            ((ViewGroup) this.mFootView.getView().getParent()).removeAllViews();
        }
        addView(mFootView.getView());
    }

    private boolean isChildTop() {
        Set<View> hashSet = mChildCalcList;
        Iterator<View> iterator = hashSet.iterator();
        while (iterator.hasNext()) {
            View child = iterator.next();
            if (ViewCompat.canScrollVertically(child, -1)) {
                return false;
            }
        }
        return true;
    }

    private boolean isChildBottom() {
        Set<View> hashSet = mChildCalcList;
        Iterator<View> iterator = hashSet.iterator();
        while (iterator.hasNext()) {
            View child = iterator.next();
            if (ViewCompat.canScrollVertically(child, 1)) {
                return false;
            }
        }
        return true;
    }

    private boolean isTouchInScrollChild(float rawX, float rawY){
        Set<View> hashSet = mChildCalcList;
        Iterator<View> iterator = hashSet.iterator();

        while (iterator.hasNext()) {
            View child = iterator.next();
            if (isTouchInView(child, rawX, rawY)){
                return true;
            }
        }
        return false;
    }

    private boolean isTouchInView(View view, float rawX, float rawY){
        Rect mChangeImageBackgroundRect = new Rect();
        view.getDrawingRect(mChangeImageBackgroundRect);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        mChangeImageBackgroundRect.left = location[0];
        mChangeImageBackgroundRect.top = location[1];
        mChangeImageBackgroundRect.right = mChangeImageBackgroundRect.right + location[0];
        mChangeImageBackgroundRect.bottom = mChangeImageBackgroundRect.bottom + location[1];
        return mChangeImageBackgroundRect.contains((int) rawX, (int) rawY);
    }

    public boolean isRefreshStatus() {
        return isRefresh;
    }

    public void setRefreshStatus(boolean status) {
        isRefresh = status;
        isLoadMore = !status;
    }

    public boolean isLoadMoreStatus() {
        return isLoadMore;
    }

    public void setLoadMoreStatus(boolean status) {
        isLoadMore = status;
        isRefresh = !status;
    }

    public void setMaxPullHeight(int height) {
        if (height > mHeadViewHeight) {
            maxPullHeight = height;
        }
    }

    public void setMaxPushHeight(int height) {
        if (height > mFootViewHeight) {
            maxPushHeight = height;
        }
    }

    public void addChildNeedCalc(View child) {
        mChildCalcList.add(child);
    }

    public void removeChildNeedCalc(View child) {
        mChildCalcList.remove(child);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (view instanceof AbsListView || view instanceof ScrollView) {
                mChildCalcList.add(view);
            }
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        scrollTo(0, mHeadViewHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mTotalWidth = 0;
        int mTotalHeight = 0;

        final int count = getChildCount();

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        boolean matchWidth = false;

        // See how tall everyone is. Also remember max width.
        for (int i = 0; i < count; ++i) {

            final View child = getChildAt(i);

            if (child.getVisibility() == View.GONE) {
                continue;
            }
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int usedHeight = mTotalHeight;
            measureChildBeforeLayout(child, i, widthMeasureSpec, 0,
                    heightMeasureSpec, 0);
            final int childHeight = child.getMeasuredHeight();
            if (mHeaderView != null && child == mHeaderView.getView()) {
                mHeadViewHeight = childHeight;
            } else if (mFootView != null && child == mFootView.getView()) {
                mFootViewHeight = childHeight;
            } else if (mErrorView == child) {
                //mErrorView覆盖在上面，不计算入高度
                continue;
            }
            final int totalLength = mTotalHeight;
            mTotalHeight = totalLength + childHeight + lp.topMargin +
                    lp.bottomMargin;

            final int margin = lp.leftMargin + lp.rightMargin;
            final int measuredWidth = child.getMeasuredWidth() + margin;
            mTotalWidth = Math.max(mTotalWidth, measuredWidth);

        }
        mTotalHeight += getPaddingTop() + getPaddingBottom();
        mTotalWidth += getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(mTotalWidth, mTotalHeight);
    }

    void measureChildBeforeLayout(View child, int childIndex,
                                  int widthMeasureSpec, int totalWidth, int heightMeasureSpec,
                                  int totalHeight) {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth,
                heightMeasureSpec, totalHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //super.onLayout(changed, l, t, r, b);
        //if(changed) {
        int count = getChildCount();
        int left = getPaddingLeft();
        int right = getPaddingRight();
        int top = getPaddingTop();
        int bottom = getPaddingBottom();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            left = getPaddingLeft() + layoutParams.leftMargin;
            right = getPaddingRight() + view.getMeasuredWidth() + layoutParams.rightMargin;
            top = top + view.getPaddingTop() + layoutParams.topMargin;
            bottom = top + view.getMeasuredHeight() + layoutParams.bottomMargin;
            view.layout(left, top, right, bottom);
            top = top + view.getMeasuredHeight() + view.getPaddingBottom();
            if (view == mErrorView) {
                continue;
            }
        }
        if (mErrorView != null) {
            Rect rect = new Rect();
            if (mBaseView == null) {
                rect.left = 0;
                rect.right = mErrorView.getMeasuredWidth();
                rect.top = mHeadViewHeight;
                rect.bottom = rect.top + mErrorView.getMeasuredHeight();
            } else {
                rect.left = mBaseView.getLeft();
                rect.right = rect.left + mErrorView.getMeasuredWidth();
                rect.top = mBaseView.getTop();
                rect.bottom = rect.top + mErrorView.getMeasuredHeight();
            }
            mErrorView.layout(rect.left, rect.top, rect.right, rect.bottom);
        }
        //}

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //mScroller.abortAnimation();
                mLastX = event.getX();
                mLastY = event.getY();
                mLastRawX = event.getRawX();
                mLastRawY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveY = event.getY() - mLastY;
                if ((!isTouchInScrollChild(mLastRawX, mLastRawY) || isChildTop()) && moveY > 0 && isCanRefresh()) {
                    mScroller.abortAnimation();
                    setRefreshStatus(true);
                    mHeaderView.begin();
                    if (mFootView != null) {
                        mFootView.reset();
                    }
                    return true;
                } else if (moveY < 0 && isCanLoadMore()) {
                    if ((!isTouchInScrollChild(mLastRawX, mLastRawY) || isChildBottom()) || getScrollY() < mHeadViewHeight) {
                        mScroller.abortAnimation();
                        setLoadMoreStatus(true);
                        mFootView.begin();
                        if (mHeaderView != null) {
                            mHeaderView.reset();
                        }
                        return true;
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.abortAnimation();
                restoreStatus();
                mLastX = event.getX();
                mLastY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float moveY = event.getY() - mLastY;
                float move = getScrollValue(moveY);
                if (isUnknownStatus()) {
                    //如果直接Touch在RefreshLayout则不会走onInterceptTouchEvent，
                    // 所以需要调用一下onInterceptTouchEvent去判断是下拉还是上拉
                    onInterceptTouchEvent(event);
                }

                if (isRefreshStatus()) {
                    float progress;
                    if (getScrollY() > 0) {
                        progress = (mHeadViewHeight - getScrollY()) / (float) mHeadViewHeight;
                    } else {
                        progress = (Math.abs(getScrollY()) + mHeadViewHeight) / (float) mHeadViewHeight;
                    }
                    mHeaderView.progress(progress);
                    scrollBy(0, (int) -move);
                } else if (isLoadMoreStatus()) {
                    float progress = (getScrollY() - mHeadViewHeight) / (float) mFootViewHeight;
                    if (mFootView != null) {
                        mFootView.progress(progress);
                    }
                    scrollBy(0, (int) -move);
                }

                mLastY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                if (isRefreshStatus()) {
                    if (mRefreshListener != null) {
                        if (getScrollY() > 0) {
                            mHeaderView.reset();
                            mScroller.startScroll(0, getScrollY(), 0, mHeadViewHeight - getScrollY(), mOutRangeScrollTime);
                            postInvalidate();
                        } else {
                            mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), mOutRangeScrollTime);
                            postInvalidate();
                            mHeaderView.loading();
                            mRefreshListener.refresh();
                        }
                    }
                } else if (isLoadMoreStatus()) {
                    if (mRefreshListener != null) {
                        if (getScrollY() >= mHeadViewHeight + mFootViewHeight) {
                            mFootView.loading();
                            mRefreshListener.loadMore();
                            mScroller.startScroll(0, getScrollY(), 0, mFootViewHeight + mHeadViewHeight - getScrollY(), mOutRangeScrollTime);
                            postInvalidate();
                        } else {
                            mScroller.startScroll(0, getScrollY(), 0, mHeadViewHeight - getScrollY(), mOutRangeScrollTime);
                            postInvalidate();

                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void scrollTo(int x, int y) {
        if (isRefreshStatus() && !isSupportFullPull()) {
            if (y > mHeadViewHeight) {
                y = mHeadViewHeight;
            }
        } else if (isLoadMoreStatus() && !isSupportFullPush()) {
            if (y > mHeadViewHeight + mFootViewHeight) {
                y = mHeadViewHeight + mFootViewHeight;
            }
        }
        super.scrollTo(x, y);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset() && !mScroller.isFinished()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }

    }

    public boolean isUnknownStatus() {
        if (isRefreshStatus()) {
            return false;
        } else if (isLoadMoreStatus()) {
            return false;
        } else {
            return true;
        }
    }

    public void restoreStatus() {
        isRefresh = false;
        isLoadMore = false;
    }

    public void finishRefresh(boolean success) {
        if (isUnknownStatus()) {
        } else {
            removeCallbacks(finishRunnable);
            mHeaderView.showPause(success);
            postDelayed(finishRunnable, mHeaderView.getPauseMillTime());
        }
    }

    public void finishLoadMore(boolean success) {
        if (isUnknownStatus()) {
        } else {
            removeCallbacks(finishRunnable);
            mFootView.showPause(success);
            postDelayed(finishRunnable, mFootView.getPauseMillTime());
        }
    }

    public int getOutRangeScrollTime() {
        return mOutRangeScrollTime;
    }

    /**
     * headerView或者footerView 在可全屏滑动下，松手后滑动进入刷新/加载状态的时间
     **/
    public void setOutRangeScrollTime(int millTime) {
        mOutRangeScrollTime = millTime;
    }

    public int getHideHeadFootViewMillTime() {
        return hideHeadFootViewTime;
    }

    /**
     * headerView或者footerView 在刷新/加载结束后，隐藏的滑动时间
     */
    public void setHideHeadFootViewMillTime(int millTime) {
        hideHeadFootViewTime = millTime;
    }

    public void finish() {
        mScroller.abortAnimation();
        mScroller.startScroll(
                0,
                getScrollY(),
                0,
                mHeadViewHeight - getScrollY(),
                hideHeadFootViewTime);
        postInvalidate();
    }

    /*允许拉出超过headerview的高度*/
    public boolean isSupportFullPull() {
        return isFullPull;
    }

    public void setFullPull(boolean isFullPull) {
        this.isFullPull = isFullPull;
    }

    /*允许拉出超过Footview的高度*/
    public boolean isSupportFullPush() {
        return isFullPush;
    }

    private void setFullPush(boolean isFullPush) {
        this.isFullPush = isFullPush;
    }

    public float getMoveRate() {
        return mMoveRate;
    }

    public void setMoveRate(float num) {
        mMoveRate = num;
    }

    private float getScrollValue(float moveY) {
        if (isRefreshStatus() && mHeaderView != null) {
            if (isSupportFullPull() || getScrollY() > 0 || getScrollY() > (mHeadViewHeight - maxPullHeight)) {
                return moveY * mMoveRate;
            } else {
                return 0;
            }
        } else {
            if (isSupportFullPush() || getScrollY() < (Math.max(mHeadViewHeight + mFootViewHeight, maxPushHeight))) {
                return moveY * mMoveRate;
            } else {
                return 0;
            }
        }
    }

    public void removeHeaderView() {
        if (mHeaderView != null) {
            removeView(mHeaderView.getView());
            mHeaderView = null;
        }
    }

    public void removeFootView() {
        if (mFootView != null) {
            removeView(mFootView.getView());
            mFootView = null;
        }
    }

    public boolean isCanRefresh() {
        return mHeaderView != null && isCanRefresh;
    }

    public void setCanRefresh(boolean canRefresh) {
        isCanRefresh = canRefresh;
    }

    public boolean isCanLoadMore() {
        return mFootView != null && isCanLoadMore;
    }

    public void setCanLoadMore(boolean canLoadMore) {
        isCanLoadMore = canLoadMore;
    }


    @Override
    public RefreshLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new RefreshLayout.LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(visibility == GONE) {
            restoreStatus();
            if (mHeaderView != null) {
                mHeaderView.reset();
            }

            if (mFootView != null) {
                mFootView.reset();
            }
        }
    }
    
}
