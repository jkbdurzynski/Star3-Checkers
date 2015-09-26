package com.star3.checkers.models;

/**
 * Created by jakubdurzynski on 13.06.2015.
 */
public class Edge extends Pawn {

    static protected int detected = 0;

    public Edge() {

    }

    public Edge(int x, int y, Color color) {
        super(x,y,color);
        ++detected;
    }

    public static int getDetected() {
        return detected;
    }

    public static void incrementDetected() {
        ++detected;
    }

    protected void finalize() throws Throwable {
        detected--;
        super.finalize();
    }
}
