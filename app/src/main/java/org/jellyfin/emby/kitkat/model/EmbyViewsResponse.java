package org.jellyfin.emby.kitkat.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Emby 用户媒体库分类响应体。
 * <p>
 * 对应 {@code GET /Users/{UserId}/Views} 的返回结果，
 * 包含用户可见的媒体库分类列表（如"电影"、"剧集"等）。
 */
public class EmbyViewsResponse {

    @SerializedName("Items")
    private List<EmbyItem> items;

    public List<EmbyItem> getItems() {
        return items;
    }

    public void setItems(List<EmbyItem> items) {
        this.items = items;
    }
}
