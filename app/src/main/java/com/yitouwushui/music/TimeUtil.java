package com.yitouwushui.music;

/**
 * Created by yitouwushui on 2015/12/24.
 */
public class TimeUtil {

    /**
     * 歌曲时间格式化
     *
     * @param duration
     * @return
     */
    public static String formatDuration(long duration) {
        long t = duration / 1000;
        int m = (int) (t / 60);
        int s = (int) (t % 60);
        return String.format("%d:%02d", m, s);
    }
}
