package com.danielacedo.trabajoficheros;

import android.os.CountDownTimer;

/**
 * Created by Daniel on 30/11/2016.
 */

public class CustomTimer extends CountDownTimer {

    public interface TimerCallback{
        void onTick();
    }

    private int numTicks;

    private TimerCallback callback;

    public CustomTimer(int loopNumber, int tickRate, TimerCallback callback) {
        super((loopNumber * tickRate * 1000) + 1000, tickRate * 1000);
        this.callback = callback;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if(numTicks >= 1)
            callback.onTick();
        else{
            numTicks++;
        }
    }

    @Override
    public void onFinish() {

    }
}
