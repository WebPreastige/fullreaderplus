/*
Vudroid
Copyright 2010-2011 Pavel Tiunov

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.vudroid.core.multitouch;

import org.vudroid.core.models.ZoomModel;

import android.util.Log;
import android.view.MotionEvent;

public class MultiTouchZoomImpl implements MultiTouchZoom {
    private static final float MAX_VALUE = 3.5f;
    
	private final ZoomModel zoomModel;
    private boolean resetLastPointAfterZoom;
    private float lastZoomDistance;

    public MultiTouchZoomImpl(ZoomModel zoomModel) {
        this.zoomModel = zoomModel;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if ((ev.getAction() & MotionEvent.ACTION_POINTER_DOWN) == MotionEvent.ACTION_POINTER_DOWN) {
            lastZoomDistance = getZoomDistance(ev);
            return true;
        }
        if ((ev.getAction() & MotionEvent.ACTION_POINTER_UP) == MotionEvent.ACTION_POINTER_UP) {
            lastZoomDistance = 0;
            zoomModel.commit();
            resetLastPointAfterZoom = true;
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE && lastZoomDistance != 0) {
            float zoomDistance = getZoomDistance(ev);
            

            float currentValue = zoomModel.getZoom() * zoomDistance / lastZoomDistance;
			if (currentValue > MAX_VALUE) 
            	currentValue = MAX_VALUE;
			
			Log.w("ZOOM", "currentValue: "+currentValue);
            zoomModel.setZoom(currentValue);
            lastZoomDistance = zoomDistance;
            return true;
        }
        return false;
    }

    private float getZoomDistance(MotionEvent ev) {
        return (float) Math.sqrt(Math.pow(ev.getX(0) - ev.getX(1), 2) + Math.pow(ev.getY(0) - ev.getY(1), 2));
    }

    public boolean isResetLastPointAfterZoom() {
        return resetLastPointAfterZoom;
    }

    public void setResetLastPointAfterZoom(boolean resetLastPointAfterZoom) {
        this.resetLastPointAfterZoom = resetLastPointAfterZoom;
    }
}
