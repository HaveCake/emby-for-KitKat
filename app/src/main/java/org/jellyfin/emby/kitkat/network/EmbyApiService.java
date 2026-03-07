package org.jellyfin.emby.kitkat.network;

import org.jellyfin.emby.kitkat.model.LoginRequest;
import org.jellyfin.emby.kitkat.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Emby Server Retrofit API 接口定义。
 * <p>
 * 所有接口路径均为相对路径，基地址（baseUrl）由 {@link NetworkManager}
 * 在初始化 Retrofit 时统一设置。
 */
public interface EmbyApiService {

    /**
     * 用户名 + 密码登录。
     * <p>
     * Emby Server 要求该请求同时携带 {@code X-Emby-Authorization} 头，
     * 此头由 {@link EmbyAuthInterceptor} 自动注入，无需调用方手动添加。
     *
     * @param request 包含用户名和密码的请求体
     * @return 包含 AccessToken 与 SessionInfo 的登录响应
     */
    @POST("/Users/AuthenticateByName")
    Call<LoginResponse> login(@Body LoginRequest request);
}
