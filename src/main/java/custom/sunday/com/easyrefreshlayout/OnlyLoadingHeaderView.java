package custom.sunday.com.easyrefreshlayout;

import android.content.Context;
import android.support.annotation.IntDef;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by zhongfei.sun on 2018/1/24.
 */

public class OnlyLoadingHeaderView implements HeaderView {
    public static final int WHITE = 0;
    public static final int BLUE = 1;
    private Context mContext;
    private View mParent;
    private LoadingView mLoadingView;

    public OnlyLoadingHeaderView(Context context) {
        mContext = context;
    }

    @Override
    public void begin() {
        reset();
    }

    @Override
    public void progress(float progress) {

    }

    @Override
    public void loading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void reset() {
        mLoadingView.setVisibility(View.INVISIBLE);
    }

    @Override
    public View getView() {
        if (mParent == null) {
            mParent = LayoutInflater.from(mContext).inflate(R.layout.layout_head_loading_only, null, false);
            mLoadingView = (LoadingView) mParent.findViewById(R.id.loading);
        }
        return mParent;
    }

    public void setStyle(@LoadingColor int style) {
        getView();
        if (style == WHITE) {
            mLoadingView.setLoadingBitmap(R.mipmap.loading_white);
        } else {
            mLoadingView.setLoadingBitmap(R.mipmap.loading_blue);
        }
    }

    @Override
    public void showPause(boolean success) {

    }

    @Override
    public boolean isPauseTime() {
        return false;
    }

    @Override
    public long getPauseMillTime() {
        return 0;
    }

    @IntDef({WHITE, BLUE})
    @Retention(RetentionPolicy.SOURCE)
    @interface LoadingColor {
    }
}
