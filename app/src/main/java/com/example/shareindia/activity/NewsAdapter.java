package com.example.shareindia.activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shareindia.R;
import com.example.shareindia.adapter.PathResolverRecyclerAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsListItem> listItem;
    private Context context;

    public NewsAdapter(List<NewsListItem> listItem) {
        this.listItem = listItem;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final NewsListItem currentListItem = listItem.get(position);

        Picasso.get().load(currentListItem.getImageUrl()).into(holder.imageView);
        holder.titleView.setText(currentListItem.getTitle());
        holder.descView.setText(currentListItem.getDesc());
        String source="- by  ";
        holder.sourceView.setText(currentListItem.getSource());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = currentListItem.getNewsUrl();
                Uri newsUri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, newsUri);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView titleView;
        private TextView descView;
        private TextView sourceView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context=itemView.getContext();
            imageView = itemView.findViewById(R.id.image_view);
            titleView = itemView.findViewById(R.id.text_view_title);
            descView = itemView.findViewById(R.id.text_view_desc);
            sourceView = itemView.findViewById(R.id.text_view_source);

        }


    }


}
