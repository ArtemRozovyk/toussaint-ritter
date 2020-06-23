package sample;

import javafx.geometry.*;
import javafx.scene.shape.*;

import java.util.*;

public class Algorithms {


    public static double computePolygonArea(List<Point2D> points){
        double divident=0.0;
        for(int i=0;i<points.size();i++){
            Point2D u=points.get(i);
            Point2D v=points.get((i+1)%points.size());
            divident+=(u.getX()*v.getY()-u.getY()*v.getX());
        }
        return Math.abs(divident/2);

    }
    static int westIndex=0,eastIndex=1,northIndex=2,southIndex=3;

    /**
     * Graham's convex hull algoritm
     * @param pointCloud
     * @return polygone convexe
     */
    public static List<Point2D> convexHull(List<Point2D> pointCloud){

        if (pointCloud.size()<3) {
            return null;
        }

        pointCloud = triPixel(pointCloud);

        //graham
        double prodvect;
        Point2D a,b,c;
        int i=0;
        do{
            a=pointCloud.get(i);
            b=pointCloud.get((i+1)%pointCloud.size());
            c=pointCloud.get((i+2)%pointCloud.size());
            do{
                prodvect=(b.getX()-a.getX())*(c.getY()-a.getY())-(b.getY()-a.getY())*(c.getX()-a.getX());
                if((prodvect>0)){
                    pointCloud.remove((i+1)%pointCloud.size());
                    if(i>0){
                        i--;
                        //fight turn
                        a=pointCloud.get(i);
                        b=pointCloud.get((i+1));
                    }else {
                        i=-1;
                        break;
                    }
                }
            }while (prodvect>0);
            //traiter les exceptions
            i++;
        }while(i<pointCloud.size());
        return pointCloud;
    }

    /**
     * Trier les points suivant leurs coordonnées x y
      * @param points Nuage de points
     * @return les points consecutif (trigonometriquement) dans
     * le sens d'une aguille sens de l'aiguille d'une montre
     * ayant allant de x min (y max)* à x max puis de x max (y min)* à x min.
     */
    private static ArrayList<Point2D> triPixel(List<Point2D> points) {

        double xmax=0;
        for(Point2D p : points)
            if(p.getX()>xmax) xmax=p.getX();

        Point2D[] ymin =new Point2D[100000];
        Point2D[] ymax =new Point2D[100000];
        for(Point2D p: points){
            if(ymin[(int)p.getX()]==null||ymin[(int)p.getX()].getY()>p.getY()){
                ymin[(int)p.getX()]=p;
            }

            if(ymax[(int)p.getX()]==null||ymax[(int)p.getX()].getY()<p.getY()){
                ymax[(int)p.getX()]=p;
            }
        }

        ArrayList<Point2D> res=new ArrayList<>();
        for(int i=0;i<ymax.length-1;i++){
            if(ymax[i]!=null)
                res.add(ymax[i]);
        }
        for(int i=ymin.length-1;i>0;i--){
            if(ymin[i]!=null)
                res.add(ymin[i]);
        }
        ArrayList<Point2D> clean=new ArrayList<>();
        //enlever les doublons
        for(int i=0;i<res.size();i++){
            if(res.get(i).equals(res.get((i+1)%res.size()))){
                res.remove(i);
            }
        }
        return res;
    }

    /**
     * Intersection de 2 droites
     * @param l1
     * @param l2
     * @return point d'untersection
     */
    public static Point2D findIntersection(Line l1, Line l2){
        double x1=l1.getStartX();
        double y1=l1.getStartY();
        double x2=l1.getEndX();
        double y2=l1.getEndY();
        double x3=l2.getStartX();
        double y3=l2.getStartY();
        double x4=l2.getEndX();
        double y4=l2.getEndY();
        if ((x1 == x2 && y1 == y2) || (x3 == x4 && y3 == y4)) {
            throw new RuntimeException("Some line has length of 0");
        }
        double denominator = ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));

        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denominator;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denominator;

        double x = x1 + ua * (x2 - x1);
        double y = y1 + ua * (y2 - y1);
        return new Point2D(x,y);
    }


    private static Point2D getMaxDist(List<Point2D> points,Point2D dummy ){
        double distmax=0;
        Point2D pMAX=null;
        for (Point2D p : points) {
            if(dummy.distance(p)>distmax){
                pMAX=p;
                distmax=dummy.distance(p);
            }
        }
        return pMAX;
    }


    /**
     * Ritter's algorithm of min circle approximation
     * @param points
     * @return cercle min
     */
    public static Circle ritter(List<Point2D> points) {
        //get random point
        Point2D dummy = points.get(0);
        //find two points P Q that are the farthest from each other
        Point2D pmaxP=getMaxDist(points,dummy);
        Point2D pmaxQ=getMaxDist(points,pmaxP);
        Point2D midpoint = pmaxQ.midpoint(pmaxP);
        double x=midpoint.getX();
        double y=midpoint.getY();
        double r=pmaxQ.distance(pmaxP)/2;
        //make a circle with the middle at midpoint of P Q
        Circle whole = new Circle(x, y,r);
        //remove them from points because they are contained by the circle C
        points.remove(pmaxP);
        points.remove(pmaxQ);

        while (points.size()>1){
            //get random point
            Point2D s = points.get((int) (Math.random() * points.size()));
            //delete if current circle contains it
            if(s.distance(midpoint)<=r) {
                points.remove(s);
            }
            else {
                //circle doesnt contain point s,
                //find the line equation of line |SM| where m is midpoint(circle center)
                double div = (s.getX()-midpoint.getX());
                if (div == 0.0) continue;
                double m = (s.getY()-midpoint.getY())/ div;
                double n = s.getY() -m*s.getX();
                double b = 2*(m*n-m*midpoint.getY()-midpoint.getX());
                double a = (m*m+1);
                double c =midpoint.getY()*midpoint.getY()-(r*r)+midpoint.getX()*midpoint.getX()
                                -2*n*midpoint.getY()+n*n;
                //find the farthest point T that is on the line |SM|
                //and that intersects the cicle
                double delta = b*b-4*a*c;
                double x1 = (-b-Math.sqrt(delta))/(2.0*a);
                double y1 = m*x1+n;

                double x2 = (-b+Math.sqrt(delta))/(2.0*a);
                double y2 = m*x2+n;
                double dSx1=s.distance(x1,y1);
                double dSx2=s.distance(x2,y2);
                Point2D T;
                if (dSx1>dSx2)
                    T=new Point2D(x1,y1);
                else {
                    T=new Point2D(x2,y2);
                }
                //update the current circle
                x=(s.getX()+T.getX())/2.0;
                y=(s.getY()+T.getY())/2.0;
                midpoint=new Point2D(x,y);
                r = midpoint.distance(T);
                whole = new Circle(x, y,r);
            }

        }
        return whole;
    }

    public static class Square {
        Point2D brright;
        Point2D bleft;
        Point2D tright;
        Point2D tleft;

        public double area(){
            return computePolygonArea(Arrays.asList(brright,bleft,tleft,tright));
        }
        public List<Line> getGraphics(int i){
            Line l = new Line(brright.getX(),brright.getY(),bleft.getX(),bleft.getY());
            Line l2 = new Line(bleft.getX(),bleft.getY(),tleft.getX(),tleft.getY());
            Line l3 = new Line(tleft.getX(),tleft.getY(),tright.getX(),tright.getY());
            Line l4 = new Line(tright.getX(),tright.getY(),brright.getX(),brright.getY());
            if(i==0){
                l.setStyle("-fx-stroke: lightslategrey;");
                l2.setStyle("-fx-stroke: lightslategrey;");
                l3.setStyle("-fx-stroke: lightslategrey;");
                l4.setStyle("-fx-stroke: lightslategrey;");
            }else{
                l.setStyle("-fx-stroke: blue;");
                l2.setStyle("-fx-stroke: blue;");
                l3.setStyle("-fx-stroke: blue;");
                l4.setStyle("-fx-stroke: blue;");
            }

            return Arrays.asList(l,l2,l3,l4);
        }
        public Point2D getBrright() {
            return brright;
        }

        public void setBrright(Point2D brright) {
            this.brright = brright;
        }

        public Point2D getBleft() {
            return bleft;
        }

        public void setBleft(Point2D bleft) {
            this.bleft = bleft;
        }

        public Point2D getTright() {
            return tright;
        }

        public void setTright(Point2D tright) {
            this.tright = tright;
        }

        public Point2D getTleft() {
            return tleft;
        }

        public void setTleft(Point2D tleft) {
            this.tleft = tleft;
        }

        public Square(Point2D brright, Point2D bleft, Point2D tright, Point2D tleft) {
            this.brright = brright;
            this.bleft = bleft;
            this.tright = tright;
            this.tleft = tleft;
        }
    }


    /**
     * Toussaint's algorithm
     * @param pointCloud
     * @return rectangle minimum containing all poitns
     */
    public static List<Line> recMin(List<Point2D> pointCloud){
        //get the convex hull
        List<Point2D> convexHull=convexHull(pointCloud);
        //compute its area
        double chArea= Algorithms.computePolygonArea(convexHull);
        if(convexHull.size()<3) return null;
        Point2D west=convexHull.get(0)
                ,east=convexHull.get(1)
                ,north=convexHull.get(2)
                ,south=convexHull.get(3 % convexHull.size());
        int westIndex=0;eastIndex=1;northIndex=2;southIndex=3%convexHull.size();

        //find limit points
        for(int i = 0; i<convexHull.size();i++){
            Point2D p = convexHull.get(i);
            double x=p.getX();
            double y=p.getY();
            if(west.getX()>x){
                west=p;
                westIndex=i;
            }
            if(east.getX()<x){
                east=p;
                eastIndex=i;
            }
            if(south.getY()<y){
                south=p;
                southIndex=i;
            }
            if(north.getY()>y){
                north=p;
                northIndex=i;
            }
        }
        //get first parallel lines that touch limit points
        Line leftline=(new Line(west.getX(),700,west.getX(),0));
        Line botline=(new Line(700,south.getY(),0,south.getY()));
        Line rightline=(new Line(east.getX(),0,east.getX(),700));
        Line topline=(new Line(0,north.getY(),700,north.getY()));

        double sAngle,wAngle,nAngle,eAngle;
        double minAngle;
        //xi x being in {s,w,n,e} is the index of next considered point
        int si=southIndex-1,wi=westIndex-1,ei=eastIndex-1,ni=northIndex-1;
        Square minsquare=null;
        double minarea=Double.MAX_VALUE;
        List<Line> res= new ArrayList<>();
        do{
            //make a first square
            Square square=linesToSquare(leftline,botline,rightline,topline);
            double sqArea= square.area();
            if(minarea>sqArea){
                minsquare=square;
                minarea=sqArea;
            }
            //assure circular checkup of convex hull (stored as a simple-linked list)
            if(wi<0)wi=convexHull.size()+wi;
            if(si<0)si=convexHull.size()+si;
            if(ni<0)ni=convexHull.size()+ni;
            if(ei<0)ei=convexHull.size()+ei;
            //find angles between current(!) limit points (initially, the north, south... etc limits)
            //and the succeeding point in the convex hull
            sAngle=south.angle(new Point2D(botline.getEndX(),botline.getEndY()),convexHull.get(si))%180;
            wAngle=west.angle(new Point2D(leftline.getEndX(),leftline.getEndY()),convexHull.get(wi))%180;
            eAngle=east.angle(new Point2D(rightline.getEndX(),rightline.getEndY()),convexHull.get(ei))%180;
            nAngle=north.angle(new Point2D(topline.getEndX(),topline.getEndY()),convexHull.get(ni))%180;
            //find min
            minAngle= Math.min(sAngle,Math.min(eAngle,Math.min(wAngle,nAngle)));
            //rotate 4 lines to with the angle
            if (minAngle != 0 ) {
                botline=rotateLine(si,south,botline,minAngle);
                rightline=rotateLine(ei,east,rightline,minAngle);
                leftline=rotateLine(wi,west,leftline,minAngle);
                topline=rotateLine(ni,north,topline,minAngle);

            }


            //update point with min angle because there was a rotation of
            //correspondent line thus the "succeeding" point touches it
            if(minAngle==sAngle){
                south=convexHull.get(si--);
            }
            if(minAngle==wAngle){
                west=convexHull.get(wi--);
            }
            if(minAngle==eAngle){
                east=convexHull.get(ei--);
            }
            if(minAngle==nAngle){
                north=convexHull.get(ni--);
            }

        }while(si!=southIndex );
        res.addAll(minsquare.getGraphics(1));
        return res;
    }



    public static Square linesToSquare(Line leftline, Line botline, Line rightline, Line topline){
        Point2D bright=findIntersection(botline,rightline);
        Point2D bleft=findIntersection(botline,leftline);
        Point2D tright=findIntersection(topline,rightline);
        Point2D tleft=findIntersection(topline,leftline);
        return new Square(bright,bleft,tright,tleft);
    }
    private static Line rotateLine(int current, Point2D origin, Line line, double minAngle) {
        Line limit=null;
        //extend the lines in order to always cover the points
        //wich arbitrary have only positive coordinates
        //the more elegant(general) way would be to simply use line equations instead of just 2 line end points
        if(current<=southIndex&&current>westIndex){
            limit=new Line(0,0,0,1000);
        }
        if(current>northIndex||current==0){
            limit=new Line(0,0,1000,0);
        }
        if(current<=northIndex&&current>eastIndex){
            limit=new Line(1000,0,1000,1000);
        }
        if(current<=eastIndex&&current>southIndex){
            limit= new Line(0,1000,1000,1000);
        }
        Point2D newEnd=rotateLineClockWise(origin,new Point2D(line.getEndX(),line.getEndY()),minAngle);
       Line shrt= new Line(origin.getX(),origin.getY(),newEnd.getX(),newEnd.getY());

       Point2D inrt=findIntersection(limit,shrt);
       return new Line(origin.getX(),origin.getY(),inrt.getX(),inrt.getY());


    }

    static Point2D rotateLineClockWise(Point2D p1, Point2D p2, double angle) {
        double x=Math.cos(Math.toRadians(angle))*(p2.getX()-p1.getX())+((-Math.sin(Math.toRadians(angle)))*(p2.getY()-p1.getY()))+p1.getX();
        double y=Math.sin(Math.toRadians(angle))*(p2.getX()-p1.getX())+(Math.cos(Math.toRadians(angle))*(p2.getY()-p1.getY()))+p1.getY();
        return new Point2D( x, y);
    }





}
