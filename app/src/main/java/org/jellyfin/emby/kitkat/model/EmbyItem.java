package org.jellyfin.emby.kitkat.model;

import com.google.gson.annotations.SerializedName;

/**
 * Emby 媒体项实体。
 * <p>
 * 对应 Emby Server 返回的单个媒体项 JSON 对象（电影、剧集、分类等）。
 * 字段使用 Pascal-Case 映射。
 */
public class EmbyItem {

    @SerializedName("Id")
    private String id;

    @SerializedName("Name")
    private String name;

    @SerializedName("Type")
    private String type;

    @SerializedName("Overview")
    private String overview;

    @SerializedName("ProductionYear")
    private Integer productionYear;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Integer getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(Integer productionYear) {
        this.productionYear = productionYear;
    }
}
