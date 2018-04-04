package net.ouftech.popularmovies.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.ouftech.popularmovies.R;
import net.ouftech.popularmovies.model.Video;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.video_card_thumbnail_iv)
    protected ImageView thumbnailIV;
    @BindView(R.id.video_card_title_tv)
    protected TextView titleTV;

    public VideoViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(@NonNull Video video) {
        titleTV.setText(video.name);
    }



}
