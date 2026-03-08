package org.jellyfin.emby.kitkat.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Emby 设备认证拦截器（极其关键）。
 * <p>
 * Emby Server 要求每一个请求都携带 {@code X-Emby-Authorization} 头，
 * 用于标识客户端设备信息。本拦截器会在 OkHttp 发出请求前自动注入该头。
 * <p>
 * 头部格式示例：
 * <pre>
 * X-Emby-Authorization: MediaBrowser Client="AndroidTV", Device="TV Box",
 *                        DeviceId="123456789", Version="1.0.0"
 * </pre>
 *
 * <p>如果后续需要在登录成功后追加 {@code Token="xxx"} 字段，可通过
 * {@link #setAccessToken(String)} 动态更新。</p>
 */
public class EmbyAuthInterceptor implements Interceptor {

    /**
     * 认证头的固定前缀，包含客户端、设备名、设备 ID 和版本号。
     * 这些值在整个应用生命周期内保持不变。
     */
    private static final String AUTH_HEADER_PREFIX =
            "MediaBrowser Client=\"AndroidTV\""
                    + ", Device=\"TV Box\""
                    + ", DeviceId=\"123456789\""
                    + ", Version=\"1.0.0\"";

    /** 登录成功后由服务端返回的访问令牌，初始为 null（未登录状态） */
    private volatile String accessToken;

    /**
     * 登录成功后调用此方法保存令牌。
     * 之后的每个请求头都会自动追加 {@code Token="xxx"}。
     *
     * @param token 服务端颁发的 AccessToken
     */
    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    /**
     * 获取当前保存的 AccessToken。
     *
     * @return AccessToken，未登录时为 null
     */
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // 拼接认证头：固定前缀 + 可选的 Token 字段
        String headerValue = AUTH_HEADER_PREFIX;
        if (accessToken != null && !accessToken.isEmpty()) {
            headerValue += ", Token=\"" + accessToken + "\"";
        }

        // 将认证头添加到请求中
        Request authorized = original.newBuilder()
                .header("X-Emby-Authorization", headerValue)
                .build();

        return chain.proceed(authorized);
    }
}
