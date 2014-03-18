package com.vork.colorpaletteimageview.sample;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
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
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ColorPaletteImageView imageView = (ColorPaletteImageView) rootView.findViewById(R.id.doge_imageview);

            Bitmap bitmap = getBitmapFromAssets("DOGE.png");

            final LinearLayout primaryColor = (LinearLayout) rootView.findViewById(R.id.primaryColor);
            final LinearLayout secondary = (LinearLayout) rootView.findViewById(R.id.secondaryColor);
            final LinearLayout tertiary = (LinearLayout) rootView.findViewById(R.id.tertiaryColor);

            final TextView primaryText = (TextView) rootView.findViewById(R.id.primaryText);
            final TextView secondaryText = (TextView) rootView.findViewById(R.id.secondaryText);

            imageView.addColorPaletteListener(new ColorPaletteListener() {
                @Override
                public void getColorPalette(ColorScheme scheme) {
                    Log.d("Color", "Received color palette");
                    primaryColor.setBackgroundColor(scheme.primaryAccent);
                    secondary.setBackgroundColor(scheme.secondaryAccent);
                    tertiary.setBackgroundColor(scheme.tertiaryAccent);

                    primaryText.setTextColor(scheme.primaryText);
                    secondaryText.setTextColor(scheme.secondaryText);
                }
            });
            imageView.setImageBitmap(bitmap);


            return rootView;
        }

        public Bitmap getBitmapFromAssets(String fileName) {
            AssetManager assetManager = getActivity().getAssets();

            InputStream istr = null;
            try {
                istr = assetManager.open(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return BitmapFactory.decodeStream(istr);
        }
    }
}
