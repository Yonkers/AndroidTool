/*
 * 项目名称：Ivideo
 * 
 * 创建时间：
 *
 * 创建人：
 * 
 *  。
*/
package com.leeo.tool.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;


/**
 * 滚动文本控件，默认手动调用滚动，如果需要自动滚动，即TextView的原生效果，调用setScrollMode(ScrollMode.AUTOMATIC);
 */
public class SmartMarqueeTextView extends FixedTextView{
    /**
     * 滚动模式，
     */
    public enum ScrollMode{
        AUTOMATIC,//列表中自动滚动
        MANUAL    //非列表控件中需要手动调用滚动和停止滚动
    }
    private ScrollMode scrollMode = ScrollMode.MANUAL;

    //滚动速度，每次移动的像素
    private float scrollSpeed = 1;

    /** 是否停止滚动 */
    private boolean isStop = true;
    private String mText = "";
    private float mCoordinateX;

    //文本的宽度
    private float mTextWidth;

    /**
     * 设置滚动模式
     * @param scrollMode
     */
    public void setScrollMode(ScrollMode scrollMode) {
        this.scrollMode = scrollMode;
    }

    public SmartMarqueeTextView(Context context) {
        super(context);
    }

    public SmartMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        if(scrollMode == ScrollMode.AUTOMATIC){
            return super.isFocused();
        }
        return false;
    }

    public void setText(String text) {
        if(null == text) text = "";
        this.mText = text;
        mTextWidth = getPaint().measureText(mText);
        if(scrollMode == ScrollMode.AUTOMATIC){
            super.setText(text);
        }
        invalidate();
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        mTextWidth = getPaint().measureText(mText);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (TextUtils.isEmpty(mText)) return;
        Paint p = getPaint();
        float size = p.getTextSize();
        float y = (getHeight()-size) / 2 + size;

        if(mTextWidth <= getWidth()){
            float x = (getWidth() - mTextWidth) / 2;
            p.setColor(getCurrentTextColor());
            canvas.drawText(mText, x, y, p);
        } else if(scrollMode == ScrollMode.MANUAL){
            p.setColor(getCurrentTextColor());
            canvas.drawText(mText, mCoordinateX, y, p);
        }else{
            super.onDraw(canvas);
        }
    }

    /**
     * 非列表控件需要滚动时调用
     */
    public void startScroll(){
        stopScroll();
        isStop = false;
        init();
        if(scrollMode == ScrollMode.MANUAL){
            if (!TextUtils.isEmpty(mText)){
                postDelayed(drawRunnable,1500);
            }
        }
    }

    /**
     * 非列表控件需要停止滚动时调用
     */
    public void stopScroll(){
        if(isStop) return;
        isStop = true;
        if(scrollMode == ScrollMode.MANUAL){
            if (null != drawRunnable)
                removeCallbacks(drawRunnable);
        }
        mCoordinateX = 0;
        invalidate();
    }

    private static class DrawRunnable implements Runnable {
        private final WeakReference<SmartMarqueeTextView> textViewWeakReference;

        DrawRunnable(SmartMarqueeTextView textView) {
            this.textViewWeakReference = new WeakReference<SmartMarqueeTextView>(textView);
        }

        @Override
        public void run() {
            SmartMarqueeTextView textView = textViewWeakReference.get();
            if(null == textView) return;
            if (textView.mCoordinateX < 0 && Math.abs(textView.mCoordinateX) > (textView.mTextWidth- textView.getWidth() / 4)) {
                textView.mCoordinateX =  textView.getWidth();
                textView.invalidate();
                if (!textView.isStop) {
                    if(null != textView.drawRunnable) textView.postDelayed(textView.drawRunnable,50);
                }
            } else {
                textView.mCoordinateX -= textView.scrollSpeed;
                textView.invalidate();
                if (!textView.isStop) {
                	if(null != textView.drawRunnable) textView.postDelayed(textView.drawRunnable, 10);
                }
            }
        }
    }

    private DrawRunnable drawRunnable;

//    private Handler mHandler;

    private void init(){
    	if(null != drawRunnable){
    		return;
    	}
        drawRunnable = new DrawRunnable(this);
    }
    
    public void release(){
    	if(null != drawRunnable){
    		removeCallbacks(drawRunnable);
            drawRunnable = null;
    	}
    }

    //保存view在可见状态改变之前的滚动状态
    boolean preStarted = false;
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(View.VISIBLE == visibility){
            if(preStarted){
                startScroll();
                preStarted = false;
            }else{
                stopScroll();
            }
        }else{
            if(!isStop){
                preStarted = true;
                stopScroll();
            }
        }
    }
}
