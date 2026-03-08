package org.jellyfin.emby.kitkat;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.jellyfin.emby.kitkat.model.EmbyItem;
import org.jellyfin.emby.kitkat.network.NetworkManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Leanback 卡片 Presenter —— 把 {@link EmbyItem} 渲染为 {@link ImageCardView}。
 * <p>
 * 使用 Glide 4.8.0 加载 Emby Server 的真实海报图片。
 * 优先使用 Primary 图片，不存在时依次回退到 Thumb、Backdrop。
 * 只有当 {@code ImageTags} 中存在对应类型时才发起请求，避免无谓的 404。
 * <p>
 * 鉴权方式：通过 URL Query 参数 {@code api_key} 传递 Token，
 * 兼容 CDN / 反向代理剥离自定义 Header 的场景。
 * 同时追加 {@code format=jpg} 强制服务端输出 JPEG，兼容 Android 4.4。
 */
public class CardPresenter extends Presenter {

    private static final String TAG = "EmkatGlide";
    private static final int CARD_WIDTH = 150;
    private static final int CARD_HEIGHT = 225;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        ImageCardView cardView = new ImageCardView(parent.getContext());
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        EmbyItem embyItem = (EmbyItem) item;
        final ImageCardView cardView = (ImageCardView) viewHolder.view;

        cardView.setTitleText(embyItem.getName());

        // 显示类型和年份作为描述
        String contentText = embyItem.getType() != null ? embyItem.getType() : "";
        if (embyItem.getProductionYear() != null) {
            contentText += " (" + embyItem.getProductionYear() + ")";
        }
        cardView.setContentText(contentText);

        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);

        // 根据 ImageTags 确定可用的图片类型及标签
        String imageUrl = resolveImageUrl(embyItem);
        if (imageUrl == null) {
            // 该媒体项没有任何可用图片，直接显示占位图，不发起网络请求
            cardView.setMainImage(
                    cardView.getContext().getResources()
                            .getDrawable(android.R.drawable.ic_menu_report_image));
            return;
        }

        Glide.with(cardView.getContext())
                .load(imageUrl)
                .apply(new RequestOptions()
                        .centerCrop()
                        .override(CARD_WIDTH, CARD_HEIGHT)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(android.R.drawable.ic_menu_report_image))
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                            Object model, Target<Drawable> target,
                            boolean isFirstResource) {
                        final String errorMsg = e != null ? e.getMessage() : "Unknown Error";
                        Log.e(TAG, "海报加载失败: " + errorMsg, e);
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                cardView.setContentText("异常: " + errorMsg);
                            }
                        });
                        return false; // 让 Glide 继续显示 error placeholder
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                            Object model, Target<Drawable> target,
                            DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(cardView.getMainImageView());
    }

    /**
     * 根据 {@link EmbyItem} 的 ImageTags 和 BackdropImageTags 解析可用的图片 URL。
     * <p>
     * 优先级：Primary → Thumb → Backdrop。仅当对应标签存在时才构建 URL，
     * 并在查询参数中附带 {@code tag} 以利用服务端缓存。
     * <p>
     * 鉴权通过 {@code api_key} 查询参数完成，兼容 CDN / 反向代理。
     * 追加 {@code format=jpg} 强制服务端输出 JPEG，兼容 Android 4.4。
     *
     * @param embyItem 媒体项
     * @return 图片 URL，无可用图片时返回 null
     */
    private String resolveImageUrl(EmbyItem embyItem) {
        String baseUrl = NetworkManager.getInstance().getBaseUrl();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String token = NetworkManager.getInstance().getAuthInterceptor().getAccessToken();

        // 1. Primary
        if (embyItem.hasImage("Primary")) {
            return normalizedBaseUrl + "Items/" + embyItem.getId()
                    + "/Images/Primary?tag=" + embyItem.getImageTag("Primary")
                    + appendAuthAndFormat(token);
        }

        // 2. Thumb
        if (embyItem.hasImage("Thumb")) {
            return normalizedBaseUrl + "Items/" + embyItem.getId()
                    + "/Images/Thumb?tag=" + embyItem.getImageTag("Thumb")
                    + appendAuthAndFormat(token);
        }

        // 3. Backdrop（使用第一张）
        List<String> backdropTags = embyItem.getBackdropImageTags();
        if (backdropTags != null && !backdropTags.isEmpty()) {
            return normalizedBaseUrl + "Items/" + embyItem.getId()
                    + "/Images/Backdrop/0?tag=" + backdropTags.get(0)
                    + appendAuthAndFormat(token);
        }

        return null;
    }

    /**
     * 构建 URL 尾部的鉴权与格式参数。
     *
     * @param token AccessToken，可能为 null 或空
     * @return 形如 {@code &api_key=xxx&format=jpg} 的字符串
     */
    private String appendAuthAndFormat(String token) {
        StringBuilder sb = new StringBuilder();
        if (token != null && !token.isEmpty()) {
            try {
                sb.append("&api_key=").append(URLEncoder.encode(token, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // UTF-8 is always supported; fall back to raw token
                sb.append("&api_key=").append(token);
            }
        }
        sb.append("&format=jpg");
        return sb.toString();
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
