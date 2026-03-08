package org.jellyfin.emby.kitkat.model;

/**
 * 电影海报卡片的数据模型（用于 Leanback 海报墙展示）。
 */
public class Movie {

    private String title;
    private String description;
    private String cardImageUrl;

    public Movie(String title, String description, String cardImageUrl) {
        this.title = title;
        this.description = description;
        this.cardImageUrl = cardImageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }
}
