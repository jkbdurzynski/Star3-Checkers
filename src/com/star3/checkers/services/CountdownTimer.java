package com.star3.checkers.services;

/**
 * Created by jakubdurzynski on 12.09.2015.
 */
public class CountdownTimer {

    private int min;

    private int sec;

    private int millis;

    public CountdownTimer(int min, int sec, int millis) {
        this.min = min;
        this.sec = sec;
        this.millis = millis;
    }

    public void countdown() {
        if (millis == 0) {
            if (sec == 0) {
                min--;
                sec = 59;
            } else {
                sec--;
            }
            millis = 999;
        } else {
            millis--;
        }
    }

    public boolean timedOut() {
        return (min == 0 & sec == 0) ? true : false;
    }

    public String toString() {
        return min + ":" + (sec < 10 ? "0" + sec : sec) + ":" + (millis < 100 ? "0" + millis : (millis < 10 ? "00" + millis : millis));
    }
}
