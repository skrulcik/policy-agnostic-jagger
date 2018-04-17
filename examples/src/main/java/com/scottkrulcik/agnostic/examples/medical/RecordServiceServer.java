package com.scottkrulcik.agnostic.examples.medical;

import javax.inject.Inject;

/**
 * Service definition for a simple EMR management/analysis tool.
 *
 * TODO(skrulcik): Services like this can be generated given a set of endpoints
 */
final class RecordServiceServer {
    private final HistoryEndpoint historyEndpoint;
    private final SearchEndpoint searchEndpoint;

    @Inject
    RecordServiceServer(HistoryEndpoint endpoint, SearchEndpoint searchEndpoint) {
        historyEndpoint = endpoint;
        this.searchEndpoint = searchEndpoint;
    }

    /**
     * Fetches the medical history for the given patient, consisting of all of their records.
     */
    HistoryEndpoint.Response medicalHistory(HistoryEndpoint.Request request) {
        return historyEndpoint.handleRequest(request);
    }


    /**
     * Fetches the set of patients who are afflicted with a specific condition.
     */
    SearchEndpoint.Response patientsWithCondition(SearchEndpoint.Request request) {
        return searchEndpoint.handleRequest(request);
    }
}
