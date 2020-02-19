package com.peatral.anisync.fragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.peatral.anisync.App;
import com.peatral.anisync.R;
import com.peatral.anisync.graphql.classes.MediaList;
import com.peatral.anisync.graphql.enums.MediaListStatus;
import com.squareup.picasso.Picasso;

import mva3.adapter.ItemBinder;
import mva3.adapter.ItemViewHolder;
import mva3.adapter.util.Mode;

public class MediaListBinder extends ItemBinder<MediaList, MediaListBinder.ViewHolder> {

    private MediaListAdapter adapter;

    public MediaListBinder(MediaListAdapter adapter) {
        this.adapter = adapter;
    }


    @Override
    public ViewHolder createViewHolder(ViewGroup parent) {
        return new ViewHolder(inflate(parent, R.layout.fragment_animeentry));
    }

    @Override
    public void bindViewHolder(ViewHolder holder, MediaList mediaList) {
        adapter.addHolder(holder);

        holder.title.setText(mediaList.getMedia().getTitle().getRomajiTitle());
        holder.progress.setText(App.getContext().getString(R.string.progress, mediaList.getProgress()));
        holder.score.setText(App.getContext().getString(R.string.score, (int) mediaList.getScore()));
        holder.status.setText(App.getContext().getResources().getText(App.getContext().getResources().getIdentifier("status_" + MediaListStatus.getMalClass(mediaList.getStatus()), "string", App.getContext().getPackageName())));
        if (!mediaList.getMedia().getCoverImage().getExtraLarge().equals(""))
            Picasso.get().load(mediaList.getMedia().getCoverImage().getExtraLarge()).into(holder.image);
        holder.checkIcon.setVisibility(holder.isItemSelected() ? View.VISIBLE : View.INVISIBLE);
        holder.image.setColorFilter(holder.title.getContext().getColor(holder.isItemSelected() ? R.color.colorCoverImageSelected : R.color.colorCoverImage));
    }

    @Override
    public boolean canBindData(Object item) {
        return item instanceof MediaList;
    }

    class ViewHolder extends ItemViewHolder<MediaList> implements View.OnLongClickListener, View.OnClickListener {

        TextView title;
        TextView progress;
        TextView score;
        TextView status;
        ImageView image;
        ImageView checkIcon;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);

            title = itemView.findViewById(R.id.list_item_anime_title);
            progress = itemView.findViewById(R.id.list_item_anime_progress);
            score = itemView.findViewById(R.id.list_item_anime_score);
            status = itemView.findViewById(R.id.list_item_anime_status);
            image = itemView.findViewById(R.id.list_item_anime_image);
            checkIcon = itemView.findViewById(R.id.list_item_check_image);

        }

        @Override
        public boolean onLongClick(View view) {
            if (adapter.getSelectionMode() == Mode.NONE) adapter.startActionMode(((AppCompatActivity) title.getContext()));
            toggleItemSelection();
            return true;
        }

        @Override
        public void onClick(View view) {
            toggleItemSelection();
            if (adapter.getSelectionMode() == Mode.NONE) {
                MediaList media = getItem();
                if (adapter.getState() != 2) {
                    new AlertDialog.Builder(itemView.getContext())
                            .setMessage(itemView.getContext().getString(R.string.dialog_ignore_anime, media.getMedia().getTitle().getRomajiTitle()))
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            })
                            .setPositiveButton(R.string.ok, (dialog, which) -> {
                                adapter.addToIgnore(media);
                            }).create().show();
                } else {
                    new AlertDialog.Builder(itemView.getContext())
                            .setMessage(itemView.getContext().getString(R.string.dialog_not_ignore_anime, media.getMedia().getTitle().getRomajiTitle()))
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            })
                            .setPositiveButton(R.string.ok, (dialog, which) -> {
                                adapter.removeFromIgnore(media);
                            }).create().show();
                }
            }
        }
    }
}
