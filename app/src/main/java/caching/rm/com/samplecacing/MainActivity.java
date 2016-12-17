package caching.rm.com.samplecacing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static java.util.Collections.copy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CacheStore cache;
        cache = CacheStore.getInstance(this.getApplicationContext());
        final String imgUrl = "http://www.pixelstalk.net/wp-content/uploads/2016/05/Desktop-Darth-Vader-Wallpapers.jpg";
        final ImageView myImage = (ImageView) findViewById(R.id.imgdarth);
        Bitmap cachedBitmap = cache.getCacheFile(imgUrl);

        if (cachedBitmap == null) {

            Picasso.with(this).load(imgUrl).into(myImage, new Callback() {
                @Override
                public void onSuccess() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            //Set it in the ImageView
                            //myImage.setImageBitmap(innerBitmap);

                            Bitmap bitmap = ((BitmapDrawable)myImage.getDrawable()).getBitmap();
                            cache.saveCacheFile(imgUrl, bitmap);

                        }
                    }, 100);
                }

                @Override
                public void onError() {

                }
            });

        }else{

            myImage.setImageBitmap(cachedBitmap);

        }


    }

}
