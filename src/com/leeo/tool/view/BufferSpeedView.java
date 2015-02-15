package com.leeo.tool.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.TrafficStats;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by leeo on 14-6-27.
 */
public class BufferSpeedView extends View implements Runnable{

    private Paint paint;

    /**
     * 圆环的颜色
     */
    private int roundColor;

    /**
     * 圆环进度的颜色
     */
    private int roundProgressColor;

    /**
     * 中间进度百分比的字符串的颜色
     */
    private int textColor;

    /**
     * 中间进度百分比的字符串的字体
     */
    private float textSize;

    /**
     * 圆环的宽度
     */
    private float roundWidth;

    public BufferSpeedView(Context context) {
        super(context);
        init(context,null);
    }

    public BufferSpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public BufferSpeedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs){
        paint = new Paint();
        paint.setAntiAlias(true);
        roundColor = Color.DKGRAY;
        roundProgressColor = Color.BLUE;
        textColor = roundProgressColor;
        textSize = 22;
        roundWidth = 7;
    }

    private long drawDelay = 50;
    private String currentSpeed = "0KB/S";

    private int startAngle = 0;

    //旋转速度
    private int loadingSpeed = 5;

    private int drawTimes = 0;

    @Override
    public void run() {
        if(loading){
            if(showSpeed && drawTimes == 0){
                long bytes = getByte();
                long speed  = bytes - lastBytes;
                if(lastBytes > 0){
                    //set text speed
                    if (speed >= 1024) {
                        String speedStr = speed / 1024.0f + "";
                        currentSpeed = speedStr.substring(0, speedStr.indexOf(".")) + speedStr.substring(speedStr.indexOf("."), speedStr.indexOf(".") + 2) + "MB/S";
                    } else{
                        currentSpeed = speed + "KB/S";
                    }
                }
                lastBytes = bytes;
            }
            invalidate();
            startAngle += loadingSpeed;
            if(startAngle == 360){
                startAngle = 0;
            }
            drawTimes ++;
            if(drawTimes > 500 / drawDelay){
                drawTimes = 0;
            }

        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if(width > 0 && height > 0){

            int centerX = width / 2; //获取圆心的x坐标
            int centerY = height / 2;
            int maxRadius = (int) (centerX - roundWidth / 2); //圆环的半径
            if(width > 100){
                maxRadius = (int) (100 / 2 - roundWidth / 2);
            }
            RectF oval = new RectF(centerX - maxRadius, centerY - maxRadius, centerX
                    + maxRadius, centerY + maxRadius);  //用于定义的圆弧的形状和大小的界限

//            LinearGradient gradient = new LinearGradient(0, 0, 50, 50, Color.argb(255,127,255,212), Color.argb(50, 127, 255, 212), Shader.TileMode.MIRROR);
//            paint.setShader(gradient);

            paint.setStrokeWidth(roundWidth);

            //绘制透明部分
            paint.setARGB(255, 240,242,247);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawArc(oval, 0, 360, false, paint);
//            canvas.drawArc(oval, 90+ startAngle, 120, false, paint);
//            canvas.drawArc(oval, 270+ startAngle, 90, false, paint);

            //绘制不透明部分
            paint.setColor(roundProgressColor);
//            canvas.drawArc(oval, 180+ startAngle, 90, false, paint);
//            canvas.drawArc(oval, 0+ startAngle, 90, false, paint);
            canvas.drawArc(oval, 180+ startAngle, 120, false, paint);

            if(showSpeed){
                paint.setStrokeWidth(0);
                paint.setTextSize(textSize);
                paint.setColor(textColor);
                Typeface font = Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD);
                paint.setTypeface(font);
                float fixedTextSize = fixTextSize(currentSpeed,getWidth() - 2 * roundWidth - 4,paint);
                float textWidth = paint.measureText(currentSpeed);
                float x = centerX - textWidth/2;
                float y = centerY +  fixedTextSize / 2 - 4;
                canvas.drawText(currentSpeed,x,y,paint);
            }
        }
        if(loading){
            postDelayed(this,drawDelay);
        }
    }

    private float fixTextSize(String text,float maxWidth,Paint paint){
        float size = paint.getTextSize();
        float textWidth = paint.measureText(text);

        while (textWidth > maxWidth){
            size --;
            paint.setTextSize(size);
            textWidth = paint.measureText(text);
        }
        return size;
    }

    //显示进度
    private boolean loading = true;

    //显示网速
    private boolean showSpeed = true;

    private long lastBytes = -1;


    /**
     * 获取当前网络总流量
     *
     * @return
     */
    private long getByte() {
        return TrafficStats.getTotalRxBytes() / 1024;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(w < 70){
            roundWidth = 4;
        }
    }

    /**
     * @param visibility
     */
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(VISIBLE == visibility){
            loading = true;
            showSpeed = true;
            postDelayed(this,1000);
        }else{
            loading = false;
            resetBuffer();
        }
    }

    private void resetBuffer(){
        lastBytes = -1;
        drawTimes = 0;
        currentSpeed = "0KB/S";
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(VISIBLE == visibility){
            loading = true;
            showSpeed = true;
            postDelayed(this,1000);
        }else{
            loading = false;
            resetBuffer();
        }
    }
}
