package net.ouftech.popularmovies.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ouftech.popularmovies.R;
import net.ouftech.popularmovies.commons.CollectionUtils;
import net.ouftech.popularmovies.commons.Logger;
import net.ouftech.popularmovies.model.Video;

import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter<VideoViewHolder> {

    private List<Video> videos;

    @NonNull
    protected String getLotTag() {
        return "VideosAdapter";
    }

    public VideosAdapter() {
    }

    public void swapData(List<Video> videos) {
        this.videos = videos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        int videosCount = CollectionUtils.getSize(videos);
        if (videos == null || videosCount == 0 || position < 0 || position >= videosCount) {
            Logger.e(getLotTag(), new ArrayIndexOutOfBoundsException(String.format("Cannot bind item at position %s. Videos count is %s", position, videosCount)));
            return;
        }

        holder.bind(videos.get(position));
    }

    @Override
    public int getItemCount() {
        return CollectionUtils.getSize(videos);
    }

}
