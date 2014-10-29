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

import org.vudroid.core.events.BringUpZoomControlsListener;
import org.vudroid.core.models.ZoomModel;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

public class PageViewZoomControls extends LinearLayout implements BringUpZoomControlsListener
{
	private SherlockActivity mActivity;
    public PageViewZoomControls(Context context, final ZoomModel zoomModel)
    {
        super(context);
        show();
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.BOTTOM);
        addView(new ZoomRoll(context, zoomModel));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return false;
    }

    public void toggleZoomControls()
    {
        if (getVisibility() == View.VISIBLE)
        {
            hide();
            mActivity.getSupportActionBar().hide();
        }
        else
        {
            show();
            mActivity.getSupportActionBar().show();
        }
    }

    public void show()
    {
        fade(View.VISIBLE, getWidth(), 0.0f);
    }

    public void hide()
    {
        fade(View.GONE, 0.0f, getWidth());
    }

    private void fade(int visibility, float startDelta, float endDelta)
    {
        Animation anim = new TranslateAnimation(0,0, startDelta, endDelta);
        anim.setDuration(500);
        startAnimation(anim);
        setVisibility(visibility);
    }
    
    public void setActivity(SherlockActivity activity){
    	mActivity = activity;
    }
}
