package org.jellyfin.emby.kitkat;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import org.jellyfin.emby.kitkat.network.NetworkManager;

import java.io.InputStream;

/**
 * 自定义 Glide 模块 —— 强制 Glide 使用我们的安全 {@link okhttp3.OkHttpClient}。
 * <p>
 * 默认情况下 Glide 使用 {@code HttpURLConnection} 加载网络图片，绕过了我们
 * 在 {@link org.jellyfin.emby.kitkat.network.SecureOkHttpClientFactory} 中所做的
 * TLS 1.2 补丁和 OkHttp hostname-verification 缓解。这会导致：
 * <ul>
 *   <li>Android 4.4 设备上 HTTPS 握手失败（TLS 1.2 未启用）；</li>
 *   <li>认证头（{@code X-Emby-Authorization}）无法通过拦截器自动注入。</li>
 * </ul>
 * <p>
 * 通过注册 {@link OkHttpUrlLoader.Factory}，Glide 的所有网络请求都会走
 * {@link NetworkManager#getOkHttpClient()} 创建的安全连接，继承 TLS 1.2 补丁
 * 和 {@link org.jellyfin.emby.kitkat.network.EmbyAuthInterceptor} 认证拦截器。
 *
 * <p><b>初始化顺序要求：</b>{@link NetworkManager#init(String)} 必须在 Glide 首次
 * 被使用之前调用（通常在 {@code Application.onCreate()} 或登录流程中完成）。</p>
 */
@GlideModule
public class MyGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(
                        NetworkManager.getInstance().getOkHttpClient()));
    }
}
