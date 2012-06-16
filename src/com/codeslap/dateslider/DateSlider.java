/*
 * Copyright (C) 2011 Daniel Berndt - Codeus Ltd  -  DateSlider
 * 
 * Class for setting up the dialog and initializing the underlying
 * ScrollLayouts
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

package com.codeslap.dateslider;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public abstract class DateSlider extends Dialog {

    private final OnDateSetListener mOnDateSetListener;
    Calendar mTime;
    TimeZone mTimeZone;
    TextView mTitleText;
    final List<ScrollLayout> mScrollerList = new ArrayList<ScrollLayout>();
    LinearLayout mLayout;

    public DateSlider(Context context, OnDateSetListener l, Calendar calendar) {
        super(context);
        this.mOnDateSetListener = l;
        mTimeZone = calendar.getTimeZone();
        mTime = Calendar.getInstance(mTimeZone);
        mTime.setTimeInMillis(calendar.getTimeInMillis());
    }


    /**
     * Set up the dialog with all the views and their listeners
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            long time = savedInstanceState.getLong("time", mTime.getTimeInMillis());
            mTime.setTimeInMillis(time);
        }

        this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.date_slider);
        this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.dialog_title);

        mTitleText = (TextView) this.findViewById(R.id.dateSliderTitleText);
        mLayout = (LinearLayout) findViewById(R.id.dateSliderMainLayout);

        Button okButton = (Button) findViewById(R.id.dateSliderOkButton);
        okButton.setOnClickListener(mOkButtonClickListener);

        Button cancelButton = (Button) findViewById(R.id.dateSliderCancelButton);
        cancelButton.setOnClickListener(mCancelButtonClickListener);

        arrangeScroller(null);
    }

    /**
     * This method allows to change the displayed time of the slider(s).
     * this can be handy if you need to invoke the dialog several times
     * using OnPrepareDialog.
     *
     * @param calendar the calendar object containing the new time
     */
    public void updateCalendar(Calendar calendar) {
        mTimeZone = calendar.getTimeZone();
        mTime = Calendar.getInstance(mTimeZone);
        mTime.setTimeInMillis(calendar.getTimeInMillis());
    }

    private final android.view.View.OnClickListener mOkButtonClickListener = new android.view.View.OnClickListener() {
        public void onClick(View v) {
            if (mOnDateSetListener != null)
                mOnDateSetListener.onDateSet(DateSlider.this, mTime);
            dismiss();
        }
    };

    private final android.view.View.OnClickListener mCancelButtonClickListener = new android.view.View.OnClickListener() {
        public void onClick(View v) {
            dismiss();
        }
    };

    /**
     * Sets the Scroll listeners for all ScrollLayouts in "mScrollerList"
     */
    void setListeners() {
        for (final ScrollLayout sl : mScrollerList) {
            sl.setOnScrollListener(
                    new ScrollLayout.OnScrollListener() {
                        public void onScroll(long x) {
                            mTime.setTimeInMillis(x);
                            arrangeScroller(sl);
                        }
                    });
        }

    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle savedInstanceState = super.onSaveInstanceState();
        if (savedInstanceState == null) savedInstanceState = new Bundle();
        savedInstanceState.putLong("time", mTime.getTimeInMillis());
        return savedInstanceState;
    }

    void arrangeScroller(ScrollLayout source) {
        setTitle();
        if (source != null) {
            for (ScrollLayout scroller : mScrollerList) {
                if (scroller == source) continue;
                scroller.setTime(mTime.getTimeInMillis(), 0);
            }
        }
    }

    /**
     * This method sets the title of the dialog
     */
    void setTitle() {
        if (mTitleText != null) {
            mTitleText.setText(getContext().getString(R.string.dateSliderTitle) +
                    String.format(": %te. %tB %tY", mTime, mTime, mTime));
        }
    }


    /**
     * Defines the interface which defines the methods of the OnDateSetListener
     */
    public interface OnDateSetListener {
        /**
         * this method is called when a date was selected by the user
         *
         * @param view the caller of the method
         */
        public void onDateSet(DateSlider view, Calendar selectedDate);
    }

    /**
     * This class has the purpose of telling the corresponding scroller, which values make up
     * a single TimeTextView element.
     */
    public abstract class Labeler {

        /**
         * gets called once, when the scroller gets initialised
         *
         * @param time the time in milliseconds
         * @return the TimeObject representing "time"
         */
        public TimeObject getElem(long time) {
            Calendar c = Calendar.getInstance(mTimeZone);
            c.setTimeInMillis(time);
            return timeObjectFromCalendar(c);
        }

        /**
         * returns a new TimeTextView instance, is only called a couple of times in the
         * initialisation process
         *
         * @param context      used to create the view
         * @param isCenterView is true when the view is the central view
         * @return a TimeView instance
         */
        public TimeView createView(Context context, boolean isCenterView) {
            return new TimeView.TimeTextView(context, isCenterView, 25);
        }

        /**
         * This method will be called constantly, whenever new date information is required
         * it receives a timestamps and adds "val" time units to that time and returns it as
         * a TimeObject
         *
         * @param time the time in milliseconds
         * @param val  days to add
         * @return new time object
         */
        public abstract TimeObject add(long time, int val);

        protected abstract TimeObject timeObjectFromCalendar(Calendar c);
    }

    /**
     * Very simple helper class that defines a time unit with a label (text) its start-
     * and end date
     */
    public static class TimeObject {
        public final CharSequence text;
        public final long startTime, endTime;

        public TimeObject(final CharSequence text, final long startTime, final long endTime) {
            this.text = text;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}