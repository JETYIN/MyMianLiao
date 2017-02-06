package com.tjut.mianliao.live;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * Created by YoopWu on 2016/6/17 0017.
 */
public class BessalEvaluator implements TypeEvaluator<PointF> {

    private PointF p1;
    private PointF p2;

    public BessalEvaluator(PointF p1, PointF p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public BessalEvaluator(PointF p1) {
        this.p1 = p1;
    }

    /**
     * @param t
     *            t is 0~1
     * @param p0
     *            p0 is the start value
     * @param p3
     *            p3 is the end value
     * @return PointF
     */
    @Override
    public PointF evaluate(float t, PointF p0, PointF p3) {
        PointF pointF = new PointF();
//        pointF.x = (float) (p0.x * Math.pow((1 - t), 3) + 3 * p1.x * t
//                * Math.pow((1 - t), 2) + 3 * p2.x * Math.pow(t, 2)
//                * (1 - t) + p3.x * Math.pow(t, 3));
//        pointF.y = (float) (p0.y * Math.pow((1 - t), 3) + 3 * p1.y * t
//                * Math.pow((1 - t), 2) + 3 * p2.y * Math.pow(t, 2)
//                * (1 - t) + p3.y * Math.pow(t, 3));\
        pointF.x = (float) (p0.x * Math.pow((1 - t), 2) + 2 * t * (1 - t) * p1.x + t * t * p3.x);
        pointF.y = (float) (p0.y * Math.pow((1 - t), 2) + 2 * t * (1 - t) * p1.y + t * t * p3.y);
        return pointF;
    }
}