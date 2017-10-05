package com.scottkrulcik.agnostic.examples.history;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public final class SearchHistory {
    private final List<String> pastSearches = new ArrayList<>();

    @Inject SearchHistory() { }

    public void recordSearch(String search) {
        pastSearches.add(search);
    }

    public boolean contains(String search) {
        return pastSearches.contains(search);
    }
}
