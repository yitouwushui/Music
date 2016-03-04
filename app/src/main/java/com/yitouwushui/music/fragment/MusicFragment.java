package com.yitouwushui.music.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yitouwushui.music.MainActivity;
import com.yitouwushui.music.MusicAdapter;
import com.yitouwushui.music.MusicService;
import com.yitouwushui.music.R;
import com.yitouwushui.music.Song;

import java.util.ArrayList;

/**
 * Created by yitouwushui on 2016/1/13.
 */
public class MusicFragment extends Fragment {

    ListView listView;
    MusicAdapter adapter;
    ArrayList<Song> songList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        listView = (ListView) view.findViewById(R.id.listView_music);
        adapter = new MusicAdapter(getActivity(), songList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) getActivity()).play(position);
            }
        });


        return view;
    }

    public void loadMusicList(ArrayList<Song> data) {
        for (int i = 0; i < songList.size(); i++) {
            songList.add(songList.get(i));
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
