package org.jellyfin.emby.kitkat.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

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

    /**
     * 媒体项拥有的图片标签映射，键为图片类型（如 "Primary"、"Thumb"），
     * 值为对应图片的 ETag 哈希。若某类型键不存在，则该媒体项无此类型图片。
     */
    @SerializedName("ImageTags")
    private Map<String, String> imageTags;

    /**
     * 背景图标签数组，数组中每个元素为一张背景图的 ETag 哈希。
     * 为空或 null 时表示没有背景图。
     */
    @SerializedName("BackdropImageTags")
    private List<String> backdropImageTags;

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

    public Map<String, String> getImageTags() {
        return imageTags;
    }

    public void setImageTags(Map<String, String> imageTags) {
        this.imageTags = imageTags;
    }

    public List<String> getBackdropImageTags() {
        return backdropImageTags;
    }

    public void setBackdropImageTags(List<String> backdropImageTags) {
        this.backdropImageTags = backdropImageTags;
    }

    /**
     * 获取指定类型的图片标签。
     *
     * @param imageType 图片类型，如 "Primary"、"Thumb"
     * @return 图片 ETag 哈希，如果该类型不存在则返回 null
     */
    public String getImageTag(String imageType) {
        if (imageTags != null) {
            return imageTags.get(imageType);
        }
        return null;
    }

    /**
     * 判断是否拥有指定类型的图片。
     *
     * @param imageType 图片类型，如 "Primary"、"Thumb"
     * @return true 表示有该类型图片
     */
    public boolean hasImage(String imageType) {
        return imageTags != null && imageTags.containsKey(imageType);
    }
}
