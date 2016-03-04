package com.yitouwushui.music;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

/**
 * Created by yitouwushui on 2015/12/23.
 */
public class App extends Application {

    private static final String TAG = "App";

    /**
     * 广播
     */
    public static final String EXTRA_SONG_LIST = "song_list";
    public static final String PLAY_SONG_CURRENT = "play_song_current";
    public static final String EXTRA_SONG = "song";
    public static final String LOAD_MUSIC_LIST
            = "com.yitouwushui.music.action.LOAD_MUSIC_LIST";

    /**
     * 播放广播
     */
    public static final String PLAY_PREVIOUS = "com.yitouwushui.music.ation.PLAY_PREVIOUS";
    public static final String PLAY_NEXT = "com.yitouwushui.music.ation.PLAY_NEXT";
    public static final String PLAY = "com.yitouwushui.music.ation.PLAY";


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");
        startService(new Intent(this, MusicService.class));
    }
}
