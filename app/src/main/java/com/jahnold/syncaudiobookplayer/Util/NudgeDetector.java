package com.jahnold.syncaudiobookplayer.Util;


import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

/**
 * Class for reporting when the device's acceleration (excluding gravity) exceeds
 * a certain value. Compatible with all Android versions as it uses Sensor.TYPE_ACCELEROMETER
 * rather than Sensor.TYPE_LINEAR_ACCELERATION.
 *
 * NudgeDetector objects are initially disabled. To use, implement
 * the NudgeDetectorEventListener interface in your class, then register it
 * to a new NudgeDetector object with registerListener(). Finally, call
 * setEnabled(true) to start detecting device movement. You should add a call
 * to stopDetection() in your Activity's onPause() method to conserve battery
 * life.
 *
 * @author kiwiandroiddev
 *
 */
public class NudgeDetector implements SensorEventListener {

    private ArrayList<NudgeDetectorEventListener> mListeners;
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean mEnabled = false;
    private boolean mCurrentlyDetecting = false;
    private boolean mCurrentlyChecking = false;
    private int mGraceTime = 1000;                                  // milliseconds
    private int mSampleRate = SensorManager.SENSOR_DELAY_GAME;
    private double mDetectionThreshold = 0.5f;                      // ms^-2
    private float[] mGravity = new float[] { 0.0f, 0.0f, 0.0f };
    private float[] mLinearAcceleration = new float[] { 0.0f, 0.0f, 0.0f };

    /**
     * Client activities should implement this interface and register themselves using
     * registerListener() to be alerted when a nudge has been detected
     */
    public interface NudgeDetectorEventListener {
        public void onNudgeDetected();
    }

    public NudgeDetector(Context context) {
        mContext = context;
        mListeners = new ArrayList<NudgeDetectorEventListener>();
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    // Accessors follow

    public void registerListener(NudgeDetectorEventListener newListener) {
        mListeners.add(newListener);
    }

    public void removeListeners() {
        mListeners.clear();
    }

    public void setEnabled(boolean enabled) {
        if (!mEnabled && enabled) {
            startDetection();
        } else if (mEnabled && !enabled) {
            stopDetection();
        }
        mEnabled = enabled;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Returns whether this detector is currently registered with the sensor manager
     * and is receiving accelerometer readings from the device.
     */
    public boolean isCurrentlyDetecting() {
        return mCurrentlyDetecting;
    }

    /**
     * Sets the the amount of acceleration needed to trigger a "nudge".
     * Units are metres per second per second (ms^-2)
     */
    public void setDetectionThreshold(double threshold) {
        mDetectionThreshold = threshold;
    }

    public double getDetectionThreshold() {
        return mDetectionThreshold;
    }

    /**
     * Sets the minimum amount of time between when startDetection() is called
     * and nudges are actually detected. This should be non-zero to avoid
     * false positives straight after enabling detection (e.g. at least 500ms)
     *
     * @param milliseconds_delay
     */
    public void setGraceTime(int milliseconds_delay) {
        mGraceTime = milliseconds_delay;
    }

    public int getGraceTime() {
        return mGraceTime;
    }

    /**
     * Sets how often accelerometer readings are received. Affects the accuracy of
     * nudge detection. A new sample rate won't take effect until stopDetection()
     * then startDetection() is called.
     *
     * @param rate  must be one of SensorManager.SENSOR_DELAY_UI,
     *      SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME,
     *      SensorManager.SENSOR_DELAY_FASTEST
     */
    public void setSampleRate(int rate) {
        mSampleRate = rate;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    /**
     * Starts listening for device movement
     * after an initial delay specified by grace time attribute -
     * change this using setGraceTime().
     * Client Activities might want to call this in their onResume() method.
     *
     * The actual sensor code uses a moving average to remove the
     * gravity component from acceleration. This is why readings
     * are collected and not checked during the grace time
     */
    public void startDetection() {
        if (mEnabled && !mCurrentlyDetecting) {
            mCurrentlyDetecting = true;
            mSensorManager.registerListener(this, mAccelerometer, mSampleRate);

            Handler myHandler = new Handler();
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mEnabled && mCurrentlyDetecting) {
                        mCurrentlyChecking = true;
                    }
                }
            }, mGraceTime);
        }
    }

    /**
     * Deregisters accelerometer sensor from the sensor manager.
     * Does nothing if nudge detector is currently disabled.
     * Client Activities should call this in their onPause() method.
     */
    public void stopDetection() {
        if (mEnabled && mCurrentlyDetecting) {
            mSensorManager.unregisterListener(this);
            mCurrentlyDetecting = false;
            mCurrentlyChecking = false;
        }
    }

    // SensorEventListener callbacks follow

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        final float alpha = 0.8f;

        mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
        mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
        mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];

        mLinearAcceleration[0] = event.values[0] - mGravity[0];
        mLinearAcceleration[1] = event.values[1] - mGravity[1];
        mLinearAcceleration[2] = event.values[2] - mGravity[2];

        // find length of linear acceleration vector
        double scalarAcceleration = mLinearAcceleration[0] * mLinearAcceleration[0]
                + mLinearAcceleration[1] * mLinearAcceleration[1]
                + mLinearAcceleration[2] * mLinearAcceleration[2];
        scalarAcceleration = Math.sqrt(scalarAcceleration);

        if (mCurrentlyChecking && scalarAcceleration >= mDetectionThreshold) {
            for (NudgeDetectorEventListener listener : mListeners)
                listener.onNudgeDetected();
        }
    }
}
