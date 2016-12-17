package caching.rm.com.samplecacing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

/**
 * Created by ric on 16/12/2016.
 */

public class CacheStore {
    private static CacheStore INSTANCE = null;
    private HashMap<String, String> cacheMap;
    private HashMap<String, Bitmap> bitmapMap;
    private static final String cacheDir = "cache";
    private static final String CACHE_FILENAME = ".cache";
    private Context context;

    private synchronized static void createInstance(Context context) {
        if(INSTANCE == null) {
            INSTANCE = new CacheStore(context);
        }
    }

    public static CacheStore getInstance(Context context) {
        if(INSTANCE == null) createInstance(context);
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    private CacheStore(Context context) {
        this.context = context;
        cacheMap = new HashMap<String, String>();
        bitmapMap = new HashMap<String, Bitmap>();
        String cachePath = this.context.getFilesDir().getAbsolutePath() + File.separator + cacheDir;
        String extPath = Environment.getExternalStorageDirectory().toString() + File.separator + cacheDir;
        File fullCacheDir = new File(cachePath);
        if(!fullCacheDir.exists()) {
            Log.i("CACHE", "Directory '"+cacheDir +"' doesn't exist");
            cleanCacheStart();
            return;
        }
        try {
            ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(fullCacheDir.toString(), CACHE_FILENAME))));
            cacheMap = (HashMap<String,String>)is.readObject();
            is.close();
        } catch (StreamCorruptedException e) {
            Log.i("CACHE", "Corrupted stream");
            cleanCacheStart();
        } catch (FileNotFoundException e) {
            Log.i("CACHE", "File not found");
            cleanCacheStart();
        } catch (IOException e) {
            Log.i("CACHE", "Input/Output error");
            cleanCacheStart();
        } catch (ClassNotFoundException e) {
            Log.i("CACHE", "Class not found");
            cleanCacheStart();
        }
    }

    private void cleanCacheStart() {
        cacheMap = new HashMap<String, String>();
        String cachePath = this.context.getFilesDir().getAbsolutePath() + File.separator + cacheDir;
        String extPath = Environment.getExternalStorageDirectory().toString() + File.separator + cacheDir;
        File fullCacheDir = new File(cachePath);
        fullCacheDir.mkdirs();
        File noMedia = new File(fullCacheDir.toString(), ".nomedia");
        try {
            noMedia.createNewFile();
            Log.i("CACHE", "Cache created");
        } catch (IOException e) {
            Log.i("CACHE", "Couldn't create .nomedia file");
            e.printStackTrace();
        }
    }

    public void saveCacheFile(String cacheUri, Bitmap image) {
        String cachePath = this.context.getFilesDir().getAbsolutePath() + File.separator + cacheDir;
        String extPath = Environment.getExternalStorageDirectory().toString() + File.separator + cacheDir;
        File fullCacheDir = new File(cachePath);
        String fileLocalName = new SimpleDateFormat("ddMMyyhhmmssSSS").format(new java.util.Date())+".PNG";
        File fileUri = new File(fullCacheDir.toString(), fileLocalName);
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(fileUri);
            image.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            cacheMap.put(cacheUri, fileLocalName);
            Log.i("CACHE", "Saved file "+cacheUri+" (which is now "+fileUri.toString()+") correctly");
            bitmapMap.put(cacheUri, image);
            ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(new File(fullCacheDir.toString(), CACHE_FILENAME))));
            os.writeObject(cacheMap);
            os.close();
        } catch (FileNotFoundException e) {
            Log.i("CACHE", "Error: File "+cacheUri+" was not found!");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("CACHE", "Error: File could not be stuffed!");
            e.printStackTrace();
        }
    }

    public Bitmap getCacheFile(String cacheUri) {
        if(bitmapMap.containsKey(cacheUri)) return (Bitmap)bitmapMap.get(cacheUri);

        if(!cacheMap.containsKey(cacheUri)) return null;
        String fileLocalName = cacheMap.get(cacheUri).toString();
        String cachePath = this.context.getFilesDir().getAbsolutePath() + File.separator + cacheDir;
        String extPath = Environment.getExternalStorageDirectory().toString() + File.separator + cacheDir;
        File fullCacheDir = new File(cachePath);
        File fileUri = new File(fullCacheDir.toString(), fileLocalName);
        if(!fileUri.exists()) return null;

        Log.i("CACHE", "File "+cacheUri+" has been found in the Cache");
        Bitmap bm = BitmapFactory.decodeFile(fileUri.toString());
        bitmapMap.put(cacheUri, bm);
        return bm;
    }

    // @todo: option to place cached directory on mounted storage
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
