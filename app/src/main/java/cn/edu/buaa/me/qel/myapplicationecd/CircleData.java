package cn.edu.buaa.me.qel.myapplicationecd;

import org.opencv.core.Point;

public class CircleData {
    private Point center;
    private double radius;
    public void setCenter(Point center){
        this.center = center;
    }

    public void setRadius(double radius){
        this.radius = radius;
    }

    public Point getCenter(){
        return this.center;
    }

    public double getRadius(){
        return this.radius;
    }
}
