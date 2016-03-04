package com.yitouwushui.music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore.Audio.Media;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MusicService extends Service {

    private static final String TAG = "MusicService";

    public static HashMap<String, Song> hashMap = new HashMap();

    /**
     * 音乐播放，内置子线程
     */
    MediaPlayer mediaPlayer;

    /**
     * 音乐列表
     */
    ArrayList<Song> songList = new ArrayList<>();

    /**
     * 音乐列表是否初始化完成
     */
    private boolean isInit = false;

    /**
     * 是否正在播放
     */
    public static boolean isRunning = false;

    /**
     * 当前的曲目
     */
    public int current;

    public MusicService() {
    }

    Bitmap bitmap;

    Notification notif;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    // 如果在播放状态，才自动播放下一曲
                    if (isRunning) {
                        // 自动播放下一曲
                        playNext();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tx);

        Log.d(TAG, "onCreate2");
        // 启动子线程,加载音乐列表
        new Thread() {
            @Override
            public void run() {
                super.run();

                // 加载
                Cursor cursor = getContentResolver().query(
                        Media.EXTERNAL_CONTENT_URI,
                        new String[]{Media._ID, Media.DATA, Media.TITLE,
                                Media.ARTIST, Media.DURATION, Media.ALBUM},
                        "is_music != ?",
                        new String[]{"0"},
                        null);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String data = cursor.getString(1);
                    String time = cursor.getString(2);
                    String artist = cursor.getString(3);
                    long duration = cursor.getLong(4);
                    String album = cursor.getString(5);

                    songList.add(new Song(id, data, time, artist, duration, album));
                }
                cursor.close();

                for (int i = 0; i < songList.size(); i++) {
                    Log.d("MusicService", songList.get(i).toString());
                }

                // 将songList 打包存入意图
                Intent intent = new Intent(App.LOAD_MUSIC_LIST);
                Bundle bundle = new Bundle();
                bundle.putSerializable(App.EXTRA_SONG_LIST, songList);
                intent.putExtras(bundle);

                // 初始化的标记
                isInit = true;
                // 广播
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(intent);

            }
        }.start();

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(App.PLAY);
        filter.addAction(App.PLAY_NEXT);
        filter.addAction(App.PLAY_PREVIOUS);

        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);

        if (mediaPlayer != null) {
            // 释放
            mediaPlayer.release();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {

        return new LocalBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return true;
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }

    }

    /**
     * 音乐列表是否初始化完成
     *
     * @return
     */
    public boolean isInit() {
        return isInit;
    }

    /**
     * 获得音乐列表
     *
     * @return
     */
    public ArrayList<Song> getSongList() {
        return songList;
    }

    /**
     * 播放
     *
     * @param position
     */
    public void play(int position) {

        current = position;

        // 重置，回到空闲状态
        mediaPlayer.reset();
        try {
            // 设置数据源
            mediaPlayer.setDataSource(songList.get(position).data);

            // 加载解码器的相关数据
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //播放
        mediaPlayer.start();

        isRunning = true;

        // 发广播
        Song song = songList.get(position);
        Intent intent = new Intent(App.PLAY_SONG_CURRENT);
        intent.putExtra(App.EXTRA_SONG, song);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // 发通知
//        sendNotification(song);

        sendNotificationCompat(song);
    }

    private void sendNotificationCompat(Song song) {
        notif = new NotificationCompat.Builder(this)
                .setContentTitle(song.title)
                .setContentText(song.artist)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setColor(Color.argb(100, 255, 0, 0))
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap))
//                .setStyle(new NotificationCompat.BigTextStyle()
//                        .setBigContentTitle("标题")
//                        .setSummaryText("摘要")
//                        .bigText("this is text this is text"))
                .setContentIntent(PendingIntent.getActivity(
                        this,
                        2,
                        new Intent(this, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(
                        android.R.drawable.ic_media_previous,
                        "上一首",
                        PendingIntent.getBroadcast(this, 1, new Intent(App.PLAY_PREVIOUS), PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(
                        android.R.drawable.ic_media_play,
                        isRunning ? "暂停" : "播放",
                        PendingIntent.getBroadcast(this, 1, new Intent(App.PLAY), PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(
                        android.R.drawable.ic_media_next,
                        "下一首",
                        PendingIntent.getBroadcast(this, 1, new Intent(App.PLAY_NEXT), PendingIntent.FLAG_UPDATE_CURRENT))
                .setLargeIcon(bitmap)
                .build();

        NotificationManagerCompat.from(getApplicationContext())
                .notify(1, notif);
    }

    /**
     * 发通知
     *
     * @param song
     */
    private void sendNotification(Song song) {
        // 发通知
        Notification nof = new Notification.Builder(getApplicationContext())
                .setContentTitle(song.title)
                .setContentText(song.artist)
                .setSmallIcon(android.R.drawable.ic_media_play)
                        // 通知栏不能被消除
                .setOngoing(true)
                .build();

        // 通知提示声音和呼吸灯
        nof.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;
//        nof.sound = Uri.fromFile();
//        nof.color
//        nof.ledARGB

        // 通知次数
//        nof.flags = Notification.FLAG_ONLY_ALERT_ONCE;

        nof.contentIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager nm = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);

        // 发通知
//        nm.notify(1, nof);

        // 将活动运行到前台
        startForeground(1, nof);
    }

    /**
     * 暂停，播放
     */
    public void play() {
        if (mediaPlayer.isPlaying()) {
            // 暂停
            mediaPlayer.pause();
            isRunning = false;
        } else {
            mediaPlayer.start();
            isRunning = true;
        }
    }

    /**
     * 播放下一曲
     *
     * @throws IOException
     */
    public void playNext() throws IOException {
        if (current == songList.size() - 1) {
            current = 0;
        } else {
            current++;
        }

        play(current);
    }

    /**
     * 播放上一首
     *
     * @throws IOException
     */
    public void playPrevious() throws IOException {
        if (current == 0) {
            current = songList.size() - 1;
        } else {
            current--;
        }
        play(current);
    }

    /**
     * 获得当前播放进度
     *
     * @return
     */
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 修改进度
     *
     * @param progress 进度
     */
    public void seekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case App.PLAY:
                    play();
                    break;
                case App.PLAY_NEXT:
                    try {
                        playNext();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case App.PLAY_PREVIOUS:
                    try {
                        playPrevious();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

}
