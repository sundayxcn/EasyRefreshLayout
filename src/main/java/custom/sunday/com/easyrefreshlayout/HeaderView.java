package custom.sunday.com.easyrefreshlayout;

import android.view.View;

/**
 * Created by zhongfei.sun on 2018/1/19.
 */

public interface HeaderView {
    void begin();

    /**
     * 相对于HeaderView高度的倍数
     * **/
    void progress(float progress);

    void loading();

    //初始化和结束后调用
    void reset();

    View getView();

    //刷新成功后暂停几秒用于显示动画效果后
    void showPause(boolean success);

    boolean isPauseTime();

    long getPauseMillTime();
}
