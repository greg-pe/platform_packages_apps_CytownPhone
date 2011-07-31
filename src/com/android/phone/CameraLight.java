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

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import java.io.IOException;

/**
 * This listener uses camera to detect light around the phone.
 * Used for detecting low light environment to louder the ringer
 */
public final class CameraLight implements SurfaceHolder.Callback {

    public interface CameraLightListener {
        public void cameraLightChanged(int value);
    }

	private final static int WIDTH = 320;
	private final static int HEIGHT = 240;
    private static final String TAG = "CameraLight";
    private static final boolean DEBUG = true;

    private boolean mEnabled;
    private boolean mSurfaceReady;
    private boolean mPreviewRunning;
	private boolean mWaitingSurface;
    private int mFrameCount;
	private CameraLightListener mListener;
    private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;
	
	
	private PreviewCallback pcb = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] previewData, Camera c) {
			if (previewData != null) {
				long value = 0;
				mFrameCount++;
				for (int i = 0; i < WIDTH * HEIGHT - 1; i++) {
					value += (long) previewData[i] & 0xFF;
				}
				value /= (WIDTH * HEIGHT);
				Log.i(TAG, "previewData gotten " + previewData.length + " bytes, #" + mFrameCount + ", val=" + value);
				setLightValue((int)value);
			}
			else {
				Log.i(TAG, "previewData is null");
			}
		}
	};

    public CameraLight(Context context, SurfaceView surfaceView, CameraLightListener listener) {
        mListener = listener;
        mSurfaceView = surfaceView;
        
        try {
			mSurfaceHolder = mSurfaceView.getHolder();
			mSurfaceHolder.addCallback(this);
			mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			mSurfaceHolder.setFixedSize(1, 1);
		} catch (Exception e) {
		}
    }
    
    @Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
   		if (DEBUG) Log.d(TAG, "dx: surface changed");
		mSurfaceReady = true;
		// were we waiting for the surface?
		if (mWaitingSurface) {
			// yes, preview now
			startPreview();
		}
		mWaitingSurface = false;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
   		if (DEBUG) Log.d(TAG, "dx: surface created");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
   		if (DEBUG) Log.d(TAG, "dx: surface destroyed");
		stopPreview();
	}
    
    public boolean isEnabled() {
    	return mEnabled;
    }
    
	// start the preview to get light data
    private boolean startPreview() {
    	if (!mSurfaceReady)	{
    		// surface is not ready, we have to wait till it's ready
    		if (DEBUG) Log.d(TAG, "dx: surface is not ready, waiting for it...");
    		mWaitingSurface = true;
			return false;    	
    	}
    	
    	mFrameCount = 0;
    	// open camera
    	try {
			mCamera = Camera.open();
    	}
    	catch (Exception e) {
    		return true;
		}
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}
		
		// setup preview parameters		
		Camera.Parameters p = mCamera.getParameters();
		p.setPreviewFrameRate(5);
		p.setPreviewSize(WIDTH, HEIGHT);
		mCamera.setParameters(p);
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// and the callback
		mCamera.setPreviewCallback(pcb);
		
		// go!
		if (DEBUG) Log.d(TAG, "dx: start preview");
		mCamera.startPreview();
		mPreviewRunning = true;   
		return true;
    }
    
    // stop light preview and release the camera
    private void stopPreview() {
    	if (mPreviewRunning) {
			try {
				// no more receiving preview...
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				mCamera.release();
				if (DEBUG) Log.d(TAG, "dx: stopped preview");
			}
			catch (Exception e) {
				//e.printStackTrace();
			}
			mPreviewRunning = false;
    	}
    }

    public void enable(boolean enable) {
    	mEnabled = enable;
        if (DEBUG) Log.d(TAG, "dx: enable(" + enable + ")");
        synchronized (this) {
            if (enable) {
            	enable = startPreview();
            } else {
            	stopPreview();
            }
        }
    }

    private void setLightValue(int value) {
		mListener.cameraLightChanged(value);
    }

}
