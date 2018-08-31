package cn.edu.buaa.me.qel.myapplicationecd;

import org.opencv.core.Point;

public class PointPair {
    private Point p1;
    private Point p2;
    private double dis;

    public void setP1(Point p1){
        this.p1 = p1;
    }

    public void setP2(Point p2){
        this.p2 = p2;
    }

    public void setDis(double dis) {
        this.dis = dis;
    }

    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

    public double getDis() {
        return dis;
    }
}
