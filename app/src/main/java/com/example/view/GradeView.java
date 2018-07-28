package com.example.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

import com.example.gradeview.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/7/24.
 */

public class GradeView extends View {
    private String TAG = GradeView.class.getSimpleName();


    //等级
    private int[] grade = {0, 2000, 3500, 4500, 5000};
    //    private String[] gradeName = {"普通", "白银", "黄金", "铂金", "钻石"};
    private String[] gradeName;
    private int[] bgColor = {R.color.color_fdebd4, R.color.color_eeeeee, R.color.color_ffd59e, R.color.color_eeeeee, R.color.color_8d00d6};
    private int[] textColor = {R.color.color_656565, R.color.color_656565, R.color.color_656565, R.color.color_8d00d6, R.color.color_ffffff};

    private Context context;

    public GradeView(Context context) {
        super(context);
        init(context);
    }

    public GradeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GradeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GradeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private int leftPadding = 0;
    private int rightPadding;
    private int bubbleRadian = dip2px(3);//气泡的圆弧大小
    private int bubbleLeftAndRightPadding = dip2px(6);//气泡的左边及右边padding
    private int bubbleTopAndBottomPadding = dip2px(3);//气泡的上部及底部padding

    //width = 0, height = 0,
    private float width = 0, height = 0, center = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        if (gradeName == null || gradeName.length == 0) {
            return;
        }
        //---------------------------------绘制等级及进度线-----------------------------------------
        if (lineWidth == 0) {
            getWidthAndHeight();//计算出宽高
        }
        float totalWidth = 0;
        for (int i = 0; i < gradeName.length; i++) {
            width = widths[i];
            height = heights[i];
            totalWidth += i == 0 ? 0 : widths[i - 1];
            paint.setColor(getResources().getColor(bgColor[i]));  //设置画笔颜色
            paint.setStyle(Paint.Style.FILL);//设置填充样式
            float left = lineWidth * i + totalWidth + leftPadding;
            rectF.set(left, 0, left + width, height);
            canvas.drawRoundRect(rectF, height / 2, height / 2, paint);//绘制等级的背景
            textPaint.setColor(getResources().getColor(textColor[i]));
            baseline = (rectF.bottom + rectF.top - textPaint.getFontMetricsInt().bottom - textPaint.getFontMetricsInt().top) / 2;
            canvas.drawText(gradeName[i], left + textLeftPadding, baseline, textPaint);//绘制等级的文字
            if (i != gradeName.length - 1) {//绘制进度线
                float top = height / 2 - lineHeight / 2;
                left = left + width;
                float right = left;
                float bottom = height / 2 + lineHeight / 2;
                if (currentGrade < grade[i + 1] && currentGrade > grade[i]) {
                    int gradeLength = grade[i + 1] - grade[i];
                    float percentage = (float) (currentGrade - grade[i]) / (float) gradeLength;
//                    center = lineWidth * percentage + i * (lineWidth + width) + width;
                    paint.setColor(context.getResources().getColor(R.color.color_f4bb70));
                    rectF.set(left, top, lineWidth * percentage + right, bottom);
                    canvas.drawRect(rectF, paint);
                    paint.setColor(context.getResources().getColor(R.color.color_dfdcdc));
                    rectF.set(left + lineWidth * percentage, top, lineWidth + right, bottom);
                    canvas.drawRect(rectF, paint);
                    continue;
                } else if (currentGrade > grade[i]) {
                    paint.setColor(context.getResources().getColor(R.color.color_f4bb70));
//                    center = (i + 1) * (lineWidth + width) + width / 2;
                } else {
                    paint.setColor(context.getResources().getColor(R.color.color_dfdcdc));

                }
                rectF.set(left, top, lineWidth + right, bottom);
                canvas.drawRect(rectF, paint);
            }
        }
        //------------------------------------------------------------------------------------------
        //--------------------------------------绘制小三角形----------------------------------------
        for (float v : heights) {//拿到最大的高度
            if (v > height) {
                height = v;
            }
        }
        center = center == 0 ? widths[0] / 2 : center;
        //小三角的宽及高
        int triangleWidth = dip2px(10);
        int triangleHeight = dip2px(10);
        path.reset();//充值
        paint.setColor(getResources().getColor(R.color.color_fdebd4));
        path.moveTo(center + leftPadding, height + dip2px(5));
        path.lineTo(center - triangleWidth / 2 + leftPadding, height + dip2px(5) + triangleHeight);
        path.lineTo(center + triangleWidth / 2 + leftPadding, height + dip2px(5) + triangleHeight);
        path.close();
        canvas.drawPath(path, paint);
        //------------------------------------------------------------------------------------------
        //----------------------------------绘制提示的气泡------------------------------------------
        //获取文字所占高度
        int strHeight = textPaint.getFontMetricsInt().bottom - textPaint.getFontMetricsInt().top;//获取不换行时文字的高度
        float strWidth = textPaint.measureText(gradeTip);//获取不换行时文字的长长度
        String[] strings = null;
        if (gradeTip.contains("\n")) {//带有\n则换行
            strings = gradeTip.split("\n");
            strHeight = strHeight * strings.length;//获取多行的高度
            strWidth = strWidth / (strings.length == 0 ? 1 : strings.length);//获取每行的宽度
        }
        rectF.set(center - (strWidth + bubbleLeftAndRightPadding * 2) / 2 + leftPadding, height + triangleHeight + dip2px(5)/*三角形距离上面有5dp间距*/, center + (strWidth + bubbleLeftAndRightPadding * 2) / 2 + leftPadding, strHeight + height + bubbleTopAndBottomPadding * 2 + dip2px(5) * 2/*三角形距离上面有5dp间距*/);
        paint.setColor(getResources().getColor(R.color.color_fdebd4));  //设置画笔颜色
        canvas.drawRoundRect(rectF, bubbleRadian, bubbleRadian, paint);
        textPaint.setColor(getResources().getColor(textColor[0]));
        textPaint.setTextSize(sp2px(textSize));
        baseline = (rectF.bottom + rectF.top - textPaint.getFontMetricsInt().bottom - textPaint.getFontMetricsInt().top) / 2;
        float maxLength = strWidth;
        if (strings != null && strings.length > 0) {
            maxLength = 0;
            for (String string : strings) {//获取每行的长度
                strWidth = textPaint.measureText(string);
                if (strWidth > maxLength) {
                    maxLength = strWidth;
                }
            }
        }
        point.set((int) (rectF.left + rectF.right) / 2, (int) (rectF.bottom + rectF.top) / 2);
        textCenter(getSpannableString(gradeTip), textPaint, canvas, point, (int) maxLength, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1.0f, true);
        //------------------------------------------------------------------------------------------
    }

    private float[] widths;
    private float[] heights;

    private void getWidthAndHeight() {
        float width, height;
        for (int i = 0; i < gradeName.length; i++) {
            textPaint.setTextSize(sp2px(textSize));
            textPaint.setTextAlign(Paint.Align.LEFT);
            //获取文字所占高度
            int strHeight = textPaint.getFontMetricsInt().bottom - textPaint.getFontMetricsInt().top;
            float strWidth = textPaint.measureText(gradeName[i]);
            width = strWidth + textLeftPadding + textRightPadding;
            height = strHeight + textBottomPadding + textTopPadding;
            widths[i] = width;
            heights[i] = height;
        }
        if (lineWidth == 0) {
            width = 0;
            for (float v : widths) {
                width += v;
            }
            lineWidth = (getWidth() - width - leftPadding - rightPadding) / (gradeName.length - 1);
        }
    }

    //查找到所有数字并且对数字颜色进行变色处理
    private CharSequence getSpannableString(String string) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(string);
        SpannableString spannableString = new SpannableString(string);
        while (m.find()) {
            String number = m.group();
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_f4bb70)), string.indexOf(number), string.indexOf(number) + number.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }


    private void textCenter(CharSequence source, TextPaint textPaint, Canvas canvas, Point point, int width, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        StaticLayout staticLayout;
        if (Build.VERSION.SDK_INT >= 23) {//此方法可以减少StaticLayout对象的创建
            staticLayout = StaticLayout.Builder.obtain(source, 0, source.length(), textPaint, width).setAlignment(align).setLineSpacing(1.0f, 1.0f).setIncludePad(true).build();
        } else {
            staticLayout = new StaticLayout(source, textPaint, width, align, spacingmult, spacingadd, includepad);
        }
        canvas.save();
        canvas.translate(-staticLayout.getWidth() / 2 + point.x, -staticLayout.getHeight() / 2 + point.y);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    private int animDuration = 1000;


    private AnimatorSet animatorSet;

    private void anim() {
        if (animatorSet == null || !animatorSet.isRunning()) {
            animatorSet = new AnimatorSet();
            ValueAnimator valueAnimator1 = ValueAnimator.ofInt(0, currentGrade);
            ValueAnimator valueAnimator2 = ValueAnimator.ofFloat(widths[0] / 2, center < widths[0] / 2 ? widths[0] / 2 : center);
            valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    currentGrade = (int) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    center = (float) animation.getAnimatedValue();
                }
            });
            animatorSet.setDuration(animDuration);
            animatorSet.setInterpolator(new DecelerateInterpolator());//设置插值器（在动画开始的地方快然后慢）
            animatorSet.playTogether(valueAnimator1, valueAnimator2);
            animatorSet.start();
        }
    }

    //停止动画
    public void stopAnim() {
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.end();
        }
    }

    private int currentGrade = 0;
    private float lineWidth = 0;

    private float lineHeight;
    private float textLeftPadding = 0;
    private float textRightPadding = 0;
    private float textTopPadding = 0;
    private float textBottomPadding = 0;
    private Paint paint;

    private Point point = new Point();

    private float baseline;

    private int textSize = 12;//字体大小,默认为12，单位为sp

    private RectF rectF;

    private String gradeTip = "";

    private TextPaint textPaint;

    private Path path;

    private void init(Context context) {
        this.context = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        leftPadding = dip2px(30);
        rightPadding = dip2px(30);
        lineHeight = dip2px(2);
        textLeftPadding = dip2pxf(5);
        textRightPadding = dip2pxf(5);
        textTopPadding = dip2pxf(2);
        textBottomPadding = dip2pxf(2);
        path = new Path();
        rectF = new RectF();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {//监听当前界面是否加载完成
            @Override
            public void onGlobalLayout() {
                isFinish = true;
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private boolean isFinish = false;


    //设置当前进度
    public void setCurrentGrade(int currentGrade) {
        this.currentGrade = currentGrade;
        if (!isFinish) {//如果没有加载完成则等待加载完成
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    isFinish = true;
                    startLoading();
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        } else {
            startLoading();
        }
    }

    private void startLoading() {
        if (lineWidth == 0) {
            getWidthAndHeight();//计算出宽高
        }
        float totalWidth = 0;
        for (int i = 0; i < gradeName.length; i++) {
            width = widths[i];
            totalWidth += i == 0 ? 0 : widths[i - 1];
            if (i != gradeName.length - 1) {//绘制进度线
                if (currentGrade < grade[i + 1] && currentGrade > grade[i]) {
                    int gradeLength = grade[i + 1] - grade[i];
                    float percentage = (float) (currentGrade - grade[i]) / (float) gradeLength;
                    center = lineWidth * percentage + i * lineWidth + totalWidth + width;
                } else if (currentGrade > grade[i]) {
                    center = (i + 1) * lineWidth + totalWidth + width + width / 2;
                }
            }
        }
        anim();
    }

    //设置字体大小（单位为sp）
    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    //气泡的圆弧大小（单位为dp）
    public void setBubbleRadian(int bubbleRadian) {
        this.bubbleRadian = dip2px(bubbleRadian);
    }

    //气泡的左边及右边padding（单位为dp）
    public void setBubbleLeftAndRightPadding(int bubbleLeftAndRightPadding) {
        this.bubbleLeftAndRightPadding = dip2px(bubbleLeftAndRightPadding);
    }

    //气泡的上边及下边padding（单位为dp）
    public void setBubbleTopAndBottomPadding(int bubbleTopAndBottomPadding) {
        this.bubbleTopAndBottomPadding = dip2px(bubbleTopAndBottomPadding);
    }


    public void setGradeNameAndGradeAndTip(String[] gradeName, int[] ints, int next) {
        this.gradeName = new String[gradeName.length];
        System.arraycopy(gradeName, 0, this.gradeName, 0, gradeName.length);
        grade = new int[ints.length];
        System.arraycopy(ints, 0, grade, 0, ints.length);
        widths = new float[gradeName.length];
        heights = new float[gradeName.length];
        if(next!=-1) {
            gradeTip = String.format(getResources().getString(R.string.next_grade_tip), next);
        }
        postInvalidate();
    }

    public void setGradeNameAndGradeAndTip(String[] gradeName, int[] ints) {
        setGradeNameAndGradeAndTip(gradeName, ints, -1);
    }

    /**
     * sp转换px
     */
    public int sp2px(int sp) {
        final float scale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }

    /**
     * px转换sp
     */

    public int px2sp(int px) {
        final float scale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (px / scale + 0.5f);
    }

    /**
     * dip转换px
     */
    public int dip2px(int dip) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    public float dip2pxf(int dip) {
        final float scale = getResources().getDisplayMetrics().density;
        return (dip * scale + 0.5f);
    }

    /**
     * dip转换px
     */
    public int dip2px(float dip) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /**
     * px转换dip
     */

    public int px2dip(int px) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public float px2dipf(int px) {
        final float scale = getResources().getDisplayMetrics().density;
        return (px / scale + 0.5f);
    }
}
