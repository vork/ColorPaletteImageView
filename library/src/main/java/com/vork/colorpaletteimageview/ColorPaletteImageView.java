package com.vork.colorpaletteimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.vork.colorpaletteimageview.Quantizer.ColorScheme;
import com.vork.colorpaletteimageview.Quantizer.DominantColorCalculator;

public class ColorPaletteImageView extends ImageView {
    private ColorPaletteListener mListener;
    private ColorPaletteGeneratorTask mGeneratorTask;
    private boolean mShouldUpdate = false;
    private boolean mIsUpdating = false;

    public ColorPaletteImageView(Context context) {
        super(context);
    }

    public ColorPaletteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPaletteImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        if (mShouldUpdate) {
            Bitmap b = ((BitmapDrawable) drawable).getBitmap();
            Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

            if (!mIsUpdating) {
                mGeneratorTask = new ColorPaletteGeneratorTask(bitmap, mListener);
                mGeneratorTask.execute();
            }
        }
    }

    public void setColorPaletteListener(ColorPaletteListener listener) {
        if(mGeneratorTask != null && mGeneratorTask.getStatus() != AsyncTask.Status.FINISHED) {
            mGeneratorTask.cancel(true);
            mGeneratorTask = null;
        }
        mListener = listener;
        mShouldUpdate = true;
        postInvalidate();
    }

    public void removeColorPaletteListener() {
        if(mGeneratorTask != null && mGeneratorTask.getStatus() != AsyncTask.Status.FINISHED) {
            mGeneratorTask.cancel(true);
            mGeneratorTask = null;
            mListener = null;
        }
    }

    class ColorPaletteGeneratorTask extends AsyncTask<Void, Void, ColorScheme> {
        private final Bitmap mBitmap;
        private final ColorPaletteListener mListener;

        public ColorPaletteGeneratorTask(Bitmap bitmap, ColorPaletteListener listener) {
            mBitmap = bitmap;
            mListener = listener;
        }

        @Override
        protected void onPreExecute() {
            mIsUpdating = true;
        }

        @Override
        protected ColorScheme doInBackground(Void... params) {
            DominantColorCalculator calculator = new DominantColorCalculator(mBitmap);

            return calculator.getColorScheme();
        }

        @Override
        protected void onPostExecute(ColorScheme scheme) {
            mIsUpdating = false;
            if (mListener != null) {
                mListener.getColorPalette(scheme);
            }
        }
    }
}
