package net.ouftech.popularmovies.details;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.ouftech.popularmovies.R;
import net.ouftech.popularmovies.commons.NetworkUtils;
import net.ouftech.popularmovies.model.Video;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.video_card_thumbnail_iv)
    protected ImageView thumbnailIV;
    @BindView(R.id.video_card_title_tv)
    protected TextView titleTV;

    public VideoViewHolder(final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(@NonNull final Video video) {
        titleTV.setText(video.name);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkUtils.openYoutubeVideo(itemView.getContext(), video.key);
            }
        });


        Glide.with(itemView.getContext())
                .load(NetworkUtils.getYoutubeThumbnailURL(video.key))
                .into(thumbnailIV);
    }



}
