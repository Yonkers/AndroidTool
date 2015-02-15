/*
 * File:FixedTextView.java
 * Date:2014-4-30下午4:11:49
 *
 *  
 */
package com.leeo.tool.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;


/**
 * @author yonkers
 * @description 重写了setTextSize(),外部不用考虑不同分辨率了，只考虑720p
 */
public class FixedTextView extends TextView {

	/**
	 * @param context
	 * @param attrs
	 */
	public FixedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public FixedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context,attrs);
	}

	/**
	 * @param context
	 */
	public FixedTextView(Context context) {
		super(context);
		init(context,null);
	}

	private void init(Context context,AttributeSet attrs){
		float textSize = getTextSize();
		super.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
	}
	
	/**
	 * 文本字体大小，单位：像素
	 */
	@Override
	public void setTextSize(float size) {
		super.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);
	}

}
