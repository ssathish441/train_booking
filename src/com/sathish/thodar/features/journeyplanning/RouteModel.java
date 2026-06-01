package com.sathish.thodar.features.journeyplanning;

import java.util.ArrayList;
import java.util.List;

class RouteModel {
    private String sourceCode;
    private String destinationCode;
    private Long journeyDateEpoch;
    private List<String> searchResults;

    public RouteModel() {
        this.searchResults = new ArrayList<>();
    }


    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getDestinationCode() {
        return destinationCode;
    }

    public void setDestinationCode(String destinationCode) {
        this.destinationCode = destinationCode;
    }

    public Long getJourneyDateEpoch() {
        return journeyDateEpoch;
    }

    public void setJourneyDateEpoch(Long journeyDateEpoch) {
        this.journeyDateEpoch = journeyDateEpoch;
    }

    public List<String> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<String> searchResults) {
        this.searchResults = searchResults;
    }
}