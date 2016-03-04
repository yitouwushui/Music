package com.yitouwushui.music;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MusicService musicService;
    private List<Song> songList;
    private ListView listView;
    private MusicAdapter adapter;
    private ProgressBar progressBar;
    private TextView textView_start;
    private TextView textView_end;
    private TextView textView_song;
    private SeekBar seekBar;
    private Button button_before;
    private Button button_next;
    private Button button_song;
    private SeekBarThread seekBarThread;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        Log.d(TAG, "onCreate");

    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView_start = (TextView) findViewById(R.id.textView_start);
        textView_end = (TextView) findViewById(R.id.textView_end);
        textView_song = (TextView) findViewById(R.id.textView_song);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        button_before = (Button) findViewById(R.id.button_before);
        button_next = (Button) findViewById(R.id.button_next);
        button_song = (Button) findViewById(R.id.button_song);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(App.LOAD_MUSIC_LIST);
        intentFilter.addAction(App.PLAY_SONG_CURRENT);

        // 从本地广播管理器中注册一个广播接收器
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                receiver, intentFilter);

        // 绑定服务是异步执行
        bindService(new Intent(this, MusicService.class),
                conn,
                BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        // 注销
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(receiver);

        unbindService(conn);

    }

    @Override
    protected void onResume() {
        super.onResume();
        seekBarThread = new SeekBarThread();
        seekBarThread.start();
        setButtonText();
        Song currentSong = MusicService.hashMap.get("currentSong");
        if (currentSong != null) {
            setTextView(currentSong);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        seekBarThread.isRunning = false;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case App.LOAD_MUSIC_LIST:
                    // 加载音乐
                    loadSongList(intent);
                    break;
                case App.PLAY_SONG_CURRENT:
                    // 修改当前音乐信息
                    setCurrentText(intent);
                    break;
            }
        }
    };

    private void setCurrentText(Intent intent) {

        Song currentSong = (Song) intent.getSerializableExtra(App.EXTRA_SONG);
        setTextView(currentSong);
        setButtonText();
        MusicService.hashMap.put("currentSong", currentSong);
    }

    private void setTextView(Song currentSong) {
        textView_song.setText(currentSong.getTitle());
        textView_end.setText(
                TimeUtil.formatDuration(currentSong.getDuration()));
        seekBar.setMax((int) currentSong.getDuration());
    }

    private void loadSongList(Intent intent) {
        if (songList == null) {
            Bundle bundle = intent.getExtras();
            songList = (ArrayList<Song>) bundle.get(App.EXTRA_SONG_LIST);
            Log.d(TAG, "从广播取的");
            showSongList();
        }
    }

    /**
     * 服务连接
     */
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");

            // 从服务中获得本地绑定
            MusicService.LocalBinder localBinder = (MusicService.LocalBinder) service;
            musicService = localBinder.getService();

            // 可以调用服务
            if (musicService.isInit()) {

                if (songList == null) {
                    songList = musicService.getSongList();
                    Log.d(TAG, "获得音乐列表");

                    // 在ListView 中显示
                    showSongList();
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    /**
     * 显示音乐列表
     */
    private void showSongList() {
        progressBar.setVisibility(View.GONE);
        adapter = new MusicAdapter(this, songList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new ItemClickListener());
    }


    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(
                AdapterView<?> parent, View view,
                int position, long id) {

            musicService.play(position);
        }
    }

    /**
     * 播放，
     * @param position
     */
    public void play(int position) {
        musicService.play(position);
    }

    /**
     * 播放，暂停
     *
     * @param v
     */
    public void play(View v) {

        if (!musicService.isRunning && isFirst) {
            musicService.current = 0;
            isFirst = false;

            musicService.play(musicService.current);
        } else {
            musicService.play();
        }

        setButtonText();
    }

    /**
     * 播放上一首
     *
     * @param v
     */
    public void playNext(View v) {
        try {
            musicService.playNext();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setButtonText();
    }

    /**
     * 播放上一首
     *
     * @param v
     */
    public void playPrevious(View v) {

        try {
            musicService.playPrevious();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setButtonText();
    }

    /**
     * 修改博播放暂停
     */
    public void setButtonText() {
        if (MusicService.isRunning) {
            button_song.setText("暂停");
        } else {
            button_song.setText("播放");
        }
    }

    class SeekBarThread extends Thread {
        volatile boolean isRunning = true;

        @Override
        public void run() {
            super.run();

            while (isRunning) {
                // 已经绑定服务
                if (musicService != null) {
                    // 读取服务中的播放进度
                    final int progress = musicService.getCurrentPosition();

                    // 从子线程更新UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(progress);
                            textView_start.setText(TimeUtil.formatDuration(progress));
                        }
                    });
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }

}
