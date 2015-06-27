package com.ls.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * Created by ls on 15-6-19.
 * 圆形图形:
 * 1.构造函数获取用户设定三种状态颜色
 * 2.获取该View宽度,初始化圆心位置,半径,三角形箭头
 * 3.根据三种模式绘制对应圆形状态
 */
public class GestureLockView extends View {
    //传入颜色
    private int mColorNoFingerInner;
    private int mColorNoFingerOutter;
    private int mColorFingerOn;
    private int mColorFingerUp;
    //圆心坐标
    private Paint mPaint;
    private int mCenterX;
    private int mCenterY;
    //三角箭头（小三角最长边的一半长度 = mArrawRate * mWidth / 2 ）
    private float mArrowRate = 0.333f;

    private int mArrowDegree = -1;
    private Path mArrowPath;

    private int mRadius;//外圆半径
    private int mWidth;//宽度
    private int mHeight;//高度
    private int mStrokeWidth = 2;//画笔宽度

    private Mode mCurrentMode = Mode.STATUS_NO_FINGER;

    //内圓半径 mRadius*mInnerCircleRadiusRate
    private float mInnerCircleRadiusRate = 0.3f;


    public int getmArrowDegree() {
        return mArrowDegree;
    }

    public void setArrowDegree(int mArrowDegree) {
        this.mArrowDegree = mArrowDegree;
    }


    public Mode getmCurrentMode() {
        return mCurrentMode;
    }

    public void setCurrentMode(Mode mCurrentMode) {
        this.mCurrentMode = mCurrentMode;
        invalidate();
    }


    public GestureLockView(Context context, int colorNoFingerInner, int colorNoFingerOutter, int colorFingerOn, int colorFingerUp) {
        super(context);
        this.mColorNoFingerInner = colorNoFingerInner;
        this.mColorNoFingerOutter = colorNoFingerOutter;
        this.mColorFingerOn = colorFingerOn;
        this.mColorFingerUp = colorFingerUp;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mWidth = mWidth < mHeight ? mWidth : mHeight;
        mRadius = mCenterX = mCenterY = mWidth / 2;//圆形位置,半径
        mRadius -= mStrokeWidth / 2;//去除笔触宽度

        // 绘制三角形，初始时是个默认箭头朝上的一个等腰三角形，用户绘制结束后，根据由两个GestureLockView决定需要旋转多少度
        float mArrowLength = mWidth / 2 * mArrowRate;
        mArrowPath.moveTo(mWidth / 2, mStrokeWidth + 2);//顶点
        mArrowPath.lineTo(mWidth / 2 - mArrowLength, mStrokeWidth + 2 + mArrowLength);//左顶点
        mArrowPath.lineTo(mWidth / 2 + mArrowLength, mStrokeWidth + 2 + mArrowLength);//右顶点
        mArrowPath.close();
        mArrowPath.setFillType(Path.FillType.WINDING);
    }

    /**
     * Paint.Style.FILL    : 填充内部
     * Paint.Style.FILL_AND_STROKE  ： 填充内部和描边
     * Paint.Style.STROKE  ： 仅描边
     */
    @Override
    protected void onDraw(Canvas canvas) {
        switch (mCurrentMode) {
            case STATUS_NO_FINGER://原始状态下
                //绘制外圆
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(mColorNoFingerOutter);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                //绘制内圓
                mPaint.setColor(mColorNoFingerInner);
                canvas.drawCircle(mCenterX, mCenterY, mInnerCircleRadiusRate * mRadius, mPaint);
                break;
            case STATUS_FINGER_ON://选中状态
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(2);
                mPaint.setColor(mColorFingerOn);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY, mInnerCircleRadiusRate * mRadius, mPaint);
                break;
            case STATUS_FINGER_UP://滑动后抬起
                mPaint.setColor(mColorFingerUp);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY, mRadius * mInnerCircleRadiusRate, mPaint);
                drawArrow(canvas);
                break;

        }


    }

    /**
     * 绘制三角箭头
     */
    private void drawArrow(Canvas canvas) {
        if (mArrowDegree != -1) {
            mPaint.setStyle(Paint.Style.FILL);
            canvas.save();//暂时保存已绘制内容到栈
            canvas.rotate(mArrowDegree, mCenterX, mCenterY);//旋转画布,不影响save之前的内容
            canvas.drawPath(mArrowPath, mPaint);
            canvas.restore();//将两次绘制内容同时显示在以save之前为主的画布上
        }
    }


    /**
     * GestureLockView的三种状态
     */
    enum Mode {
        STATUS_NO_FINGER, STATUS_FINGER_ON, STATUS_FINGER_UP;
    }
}
