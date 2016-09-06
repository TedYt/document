package cn.yw.lib.animation;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * 属性动画，背景轮询切换
 * 为什么要使用SurfaceView而不是用View：
 *     1.这里稍作解释，由于SurfaceView继承了View，绘制起来和SurfaceView没有太大的区别，
 *  2.SurfaceView本身自带双缓冲技术，能够更好的支持动画操作
 * 
 * 
 * @author yw-tony
 * 
 */
@SuppressLint("NewApi")
public class BackgroundView extends SurfaceView implements
        SurfaceHolder.Callback, Runnable {
    
    private SurfaceHolder holder;
    private ShapeHolder shapHolder;

    public BackgroundView(Context context) {
        super(context);
        this.holder = this.getHolder();
        this.holder.addCallback(this);
    }

    /**
     * 创建一个小球
     */
    private void createABall(float x, float y) {
        OvalShape oval = new OvalShape();
        //设置拓原模型的宽高都为50f，即模型为原型
        oval.resize(50f, 50f);
        //创建一个模型drawable
        ShapeDrawable drawable = new ShapeDrawable(oval);
        shapHolder = new ShapeHolder(drawable);
        int red = (int) (Math.random() * 255);
        int green = (int) (Math.random() * 255);
        int blue = (int) (Math.random() * 255);
        int color = 0xff000000 | red << 16 | green << 8 | blue;
        Paint paint = drawable.getPaint(); // new Paint(Paint.ANTI_ALIAS_FLAG);
        int darkColor = 0xff000000 | red / 4 << 16 | green / 4 << 8 | blue / 4;
        RadialGradient gradient = new RadialGradient(37.5f, 12.5f, 50f, color,
                darkColor, Shader.TileMode.CLAMP);
        //设置画笔颜色
        paint.setShader(gradient);
        shapHolder.setPaint(paint);
        //设置小球的初始位置
        shapHolder.setX(x);
        shapHolder.setY(y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN
                && event.getAction() != MotionEvent.ACTION_MOVE) {
            return false;
        }
        //创建一个小球
        createABall(event.getX(), event.getY());
        //设置动画的Y轴活动范围
        float startY = shapHolder.getY();
        float endY = getHeight() - 50f;
        // int duration = (int)(500 * ((h - eventY)/h));
        // 小球弹跳动画的时间为500毫秒
        int duration = 500;
        ValueAnimator bounceAnim = ObjectAnimator.ofFloat(shapHolder, "y",
                startY, endY);
        bounceAnim.setDuration(duration);
        // 加速器，小球会加速下落
        bounceAnim.setInterpolator(new AccelerateInterpolator());
        // 以下几个是挤压动画
        
        ValueAnimator squashAnim1 = ObjectAnimator.ofFloat(shapHolder, "x",
                ////设置x周的动画范围
                shapHolder.getX(), shapHolder.getX() - 25f);
        //设置压缩动画时间为下落动画时间的四分之一
        squashAnim1.setDuration(duration / 4);
        squashAnim1.setRepeatCount(1);
        squashAnim1.setRepeatMode(ValueAnimator.REVERSE);
        //挤压是做减速运动
        squashAnim1.setInterpolator(new DecelerateInterpolator());
        ValueAnimator squashAnim2 = ObjectAnimator.ofFloat(shapHolder, "width",
                //设置小球宽度动画
                shapHolder.getWidth(), shapHolder.getWidth() + 50);
        squashAnim2.setDuration(duration / 4);
        squashAnim2.setRepeatCount(1);
        squashAnim2.setRepeatMode(ValueAnimator.REVERSE);
        //小球做减速运动
        squashAnim2.setInterpolator(new DecelerateInterpolator());
        //设置伸展动画
        ValueAnimator stretchAnim1 = ObjectAnimator.ofFloat(shapHolder, "y",
                endY, endY + 25f);
        stretchAnim1.setDuration(duration / 4);
        stretchAnim1.setRepeatCount(1);
        
        stretchAnim1.setInterpolator(new DecelerateInterpolator());
        stretchAnim1.setRepeatMode(ValueAnimator.REVERSE);
        ValueAnimator stretchAnim2 = ObjectAnimator.ofFloat(shapHolder,
                "height", shapHolder.getHeight(), shapHolder.getHeight() - 25);
        stretchAnim2.setDuration(duration / 4);
        stretchAnim2.setRepeatCount(1);
        stretchAnim2.setInterpolator(new DecelerateInterpolator());
        stretchAnim2.setRepeatMode(ValueAnimator.REVERSE);

        ValueAnimator bounceBackAnim = ObjectAnimator.ofFloat(shapHolder, "y",
                endY, startY);
        bounceBackAnim.setDuration(duration);
        // 减速器
        bounceBackAnim.setInterpolator(new DecelerateInterpolator());
        
        //设置动画对象的顺序
        AnimatorSet bouncer = new AnimatorSet();
        //先加速下落然后再执行挤压动画1
        bouncer.play(bounceAnim).before(squashAnim1);
        //播放挤压动画1的同事播放挤压动画2
        bouncer.play(squashAnim1).with(squashAnim2);
        bouncer.play(squashAnim1).with(stretchAnim1);
        bouncer.play(squashAnim1).with(stretchAnim2);
        //执行完挤压动画后执行小球弹起动画
        bouncer.play(bounceBackAnim).after(stretchAnim2);
        //开始执行动画
        bouncer.start();
        return true;
    }

    private void drawBall() {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.GRAY);
                canvas.save();
                //如果小球为空则不执行绘制动作
                if (shapHolder != null) {
                    canvas.translate(shapHolder.getX(), shapHolder.getY());
                    shapHolder.getShape().draw(canvas);
                }
                canvas.restore();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //解锁画布
                if (holder != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //开启绘制线程
        new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {
        try {
            //此处为死循环，大家在写的时候可以加上一个boolean变量值，当用户点击回退键（back）时，结束线程
            while (true) {
                drawBall();
                Thread.sleep(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}