package com.android.imusic.music.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.android.imusic.R;
import com.music.player.lib.view.MusicLrcView;

/**
 * TinyHung@Outlook.com
 * 2019/4/4
 */

public class TestActivity extends AppCompatActivity {

    private MusicLrcView mLrcView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mLrcView = (MusicLrcView) findViewById(R.id.lrc_view);
    }

    public void startParser(View view) {
        String content="" +
                "[ti:只要平凡]\n" +
                "[ar:张杰&张碧晨]\n" +
                "[00:00.73]张杰、张碧晨 - 只要平凡\n" +
                "[00:02.94]作词：格格\n" +
                "[00:03.52]作曲：黄超\n" +
                "[00:04.69]也许很远或是昨天\n" +
                "[00:08.73]在这里或在对岸\n" +
                "[00:12.87]长路辗转离合悲欢\n" +
                "[00:17.02]人聚又人散\n" +
                "[00:21.05]放过对错才知答案\n" +
                "[00:25.27]活着的勇敢\n" +
                "[00:29.32]没有神的光环\n" +
                "[00:33.28]你我生而平凡\n" +
                "[00:37.63]在心碎中认清遗憾\n" +
                "[00:41.62]生命漫长也短暂\n" +
                "[00:45.86]跳动心脏长出藤蔓\n" +
                "[00:49.81]愿为险而战\n" +
                "[00:54.24]跌入灰暗坠入深渊\n" +
                "[00:58.07]沾满泥土的脸\n" +
                "[01:02.31]没有神的光环\n" +
                "[01:06.25]握紧手中的平凡\n" +
                "[01:10.76]此心此生无憾\n" +
                "[01:14.64]生命的火已点燃\n" +
                "[01:21.21][01:21.21][01:21.21][01:21.21]有一天也许会走远\n" +
                "[01:24.94]也许还能再相见\n" +
                "[01:29.33]无论在人群在天边\n" +
                "[01:33.20]让我再看清你的脸\n" +
                "[01:37.43]任泪水铺满了双眼\n" +
                "[01:41.52]虽无言泪满面\n" +
                "[01:45.85]不要神的光环\n" +
                "[01:49.84]只要你的平凡\n" +
                "[02:39.75]在心碎中认清遗憾\n" +
                "[02:43.94]生命漫长也短暂\n" +
                "[02:47.96]跳动心脏长出藤蔓\n" +
                "[02:51.85]愿为险而战\n" +
                "[02:56.31]跌入灰暗坠入深渊\n" +
                "[03:00.33]沾满泥土的脸\n" +
                "[03:04.29]没有神的光环\n" +
                "[03:08.28]握紧手中的平凡\n" +
                "[03:12.89]有一天也许会走远\n" +
                "[03:16.68]也许还能再相见\n" +
                "[03:21.22]无论在人群在天边\n" +
                "[03:24.91]让我再看清你的脸\n" +
                "[03:29.30]任泪水铺满了双眼\n" +
                "[03:33.36]虽无言泪满面\n" +
                "[03:37.51]不要神的光环\n" +
                "[03:41.62]只要你的平凡\n" +
                "[03:45.75]此心此生无憾\n" +
                "[03:49.86]生命的火已点燃\n";
        mLrcView.setLrcRow(content);
    }
}
