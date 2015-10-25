package com.gimbal.hello_gimbal_android;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tor on 10/24/15.
 */
public class MusicUtil {

    private static final String TAG = "MusicUtil";

    public static List<Mp3Info> getMusicList(final Context context) {
        List<Mp3Info> mp3Infos = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    Mp3Info mp3Info = new Mp3Info();
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    Log.v(TAG, "music: " + url + File.pathSeparator + title);
                    int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                    if (isMusic != 0 && isMp3Type(url)) {
                        mp3Info.setId(id);
                        mp3Info.setTitle(title);
                        mp3Info.setUrl(url);
                        mp3Infos.add(mp3Info);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mp3Infos;
    }

    public static boolean isMp3Type(String url) {
        int lastIndex = url.lastIndexOf(".");
        String suffix = url.substring(lastIndex, url.length());
        return (suffix.equalsIgnoreCase(".mp3"));
    }

    public static class Mp3Info {
        long id;
        String url;
        String title;

        Mp3Info() {
        }

        long getId() {
            return id;
        }

        void setId(long id) {
            this.id = id;
        }

        String getUrl() {
            return url;
        }

        void setUrl(String url) {
            this.url = url;
        }

        String getTitle() {
            return title;
        }

        void setTitle(String title) {
            this.title = title;
        }

        public String getPath() {
            return this.url + File.pathSeparator + this.title;
        }
    }

}
