/*
 * File:Focus.java
 * Date:2014-4-15下午1:06:56
 *
 *  
 */
package com.leeo.tool.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import com.leeo.tool.R;
import com.leeo.tool.util.Utility;

/**
 * @author yonkers
 * @description 焦点控制类
 */
public class Focus {

	private static String TAG = "Focus";

	// 焦点
	private FocusImageView focusImageView;

	/** 焦点位置 */
	@SuppressWarnings("unused")
	private Rect rect;

	/** 焦点框厚度 */
	private int FOCUS_RED_WIDTH = 9;
	private int FOCUS_WHITE_WIDTH = 3;

	public Focus(ViewGroup root, Context context, Rect rect) {
		// FOCUS_RED_WIDTH = Utility.getPX((int)
		// context.getResources().getDimension(R.dimen.item_padding));
		this.rect = rect;
		focusImageView = new FocusImageView(context);
		//focusImageView.setBackgroundResource(R.drawable.gfocus); //set a focus image

		focusImageView.setPivotX(0);
		focusImageView.setPivotY(0);

		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(rect.width() + 2 * FOCUS_RED_WIDTH, rect.height()
				+ 2 * FOCUS_RED_WIDTH);
		if (root != null && rect != null) {
			root.addView(focusImageView, params);
			focusImageView.setX(rect.left - FOCUS_RED_WIDTH);
			focusImageView.setY(rect.top - FOCUS_RED_WIDTH);
		}
	}

	/**
	 * 动画形式移动焦点到目标位置
	 * 
	 * @param rect
	 * @param flash
	 *            移动完成后是否需要闪烁
	 */
	public void animTo(final Rect rect, boolean flash) {
		if (null == rect)
			return;

		ObjectAnimator lx = ObjectAnimator.ofFloat(focusImageView, "x", rect.left - FOCUS_RED_WIDTH);
		ObjectAnimator ly = ObjectAnimator.ofFloat(focusImageView, "y", rect.top - FOCUS_RED_WIDTH);
		ObjectAnimator lw = ObjectAnimator.ofInt(focusImageView, "width", rect.width() + 2 * FOCUS_RED_WIDTH);
		ObjectAnimator lh = ObjectAnimator.ofInt(focusImageView, "height", rect.height() + 2 * FOCUS_RED_WIDTH);

		focusImageView.setVisibility(View.VISIBLE);
		focusImageView.clearAnimation();
		focusImageView.setAlpha(1.0f);
		AnimatorSet animSetXY = new AnimatorSet();
		if (flash) {
			AnimatorListener listener = new CustomAnimatorListener() {
				@Override
				public void onAnimationEnd(Animator animation) {
					startFlashAnim();
				}
			};
			animSetXY.addListener(listener);
		}
		animSetXY.setDuration(200);
		animSetXY.setInterpolator(new AccelerateInterpolator());
		animSetXY.playTogether(lx, ly, lw, lh);
		animSetXY.start();

	}

	/**
	 * 闪烁动画
	 */
	private void startFlashAnim() {
		focusImageView.setVisibility(View.VISIBLE);
		ObjectAnimator show = ObjectAnimator.ofFloat(focusImageView, "alpha", 0.3f, 1.0f);
		ObjectAnimator gone = ObjectAnimator.ofFloat(focusImageView, "alpha", 1.0f, 0.3f);
		AnimatorSet animshow = new AnimatorSet();
		gone.setDuration(300);
		show.setDuration(400);
		animshow.play(gone).before(show);
		animshow.start();
	}

	/**
	 * 直接设置焦点到目标位置，无动画
	 * 
	 * @param rect
	 */
	public void resetFocusTo(Rect rect) {
		ViewGroup.LayoutParams params = focusImageView.getLayoutParams();
		if (null != params) {
			params.height = rect.height() + 2 * FOCUS_RED_WIDTH;
			params.width = rect.width() + 2 * FOCUS_RED_WIDTH;
		}
		focusImageView.setVisibility(View.VISIBLE);
		focusImageView.setX(rect.left - FOCUS_RED_WIDTH);
		focusImageView.setY(rect.top - FOCUS_RED_WIDTH);
		focusImageView.setAlpha(1.0f);
		focusImageView.requestLayout();
	}

	/**
	 * 隐藏焦点
	 */
	public void hideFocus(boolean anim) {
		focusImageView.clearAnimation();
		if (anim) {
			ObjectAnimator gone = ObjectAnimator.ofFloat(focusImageView, "alpha", 1.0f, 0.0f);
			AnimatorListener listener = new CustomAnimatorListener() {
				@Override
				public void onAnimationEnd(Animator animation) {
					focusImageView.setVisibility(View.INVISIBLE);
				}
			};
			gone.addListener(listener);
			gone.setDuration(350).start();
		} else {
			focusImageView.setVisibility(View.INVISIBLE);
			focusImageView.setAlpha(0.0f);
			focusImageView.requestLayout();
		}
	}

	/**
	 * 渐变形式显示动画
	 * 
	 * @param anim
	 */
	public void showFocus(View target, boolean anim) {
		showFocus(Utility.viewToRect(target), anim);
	}

	/**
	 * 渐变形式显示动画
	 * 
	 * @param rect
	 * @param anim
	 */
	public void showFocus(Rect rect, boolean anim) {
		if (null == focusImageView)
			return;
		focusImageView.setVisibility(View.VISIBLE);
		if (!anim) {
			resetFocusTo(rect);
			return;
		}
		focusImageView.setAlpha(0.0f);
		if (null != rect) {
			ViewGroup.LayoutParams params = focusImageView.getLayoutParams();
			if (null != params) {
				params.height = rect.height() + 2 * FOCUS_RED_WIDTH;
				params.width = rect.width() + 2 * FOCUS_RED_WIDTH;
			}
			focusImageView.setX(rect.left - FOCUS_RED_WIDTH);
			focusImageView.setY(rect.top - FOCUS_RED_WIDTH);
			focusImageView.requestLayout();
		}
		focusImageView.clearAnimation();
		ObjectAnimator show = ObjectAnimator.ofFloat(focusImageView, "alpha", 0.0f, 8.0f);
		ObjectAnimator gone = ObjectAnimator.ofFloat(focusImageView, "alpha", 8.0f, 0.3f);
		ObjectAnimator show2 = ObjectAnimator.ofFloat(focusImageView, "alpha", 0.3f, 1.0f);
		AnimatorSet animshow = new AnimatorSet();
		gone.setDuration(200);
		show.setDuration(200);
		show2.setDuration(300);
		animshow.play(show).before(gone).before(show2);
		animshow.start();
	}

	class CustomAnimatorListener implements AnimatorListener {

		@Override
		public void onAnimationStart(Animator animation) {

		}

		@Override
		public void onAnimationEnd(Animator animation) {

		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	}
}
