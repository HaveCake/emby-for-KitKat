package org.jellyfin.emby.kitkat;

import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jellyfin.emby.kitkat.model.Movie;

/**
 * Leanback 卡片 Presenter —— 把 {@link Movie} 渲染为 {@link ImageCardView}。
 * <p>
 * 使用 Glide 4.8.0 加载网络图片到卡片封面。
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
        Movie movie = (Movie) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        cardView.setTitleText(movie.getTitle());
        cardView.setContentText(movie.getDescription());
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);

        Glide.with(cardView.getContext())
                .load(movie.getCardImageUrl())
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
