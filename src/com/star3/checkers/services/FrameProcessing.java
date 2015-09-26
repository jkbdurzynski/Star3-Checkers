package com.star3.checkers.services;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

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
}


