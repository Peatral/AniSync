package com.peatral.anisync.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.peatral.anisync.App;
import com.peatral.anisync.R;
import com.peatral.anisync.lib.AnimeEntry;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListViewAdapter extends BaseAdapter {
    List<AnimeEntry> lstSource;
    private LayoutInflater layoutInflater;
    Context mContext;

    public ListViewAdapter(List<AnimeEntry> lstSource, Context mContext) {
        this.lstSource = lstSource;
        this.mContext = mContext;
        layoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return lstSource.size();
    }

    @Override
    public Object getItem(int i) {
        return lstSource.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view == null) {
            view = layoutInflater.inflate(R.layout.fragment_animeentry, null);
            holder = new ViewHolder();
            holder.title = view.findViewById(R.id.list_item_anime_title);
            holder.progress = view.findViewById(R.id.list_item_anime_progress);
            holder.score = view.findViewById(R.id.list_item_anime_score);
            holder.status = view.findViewById(R.id.list_item_anime_status);
            holder.image = view.findViewById(R.id.list_item_anime_image);


            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        AnimeEntry entry = this.lstSource.get(i);
        holder.title.setText(entry.name);
        holder.progress.setText(App.getContext().getString(R.string.progress, entry.progress));
        holder.score.setText(App.getContext().getString(R.string.score, entry.score));
        holder.status.setText(App.getContext().getResources().getText(App.getContext().getResources().getIdentifier("status_" + entry.status.getMalClass(), "string", App.getContext().getPackageName())));
        if(!entry.image_url.equals("")) Picasso.get().load(entry.image_url).into(holder.image);

        return view;
    }

    static class ViewHolder {
        TextView title;
        TextView progress;
        TextView score;
        TextView status;
        ImageView image;
    }
}
