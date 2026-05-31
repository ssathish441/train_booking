package com.sathish.thodar.features.journeyplanning;

import com.sathish.thodar.data.repository.ThodarDB;
import com.sathish.thodar.data.dto.request.admin.TrainSetupRequest;
import com.sathish.thodar.data.dto.request.admin.ScheduleRequest;
import com.sathish.thodar.util.ConsoleInput;
import com.sathish.thodar.util.ParseHelper;

import java.util.ArrayList;
import java.util.List;

public class RouteView {

    private final RouteModel routeModel = new RouteModel();
    private final ThodarDB db = ThodarDB.getInstance();

    public void showSearchScreen() {
        System.out.println("\n=================================");
        System.out.println("   TRAIN SEARCH & PLANNING    ");
        System.out.println("=================================");

        routeModel.setSourceCode(ConsoleInput.getString("Enter Source Station (Code or Name, e.g., MS/Chennai): ").toLowerCase().trim());
        routeModel.setDestinationCode(ConsoleInput.getString("Enter Destination Station (Code or Name, e.g., MDU/Madurai): ").toLowerCase().trim());

        if (routeModel.getSourceCode().equals(routeModel.getDestinationCode())) {
            System.out.println(" Source and Destination cannot be the same!");
            return;
        }

        String dateStr = ConsoleInput.getString("Enter Journey Date (dd-MM-yyyy) or 'ANY': ");
        routeModel.setJourneyDateEpoch(null);

        if (!dateStr.equalsIgnoreCase("ANY")) {
            Long epoch = ParseHelper.dateToEpoch(dateStr);
            if (epoch == null) {
                System.out.println(" Invalid Date format! Please use dd-MM-yyyy.");
                return;
            }
            routeModel.setJourneyDateEpoch(epoch);
        }

        System.out.println("\n🔍 Searching for trains from '" + routeModel.getSourceCode().toUpperCase() + "' to '" + routeModel.getDestinationCode().toUpperCase() + "'...");
        executeTrainSearch();
        displayResults();
    }

    private void executeTrainSearch() {
        List<String> results = new ArrayList<>();
        List<TrainSetupRequest> allTrains = db.getAllTrains();

        String srcInput = routeModel.getSourceCode();
        String destInput = routeModel.getDestinationCode();
        Long dateEpoch = routeModel.getJourneyDateEpoch();

        for (TrainSetupRequest train : allTrains) {
            List<String> routeStations = train.getRouteStations();

            int srcIndex = -1;
            int destIndex = -1;

            for (int i = 0; i < routeStations.size(); i++) {
                String stationNameLower = routeStations.get(i).toLowerCase();

                if (srcIndex == -1 && stationNameLower.contains(srcInput)) {
                    srcIndex = i;
                }
                // First occurrence of destination
                if (destIndex == -1 && stationNameLower.contains(destInput)) {
                    destIndex = i;
                }
            }

            if (srcIndex != -1 && destIndex != -1 && srcIndex < destIndex) {
                if (dateEpoch != null) {
                    boolean isScheduled = false;
                    for (ScheduleRequest schedule : db.getAllSchedules()) {
                        if (schedule.getTrainId().equals(train.getId()) && schedule.getJourneyDateEpoch() == dateEpoch) {
                            isScheduled = true;
                            break;
                        }
                    }
                    if (isScheduled) {
                        results.add(String.format("%-10s | %-20s | %-15s", train.getTrainNumber(), train.getTrainName(), ParseHelper.epochToDateString(dateEpoch)));
                    }
                } else {
                    results.add(String.format("%-10s | %-20s | %-15s", train.getTrainNumber(), train.getTrainName(), "Available"));
                }
            }
        }

        routeModel.setSearchResults(results);
    }

    private void displayResults() {
        List<String> results = routeModel.getSearchResults();

        if (results.isEmpty()) {
            System.out.println("\n No Direct Trains Found for this route/date!");
            return;
        }

        System.out.println("\n AVAILABLE TRAINS:");
        System.out.println("--------------------------------------------------");
        System.out.println(String.format("%-10s | %-20s | %-15s", "Train No", "Train Name", "Status/Date"));
        System.out.println("--------------------------------------------------");

        for (String result : results) {
            System.out.println(result);
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Press Enter to continue...");
        ConsoleInput.getString("");
    }
}