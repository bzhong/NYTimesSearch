package com.codepath.nytimessearch.search.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.search.activities.ArticleActivity;
import com.codepath.nytimessearch.search.models.Article;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.List;

public class ArticlesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public class TextWithThumbnailViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public ImageView thumbnail;
        public TextView title;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public TextWithThumbnailViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            thumbnail = (ImageView) itemView.findViewById(R.id.ivImage);
            title = (TextView) itemView.findViewById(R.id.tvTitle);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition(); // gets item position
            Article article = ArticlesAdapter.this.mArticles.get(position);
            Intent intent = new Intent(ArticlesAdapter.this.getContext(), ArticleActivity.class);
            intent.putExtra("article", Parcels.wrap(article));
            ArticlesAdapter.this.getContext().startActivity(intent);
        }
    }

    public class TextOnlyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private TextView title;

        public TextOnlyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tvTitle);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition(); // gets item position
            Article article = ArticlesAdapter.this.mArticles.get(position);
            Intent intent = new Intent(ArticlesAdapter.this.getContext(), ArticleActivity.class);
            intent.putExtra("article", Parcels.wrap(article));
            ArticlesAdapter.this.getContext().startActivity(intent);
        }
    }

    private List<Article> mArticles;
    private Context mContext;
    public enum articleTypes {
        ONLY_TEXT, TEXT_WITH_THUMBNAIL
    }

    public ArticlesAdapter(Context context, List<Article> articles) {
        mArticles = articles;
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View articleView;
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == articleTypes.ONLY_TEXT.ordinal()) {
            articleView = inflater.inflate(R.layout.item_article_only_text, parent, false);
            viewHolder = new TextOnlyViewHolder(articleView);
        } else {
            articleView = inflater.inflate(R.layout.item_article_results, parent, false);
            viewHolder = new TextWithThumbnailViewHolder(articleView);
        }
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder.getItemViewType() == articleTypes.ONLY_TEXT.ordinal()) {
            TextOnlyViewHolder vh = (TextOnlyViewHolder) viewHolder;
            configureTextOnlyViewHolder(vh, position);
        } else {
            TextWithThumbnailViewHolder vh = (TextWithThumbnailViewHolder) viewHolder;
            configureTextWithThumbnailViewHolder(vh, position);
        }
    }

    private void configureTextOnlyViewHolder(TextOnlyViewHolder viewHolder, int position) {
        Article article = mArticles.get(position);
        TextView tvTitle = viewHolder.title;
        tvTitle.setText(article.getHeadline());
    }

    private void configureTextWithThumbnailViewHolder(
            TextWithThumbnailViewHolder viewHolder, int position
    ) {
        Article article = mArticles.get(position);
        ImageView imageView = viewHolder.thumbnail;
        TextView tvTitle = viewHolder.title;

//        imageView.setImageResource(0);
        tvTitle.setText(article.getHeadline());

        String thumbnail = article.getThumbNail();
        if (!TextUtils.isEmpty(thumbnail)) {
            Picasso.with(getContext())
                    .load(thumbnail)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Article article = mArticles.get(position);
        if (article.getThumbNail().isEmpty()) {
            return articleTypes.ONLY_TEXT.ordinal();
        } else {
            return articleTypes.TEXT_WITH_THUMBNAIL.ordinal();
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mArticles.size();
    }
}
