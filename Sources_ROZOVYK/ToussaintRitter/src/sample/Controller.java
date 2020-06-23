package sample;

import javafx.animation.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Controller {


    public Label msElapsed;
    public Label quality;
    public Label result;
    Task<Void> sleeper = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            return null;
        }
    };
    public ScrollPane graphicPane;
    public Pane interiorPane;
    public List<Point2D> currentPoints;
    public ComboBox<File> filesLoaded;

    public void ritter(ActionEvent actionEvent) {
        long startTime = System.currentTimeMillis();

        ArrayList<Point2D> points = new ArrayList<>(currentPoints);
        double charea = Algorithms.computePolygonArea(Algorithms.convexHull(currentPoints));

        Circle  minCircle = Algorithms.ritter(points);
        Circle inside = new Circle(minCircle.getCenterX(), minCircle.getCenterY(),minCircle.getRadius()-1);

        Shape donutShape = Shape.subtract(minCircle, inside);
        donutShape.setFill(Color.BLUE);
        interiorPane.getChildren().add(donutShape);
        double circleArea=Math.PI*Math.pow(minCircle.getRadius(),2);
        quality.setText(((circleArea/charea)*100-100)+"");
        long endTime = System.currentTimeMillis();
        msElapsed.setText(""+ (endTime-startTime));





    }

    public void toussaint(ActionEvent actionEvent) {
        long startTime = System.currentTimeMillis();

        List<Line> lines = Algorithms.recMin(currentPoints);

        for(Line l : lines){
            interiorPane.getChildren().add(l);
        }

        long endTime = System.currentTimeMillis();
        List<Point2D> sq=new ArrayList<>();
        lines.forEach(line ->sq.add(new Point2D(line.getStartX(),line.getStartY())));
        msElapsed.setText(""+ (endTime-startTime));
        double sarea = Algorithms.computePolygonArea(sq);
        double charea = Algorithms.computePolygonArea(Algorithms.convexHull(currentPoints));
        quality.setText(((sarea/charea)*100-100)+"");



    }

    public void findTestFiles(ActionEvent actionEvent) {

        DirectoryChooser chooser = new DirectoryChooser();
        File selectedDirectory = chooser.showDialog(graphicPane.getScene().getWindow());
        List<File> filesToAdd=new ArrayList<>();
        try {
            Files.walk(Paths.get(selectedDirectory.getAbsolutePath()))
                    .filter(Files::isRegularFile)
                    .forEach(path -> filesToAdd.add(new File(path.toUri())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(filesToAdd);
        filesLoaded.getItems().addAll(filesToAdd);
    }

    public void convexHull(ActionEvent actionEvent) {

        long startTime = System.currentTimeMillis();
        List<Line> lines=new ArrayList<>();
        List<Point2D> filteredPoints=Algorithms.convexHull(currentPoints);
        for(int i=0;i<filteredPoints.size();i++){
            double x1=filteredPoints.get(i).getX();
            double y1=filteredPoints.get(i).getY();
            double x2=filteredPoints.get((i+1)%filteredPoints.size()).getX();
            double y2=filteredPoints.get((i+1)%filteredPoints.size()).getY();
            Line line =new Line(x1,y1,x2,y2);

                    line.setStyle("-fx-stroke: blue;");


            lines.add(line);
        }


        long endTime = System.currentTimeMillis();
        msElapsed.setText(""+ (endTime-startTime));
        interiorPane.getChildren().addAll(lines);
    }

    private boolean contientDesDoublon(List<Point2D> filteredPoints) {
        for (int i = 0; i < filteredPoints.size(); i++) {
            Point2D pi=filteredPoints.get(i);
            for (int j = 0; j < filteredPoints.size(); j++) {
                if(j!=i){
                    if(filteredPoints.get(j).equals(pi))return true;
                }
            }
        }
        return false;
    }

    public void testAllLoaded(ActionEvent actionEvent) {
        result.setText("");

        try {
            Files.deleteIfExists(Paths.get("./ritter_results.csv"));
            Files.deleteIfExists(Paths.get("./toussaint_results.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (File f : filesLoaded.getItems()){
            filesLoaded.getSelectionModel().select(f);
            loadPoints(null);
            List<Point2D> l = Algorithms.convexHull(currentPoints);
            List<Line> l2 = Algorithms.recMin(currentPoints);
            Circle circleMin = Algorithms.ritter(new ArrayList<>(currentPoints));
            List<Point2D> sq=new ArrayList<>();
            l2.forEach(line ->sq.add(new Point2D(line.getStartX(),line.getStartY())));
            double sa = Algorithms.computePolygonArea(sq);
            double ha = Algorithms.computePolygonArea(l);
            double qToussaint= sa/ha*100-100;
            double qRitter= ((Math.pow(circleMin.getRadius(),2)*Math.PI)/ha*100)-100;

            try {
                Files.write(Paths.get("./ritter_results.csv"),
                        ((qRitter) + System.lineSeparator()).getBytes(UTF_8),
                        StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                Files.write(Paths.get("./toussaint_results.csv"),
                        ((qToussaint) + System.lineSeparator()).getBytes(UTF_8),
                        StandardOpenOption.CREATE,StandardOpenOption.APPEND);

            } catch (IOException e) {
                e.printStackTrace();
            }

            result.setText("Done");

        }





    }

    public void loadPoints(ActionEvent actionEvent) {
        graphicPane.setContent(null);
        currentPoints=new ArrayList<>();
        interiorPane=new Pane();
        //pane.setPrefSize(graphicPane.getPrefWidth()-200,graphicPane.getPrefHeight()-100);
        interiorPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY, CornerRadii.EMPTY, Insets.EMPTY)));
        try {
            Files.lines(filesLoaded.getSelectionModel().getSelectedItem().toPath()).forEach(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    int coordX=Integer.parseInt(s.split(" ")[0]);
                    int coordY=Integer.parseInt(s.split(" ")[1]);
                    currentPoints.add(new Point2D(coordX,coordY));
                    interiorPane.getChildren().add(makeCircle(coordX,coordY));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        interiorPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                double zoomFactor = 1.05;
                double deltaY = event.getDeltaY();

                if (deltaY < 0){
                    zoomFactor = 0.95;
                }
                interiorPane.setScaleX(interiorPane.getScaleX() * zoomFactor);
                interiorPane.setScaleY(interiorPane.getScaleY() * zoomFactor);
                event.consume();
            }
        });

        graphicPane.setContent(interiorPane);

        /*
        try {
            Charset charset = Charset.defaultCharset();
            stringList = Files.readAllLines(filesLoaded.getSelectionModel().getSelectedItem().toPath(), charset);
            stringStream = stringList.toArray(new String[]{});
        } catch (IOException e) {
            e.printStackTrace();
        }
        sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                int coordX=Integer.parseInt(stringStream[i].split( " ")[0]);
                int coordY=Integer.parseInt(stringStream[i].split( " ")[1]);
                graphicPane.getChildren().add(makeCircle(coordX,coordY));
                i++;
            }
        });
        for(int i =0;i<stringList.size();i++)
        new Thread(sleeper).start();*/
    }


    private Circle makeCircle(double drawX, double drawY) {
        Circle circle = new Circle(drawX, drawY, 1.5, Color.BLUEVIOLET);// create the circle and its properties(color: coral to see it better)
        return circle;
    }


}
