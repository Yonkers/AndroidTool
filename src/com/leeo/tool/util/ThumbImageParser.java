package com.leeo.tool.util;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

/**
 * Created by leeo on 12/19/14.
 */
public class ThumbImageParser {

    private static final String TAG = "ThumbImageParser";

    private Executor executor = Executors.newFixedThreadPool(2);

    private static ThumbImageParser thumbImageParser;

    private static String IMAGE_LOCAL_DEFAULT = "image_local_default";
    
    private HashMap<Object,ImageColor> colorCache = new HashMap<Object,ImageColor>();
    
    private Config config;

    private ThumbImageParser(Context context){
    	this.context = context;
    }

    private Context context;
    
    public static ThumbImageParser getInstance(Context context){
        if(null == thumbImageParser){
            thumbImageParser = new ThumbImageParser(context);
        }
        return thumbImageParser;
    }
    
    public void config(Config config){
    	this.config = config;
    }

    public void parseColorFromImage(final int drawableResId,final View view){
    	final WeakReference<View> targetView = new WeakReference<View>(view);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String newUrl = "res:" + Integer.valueOf(drawableResId);
                ImageColor imageColor = getColor(newUrl);
                if(null != imageColor){
                    try {
                        Log.d(TAG, "get color from cache " + imageColor);
                        Thread.sleep(300);//速度太快显示不出来
                        displayColor(targetView, imageColor);
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Bitmap bitmap = null;
                try {
                     bitmap = BitmapFactory.decodeResource(context.getResources(), drawableResId);
                    if(null != bitmap){
                        Bitmap thumbBitmap = resizeImage(bitmap,60,80);
                        imageColor = parseColorAdvanced(thumbBitmap);
                        cacheColor(newUrl,imageColor);
                        displayColor(targetView,imageColor);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    
                }
            }
        };
        if(null != executor) executor.execute(runnable);
    }
    
    /**
     * 异步设置背景
     * @param url
     * @param view
     */
    public void parseColorFromImage(final String url, final View view){
        final WeakReference<View> targetView = new WeakReference<View>(view);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String newUrl = url;
                if(TextUtils.isEmpty(newUrl)) newUrl = IMAGE_LOCAL_DEFAULT;
                ImageColor imageColor = getColor(newUrl);
                if(null != imageColor){
                    try {
                        Log.d(TAG, "get color from cache " + imageColor);
                        Thread.sleep(300);//速度太快显示不出来
                        displayColor(targetView, imageColor);
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                HttpURLConnection conn = null;
                Bitmap bitmap = null;
                try {
                    if(IMAGE_LOCAL_DEFAULT.equals(newUrl)){
                        bitmap = BitmapFactory.decodeResource(context.getResources(), config.defaultDrawableResource);
                    }else if(newUrl.startsWith("assets/")){
                    	bitmap = BitmapFactory.decodeStream(context.getAssets().open(newUrl));
                    }else{
                        URL uri = new URL(newUrl);
                        conn = (HttpURLConnection) uri.openConnection();
                        conn.setConnectTimeout(5000);// 设置连接超时
                        conn.setReadTimeout(3000);
                        if (conn.getResponseCode() == 200) {
                            InputStream inputStream =  new FlushedInputStream(conn.getInputStream());
                            bitmap = BitmapFactory.decodeStream(inputStream);
                            inputStream.close();
                        }
                    }
                    if(null != bitmap){
                        Bitmap thumbBitmap = resizeImage(bitmap,60,80);
                        //color = parseColor(thumbBitmap);
                        //cacheColor(url,color);
                        //displayColor(targetView,color);
                        imageColor = parseColorAdvanced(thumbBitmap);
                        cacheColor(newUrl,imageColor);
                        displayColor(targetView,imageColor);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(null != conn) {
                        conn.disconnect();
                    }
                }
            }
        };
        if(null != executor) executor.execute(runnable);
    }

    /**
     * 设置背景色
     * @param targetView
     * @param color
     */
    private void displayColor(WeakReference<View> targetView , final int color){
        final View view = targetView.get();
        Runnable displayRunnable = new Runnable() {
            @Override
            public void run() {
                if(null != view){
                    view.setBackgroundColor(color);
                }
            }
        };
        if(null != view){
            view.post(displayRunnable);
        }
    }
    
    ImageColor preColor;

    /**
     * 设置背景色
     * @param targetView
     * @param color
     */
    private void displayColor(WeakReference<? extends View> targetView , final ImageColor color){
        final View view = targetView.get();
        Runnable displayRunnable = new Runnable() {
            @Override
            public void run() {
                if(null != view && null != color){
                    GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                            new int[]{color.getColor1(),color.getColor2()});
                    
                    GradientDrawable preDrawable = null;
                    
                    if(null != preColor){
                    	preDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    			new int[]{preColor.getColor1(),preColor.getColor2()});
                    }else{
                    	preDrawable = drawable;
                    }
                    
                	TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{preDrawable, drawable});
                    //transitionDrawable.setCrossFadeEnabled(true);
                    if(Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN){
                         view.setBackgroundDrawable(transitionDrawable);
                    }else{
                    	view.setBackground(transitionDrawable);
                    }
                    transitionDrawable.startTransition(800);
                    preColor = color;
                    //view.setBackgroundDrawable(drawable);
                }
            }
        };
        if(null != view){
            view.post(displayRunnable);
        }
    }

    /**
     * 根据缩略图计算出现次数最多的像素值
     * @param thumbBitmap
     * @return
     */
    private int parseColor(Bitmap thumbBitmap){
        if(thumbBitmap != null){
            int[] pixels = new int[thumbBitmap.getWidth()*thumbBitmap.getHeight()];//保存所有的像素的数组，图片宽×高
            thumbBitmap.getPixels(pixels,0,thumbBitmap.getWidth(),0,0,thumbBitmap.getWidth(),thumbBitmap.getHeight());
            int maxColor = -1;
            int lastColor = -1;
            int maxCount = 0;
            for(int i = 0; i < pixels.length; i++){
                int clr = pixels[i];
                int curColorCount = countForColor(pixels,clr,i) + 1;
                if(maxCount < curColorCount){
                    maxCount = curColorCount;
                    lastColor = maxColor;
                    maxColor = clr;
                }
            }

            int  red   = (maxColor & 0x00ff0000) >> 16;  //取高两位
            int  green = (maxColor & 0x0000ff00) >> 8; //取中两位
            int  blue  =  maxColor & 0x000000ff; //取低两位
            //int V = Math.max(blue, Math.max(red, green));

            //float bright = (V / 255.f);
            //Logger.e("lll","bright: " + bright);

            maxColor = Color.argb(200,red,green,blue);
            return maxColor;
        }
        return 0;
    }

    /**
     * 从缩略图中解析主要颜色值
     * @param thumbBitmap
     * @return
     */
    private ImageColor parseColorAdvanced(Bitmap thumbBitmap){
        if(thumbBitmap != null){
            int width = thumbBitmap.getWidth();
            int height = thumbBitmap.getHeight();
            int[] pixels = new int[width * height];//保存所有的像素的数组，图片宽×高
            thumbBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            int rectColor = -1;

            int rectHeith = height / 10 * 2;
            int rectStart = height / 10 * 2 * width;
            int rectEnd = height / 10 * 4 * width;

            rectColor = getRectMainColor(pixels,rectStart,rectEnd);

            ImageColor imageColor = new ImageColor();
            imageColor.setColor1(rectColor);

            rectStart = height / 10 * 6 * width;
            rectEnd = height / 10 * 8 * width;
            rectColor = getRectMainColor(pixels,rectStart,rectEnd);
            //rectColor = mixWithDefaultColorIfNeed(rectColor);
            imageColor.setColor2(rectColor);

            Log.e(TAG,imageColor.toString());

            return imageColor;
        }
        return null;
    }

    /**
     * 从一块区域中获取主要颜色
     * @param pixels
     * @param start
     * @param end
     * @return
     */
    private int getRectMainColor(int[] pixels,int start,int end){
        if(null == pixels) return -1;
        int len = pixels.length;
        if(end >= len) end = len -1;
        int count = end - start;
        int sampleCount = 50;
        int del = count / sampleCount ;
        if(del == 0){
            del = 1;
            sampleCount = count;
        }
        int[] samples = new int[sampleCount];
        int n = 0;
        int rc= 0, gc= 0, bc = 0;
        int curColor;
        for(int i = start;i <= end; i+= del){
            if(n >= sampleCount) break;
            curColor = pixels[i];
            rc += (curColor & 0x00ff0000) >> 16;
            gc += (curColor & 0x0000ff00) >> 8;
            bc += curColor & 0x000000ff;
            samples[n] = pixels[i];
            n ++;
        }
        int color = Color.argb(200,rc / sampleCount,gc / sampleCount,bc/sampleCount);
        //Logger.e("lll","sample color " + printColor(color));
        return color;
    }

    /**
     * 如果颜色太白，则修改颜色
     * @param color
     * @return
     */
    private int mixWithDefaultColorIfNeed(int color){
        if(isSimilarWhiteColor(color)){
            int defaultColor = Color.parseColor("#2c4864");
            //int mixColor = (defaultColor + color) / 2;
            int red   = (int) (((defaultColor & 0x00ff0000) >> 16)  * 0.6 + ((color & 0x00ff0000) >> 16 ) *0.5);
            int green = (int)(((defaultColor & 0x0000ff00) >> 8) * 0.7 + ((color & 0x0000ff00) >> 8) * 0.3);
            int blue  =  (int)((defaultColor & 0x000000ff) * 0.7 + (color & 0x000000ff) * 0.3);
//            int  red   = (defaultColor & 0x00ff0000) >> 16;  //取高两位
//            int  green = (defaultColor & 0x0000ff00) >> 8; //取中两位
//            int  blue  =  defaultColor & 0x000000ff; //取低两位
            return Color.argb(200,red, green, blue);
        }
        return color;
    }

    /**
     * 打印颜色
     * @param color
     * @return
     */
    private String printColor(int color){
        int  red   = (color & 0x00ff0000) >> 16;  //取高两位
        int  green = (color & 0x0000ff00) >> 8; //取中两位
        int  blue  =  color & 0x000000ff; //取低两位
        return red + " " + green + " " + blue;
    }

    /**
     * 判断颜色是否类似白色
     * @param color
     * @return
     */
    private boolean isSimilarWhiteColor(int color){
        int  red   = (color & 0x00ff0000) >> 16;  //取高两位
        int  green = (color & 0x0000ff00) >> 8; //取中两位
        int  blue  =  color & 0x000000ff; //取低两位
        //int V = Math.max(blue, Math.max(red, green));
        return Math.abs(red-green) < 20 && Math.abs(green - blue) < 20 && (red >= 190 || green >= 190 || blue >=190);
        //return 255 *3 - (red + blue + green) < 80;
    }

    /**
     * 缓存已经计算出来的颜色值
     * @param url
     * @param color
     */
    private void cacheColor(Object url,ImageColor color){
        if(null != colorCache){
            colorCache.put(url, color);
        }
    }

    private ImageColor getColor(Object url){
        if(null != colorCache && colorCache.containsKey(url)){
            return colorCache.get(url);
        }
        return null;
    }

    /**
     * 计算指定颜色值在后面出现的次数
     * @param pixels
     * @param pixValue
     * @param start
     * @return
     */
    private int countForColor(int[] pixels,int pixValue,int start){
        int count = 0;
        if(null != pixels){
            for(int i = start;i < pixels.length ;i++){
                if(pixels[i] == pixValue){
                    count ++;
                }
            }
        }
        return count;
    }

    /**
     * 获取缩略图
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    private Bitmap resizeImage(Bitmap bitmap, int w, int h) {

        Bitmap BitmapOrg = bitmap;

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
    }

    /**
     * 销毁
     */
    public static void destroy(){
        if(null != thumbImageParser){
            if(null != thumbImageParser.colorCache){
                thumbImageParser.colorCache.clear();
                thumbImageParser.colorCache = null;
            }
            if(null != thumbImageParser.executor){
                thumbImageParser.executor = null;
            }
            thumbImageParser = null;
        }
    }
    
    public static class Config {
        int defaultDrawableResource;
    	
    	public Config config(){
    		return this;
    	}
    	
    	public Config defaultDrawableResource(int drawable){
    		defaultDrawableResource = drawable;
    		return this;
    	}
    	
    }

}
