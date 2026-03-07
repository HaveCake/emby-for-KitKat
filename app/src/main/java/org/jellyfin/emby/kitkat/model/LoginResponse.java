package org.jellyfin.emby.kitkat.model;

import com.google.gson.annotations.SerializedName;

/**
 * Emby 登录响应体。
 * <p>
 * 对应 Emby Server 的 {@code POST /Users/AuthenticateByName} 成功响应。
 * 服务端返回的 JSON 使用 Pascal-Case 字段名，通过 {@link SerializedName}
 * 注解映射到 Java 驼峰命名。
 *
 * <pre>
 * 示例响应：
 * {
 *   "AccessToken": "abc123...",
 *   "SessionInfo": {
 *     "UserId": "e8f0...
 *   }
 * }
 * </pre>
 */
public class LoginResponse {

    /** 服务端颁发的访问令牌，后续所有请求都需要携带 */
    @SerializedName("AccessToken")
    private String accessToken;

    /** 当前会话信息，其中包含已登录用户的 ID 等字段 */
    @SerializedName("SessionInfo")
    private SessionInfo sessionInfo;

    // ---- Getter / Setter ----------------------------------------------------

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    // ---- 内嵌的 SessionInfo 实体 --------------------------------------------

    /**
     * 会话信息，包含当前登录用户的基本标识。
     */
    public static class SessionInfo {

        /** 已登录用户的唯一 ID */
        @SerializedName("UserId")
        private String userId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
