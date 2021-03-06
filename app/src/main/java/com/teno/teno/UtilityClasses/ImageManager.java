package com.teno.teno.UtilityClasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageManager {

    private static final String TAG = "ImageManager";

    public static Bitmap getBitmap(String imgUrl) {
        File imageFile = new File(imgUrl);
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Bitmap:FileNotFounfexception" + e.getMessage());
        } finally {
            try {
                fis.close();
            } catch (IOException | NullPointerException e) {
                Log.e(TAG, "Bitmap:FileNotFounfexception" + e.getMessage());
            }
        }
        return bitmap;
    }

    public static byte[] getBytesFromBitmap(Bitmap bm, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Log.d(TAG, "getBytesFromBitmap: " + stream.size());
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }
}
