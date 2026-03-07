package org.jellyfin.emby.kitkat.model;

import com.google.gson.annotations.SerializedName;

/**
 * Emby 登录请求体。
 * <p>
 * 对应 Emby Server 的 {@code POST /Users/AuthenticateByName} 接口，
 * JSON 字段名使用 Pascal-Case（与 Emby API 约定一致）。
 */
public class LoginRequest {

    /** 用户名 */
    @SerializedName("Username")
    private String username;

    /** 密码（明文，Emby Server 端会自行哈希） */
    @SerializedName("Pw")
    private String pw;

    /**
     * 创建登录请求。
     *
     * @param username 用户名
     * @param pw       密码
     */
    public LoginRequest(String username, String pw) {
        this.username = username;
        this.pw = pw;
    }

    // ---- Getter / Setter ----------------------------------------------------

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }
}
