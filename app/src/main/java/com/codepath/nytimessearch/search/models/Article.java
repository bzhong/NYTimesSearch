package com.codepath.nytimessearch.search.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Article {
    final static String API_PREFIX = "https://nytimes.com/";

    String webUrl;

    String headline;
    String thumbNail;

    public Article() {}

    public static Article fromJson(JSONObject jsonObject) {
        Article article = new Article();
        try {
            article.webUrl = jsonObject.getString("web_url");
            JSONObject headline = jsonObject.getJSONObject("headline");
            if (headline.getString("main") != null) {
                article.headline = headline.getString("main");
            } else {
                article.headline = headline.getString("name");
            }
            JSONArray multimedia = jsonObject.getJSONArray("multimedia");
            if (multimedia.length() > 0) {
                article.thumbNail = API_PREFIX + multimedia.getJSONObject(0).getString("url");
            } else {
                article.thumbNail = "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return article;
    }

    public static List<Article> fromJsonArray(JSONArray jsonArray) {
        JSONObject articleJson;
        List<Article> articles = new ArrayList<Article>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); ++i) {
            try {
                articleJson = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            Article article = Article.fromJson(articleJson);
            if (article != null) {
                articles.add(article);
            }
        }
        return articles;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getThumbNail() {
        return thumbNail;
    }
}
