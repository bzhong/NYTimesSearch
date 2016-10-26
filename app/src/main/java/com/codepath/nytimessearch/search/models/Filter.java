package com.codepath.nytimessearch.search.models;

import java.util.Map;

public class Filter {

    private int year;
    private int month;
    private int day;
    private String sortOrder;
    private int isArts;
    private int isFashion;
    private int isSports;

    public Filter() {}

    public static Filter fromMap(Map<String, Integer> obj, String sortOrder) {
        Filter filter = new Filter();
        filter.year = obj.get("year");
        filter.month = obj.get("month");
        filter.day = obj.get("day");
        filter.sortOrder = sortOrder;
        filter.isArts = obj.get("isArts");
        filter.isFashion = obj.get("isFashion");
        filter.isSports = obj.get("isSports");
        return filter;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public int getIsArts() {
        return isArts;
    }

    public int getIsFashion() {
        return isFashion;
    }

    public int getIsSports() {
        return isSports;
    }
}
