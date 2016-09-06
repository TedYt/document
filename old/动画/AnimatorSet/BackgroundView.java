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
 * ���Զ�����������ѯ�л�
 * ΪʲôҪʹ��SurfaceView��������View��
 *     1.�����������ͣ�����SurfaceView�̳���View������������SurfaceViewû��̫�������
 *  2.SurfaceView�����Դ�˫���弼�����ܹ����õ�֧�ֶ�������
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
     * ����һ��С��
     */
    private void createABall(float x, float y) {
        OvalShape oval = new OvalShape();
        //������ԭģ�͵Ŀ�߶�Ϊ50f����ģ��Ϊԭ��
        oval.resize(50f, 50f);
        //����һ��ģ��drawable
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
        //���û�����ɫ
        paint.setShader(gradient);
        shapHolder.setPaint(paint);
        //����С��ĳ�ʼλ��
        shapHolder.setX(x);
        shapHolder.setY(y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN
                && event.getAction() != MotionEvent.ACTION_MOVE) {
            return false;
        }
        //����һ��С��
        createABall(event.getX(), event.getY());
        //���ö�����Y����Χ
        float startY = shapHolder.getY();
        float endY = getHeight() - 50f;
        // int duration = (int)(500 * ((h - eventY)/h));
        // С����������ʱ��Ϊ500����
        int duration = 500;
        ValueAnimator bounceAnim = ObjectAnimator.ofFloat(shapHolder, "y",
                startY, endY);
        bounceAnim.setDuration(duration);
        // ��������С����������
        bounceAnim.setInterpolator(new AccelerateInterpolator());
        // ���¼����Ǽ�ѹ����
        
        ValueAnimator squashAnim1 = ObjectAnimator.ofFloat(shapHolder, "x",
                ////����x�ܵĶ�����Χ
                shapHolder.getX(), shapHolder.getX() - 25f);
        //����ѹ������ʱ��Ϊ���䶯��ʱ����ķ�֮һ
        squashAnim1.setDuration(duration / 4);
        squashAnim1.setRepeatCount(1);
        squashAnim1.setRepeatMode(ValueAnimator.REVERSE);
        //��ѹ���������˶�
        squashAnim1.setInterpolator(new DecelerateInterpolator());
        ValueAnimator squashAnim2 = ObjectAnimator.ofFloat(shapHolder, "width",
                //����С���ȶ���
                shapHolder.getWidth(), shapHolder.getWidth() + 50);
        squashAnim2.setDuration(duration / 4);
        squashAnim2.setRepeatCount(1);
        squashAnim2.setRepeatMode(ValueAnimator.REVERSE);
        //С���������˶�
        squashAnim2.setInterpolator(new DecelerateInterpolator());
        //������չ����
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
        // ������
        bounceBackAnim.setInterpolator(new DecelerateInterpolator());
        
        //���ö��������˳��
        AnimatorSet bouncer = new AnimatorSet();
        //�ȼ�������Ȼ����ִ�м�ѹ����1
        bouncer.play(bounceAnim).before(squashAnim1);
        //���ż�ѹ����1��ͬ�²��ż�ѹ����2
        bouncer.play(squashAnim1).with(squashAnim2);
        bouncer.play(squashAnim1).with(stretchAnim1);
        bouncer.play(squashAnim1).with(stretchAnim2);
        //ִ���꼷ѹ������ִ��С���𶯻�
        bouncer.play(bounceBackAnim).after(stretchAnim2);
        //��ʼִ�ж���
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
                //���С��Ϊ����ִ�л��ƶ���
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
                //��������
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
        //���������߳�
        new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {
        try {
            //�˴�Ϊ��ѭ���������д��ʱ����Լ���һ��boolean����ֵ�����û�������˼���back��ʱ�������߳�
            while (true) {
                drawBall();
                Thread.sleep(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}