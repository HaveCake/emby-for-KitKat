package org.jellyfin.emby.kitkat.network;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络层组装单例。
 * <p>
 * 整个应用只需要一个 {@link NetworkManager} 实例。它负责：
 * <ol>
 *   <li>通过 {@link SecureOkHttpClientFactory} 创建安全的 OkHttpClient
 *       （已包含 TLS 1.2 补丁和 OkHttp 漏洞缓解）；</li>
 *   <li>添加 {@link EmbyAuthInterceptor} 以自动注入设备认证头；</li>
 *   <li>使用上述 client 初始化 Retrofit，并对外暴露 {@link EmbyApiService}。</li>
 * </ol>
 *
 * <h3>使用方式</h3>
 * <pre>
 * // 1. 初始化（通常在 Application.onCreate 中调用一次）
 * NetworkManager.init("http://192.168.1.100:8096");
 *
 * // 2. 获取 API 接口
 * EmbyApiService api = NetworkManager.getInstance().getEmbyApiService();
 *
 * // 3. 登录成功后保存令牌（后续请求会自动携带）
 * NetworkManager.getInstance().getAuthInterceptor().setAccessToken(token);
 * </pre>
 */
public final class NetworkManager {

    /** 单例实例 */
    private static volatile NetworkManager sInstance;

    /** Retrofit 实例 */
    private final Retrofit retrofit;

    /** Emby API 接口代理 */
    private final EmbyApiService embyApiService;

    /** 认证拦截器（可在登录后通过它设置 AccessToken） */
    private final EmbyAuthInterceptor authInterceptor;

    /** 安全的 OkHttpClient（包含 TLS 1.2 补丁 + Emby 认证拦截器） */
    private final OkHttpClient okHttpClient;

    /** Emby Server 基础地址（用于拼接图片 URL 等） */
    private final String baseUrl;

    /** 已登录用户的 ID，登录成功后设置 */
    private volatile String userId;

    /**
     * 私有构造，由 {@link #init(String)} 调用。
     *
     * @param baseUrl Emby Server 地址，例如 {@code "http://192.168.1.100:8096"}
     */
    private NetworkManager(String baseUrl) {
        this.baseUrl = baseUrl;

        // 1. 创建认证拦截器
        authInterceptor = new EmbyAuthInterceptor();

        // 2. 基于 SecureOkHttpClientFactory 创建安全的 OkHttpClient，
        //    并追加 Emby 认证拦截器
        okHttpClient = SecureOkHttpClientFactory.createBuilder()
                .addInterceptor(authInterceptor)
                .build();

        // 3. 初始化 Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 4. 创建 API 接口代理
        embyApiService = retrofit.create(EmbyApiService.class);
    }

    /**
     * 初始化 NetworkManager 单例。
     * <p>
     * 必须在使用 {@link #getInstance()} 之前调用一次（通常在
     * {@code Application.onCreate()} 中）。重复调用会重新创建实例
     * （用于切换服务器地址的场景）。
     *
     * @param baseUrl Emby Server 基础地址，例如 {@code "http://192.168.1.100:8096"}
     */
    public static void init(String baseUrl) {
        synchronized (NetworkManager.class) {
            sInstance = new NetworkManager(baseUrl);
        }
    }

    /**
     * 获取单例实例。
     *
     * @return NetworkManager 实例
     * @throws IllegalStateException 如果尚未调用 {@link #init(String)}
     */
    public static NetworkManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(
                    "NetworkManager 尚未初始化，请先调用 NetworkManager.init(baseUrl)");
        }
        return sInstance;
    }

    /**
     * 获取 Retrofit 实例（高级用法：自定义创建其他 API 接口）。
     *
     * @return 已配置好的 Retrofit 实例
     */
    public Retrofit getRetrofit() {
        return retrofit;
    }

    /**
     * 获取 Emby API 接口。
     *
     * @return EmbyApiService 代理
     */
    public EmbyApiService getEmbyApiService() {
        return embyApiService;
    }

    /**
     * 获取认证拦截器，用于在登录成功后设置 AccessToken。
     * <p>
     * 示例：{@code NetworkManager.getInstance().getAuthInterceptor().setAccessToken("xxx");}
     *
     * @return EmbyAuthInterceptor 实例
     */
    public EmbyAuthInterceptor getAuthInterceptor() {
        return authInterceptor;
    }

    /**
     * 获取安全的 OkHttpClient 实例（已含 TLS 1.2 补丁和 Emby 认证拦截器）。
     * <p>
     * 主要供 Glide 的 {@code OkHttpUrlLoader} 使用，确保图片请求也经过
     * 安全的网络通道和认证拦截器。
     *
     * @return 共享的 OkHttpClient 实例
     */
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * 获取 Emby Server 基础地址（用于拼接图片 URL 等）。
     * <p>
     * 返回值不含尾部斜杠，方便直接拼接路径。
     *
     * @return 基础地址，例如 {@code "http://192.168.1.100:8096"}
     */
    public String getBaseUrl() {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    /**
     * 保存已登录用户的 ID，供后续 API 调用使用。
     *
     * @param userId 用户 ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取已登录用户的 ID。
     *
     * @return 用户 ID，未登录时为 null
     */
    public String getUserId() {
        return userId;
    }
}
