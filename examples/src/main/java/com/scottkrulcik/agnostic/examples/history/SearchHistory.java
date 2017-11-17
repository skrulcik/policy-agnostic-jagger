package com.scottkrulcik.agnostic.examples.history;

import java.util.ArrayList;
import java.util.List;

public final class SearchHistory {
    private final List<String> pastSearches = new ArrayList<>();

    SearchHistory() { }

    public void recordSearch(String search) {
        pastSearches.add(search);
    }

    public boolean contains(String search) {
        return pastSearches.contains(search);
    }

    @Override
    public String toString() {
        return "SearchHistory{" +
            "pastSearches=" + pastSearches +
            '}';
    }
}
