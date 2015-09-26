package com.star3.checkers.services;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created by cysio on 26.09.2015.
 */


public class FrameProcessing {

    Imgproc ip;
    private static final double BLUR = 5;

    public FrameProcessing() {
        ip = new Imgproc();
    }

    public Mat getThresholdMat(Mat rawMatrix, Scalar high, Scalar low) {
        Mat thresholdMat = new Mat();
        Mat hsvMat = new Mat();
        Mat blurred = new Mat();
        if (!rawMatrix.empty()) {
            ip.cvtColor(rawMatrix, hsvMat, Imgproc.COLOR_BGR2HSV);
            ip.GaussianBlur(hsvMat, blurred, new Size(BLUR, BLUR), BLUR, BLUR);
            Core.inRange(blurred, low, high, thresholdMat);
        }
        return thresholdMat;
    }

    public Mat drawCircles(Mat rawMatrix, Mat thresholdMatrix, ArrayList<Double> params) {
        Mat circles = new Mat();
        double par0 = params.get(0).doubleValue();
        double par1 = params.get(1).doubleValue();
        int par2 = params.get(2).intValue();
        int par3 = params.get(3).intValue();

        if (rawMatrix != null && thresholdMatrix != null) {
            ip.HoughCircles(rawMatrix, circles, ip.HOUGH_GRADIENT, 2.0, rawMatrix.rows() / 8, par0, par1, par2, par3);
            for (int i = 0; i < circles.cols(); i++) {

                double vCircle[] = circles.get(0, i);
                Point p = new Point(vCircle);
                double radius = vCircle[2];
                int radius0 = (int) Math.round(radius);

                ip.circle(rawMatrix, p, 3, new Scalar(0, 255, 0), -1, 8, 0);
                // circle outline
                ip.circle(rawMatrix, p, radius0, new Scalar(0, 0, 255), 3, 8, 0);
            }

        }
        return rawMatrix;
    }
}


