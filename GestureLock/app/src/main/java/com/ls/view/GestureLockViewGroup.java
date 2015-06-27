package com.ls.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.ls.gesturelock.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 整体包含n*n个GestureLockView,每个GestureLockView间间隔mMarginBetweenLockView，
 * 最外层的GestureLockView与容器存在mMarginBetweenLockView的外边距
 * 关于GestureLockView的边长（n*n）： n * mGestureLockViewWidth + ( n + 1 ) *mMarginBetweenLockView = mWidth ;
 * 得：mGestureLockViewWidth = 4 * mWidth / ( 5 * mCount + 1 )
 * 注：mMarginBetweenLockView = mGestureLockViewWidth * 0.25 ;
 * Created by ls on 15-6-27.
 */
public class GestureLockViewGroup extends RelativeLayout {

    private int mNoFingerInnerCircleColor = 0xFF939090;
    private int mNoFingerOuterCircleColor = 0xFFE0DBDB;
    private int mFingerOnColor = 0xFF378FC9;
    private int mFingerUpColor = 0xFFFF0000;
    private int mCount;
    private int mTryTimes;

    private Paint mPaint;
    private Path mPath;

    private int mWidth;
    private int mHeight;

    private GestureLockView[] mGestureLockViews;
    private int mGestureLockViewWidth;
    private int mMarginBetweenLockView;

    private List<Integer> mChoose = new ArrayList<>();

    private OnGestureLockViewListener onGestureLockViewListener;

    private int mLastPathX;
    private int mLastPathY;
    private Point mTmpTarget = new Point();


    private int[] mAnswer;


    public GestureLockViewGroup(Context context) {
        this(context, null);
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取自定义参数
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GestureLockViewGroup, defStyleAttr, 0);
        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int index = typedArray.getIndex(i);
            switch (index) {
                case R.styleable.GestureLockViewGroup_color_no_finger_inner_circle://未被触摸状态内圓颜色
                    mNoFingerInnerCircleColor = typedArray.getColor(index, mNoFingerInnerCircleColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_no_finger_outer_circle://未被触摸状态外圆颜色
                    mNoFingerOuterCircleColor = typedArray.getColor(index, mNoFingerOuterCircleColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_finger_on://触摸状态下颜色
                    mFingerOnColor = typedArray.getColor(index, mFingerOnColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_finger_up://呈现结果颜色
                    mFingerUpColor = typedArray.getColor(index, mFingerUpColor);
                    break;
                case R.styleable.GestureLockViewGroup_count://个数
                    mCount = typedArray.getInt(index, 3);
                    break;
                case R.styleable.GestureLockViewGroup_tryTimes://尝试次数
                    mTryTimes = typedArray.getInt(index, 5);
                    break;
            }
        }
        typedArray.recycle();
        //初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//起始点
        mPaint.setStrokeJoin(Paint.Join.ROUND);//连接点
        mPath = new Path();
    }

    /**
     * 1.根据父类传入参数获取宽高
     * 2.初始化每个子View的宽度,以及笔触宽度
     * 3.初始化新建所有子View,设定ID,设置布局参数,设置模式,添加View
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mHeight = mWidth = mWidth < mHeight ? mWidth : mHeight;
        mGestureLockViewWidth = (int) (4 * mWidth * 1.0f / (5 * mCount + 1));
        mMarginBetweenLockView = (int) (mGestureLockViewWidth * 0.25);
        mPaint.setStrokeWidth(mGestureLockViewWidth * 0.29f);

        if (mGestureLockViews == null) {
            mGestureLockViews = new GestureLockView[mCount * mCount];
            for (int i = 0; i < mGestureLockViews.length; i++) {
                mGestureLockViews[i] = new GestureLockView(this.getContext(), mNoFingerInnerCircleColor, mNoFingerOuterCircleColor, mFingerOnColor, mFingerUpColor);
                mGestureLockViews[i].setId(i + 1);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mGestureLockViewWidth, mGestureLockViewWidth);
                if (i % mCount != 0) {//不是第一列的元素都排放在上个元素的右边
                    layoutParams.addRule(RIGHT_OF, mGestureLockViews[i - 1].getId());
                }
                if (i > mCount - 1) {//从第二行开始,设置为上一个对应元素的下面
                    layoutParams.addRule(BELOW, mGestureLockViews[i - mCount].getId());
                }
                int rightMargin = mMarginBetweenLockView;//其他view都存在右下间隔参数
                int bottomMargin = mMarginBetweenLockView;
                int topMargin = 0;
                int leftMargin = 0;

                if (i >= 0 && i < mCount) {//第一行,存在topMargin
                    topMargin = mMarginBetweenLockView;
                }
                if (i % mCount == 0) {//第一列存在leftMargin
                    leftMargin = mMarginBetweenLockView;
                }
                layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
                mGestureLockViews[i].setCurrentMode(GestureLockView.Mode.STATUS_NO_FINGER);
                addView(mGestureLockViews[i], layoutParams);
            }
        }
    }

    /**
     * 根据手势设置状态:
     * 1.ACTION_DOWN:直接重置所有子view状态,以及画笔参数等
     * 2.ACTION_MOVE:(1)设置画笔参数.(2)根据手指位置获取子View.
     * (3)如果该子View为被选中,则添加到选中记录,设置回调,更新其状态
     * (4)获取每个子View的指引点坐标,即中心点位置.更新Path参数
     * (5)记录此时手指坐标
     * 3.ACTION_UP:(1)设置画笔参数.(2)次数递减,设置回调
     * (3)将此时手指坐标回退到上次子View指引点位置,改变每个子View状态
     * (4)计算每个指示方向元素需要旋转的角度
     * 4.所有的手势操作都触发重绘操作
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                reset();
                break;
            case MotionEvent.ACTION_MOVE:
                mPaint.setColor(mFingerOnColor);
                mPaint.setAlpha(50);
                GestureLockView gestureLockView = getChildViewByPos(x, y);//获取View
                if (gestureLockView != null) {
                    int cId = gestureLockView.getId();
                    if (!mChoose.contains(cId)) {
                        mChoose.add(cId);//添加到选中
                        if (onGestureLockViewListener != null)
                            onGestureLockViewListener.onBlockSelected(cId);
                        gestureLockView.setCurrentMode(GestureLockView.Mode.STATUS_FINGER_ON);//更新状态
                        //设置指引点坐标
                        mLastPathX = gestureLockView.getLeft() / 2 + gestureLockView.getRight() / 2;
                        mLastPathY = gestureLockView.getTop() / 2 + gestureLockView.getBottom() / 2;
                        if (mChoose.size() == 1)//第一个点
                            mPath.moveTo(mLastPathX, mLastPathY);
                        else
                            mPath.lineTo(mLastPathX, mLastPathY);
                    }
                }
                //设置指引线终点
                mTmpTarget.x = x;
                mTmpTarget.y = y;
                break;
            case MotionEvent.ACTION_UP:
                mPaint.setColor(mFingerUpColor);
                mPaint.setAlpha(50);
                this.mTryTimes--;//尝试次数递减
                if (onGestureLockViewListener != null && mChoose.size() > 1) {
                    onGestureLockViewListener.onGestureEvent(checkAnswer());
                    if (mTryTimes == 0)//超过尝试次数
                        onGestureLockViewListener.onUnmatchedExceedBoundary();
                }
                //将终点设置为最近一次起点,取消指引线
                mTmpTarget.x = mLastPathX;
                mTmpTarget.y = mLastPathY;
                changeItemMode();
                //计算每个元素需要旋转的角度
                for (int i = 0; i+1 < mChoose.size(); i++) {
                    int childId = mChoose.get(i);
                    int nextChildId = mChoose.get(i + 1);
                    GestureLockView startChild = (GestureLockView) findViewById(childId);
                    GestureLockView nextChild = (GestureLockView) findViewById(nextChildId);
                    int dx = nextChild.getLeft() - startChild.getLeft();//x差值
                    int dy = nextChild.getTop() - startChild.getTop();//y差值
                    //设置角度
                    int angle = (int) (Math.toDegrees(Math.atan2(dy, dx)) + 90);
                    startChild.setArrowDegree(angle);
                }
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 改变已经划过的View状态
     */
    private void changeItemMode() {
        for (GestureLockView gestureLockView : mGestureLockViews) {
            if (mChoose.contains(gestureLockView.getId())) {
                gestureLockView.setCurrentMode(GestureLockView.Mode.STATUS_FINGER_UP);
            }
        }
    }

    /**
     * 检查与预期目标是否一致
     */
    private boolean checkAnswer() {
        if (mAnswer.length != mChoose.size()) {
            return false;
        }
        for (int i = 0; i < mAnswer.length; i++) {
            if (mAnswer[i] != mChoose.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据坐标获取View
     */
    private GestureLockView getChildViewByPos(int x, int y) {
        for (GestureLockView gestureLockView : mGestureLockViews) {
            if (checkPositionInChild(gestureLockView, x, y)) {
                return gestureLockView;
            }
        }
        return null;
    }

    /**
     * 判断坐标是否在该View内部
     */
    private boolean checkPositionInChild(GestureLockView gestureLockView, int x, int y) {
        int padding = (int) (mGestureLockViewWidth * 0.15);
        if (x > gestureLockView.getLeft() + padding && x < gestureLockView.getRight() + padding &&
                y > gestureLockView.getTop() + padding && y < gestureLockView.getBottom() + padding) {
            return true;
        }
        return false;
    }

    /**
     * 重置所有状态
     */
    private void reset() {
        mChoose.clear();
        mPath.reset();
        for (GestureLockView gestureLockView : mGestureLockViews) {
            gestureLockView.setCurrentMode(GestureLockView.Mode.STATUS_NO_FINGER);
            gestureLockView.setArrowDegree(-1);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mPath != null) {//绘制连线
            canvas.drawPath(mPath, mPaint);
        }
        if (mChoose.size() > 0) {//绘制随手势跟随直线
            if (mLastPathX != 0 && mLastPathY != 0)
                canvas.drawLine(mLastPathX, mLastPathY, mTmpTarget.x, mTmpTarget.y, mPaint);
        }
    }

    /**
     * 设置最大尝试次数
     */
    public void setUnMatchExceedBoundary(int boundary) {
        this.mTryTimes = boundary;
    }


    public OnGestureLockViewListener getOnGestureLockViewListener() {
        return onGestureLockViewListener;
    }

    public void setOnGestureLockViewListener(OnGestureLockViewListener onGestureLockViewListener) {
        this.onGestureLockViewListener = onGestureLockViewListener;
    }


    public int[] getmAnswer() {
        return mAnswer;
    }

    /**
     * 设置手势匹配目标
     */
    public void setmAnswer(int[] mAnswer) {
        this.mAnswer = mAnswer;
    }

    public interface OnGestureLockViewListener {
        //当前手指选中的View
        void onBlockSelected(int cId);

        //结果是否匹配
        void onGestureEvent(boolean matched);

        //超过尝试次数
        void onUnmatchedExceedBoundary();
    }
}
