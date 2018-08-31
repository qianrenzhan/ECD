package cn.edu.buaa.me.qel.myapplicationecd;

import org.opencv.core.Point;

public class WireAlgorithm {
    public static CircleData findCircle1(Point pt1, Point pt2, Point pt3) {

        CircleData CD = new CircleData();

        //定义两个点，分别表示两个中点
        Point midpt1 = new Point();
        Point midpt2 = new Point();
        //求出点1和点2的中点
        midpt1.x = (pt2.x + pt1.x) / 2;
        midpt1.y = (pt2.y + pt1.y) / 2;
        //求出点3和点1的中点
        midpt2.x = (pt3.x + pt1.x) / 2;
        midpt2.y = (pt3.y + pt1.y) / 2;

        //求出分别与直线pt1pt2，pt1pt3垂直的直线的斜率
        double k1 = -(pt2.x - pt1.x) / (pt2.y - pt1.y);
        double k2 = -(pt3.x - pt1.x) / (pt3.y - pt1.y);
        //然后求出过中点midpt1，斜率为k1的直线方程（既pt1pt2的中垂线）：y - midPt1.y = k1( x - midPt1.x)
        //以及过中点midpt2，斜率为k2的直线方程（既pt1pt3的中垂线）：y - midPt2.y = k2( x - midPt2.x)

        //连立两条中垂线方程求解交点得到：
        Point center = new Point();

        center.x = (midpt2.y - midpt1.y - k2 * midpt2.x + k1 * midpt1.x) / (k1 - k2);
        center.y = midpt1.y + k1 * (midpt2.y - midpt1.y - k2 * midpt2.x + k2 * midpt1.x) / (k1 - k2);
        CD.setCenter(center);

        //用圆心和其中一个点求距离得到半径：
        double radius = Math.sqrt((CD.getCenter().x - pt1.x)*(CD.getCenter().x - pt1.x) + (CD.getCenter().y - pt1.y)*(CD.getCenter().y - pt1.y));
        CD.setRadius(radius);
        return CD;
    }
}
