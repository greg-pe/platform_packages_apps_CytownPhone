/*
 * Copyright (C) 2011 doixanh@xda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.phone;

import android.content.Context;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;

public final class LightSensorListener {
	private final String TAG = "froyobread";

	private LightSensorListenerIntf mListener;
    private Sensor mSensor;
    private SensorManager mSensorManager;
    private boolean mEnabled = false;

    public interface LightSensorListenerIntf {
        public void lightChanged(int lightValue);
    }

    SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            onSensorEvent(event.values[0]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // who cares?
        }
    };

    private void onSensorEvent(double lightValue) {
    	Log.i(TAG, "light sensor value : " + lightValue);
        mListener.lightChanged((int) lightValue);
    }
    
    public LightSensorListener(Context context, LightSensorListenerIntf listener) {
        mListener = listener;
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public boolean isEnabled() {
    	return mEnabled;
    }

    public void enable(boolean enable) {
        synchronized (this) {
           	Log.i(TAG, "enabling light sensor : " + enable);
            if (enable) {
            	if (!mEnabled) {
	                mSensorManager.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
	                mEnabled = enable;
		           	Log.i(TAG, "light sensor enabled: " + mEnabled);
                }
            } else {
            	if (mEnabled) {
	                mSensorManager.unregisterListener(mSensorListener);
	                mEnabled = enable;
		           	Log.i(TAG, "light sensor enabled: " + mEnabled);
	            }
            }
        }
    }
}
