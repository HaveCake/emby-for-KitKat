package org.jellyfin.emby.kitkat;

import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;

import org.jellyfin.emby.kitkat.model.Movie;

/**
 * Leanback 海报墙首页 Fragment。
 * <p>
 * 继承 {@link BrowseFragment}，使用假数据（Mock Data）展示几排横向滚动的
 * 电影海报卡片，验证 Leanback UI 在 Android 4.4 电视盒子上的显示效果。
 */
public class HomeFragment extends BrowseFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupUIElements();
        loadRows();
    }

    /**
     * 设置 BrowseFragment 的基本 UI 元素：标题、颜色等。
     */
    private void setupUIElements() {
        setTitle("Emby 媒体库");
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
    }

    /**
     * 加载假数据行（Mock Data）到 BrowseFragment。
     * <p>
     * 使用公开的 picsum.photos 占位图片服务作为电影封面。
     */
    private void loadRows() {
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        // ── 第 1 行：热门推荐 ──
        ArrayObjectAdapter row1Adapter = new ArrayObjectAdapter(cardPresenter);
        row1Adapter.add(new Movie("银翼杀手 2049", "科幻 / 剧情",
                "https://picsum.photos/seed/blade/313/176"));
        row1Adapter.add(new Movie("星际穿越", "科幻 / 冒险",
                "https://picsum.photos/seed/inter/313/176"));
        row1Adapter.add(new Movie("盗梦空间", "科幻 / 动作",
                "https://picsum.photos/seed/incep/313/176"));
        row1Adapter.add(new Movie("流浪地球", "科幻 / 灾难",
                "https://picsum.photos/seed/earth/313/176"));
        row1Adapter.add(new Movie("阿凡达", "科幻 / 冒险",
                "https://picsum.photos/seed/avatar/313/176"));
        HeaderItem header1 = new HeaderItem(0, "热门推荐");
        rowsAdapter.add(new ListRow(header1, row1Adapter));

        // ── 第 2 行：最近添加 ──
        ArrayObjectAdapter row2Adapter = new ArrayObjectAdapter(cardPresenter);
        row2Adapter.add(new Movie("肖申克的救赎", "剧情 / 犯罪",
                "https://picsum.photos/seed/shawshank/313/176"));
        row2Adapter.add(new Movie("教父", "剧情 / 犯罪",
                "https://picsum.photos/seed/godfather/313/176"));
        row2Adapter.add(new Movie("蝙蝠侠：黑暗骑士", "动作 / 犯罪",
                "https://picsum.photos/seed/batman/313/176"));
        row2Adapter.add(new Movie("黑客帝国", "科幻 / 动作",
                "https://picsum.photos/seed/matrix/313/176"));
        row2Adapter.add(new Movie("千与千寻", "动画 / 奇幻",
                "https://picsum.photos/seed/spirited/313/176"));
        HeaderItem header2 = new HeaderItem(1, "最近添加");
        rowsAdapter.add(new ListRow(header2, row2Adapter));

        // ── 第 3 行：经典动作 ──
        ArrayObjectAdapter row3Adapter = new ArrayObjectAdapter(cardPresenter);
        row3Adapter.add(new Movie("速度与激情 7", "动作 / 犯罪",
                "https://picsum.photos/seed/fast7/313/176"));
        row3Adapter.add(new Movie("碟中谍 6", "动作 / 冒险",
                "https://picsum.photos/seed/mi6/313/176"));
        row3Adapter.add(new Movie("复仇者联盟 4", "动作 / 科幻",
                "https://picsum.photos/seed/avengers/313/176"));
        row3Adapter.add(new Movie("疯狂的麦克斯", "动作 / 冒险",
                "https://picsum.photos/seed/madmax/313/176"));
        row3Adapter.add(new Movie("角斗士", "动作 / 剧情",
                "https://picsum.photos/seed/gladiator/313/176"));
        HeaderItem header3 = new HeaderItem(2, "经典动作");
        rowsAdapter.add(new ListRow(header3, row3Adapter));

        setAdapter(rowsAdapter);
    }
}
