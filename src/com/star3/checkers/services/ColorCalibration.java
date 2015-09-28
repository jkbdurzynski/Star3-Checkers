package com.star3.checkers.services;

import com.star3.checkers.utils.MatrixToBufferedImageConverter;
import javafx.embed.swing.SwingFXUtils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by cysio on 30.08.2015.
 */
public class ColorCalibration {



    Mat hsvMatryx = new Mat();
    Mat colorMat = new Mat();



    Imgproc ip = null;
    double lc = 0, hc = 179;
    int x0 = 0, y0 = 0;

    MatrixToBufferedImageConverter mbic = new MatrixToBufferedImageConverter();



    public final int BLUR = 5;
    public final int CLARITY_THRESHOLD = 3;
    public final int LOWER_LIMIT = 0;
    public final int HIGHER_LIMIT = 179;

    /*Lets start by thresholding the input image for anything that is not red.
    Instead of the usual RGB color space we are going to use the HSV space, which has the desirable property that allows
     us to identify a particular color using a single value, the hue, instead of three values. As a side note,
     in OpenCV H has values from 0 to 180, S and V from 0 to 255.
    The red color, in OpenCV, has the hue values approximately in the range of 0 to 10 and 160 to 180.*/


    Mat blurredHsvMatryx = null;

    Mat circles = null;
    int count = 0;

    public ColorCalibration(){
        blurredHsvMatryx = new Mat();
        circles = new Mat();
        ip = new Imgproc();
    }

    public double getHighValue(Mat matryx, int numberOfPawns){
        while (count != numberOfPawns && hc != LOWER_LIMIT) {
            Scalar low = new Scalar(lc, 100, 60);
            Scalar high = new Scalar(hc, 255, 255);
            scanForCircles(matryx, numberOfPawns, low, high);

            hc--;
        }
        return hc - CLARITY_THRESHOLD;
    }

    public double getLowValue(Mat matryx, int numberOfPawns){

        while (count != numberOfPawns || lc == HIGHER_LIMIT) {
            Scalar low = new Scalar(lc, 100, 60);
            Scalar high = new Scalar(hc, 255, 255);
            scanForCircles(matryx, numberOfPawns, low, high);

            lc++;
        }
        return lc + CLARITY_THRESHOLD;
    }

    public ArrayList<Double> getColor(Mat matryx, int numberOfPawns, double colorValue) {
        ArrayList<Double> colors = new ArrayList<>();
        double hColor, lColor;
        hColor = lColor = colorValue;
        int i = 0;
        boolean flag = true;
        Scalar low = new Scalar(lColor, 70, 60);
        Scalar high = new Scalar(hColor, 255, 255);

        Mat matryxForScreen = matryx.clone();

        Mat matrix1 = new Mat();
        Mat matrix2 = new Mat();

        ip.cvtColor(matryxForScreen, matrix1, Imgproc.COLOR_BGR2HSV);
        ip.GaussianBlur(matrix1, matrix2, new Size(BLUR, BLUR), BLUR, BLUR);


        while (count != numberOfPawns && lColor != Integer.valueOf(LOWER_LIMIT).doubleValue() && hColor != Integer.valueOf(HIGHER_LIMIT).doubleValue()) {
            low = new Scalar(lColor, 70, 60);
            high = new Scalar(hColor, 255, 255);

            if(i%5 == 0) {
                flag = !flag;
            }
            if(flag) {
                lColor--;
            } else {
                hColor++;
            }


            scanForCircles(matrix2, numberOfPawns, low, high);
            i++;
        }

        colors.add(lColor);
        colors.add(hColor);

        return colors;
    }

    public void scanForCircles(Mat matryx, int numberOfPawns, Scalar low, Scalar high) {
        Mat matrix = new Mat();

        if (!matryx.empty()) {
            Core.inRange(matryx, low, high, matrix);


                //ip.HoughCircles(blurredHsvMatryx, circles, ip.HOUGH_GRADIENT, 1, blurredHsvMatryx.rows() / 8, 20, 40, 0, 80);
                ip.HoughCircles(matrix, circles, ip.CV_HOUGH_GRADIENT,2.0, matrix.rows()/8, 5, 20, 15, 60 );
                mbic.setMatrix(matrix, ".png");
                BufferedImage bufim = mbic.getImage();

                BufferedImage bImage = SwingFXUtils.fromFXImage(SwingFXUtils.toFXImage(bufim, null), null);
                File f = new File("scanForCircles.png");
                try {
                    ImageIO.write(bImage, "png", f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    count = 0;
                for (int i = 0; i < circles.cols(); i++) {
                    count++;
                    double vCircle[] = circles.get(0, i);
                    Point p = new Point(vCircle);
                    double x = vCircle[0];
                    x0 = (int) Math.round(x);
                    double y = vCircle[1];
                    y0 = (int) Math.round(y);
                    double radius = vCircle[2];
                    int radius0 = (int) Math.round(radius);
                    System.out.println("circle nr:" + i + " " + x0 + " " + y0 + " lc:" + lc + " " + "hc" + hc);


                }
            }
        }
    }

