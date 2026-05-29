package com.sathish.thodar.data.dto.response;

public class Station {
    private String stationCode;
    private String stationName;

    public Station(String stationCode, String stationName) {
        this.stationCode = stationCode;
        this.stationName = stationName;
    }

    public String getStationCode() {
        return stationCode;
    }

    public String getStationName() {
        return stationName;
    }

    @Override
    public String toString() {
        return stationCode + " - " + stationName;
    }

    public static Station fromString(String rawStation) {
        if (rawStation == null || !rawStation.contains("/")) {
            return new Station("UNKNOWN", rawStation);
        }
        String[] parts = rawStation.split("/", 2);
        return new Station(parts[0].trim(), parts[1].trim());
    }
}