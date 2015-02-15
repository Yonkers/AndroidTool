/*
 * File:PullListView.java
 * Date:2014-1-9下午3:06:54
 *
 * 四川长虹网络科技有限责任公司 (智能应用研发部)© 版权所有 
 */
package com.leeo.tool.view;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;


/**
 * @author yonkers
 * @description pull down to refresh,and pull up to load more<br>
 * support {@link android.widget.ListView} and {@link android.widget.GridView}
 */
public class PullListView extends LinearLayout{
    
    private static String TAG = PullListView.class.getSimpleName();

    private static String HEADER_TEXT_TAG = "header_text";

    private static String FOOTER_TEXT_TAG = "footer_text";
    
    private static String PROGRESS_TAG = "loading_progress_tag";
    
    private int DEFAULT_HEADER_HEIGHT = 50;
    
    private int HEADER_FOOTER_INFO_SIZE = 18;
    
    private int DEFAULT_PROGRESS_BAR_SIZE = (int) (DEFAULT_HEADER_HEIGHT * 0.8);
    
    private boolean enablePullDown2Refresh = false;
    
    private boolean enablePullUp2LoadMore = true;
    
    private boolean enableRebound = true;

    private LinearLayout header;

    private LinearLayout footer;

    private AdapterView<?> adapterView;
    
    private ActionState pullState;
    
    private IPullStateInfo pullStateInfo;
    
    private OnPullListener pullListener ;
    
    private int headerHeight;
    private int footerHeight;
    
    private Scroller mScroller;
    
    private float density = 1.0f;
    
    private static int SCROLL_DURATION = 400;

    enum ActionState {
        PULL_DOWN,REFRESHING, REFRESH_FINISH, PULL_UP,LOADING_MORE, LOADING_MORE_FINISH
    }

    /**
     * @param context
     */
    public PullListView(Context context) {
        super(context);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public PullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
  public PullListView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      init();
  }
    
    private void init(){
        density = getResources().getDisplayMetrics().density;
        this.setOrientation(VERTICAL);
        mScroller = new Scroller(getContext());
    }

    public void setContentView(AdapterView<?> adapterView){
        initViews(adapterView);
    }
    
    private void initViews(AdapterView<?> adapterView) {
        initHeaderView();
        initContentView(adapterView);
        initFooterView();
    }
    
    /**
     * set callback to notify you to refresh or load
     * @param pullListener
     */
    public void setOnPullListener(OnPullListener pullListener) {
        this.pullListener = pullListener;
    }
    
    /**
     * @return the adapterView
     */
    public AdapterView<?> getContentView() {
        return adapterView;
    }

    private void initHeaderView() {
        if (header == null) {
            header = new LinearLayout(getContext());
            header.setOrientation(LinearLayout.HORIZONTAL);
//          header.setBackgroundColor(getResources().getColor(R.color.activity_default_bg));
            LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            headerHeight = header.getMeasuredHeight();
            if (headerHeight == 0) {
                headerHeight = (int) (density * DEFAULT_HEADER_HEIGHT);
            }
            params.height = headerHeight;
            Log.d(TAG, "headerHeight " + headerHeight);
            params.topMargin = -headerHeight;
            header.setGravity(Gravity.CENTER);
            addView(header,params);
            
            LayoutParams headerChildParams = new LayoutParams(
                    (int)density * DEFAULT_PROGRESS_BAR_SIZE,
                    (int)density * DEFAULT_PROGRESS_BAR_SIZE);
            ProgressBar progress = new ProgressBar(getContext());
            progress.setLayoutParams(headerChildParams);
            progress.setTag(PROGRESS_TAG);
            header.addView(progress);
            progress.setVisibility(GONE);
            
            TextView text = new TextView(getContext());
            text.setTextSize(HEADER_FOOTER_INFO_SIZE);
            text.setText(getPullStateInfo().onGetPrepareRefreshingText());
            text.setTag(HEADER_TEXT_TAG);
            headerChildParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            headerChildParams.leftMargin = 10;
            header.addView(text,headerChildParams);
            
        }
    }

    /**
     * @param adapterView
     */
    private void initContentView(AdapterView<?> adapterView) {
        this.adapterView = adapterView;
        if (this.adapterView == null) {
            this.adapterView = new GridView(getContext());
        }
        LayoutParams params = (LayoutParams) this.adapterView.getLayoutParams();
        if(params == null){
            params = new LayoutParams(
                    LayoutParams.MATCH_PARENT, 0);
        }
        params.weight = 10;
//      this.adapterView.setBackgroundColor(Color.GREEN);
        addView(this.adapterView,params);
    }

    private void initFooterView() {
        if (footer == null) {
            footer = new LinearLayout(getContext());
//          footer.setBackgroundColor(Color.GRAY);
            
            LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            footerHeight = headerHeight;
            
            params.height = footerHeight;
            Log.d(TAG, "footerHeight " + footerHeight);
            params.bottomMargin = -footerHeight;
            footer.setGravity(Gravity.CENTER);
            addView(footer,params);
            
            LayoutParams footerChildParams = new LayoutParams(
                    (int)density * DEFAULT_PROGRESS_BAR_SIZE,
                    (int)density * DEFAULT_PROGRESS_BAR_SIZE);
            ProgressBar progress = new ProgressBar(getContext());
            progress.setLayoutParams(footerChildParams);
            progress.setTag(PROGRESS_TAG);
            footer.addView(progress);
            progress.setVisibility(GONE);
            
            TextView text = new TextView(getContext());
            text.setTextSize(HEADER_FOOTER_INFO_SIZE);
            text.setText(getPullStateInfo().onGetPrepareLoadingText());
            text.setTag(FOOTER_TEXT_TAG);
            footerChildParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            footerChildParams.leftMargin = 10;
            footer.addView(text,footerChildParams);
        }
    }

    float mLastY = -1;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int y = (int) ev.getRawY();
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mLastY = y;
//          resetHeaderMargin();
            break;
        case MotionEvent.ACTION_MOVE:
            float deltaY = y - mLastY;
            // deltaY > 0 是向下运动,< 0是向上运动
            // get if the touch can be handle as pull down or pull up,if so
            // return true to let onTouchEvent handle this event
            
            if(canPullUpOrDown(deltaY)){
                return true;
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            break;
        }
        return false;
    }

    /**
     * if is on the bottom can pull up<br>
     * if is on the top can pull down<br>
     * @param delta
     * @return
     */
    private boolean canPullUpOrDown(float delta){
        if(null == adapterView){
            return false;
        }
        if(delta > 0){
            int clildCount = adapterView.getChildCount();
            View child = adapterView.getChildAt(0);//grid view is empty,or fist child is visible
            if(0 == clildCount || null == child || (adapterView.getFirstVisiblePosition() == 0 && child.getTop() >= 0)){
                pullState = ActionState.PULL_DOWN;
                if(enablePullDown2Refresh){
                    setHeaderVisible(true);
//                  return true;
                }else{
                    setHeaderVisible(false);
                    if(!enableRebound){//弹簧效果关闭时
                        return false;
                    }
                }
                return delta > 10 * density;
            }
            
            //handle when loading more is showing ,pull down to hide
//          View lastChild = adapterView.getChildAt(clildCount-1);
            // in the bottom
//          if(pullState == ActionState.LOADING_MORE){
//              if (lastChild.getBottom() <= getHeight()
//                      && adapterView.getLastVisiblePosition() == adapterView.getCount() - 1) {
//                     return true;
//              }
//          }
        }else if(delta < 0){
            int clildCount = adapterView.getChildCount();
            if (clildCount == 0) {
                // 如果mAdapterView中没有数据,不拦截
                return false;
            }
            View lastChild = adapterView.getChildAt(clildCount-1);
            // 最后一个子view的Bottom小于父View的高度说明mAdapterView的数据没有填满父view,
            // 等于父View的高度说明mAdapterView已经滑动到最后
            if (lastChild.getBottom() <= getHeight()
                    && adapterView.getLastVisiblePosition() == adapterView.getCount() - 1) {
                //the second condition for some devices such as samsung(screen are too sensitive)
                pullState = ActionState.PULL_UP;
                if(enablePullUp2LoadMore){ 
                    setFooterVisible(true);
//                  return true;
                }else{
                    setFooterVisible(false);
                    if(!enableRebound){//弹簧效果关闭时
                        return false;
                    }
                }
                return delta < - 10 * density;
            }
            
            //if is refreshing and pull up and fist child visible
//          if(pullState == ActionState.REFRESHING){
//              //load
//              View child = adapterView.getChildAt(0);//grid view is empty,or fist child is visible
//              if( null == child || (adapterView.getFirstVisiblePosition() == 0 && child.getTop() == 0)){
//                  return true;
//              }
//          }
        }
        
        return false;
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = (int) event.getRawY();
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            // 首先拦截down事件,记录y坐标
//          mLastY = y;
            break;
        case MotionEvent.ACTION_MOVE:
            // deltaY > 0 是向下运动,< 0是向上运动
            float deltaY = y - mLastY;
//          Log.d(TAG, "deltaY " + deltaY);

            // here we should know it's pull up or pull down
            if(pullState == ActionState.PULL_DOWN){
                setPullDownMargin(deltaY);
            }else if(pullState == ActionState.PULL_UP){
                setPullUpMargin(deltaY);
            }
            break;
        case MotionEvent.ACTION_UP:
            if(releaseCanHold()){
                if(pullState == ActionState.PULL_DOWN){
                    setHeaderPrepareRefreshMargin();
                    pullState = ActionState.REFRESHING;
                    if(null != pullListener){
                        pullListener.onRefresh();
                    }
                }else if(pullState == ActionState.PULL_UP){
                    setFooterPrepareLoadingMargin();
                    pullState = ActionState.LOADING_MORE;
                    if(null != pullListener){
                        pullListener.onLoadMore();
                    }
                }
            }else{
                resetHeaderMargin();
                
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            resetHeaderMargin();
            break;
            
        }
        return super.onTouchEvent(event);
    }
    
    /**
     * condition if can start load more data or refresh
     * @return
     */
    private boolean releaseCanHold(){
        if(enablePullDown2Refresh && pullState == ActionState.PULL_DOWN){
            if(mScroller.getCurrY() <= -headerHeight){
                return true;
            }
        }else if(enablePullUp2LoadMore && pullState == ActionState.PULL_UP){
            if(mScroller.getCurrY() >= headerHeight){
                return true;
            }
        }
        return false;
    }
    
    /**
     * pulling down ,change header margin<br>
     * or show animation;
     * 
     * @param delta
     */
    private void setPullDownMargin(float delta) {
        changeHeaderMargin(delta);
    }

    /**
     * pulling up ,change header margin ,so you actually see footer margin
     * changed<br>
     * 
     * @param delta
     */
    private void setPullUpMargin(float delta) {
        changeHeaderMargin(delta);
    }

    /**
     * change the header margin
     * 
     * @param delta
     */
    private void changeHeaderMargin(float delta) {
            smoothScrollTo(0, (int) (-delta * 0.3));
//          params.topMargin = (int) (-headerHeight + delta * 0.3);
//      }
    }

    /**
     * the header margin to the top of this view<br>
     * <0 if invisible ,>0 if visible
     * 
     * @return
     */
    private int getHeaderMargin() {
        LayoutParams lp = (LayoutParams) header
                .getLayoutParams();
        if (null != lp) {
            return lp.topMargin;
        }
        return headerHeight;
    }

    /**
     * call when finish refresh<br>
     * hide refreshing header
     */
    public void completeRefresh() {
        if(pullState != ActionState.REFRESH_FINISH){
            pullState = ActionState.REFRESH_FINISH;
            resetHeaderMargin();
        }
    }

    /**
     * call when finish load more<br>
     * hide footer
     */
    public void completeLoading() {
        if(null == pullState){
            return;
        }
        switch (pullState) {
            case REFRESHING:
                pullState = ActionState.REFRESH_FINISH;
                resetFooterMargin();
                break;
            case LOADING_MORE:
                pullState = ActionState.LOADING_MORE_FINISH;
                resetFooterMargin();
                adjustContentPositionAfterLoaded();
                break;
            default:
                break;
        }
    }

    /**
     * 完成触摸后，满足松开刷新的条件，设置header的显示状态为正在刷新 set text to "refreshing"
     */
    private void setHeaderPrepareRefreshMargin() {
        
//      LinearLayout.LayoutParams params = (LayoutParams) header
//              .getLayoutParams();
        updateHeaderText(getPullStateInfo().onGetRefreshingText());
        //show refreshing progress
        setHeaderProgressVisible(true);
        
        smoothScrollBy(0, (-mScroller.getFinalY()-headerHeight));
    }

    /**
     * 完成触摸后，满足松开刷新的条件，设置header的显示状态为正在刷新 set text to "loading"
     */
    private void setFooterPrepareLoadingMargin() {
        updateFooterText(getPullStateInfo().onGetLoadingMoreText());
        //show loading progress
        setFooterProgressVisible(true);
        smoothScrollBy(0, -(mScroller.getFinalY()-headerHeight));
    }

    /**
     * reset the header state<br>
     * 1.change text to "release to refresh"<br>
     * 2.set header margin to {@link headerHeight}
     */
    private void resetHeaderMargin() {
        if(pullState != ActionState.REFRESHING){
            updateHeaderText(getPullStateInfo().onGetPrepareRefreshingText());
            //hide refreshing progress
            setHeaderProgressVisible(false);
        }
        smoothScrollTo(0,0);
    }
    
    /**
     * set footer margin ,by change header margin to do this<br>
     * 1.change footer text to "release to load more"<br>
     * 2.set header margin to {@link headerHeight}
     */
    private void resetFooterMargin() {
        if(pullState != ActionState.LOADING_MORE){
            updateFooterText(getPullStateInfo().onGetPrepareLoadingText());
            //hide loading progress
            setFooterProgressVisible(false);
        }
        smoothScrollTo(0, 0);
    }

    /**
     * set the header text
     * 
     * @param text
     */
    private void updateHeaderText(String text) {
        TextView tv = (TextView) header.findViewWithTag(HEADER_TEXT_TAG);
        if (null != tv) {
            tv.setText(text);
        }
    }

    /**
     * set the footer text
     * 
     * @param text
     */
    public void updateFooterText(String text) {
        TextView tv = (TextView) footer.findViewWithTag(FOOTER_TEXT_TAG);
        if (null != tv) {
            tv.setText(text);
        }
    }
    
    private void setHeaderProgressVisible(boolean visible){
        ProgressBar progress = (ProgressBar) header.findViewWithTag(PROGRESS_TAG);
        if(null != progress){
            progress.setVisibility(visible ? VISIBLE:GONE);
        }
    }
    
    private void setFooterProgressVisible(boolean visible){
        ProgressBar progress = (ProgressBar) footer.findViewWithTag(PROGRESS_TAG);
        if(null != progress){
            progress.setVisibility(visible ? VISIBLE:GONE);
        }
    }
    
    /**
     * @return the pullStateInfo
     */
    public IPullStateInfo getPullStateInfo() {
        if(null == pullStateInfo){
            pullStateInfo = new IPullStateInfo(){
                String refreshing = null;
                String release2Refresh = null;
                String loading = null;
                String release2Load = null;
                @Override
                public String onGetRefreshingText() {
                    refreshing =  "Refreshing,please wait...";
//                    return "正在刷新，请稍候...";
//                  if(null == refreshing){
//                      refreshing = getResources().getString(R.string.pull_list_refreshing);
//                  }
                  return refreshing;
                }
                
                @Override
                public String onGetPrepareRefreshingText() {
                  refreshing =  "Release to refresh";
//                    return "释放手指刷新";
//                  if(null == release2Refresh){
//                      release2Refresh = getResources().getString(R.string.pull_list_release_to_refresh);
//                  }
                  return release2Refresh;
                }
                
                @Override
                public String onGetPrepareLoadingText() {
                    refreshing = "Release to load more";
//                    return "释放开始加载";
//                    if(null == release2Load){
//                      release2Load = getResources().getString(R.string.pull_list_release_to_load);
//                    }
                    return release2Load;
                }
                
                @Override
                public String onGetLoadingMoreText() {
                  refreshing = "Loading more,please wait...";
//                    return "正在加载，请稍候...";
//                    if(null == loading){
//                      loading = getResources().getString(R.string.pull_list_loading);
//                    }
                    return loading;
                }
            };
        }
        return pullStateInfo;
    }

    /**
     * call this after adapterView added new children<br>
     * to show parts of the views newly added
     */
    public void adjustContentPositionAfterLoaded(){
        if(adapterView instanceof AbsListView){
//            ((AbsListView)adapterView).smoothScrollBy(headerHeight * 2, 400);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((AbsListView)adapterView).smoothScrollBy(headerHeight, 400);
                }
            }, 100);
        }
    }
    
    /**
     * @param pullStateInfo the pullStateInfo to set
     */
    public void setPullStateInfo(IPullStateInfo pullStateInfo) {
        this.pullStateInfo = pullStateInfo;
    }

    /**
     * @return the enablePullDown2Refresh
     */
    public boolean isEnablePullDown2Refresh() {
        return enablePullDown2Refresh;
    }

    /**
     * set enable pull down or not<br>
     * default <b>not enabled<b>
     * @param enablePullDown2Refresh 
     */
    public void enablePullDown2Refresh(boolean enablePullDown2Refresh) {
        this.enablePullDown2Refresh = enablePullDown2Refresh;
    }

    /**
     * @return the enablePullUp2LoadMore
     */
    public boolean isEnablePullUp2LoadMore() {
        return enablePullUp2LoadMore;
    }

    /**
     * @return the enableRebound
     */
    public boolean isEnableRebound() {
        return enableRebound;
    }

    /**
     * 弹簧效果开关
     * @param enableRebound the enableRebound to set
     */
    public void enableRebound(boolean enableRebound) {
        this.enableRebound = enableRebound;
    }

    /**
     * set enable pull up or not<br>
     * default <b>enabled<b>
     * @param enablePullUp2LoadMore 
     */
    public void enablePullUp2LoadMore(boolean enablePullUp2LoadMore) {
        this.enablePullUp2LoadMore = enablePullUp2LoadMore;
    }

    private void setHeaderVisible(boolean visible){
        if(null != header){
            header.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }
    
    private void setFooterVisible(boolean visible){
        if(null != footer){
            footer.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }
    
    public void smoothScrollTo(int fx, int fy,int duration) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        if(duration > 0){
            smoothScrollBy(dx, dy,duration);
        }else{
            smoothScrollBy(dx, dy);
        }
    }
    
    //调用此方法滚动到目标位置
    public void smoothScrollTo(int fx, int fy) {
        smoothScrollTo(fx,fy,SCROLL_DURATION);
    }

    //调用此方法设置滚动的相对偏移
    public void smoothScrollBy(int dx, int dy) {
        smoothScrollBy(dx,dy,SCROLL_DURATION);
    }
    
    public void smoothScrollBy(int dx, int dy,int duration){
        if(duration > 0){
            //设置mScroller的滚动偏移量
            mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy,duration);
        }else{
            mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        }
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

    @Override
    public void computeScroll() {

        //先判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset()) {

            //这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

            //必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        super.computeScroll();
    }

    /**
     * callback when you should refresh or load more data
     * @author yonkers
     */
    public interface OnPullListener {
        void onRefresh();
        void onLoadMore();
    }
    
    /**
     * custom the text shown on loading or refresh state view
     * @author yonkers
     */
    interface IPullStateInfo{
        /**
         * 正在刷新的提示信息
         * @return
         */
        String onGetRefreshingText();
        
        /**
         * 初始状态，正在拉动过程中<br>
         * eg:release to refresh
         * @return
         */
        String onGetPrepareRefreshingText();
        
        /**
         * set loading info<br>
         * eg:loading... please wait
         * @return
         */
        String onGetLoadingMoreText();
        
        /**
         * initial state info for loading<br>
         * eg:release to load more
         * @return
         */
        String onGetPrepareLoadingText();
    }
}
