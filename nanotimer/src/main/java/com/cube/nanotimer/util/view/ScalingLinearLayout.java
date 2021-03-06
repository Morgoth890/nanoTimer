package com.cube.nanotimer.util.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cube.nanotimer.util.ScaleUtils;

public class ScalingLinearLayout extends LinearLayout {

  private Integer screenWidth;
  private Integer screenHeight;
  private int previousWidth;
  private int previousHeight;

  public ScalingLinearLayout(Context context) {
    super(context);
  }

  public ScalingLinearLayout(Context context, AttributeSet attributes) {
    super(context, attributes);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    refreshScreenScale();
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  private void refreshScreenScale() {
    if (screenWidth == null || screenHeight == null) {
      screenWidth = ScaleUtils.getScreenWidth(getContext());
      screenHeight = ScaleUtils.getScreenHeight(getContext());
    }

    if (screenWidth != previousWidth || screenHeight != previousHeight) {
      float xScale = ScaleUtils.getXScale(getContext());
      float yScale = ScaleUtils.getYScale(getContext());
      float scale = Math.min(xScale, yScale);
      scaleViewAndChildren(this, scale, 0);

      previousWidth = screenWidth;
      previousHeight = screenHeight;
    }
  }

  // Scale the given view, its contents, and all of its children by the given factor.
  private void scaleViewAndChildren(View root, float scale, int canary) {
    // Retrieve the view's layout information
    ViewGroup.LayoutParams layoutParams = root.getLayoutParams();

    // Scale the View itself
    if (layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT && layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
      layoutParams.width *= scale;
    }
    if (layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT && layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
      layoutParams.height *= scale;
    }

    // If the View has margins, scale those too
    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
      ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams)layoutParams;
      marginParams.leftMargin *= scale;
      marginParams.topMargin *= scale;
      marginParams.rightMargin *= scale;
      marginParams.bottomMargin *= scale;
    }
    root.setLayoutParams(layoutParams);

    // Same treatment for padding
    root.setPadding(
        (int)(root.getPaddingLeft() * scale),
        (int)(root.getPaddingTop() * scale),
        (int)(root.getPaddingRight() * scale),
        (int)(root.getPaddingBottom() * scale)
    );

    // If it's a TextView, scale the font size
    if (root instanceof TextView) {
      TextView tv = (TextView)root;
      tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize() * scale);
    }

    // If it's a ViewGroup, recurse!
    if (root instanceof ViewGroup) {
      ViewGroup vg = (ViewGroup)root;
      for (int i = 0; i < vg.getChildCount(); i++) {
        scaleViewAndChildren(vg.getChildAt(i), scale, canary + 1);
      }
    }
  }

}
