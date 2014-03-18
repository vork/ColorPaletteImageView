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

import java.util.ArrayList;

public class ColorPaletteImageView extends ImageView {
    ArrayList<ColorPaletteListener> mListener = new ArrayList<ColorPaletteListener>();

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
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

        new ColorPaletteGeneratorTask(bitmap, mListener);
    }

    public void addColorPaletteListener(ColorPaletteListener listener) {
        mListener.add(listener);
    }

    public void removeColorPaletteListener(ColorPaletteListener listener) {
        mListener.remove(listener);
    }

    class ColorPaletteGeneratorTask extends AsyncTask<Void, Void, ColorScheme> {
        private final Bitmap mBitmap;
        private final ArrayList<ColorPaletteListener> mListeners;

        public ColorPaletteGeneratorTask(Bitmap bitmap, ArrayList<ColorPaletteListener> listeners) {
            mBitmap = bitmap;
            mListeners = listeners;
        }

        @Override
        protected ColorScheme doInBackground(Void... params) {
            DominantColorCalculator calculator = new DominantColorCalculator(mBitmap);

            return calculator.getColorScheme();
        }

        @Override
        protected void onPostExecute(ColorScheme scheme) {
            for (ColorPaletteListener listener : mListener) {
                listener.getColorPalette(scheme);
            }
        }
    }
}
