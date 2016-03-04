package com.yitouwushui.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by yitouwushui on 2015/12/23.
 */
public class MusicAdapter extends BaseAdapter {

    private Context context;
    private List<Song> songList;
    private LayoutInflater inflater;

    public MusicAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        ImageButtonListener listener;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new Holder(convertView);
            listener = new ImageButtonListener();
            holder.imageButton.setOnClickListener(listener);
            convertView.setTag(holder);
            holder.imageButton.setTag(listener);
        } else {
            holder = (Holder) convertView.getTag();
            listener = (ImageButtonListener) holder.imageButton.getTag();
        }

        listener.setPosition(position);
        Song song = songList.get(position);
        // 绑定数据
        holder.bindDate(song);

        return convertView;
    }

    static class Holder {
        TextView textView_title;
        TextView textView_artist;
        TextView textView_time;
        ImageButton imageButton;

        public Holder(View v) {
            imageButton = (ImageButton) v.findViewById(R.id.imageButton);
            textView_title = (TextView) v.findViewById(R.id.textView_title);
            textView_artist = (TextView) v.findViewById(R.id.textView_artist);
            textView_time = (TextView) v.findViewById(R.id.textView_time);

        }

        public void bindDate(Song song) {
            textView_title.setText(song.getTitle());
            textView_artist.setText(song.getArtist());
            imageButton.setImageResource(R.drawable.ic_more_vert_grey600_16dp);

            textView_time.setText(TimeUtil.formatDuration(song.getDuration()));
        }
    }

    private class ImageButtonListener implements View.OnClickListener {

        private int position;

        @Override
        public void onClick(View v) {

        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
}
