package com.vork.colorpaletteimageview.sample;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vork.colorpaletteimageview.ColorPaletteImageView;
import com.vork.colorpaletteimageview.ColorPaletteListener;
import com.vork.colorpaletteimageview.Quantizer.ColorScheme;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends ActionBarActivity {
    private static final int NUM_PAGES = 3;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPager = (ViewPager) findViewById(R.id.pager);
        String[] filename = new String[3];
        filename[0] = "doge2.jpg";
        filename[1] = "doge3.jpg";
        filename[2] = "doge4.jpg";

        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), filename);
        mPager.setAdapter(pagerAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private String[] mFilenames;

        public ScreenSlidePagerAdapter(FragmentManager fm, String[] filenames) {
            super(fm);
            mFilenames = filenames;
        }

        @Override
        public Fragment getItem(int position) {
            return new DogeFragment(mFilenames[position]);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DogeFragment extends Fragment {
        private String mFilename;
        private ColorPaletteImageView mImageView;

        public DogeFragment(String filename) {
            mFilename = filename;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mImageView = (ColorPaletteImageView) rootView.findViewById(R.id.doge_imageview);

            Bitmap bitmap = getBitmapFromAssets(mFilename);

            final LinearLayout primary = (LinearLayout) rootView.findViewById(R.id.primaryColor);
            final LinearLayout secondary = (LinearLayout) rootView.findViewById(R.id.secondaryColor);
            final LinearLayout tertiary = (LinearLayout) rootView.findViewById(R.id.tertiaryColor);

            final TextView primaryText = (TextView) rootView.findViewById(R.id.primaryText);
            final TextView secondaryText = (TextView) rootView.findViewById(R.id.secondaryText);
            final TextView primaryTextValue = (TextView) rootView.findViewById(R.id.primaryTextValue);
            final TextView secondaryTextValue = (TextView) rootView.findViewById(R.id.secondaryTextValue);

            if(bitmap != null) {
                mImageView.setImageBitmap(bitmap);
                mImageView.setColorPaletteListener(new ColorPaletteListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void getColorPalette(ColorScheme scheme) {
                        Log.d("Color", "Received color palette");
                        primary.setBackgroundColor(scheme.primaryAccent);
                        secondary.setBackgroundColor(scheme.secondaryAccent);
                        tertiary.setBackgroundColor(scheme.tertiaryAccent);

                        primaryText.setTextColor(scheme.primaryText);
                        secondaryText.setTextColor(scheme.secondaryText);
                        primaryTextValue.setTextColor(scheme.primaryText);
                        secondaryTextValue.setTextColor(scheme.secondaryText);

                        primaryTextValue.setText("#" + Integer.toHexString(
                                scheme.primaryAccent).toUpperCase().substring(2));

                        secondaryTextValue.setText("#" + Integer.toHexString(
                                scheme.secondaryAccent).toUpperCase().substring(2));
                    }
                });
            }

            return rootView;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mImageView.removeColorPaletteListener();
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        public Bitmap getBitmapFromAssets(String fileName) {
            AssetManager assetManager = getActivity().getAssets();

            InputStream istr = null;
            try {
                istr = assetManager.open(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(istr, null, options);
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            int width;
            int height;
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
                width = display.getWidth();
                height = display.getHeight();
            } else {
                Point size = new Point();
                display.getSize(size);
                width = size.x;
                height = size.y;
            }
            width /= 2;
            height /= 2;

            options.inSampleSize = calculateInSampleSize(options, width, height);

            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeStream(istr, null, options);
        }

        public static int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }
    }
}
