# easyrefreshlayout
经过完整测试无bug的下拉刷新组件


优点：
- 采用scroll方式改变scroll值，而不是更改上下Headview的高度，不会重新布局，效率更高，速度更快
- 支持多顶部计算，提供支持接口，是否滑动自己决定
- 简单的HeadView继承，实现自己的HeadView和FootView非常方便

## 导入方式
第一步：
工程根目录中build.gradle中仓库地址增加maven jitpack
```
allprojects {
    repositories {
        jcenter()
        repositories {
            maven { url 'https://www.jitpack.io' }
        }
    }
}
```
第二步：
模块build.gradle中引入
```
dependencies {
    compile 'com.github.xindasunday:easyrefreshlayout:1.0.2'
}
```
## 使用方法
请注意：默认没有FootView，如果需要加载更多，需要在代码中增加
```java
mRefreshLayout.setFootView(new ClassicsFootView(getBaseContext()));
```
### 基本用法：

```java
    private RefreshLayout mRefreshLayout;
        mRefreshLayout = (RefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setRefreshListener(new RefreshListener() {
            @Override
            public void refresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.finishRefresh(true);
                    }
                },2000);
            }

            @Override
            public void loadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.finishLoadMore(true);
                    }
                },2000);
            }
        });

```
### 更多设置
```java
        //设置新的HeadView
        mRefreshLayout.setHeadView(new RotateHeaderView(getBaseContext()));
        //android默认滑动速度1f-值越大越快，默认0.3f
        mRefreshLayout.setMoveRate(1f);
        //用于计算子View是否到底/到顶，轻松解决嵌套/组合 可滑动控件
        mRefreshLayout.addChildNeedCalc(view);
        //headerView或者footerView 在刷新/加载结束后，隐藏的滑动时间 时间单位为毫秒
        mRefreshLayout.setHideHeadFootViewMillTime(800);
        //是否可以拉动超出HeaderView的高度
        mRefreshLayout.setFullPull(false);
        //是否可以拉动超出FooterView的高度
        mRefreshLayout.setFullPush(false);
        //headerView或者footerView 在可全屏滑动下，松手后滑动进入刷新/加载状态的时间
        mRefreshLayout.setOutRangeScrollTime(800);
        //是否允许刷新
        mRefreshLayout.setCanRefresh(true);
        //是否允许加载更多
        mRefreshLayout.setCanLoadMore(true);
```
### 效果图
![效果图](http://img.blog.csdn.net/20180126145556196?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvVnhpYW9jYWk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
