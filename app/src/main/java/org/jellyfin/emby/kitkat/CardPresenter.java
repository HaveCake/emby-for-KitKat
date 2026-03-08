package org.jellyfin.emby.kitkat;

import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;

import org.jellyfin.emby.kitkat.model.EmbyItem;
import org.jellyfin.emby.kitkat.network.NetworkManager;

/**
 * Leanback 卡片 Presenter —— 把 {@link EmbyItem} 渲染为 {@link ImageCardView}。
 * <p>
 * 使用 Glide 4.8.0 加载 Emby Server 的真实海报图片。
 * 海报 URL 拼接规则：{@code baseUrl + "/Items/" + itemId + "/Images/Primary"}
 */
public class CardPresenter extends Presenter {

    private static final int CARD_WIDTH = 313;
    private static final int CARD_HEIGHT = 176;

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
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        cardView.setTitleText(embyItem.getName());

        // 显示类型和年份作为描述
        String contentText = embyItem.getType() != null ? embyItem.getType() : "";
        if (embyItem.getProductionYear() != null) {
            contentText += " (" + embyItem.getProductionYear() + ")";
        }
        cardView.setContentText(contentText);

        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);

        // 拼接 Emby 海报 URL
        String baseUrl = NetworkManager.getInstance().getBaseUrl();
        String imageUrl = baseUrl + "/Items/" + embyItem.getId() + "/Images/Primary";

        // 通过请求头携带 AccessToken 进行认证（token 为空时仍可请求无需认证的图片）
        String token = NetworkManager.getInstance().getAuthInterceptor().getAccessToken();
        LazyHeaders.Builder headersBuilder = new LazyHeaders.Builder();
        if (token != null && !token.isEmpty()) {
            headersBuilder.addHeader("X-Emby-Token", token);
        }
        GlideUrl glideUrl = new GlideUrl(imageUrl, headersBuilder.build());

        Glide.with(cardView.getContext())
                .load(glideUrl)
                .apply(new RequestOptions()
                        .centerCrop()
                        .error(android.R.drawable.ic_menu_report_image))
                .into(cardView.getMainImageView());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
