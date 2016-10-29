package com.codepath.nytimessearch.search.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.search.adapters.ArticlesAdapter;
import com.codepath.nytimessearch.search.fragments.FilterFragment;
import com.codepath.nytimessearch.search.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.nytimessearch.search.models.Article;
import com.codepath.nytimessearch.search.models.Filter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity
        implements FilterFragment.OnFragmentInteractionListener {

    private final String INDEX_URL = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    private final String API_KEY = "bfacca665da24f8a8770db74acbe5039";
    private final String ARTS = "Arts";
    private final String FASHION = "Fashion & Style";
    private final String SPORTS = "Sports";

    List<Article> articles;
    String defaultQuery;
    Filter defaultFilter;
    RecyclerView recyclerView;
    ArticlesAdapter adapter;
    Handler handler;
    Runnable runnableCode;
    EndlessRecyclerViewScrollListener scrollListener;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    class RunnableCode implements Runnable {
        private int page;

        public RunnableCode(int nextPage) {
            page = nextPage;
        }

        public void run() {
            SearchActivity.this.loadNextDataFromApi(page);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupViews();
    }

    private void setupViews() {
        handler = new Handler();
        articles = new ArrayList<>();
        defaultQuery = new String("");
        defaultFilter = null;
        recyclerView = (RecyclerView) findViewById(R.id.rvResults);
        adapter = new ArticlesAdapter(this, articles);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(staggeredGridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                runnableCode = new RunnableCode(page);
                handler.postDelayed(runnableCode, 1000);
            }
        };
        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(scrollListener);
    }

    private void loadNextDataFromApi(int page) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("api-key", API_KEY);
        if (!defaultQuery.isEmpty()) {
            params.put("q", defaultQuery);
        }
        setFilter(params, defaultFilter);
        params.put("page", page);
        client.get(INDEX_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray articleJsonResults = null;

                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    List<Article> newArticles = Article.fromJsonArray(articleJsonResults);
                    if (newArticles.size() == 0) {
                        Toast.makeText(
                                SearchActivity.this, "No more article found", Toast.LENGTH_LONG
                        ).show();
                    } else {
                        articles.addAll(Article.fromJsonArray(articleJsonResults));
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(R.string.hint));
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                onArticleSearch(query, defaultFilter);

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.settings:
                openFilterFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openFilterFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FilterFragment filterFragment = FilterFragment.newInstance(defaultFilter);
        filterFragment.show(fm, "fragment_filter");
    }

    private void resetState() {
        articles.clear();
        adapter.notifyDataSetChanged();
        scrollListener.resetState();
    }

    private void onArticleSearch(String query, Filter filter) {
        if (!isNetworkAvailable() || !isOnline()) {
            Toast.makeText(this, "Network Unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        resetState();
        defaultQuery = query;
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("api-key", API_KEY);
        if (!defaultQuery.isEmpty()) {
            params.put("q", defaultQuery);
        }
        setFilter(params, filter);
        client.get(INDEX_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray articleJsonResults = null;

                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    articles.clear();
                    List<Article> newArticles = Article.fromJsonArray(articleJsonResults);
                    if (newArticles.size() == 0) {
                        Toast.makeText(SearchActivity.this, "No article found", Toast.LENGTH_LONG).show();
                    } else {
                        appendArticles(newArticles);
                        articles.addAll(newArticles);
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(SearchActivity.this, "Failed to load articles", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void appendArticles(List<Article> newArticles) {
        int curSize = adapter.getItemCount();
        articles.addAll(newArticles);
        adapter.notifyItemRangeChanged(curSize, newArticles.size());
    }

    private void setFilter(RequestParams params, Filter filter) {
        if (filter == null) {
            return;
        }
        String beginDate = String.valueOf(filter.getYear())
                + String.format("%02d", filter.getMonth())
                + String.format("%02d", filter.getDay());
        params.put("begin_date", beginDate);
        params.put("sort", filter.getSortOrder().toLowerCase());
        String newsDeskvalues = getNewsDeskValues(filter);
        if (!newsDeskvalues.isEmpty()) {
            params.put("fq", "news_desk:(" + newsDeskvalues + ")");
        }
    }

    private String getNewsDeskValues(Filter filter) {
        List<String> newsDeskValues = new ArrayList<>();
        if (filter.getIsArts() != 0) {
            newsDeskValues.add(quotedString(ARTS));
        }
        if (filter.getIsFashion() != 0) {
            newsDeskValues.add(quotedString(FASHION));
        }
        if (filter.getIsSports() != 0) {
            newsDeskValues.add(quotedString(SPORTS));
        }
        if (newsDeskValues.isEmpty()) {
            return new String("");
        }
        return TextUtils.join(" ", newsDeskValues);
    }

    private String quotedString(String str) {
        return "\"" + str + "\"";
    }

    public void onFragmentInteraction(Filter filter) {
        defaultFilter = filter;
        onArticleSearch(defaultQuery, defaultFilter);
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}
