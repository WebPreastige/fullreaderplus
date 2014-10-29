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
package org.vudroid.core.views;

import org.geometerplus.android.fbreader.IConstants;
import org.vudroid.core.models.ZoomModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.fullreader.R;

public class ZoomRoll extends View
{
    private Bitmap left;
    private Bitmap right;
    private Bitmap center;
    private Bitmap serifs;
	private Bitmap sticker;
    private VelocityTracker velocityTracker;
    private Scroller scroller;
    private float lastX;
    private static final int MAX_VALUE = 1000;
    private final ZoomModel zoomModel;
    private static final float MULTIPLIER = 400.0f;

    public ZoomRoll(Context context, ZoomModel zoomModel)
    {
        super(context);
        this.zoomModel = zoomModel;
        
        
		int theme = PreferenceManager.getDefaultSharedPreferences(context).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        switch(theme){
        case IConstants.THEME_REDTREE:
            left = BitmapFactory.decodeResource(context.getResources(), R.drawable.theme_redtree_zoom_minus);
            right = BitmapFactory.decodeResource(context.getResources(), R.drawable.theme_redtree_zoom_plus);
            center = BitmapFactory.decodeResource(context.getResources(), R.drawable.theme_redtree_vu_center);
        	break;
        case IConstants.THEME_LAMINAT:
            left = BitmapFactory.decodeResource(context.getResources(), R.drawable.theme_laminat_zoom_minus);
            right = BitmapFactory.decodeResource(context.getResources(), R.drawable.theme_laminat_zoom_plus);
            center = BitmapFactory.decodeResource(context.getResources(), R.drawable.theme_laminat_vu_center);
        	break;
        case IConstants.THEME_MYBLACK:
            left = BitmapFactory.decodeResource(context.getResources(), R.drawable.vu_left_theme_black);
            right = BitmapFactory.decodeResource(context.getResources(), R.drawable.vu_right_theme_black);
            center = BitmapFactory.decodeResource(context.getResources(), R.drawable.theme_black_vu_center);
        	break;
        default:
            left = BitmapFactory.decodeResource(context.getResources(), R.drawable.vu_left);
            right = BitmapFactory.decodeResource(context.getResources(), R.drawable.vu_right);
            center = BitmapFactory.decodeResource(context.getResources(), R.drawable.vu_center);
            break;
        }

        serifs = BitmapFactory.decodeResource(context.getResources(), R.drawable.vu_serifs);
        sticker = BitmapFactory.decodeResource(context.getResources(), R.drawable.vu_but_in_line);
        
        

        scroller = new Scroller(context);
        
        setBackgroundColor(Color.TRANSPARENT);

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), Math.max(left.getHeight(), right.getHeight())*2);
    }

    @Override
    public void draw(Canvas canvas)
    {
        super.draw(canvas);
        final Paint paint = new Paint();
        
//        canvas.drawColor(Color.DKGRAY);
        canvas.drawBitmap(center, 
        		new Rect(0, 0, center.getWidth(), center.getHeight()), 
        		new Rect(0, 0, getWidth(), getHeight()), paint);
        
        
        float currentOffset =  getCurrentValue() /MAX_VALUE *(getWidth() - 2*3*left.getWidth() - sticker.getWidth());
        
        
        
       // while (currentOffset < getWidth())
        {
//            canvas.drawBitmap(serifs, currentOffset, (getHeight() - serifs.getHeight()) / 2.0f, paint);
            canvas.drawBitmap(
            		serifs, 
		        	new Rect((int) 0, 0, serifs.getWidth(), serifs.getHeight()),
		        	new Rect(
				        	0 + left.getWidth()*3, 
				        	getHeight()/3, 
							getWidth() - left.getWidth()*3, 
							getHeight()-getHeight()/3),
					paint);
        	
        	//currentOffset += serifs.getWidth();
        }
        canvas.drawBitmap(left,  left.getWidth(), getHeight()/2, paint);
        canvas.drawBitmap(right, getWidth() + right.getWidth()/2 - right.getWidth()*2, getHeight()/2 - right.getHeight()/2, paint);
        
        canvas.drawBitmap(sticker, left.getWidth()*3  + currentOffset, getHeight()/2 - sticker.getHeight()/2, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        super.onTouchEvent(ev);

        if (velocityTracker == null)
        {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished())
                {
                    scroller.abortAnimation();
                    zoomModel.commit();
                }
                lastX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                setCurrentValue(getCurrentValue() + (ev.getX() - lastX));
                lastX = ev.getX();
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                //scroller.fling((int) getCurrentValue(), 0, (int) -velocityTracker.getXVelocity(), 0, 0, MAX_VALUE, 0, 0);
                velocityTracker.recycle();
                velocityTracker = null;
                if (!scroller.computeScrollOffset())
                {
                    zoomModel.commit();
                }
                break;
        }
        invalidate();
        return true;
    }

    @Override
    public void computeScroll()
    {
        if (scroller.computeScrollOffset())
        {
            setCurrentValue(scroller.getCurrX());
            invalidate();
        }
        else
        {
            zoomModel.commit();
        }
    }

    public float getCurrentValue()
    {
        return (zoomModel.getZoom() - 1.0f) * MULTIPLIER;
    }

    public void setCurrentValue(float currentValue)
    {
        if (currentValue < 0.0) currentValue = 0.0f;
        if (currentValue > MAX_VALUE) currentValue = MAX_VALUE;
        final float zoom = 1.0f + currentValue / MULTIPLIER;
        zoomModel.setZoom(zoom);
    }
}
