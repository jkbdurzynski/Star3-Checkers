package com.star3.checkers.utils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MatrixToBufferedImageConverter {
    Mat matrix;
    MatOfByte mob;
    String fileExten;

    public MatrixToBufferedImageConverter() {
        mob = new MatOfByte();

    }

    public void setMatrix(Mat matrix, String fileExtension) {
        this.matrix = matrix;
        this.fileExten = fileExtension;
    }

    // The file extension string should be ".jpg", ".png", etc
    public MatrixToBufferedImageConverter(Mat amatrix, String fileExtension) {
        matrix = amatrix;
        fileExten = fileExtension;
        mob = new MatOfByte();
    }

    public BufferedImage getImage() {
        Imgcodecs.imencode(fileExten, matrix, mob);
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }
}

