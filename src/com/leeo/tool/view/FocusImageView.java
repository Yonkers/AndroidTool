package com.leeo.tool.view;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Yonkers on 14-5-19.
 * 用于动画,动画过程中改变focus的实际大小
 */
public class FocusImageView extends ImageView {

    //用于动画
    private int width;
    //用于动画
    private int height;

    public FocusImageView(Context context) {
        super(context);
    }

    /**
     * 用于动画
     * @param width
     */
    public void setWidth(int width){
        if(this.width == width) return;
        this.width = width;
        getLayoutParams().width = width;
        requestLayout();
    }

    /**
     * 用于动画
     * @param height
     */
    public void setHeight(int height){
        if(this.height == height) return;
        this.height = height;
        getLayoutParams().height = height;
        requestLayout();
    }
}
