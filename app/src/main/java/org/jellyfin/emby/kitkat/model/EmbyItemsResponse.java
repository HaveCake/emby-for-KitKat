package org.jellyfin.emby.kitkat.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Emby 媒体项列表响应体。
 * <p>
 * 对应 {@code GET /Users/{UserId}/Items} 的返回结果，
 * 包含指定分类下的媒体项列表。
 */
public class EmbyItemsResponse {

    @SerializedName("Items")
    private List<EmbyItem> items;

    @SerializedName("TotalRecordCount")
    private int totalRecordCount;

    public List<EmbyItem> getItems() {
        return items;
    }

    public void setItems(List<EmbyItem> items) {
        this.items = items;
    }

    public int getTotalRecordCount() {
        return totalRecordCount;
    }

    public void setTotalRecordCount(int totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }
}
