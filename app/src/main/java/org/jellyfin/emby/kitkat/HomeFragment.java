package org.jellyfin.emby.kitkat;

import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.util.Log;

import org.jellyfin.emby.kitkat.model.EmbyItem;
import org.jellyfin.emby.kitkat.model.EmbyItemsResponse;
import org.jellyfin.emby.kitkat.model.EmbyViewsResponse;
import org.jellyfin.emby.kitkat.network.NetworkManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Leanback 海报墙首页 Fragment。
 * <p>
 * 继承 {@link BrowseFragment}，通过 Emby API 加载真实的媒体库数据，
 * 为每个分类（View）创建一行横向滚动的海报卡片。
 */
public class HomeFragment extends BrowseFragment {

    private static final String TAG = "HomeFragment";

    private ArrayObjectAdapter rowsAdapter;

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
     * 通过 Emby API 加载真实媒体库数据。
     * <p>
     * 先调用 getUserViews 获取分类列表，然后为每个分类异步加载媒体项。
     */
    private void loadRows() {
        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(rowsAdapter);

        final String userId = NetworkManager.getInstance().getUserId();
        if (userId == null) {
            Log.e(TAG, "UserId 为空，无法加载媒体库");
            return;
        }

        NetworkManager.getInstance().getEmbyApiService()
                .getUserViews(userId)
                .enqueue(new Callback<EmbyViewsResponse>() {
                    @Override
                    public void onResponse(Call<EmbyViewsResponse> call,
                                           Response<EmbyViewsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<EmbyItem> views = response.body().getItems();
                            if (views != null) {
                                for (int i = 0; i < views.size(); i++) {
                                    EmbyItem view = views.get(i);
                                    addViewRow(view, i);
                                }
                            }
                        } else {
                            Log.e(TAG, "获取 Views 失败: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<EmbyViewsResponse> call, Throwable t) {
                        Log.e(TAG, "获取 Views 网络错误: " + t.getMessage());
                    }
                });
    }

    /**
     * 为一个媒体库分类创建行，并异步加载该分类下的媒体项。
     *
     * @param view     分类信息（如"电影"、"剧集"）
     * @param rowIndex 行索引（用于 HeaderItem id）
     */
    private void addViewRow(final EmbyItem view, int rowIndex) {
        CardPresenter cardPresenter = new CardPresenter();
        final ArrayObjectAdapter rowAdapter = new ArrayObjectAdapter(cardPresenter);

        HeaderItem header = new HeaderItem(rowIndex, view.getName());
        rowsAdapter.add(new ListRow(header, rowAdapter));

        String userId = NetworkManager.getInstance().getUserId();
        NetworkManager.getInstance().getEmbyApiService()
                .getItems(userId, view.getId(), "Overview,ProductionYear,ImageTags", 20)
                .enqueue(new Callback<EmbyItemsResponse>() {
                    @Override
                    public void onResponse(Call<EmbyItemsResponse> call,
                                           Response<EmbyItemsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<EmbyItem> items = response.body().getItems();
                            if (items != null) {
                                for (EmbyItem item : items) {
                                    rowAdapter.add(item);
                                }
                            }
                        } else {
                            Log.e(TAG, "获取 " + view.getName()
                                    + " 媒体项失败: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<EmbyItemsResponse> call, Throwable t) {
                        Log.e(TAG, "获取 " + view.getName()
                                + " 媒体项网络错误: " + t.getMessage());
                    }
                });
    }
}
