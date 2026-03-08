package org.jellyfin.emby.kitkat.network;

import org.jellyfin.emby.kitkat.model.EmbyItemsResponse;
import org.jellyfin.emby.kitkat.model.EmbyViewsResponse;
import org.jellyfin.emby.kitkat.model.LoginRequest;
import org.jellyfin.emby.kitkat.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    /**
     * 获取用户的媒体库分类（Views）。
     * <p>
     * 返回当前用户可见的媒体库分类列表，如"电影"、"剧集"、"音乐"等。
     *
     * @param userId 用户 ID
     * @return 媒体库分类列表
     */
    @GET("/Users/{UserId}/Views")
    Call<EmbyViewsResponse> getUserViews(@Path("UserId") String userId);

    /**
     * 获取指定分类下的媒体项列表。
     *
     * @param userId   用户 ID
     * @param parentId 父级分类 ID（即 View 的 Id）
     * @param fields   需要返回的额外字段，逗号分隔（如 "Overview,ProductionYear"）
     * @param limit    返回数量上限
     * @return 媒体项列表
     */
    @GET("/Users/{UserId}/Items")
    Call<EmbyItemsResponse> getItems(
            @Path("UserId") String userId,
            @Query("ParentId") String parentId,
            @Query("Fields") String fields,
            @Query("Limit") int limit);
}
