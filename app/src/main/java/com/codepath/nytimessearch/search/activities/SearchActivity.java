package com.codepath.nytimessearch.search.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.search.adapters.ArticleArrayAdapter;
import com.codepath.nytimessearch.search.fragments.FilterFragment;
import com.codepath.nytimessearch.search.models.Article;
import com.codepath.nytimessearch.search.models.Filter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

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
    ArticleArrayAdapter adapter;
    GridView gvResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupViews();
    }

    private void setupViews() {
        articles = new ArrayList<>();
        defaultQuery = new String("");
        defaultFilter = null;
        gvResults = (GridView) findViewById(R.id.gvResults);
        adapter = new ArticleArrayAdapter(this, articles);
        gvResults.setAdapter(adapter);

        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                Article article = articles.get(i);
                intent.putExtra("article", Parcels.wrap(article));
                startActivity(intent);
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

    private void onArticleSearch(String query, Filter filter) {
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
                    articles.addAll(Article.fromJsonArray(articleJsonResults));
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
}
