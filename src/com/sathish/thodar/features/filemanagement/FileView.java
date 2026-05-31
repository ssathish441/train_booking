package com.sathish.thodar.features.filemanagement;

import com.sathish.thodar.data.repository.ThodarDB;
import com.sathish.thodar.util.ConsoleInput;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class FileView {

    private final FileModel fileModel = new FileModel();
    private final ThodarDB db = ThodarDB.getInstance();

    public FileView() {
        File directory = new File(fileModel.getBackupDirectory());
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public void showFileManagementMenu() {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("    DATA MANAGEMENT SYSTEM    ");
            System.out.println("=================================");
            System.out.println("1. Backup All Data (Save to Disk)");
            System.out.println("2. Restore All Data (Load from Disk)");
            System.out.println("0. Back to Main Menu");

            int choice = ConsoleInput.getInt("Select Option: ");

            switch (choice) {
                case 1:

                    if (saveAllData()) {
                        System.out.println("Data successfully backed up!");
                    }
                    break;
                case 2:
                    System.out.println("\nRestoring data from disk...");
                    if (loadAllData()) {
                        System.out.println(" Data successfully restored!");
                    } else {
                        System.out.println("No backup files found ");
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("❌Invalid Option!");
            }
        }
    }

    public boolean saveAllData() {
        try {
            String dir = fileModel.getBackupDirectory();
            if (!dir.endsWith("/") && !dir.endsWith("\\")) {
                dir += "/";
            }

            saveToFile(dir + fileModel.getTrainsFile(), db.getAllTrains());
            saveToFile(dir + fileModel.getSchedulesFile(), db.getAllSchedules());
            saveToFile(dir + fileModel.getTicketsFile(), db.getAllTickets());
            saveToFile(dir + "users.dat", db.getAllUsers());

            saveToFile(dir + "train_wise_bookings_report.dat", generateTrainWiseBinaryData());
            exportSeparateTextReports(dir);

            return true;
        } catch (Exception e) {
            System.out.println(" Data Save Error: " + e.getMessage());
            return false;
        }
    }

    private void saveToFile(String filePath, Object data) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
        }
    }

    //  BINARY REPORT FORMAT
    private java.util.List<String> generateTrainWiseBinaryData() {
        java.util.List<String> binaryReportList = new java.util.ArrayList<>();

        binaryReportList.add("PassengerName,Age,TrainNo,TrainName,From,To,TravelDate,DepTime,ArrTime,SeatNo,Status");

        if (db.getAllTickets() != null) {
            Set<String> uniqueRows = new HashSet<>();

            for (com.sathish.thodar.data.dto.request.passenger.BookingRequest b : db.getAllTickets()) {
                com.sathish.thodar.data.dto.request.admin.ScheduleRequest s = db.getScheduleById(b.getScheduleId());
                if (s == null) continue;

                com.sathish.thodar.data.dto.request.admin.TrainSetupRequest t = db.getTrainById(s.getTrainId());
                if (t == null) continue;

                String travelDate = com.sathish.thodar.util.ParseHelper.epochToDateString(s.getJourneyDateEpoch());
                String bTimeStr = (s.getTimetable() != null && b.getBoardingStation() != null) ? s.getTimetable().get(b.getBoardingStation()) : "N/A";
                String dTimeStr = (s.getTimetable() != null && b.getDropStation() != null) ? s.getTimetable().get(b.getDropStation()) : "N/A";

                String fromStation = b.getBoardingStation() != null ? b.getBoardingStation().split("/")[0] : "N/A";
                String toStation = b.getDropStation() != null ? b.getDropStation().split("/")[0] : "N/A";

                if (b.getPassengers() != null) {
                    for (com.sathish.thodar.data.dto.request.passenger.BookingRequest.PassengerDetail pd : b.getPassengers()) {

                        String row = pd.name + "," +
                                pd.age + "," +
                                t.getTrainNumber() + "," +
                                t.getTrainName() + "," +
                                fromStation + "," +
                                toStation + "," +
                                travelDate + "," +
                                bTimeStr.replace("Dep:", "").trim() + "," +
                                dTimeStr.replace("Arr:", "").trim() + "," +
                                (pd.currentCoachSeat != null ? pd.currentCoachSeat : "N/A") + "," +
                                (pd.currentStatus != null ? pd.currentStatus : b.getStatus().toString());

                        uniqueRows.add(row);
                    }
                }
            }
            binaryReportList.addAll(uniqueRows);
        }
        return binaryReportList;
    }

    private void exportSeparateTextReports(String dir) {
        System.out.println(" Generating Text Reports in: " + dir);

        try (java.io.PrintWriter w = new java.io.PrintWriter(new java.io.FileWriter(dir + "users_report.txt"))) {
            w.println("--- REGISTERED USERS ---");
            if (db.getAllUsers() != null) {
                for (com.sathish.thodar.data.dto.entity.User u : db.getAllUsers()) {
                    w.println("Name: " + u.getName() + " | Email: " + u.getEmail() + " | Role: " + u.getRole() + " | Wallet: Rs. " + u.getWalletBalance());
                }
            }
        } catch (Exception e) {}

        try (java.io.PrintWriter w = new java.io.PrintWriter(new java.io.FileWriter(dir + "trains_report.txt"))) {
            w.println("--- TRAIN DETAILS ---");
            if (db.getAllTrains() != null) {
                for (com.sathish.thodar.data.dto.request.admin.TrainSetupRequest t : db.getAllTrains()) {
                    w.println("Train: " + t.getTrainNumber() + " - " + t.getTrainName() + " | Route: " + t.getSourceStation() + " -> " + t.getDestinationStation());
                }
            }
        } catch (Exception e) {}

        try (java.io.PrintWriter w = new java.io.PrintWriter(new java.io.FileWriter(dir + "tickets_report.txt"))) {
            w.println("--- BOOKED TICKETS ---");
            if (db.getAllTickets() != null) {
                for (com.sathish.thodar.data.dto.request.passenger.BookingRequest b : db.getAllTickets()) {
                    w.println("PNR: " + b.getPnrNumber() + " | UserID: " + b.getUserId() + " | Status: " + b.getStatus() + " | Fare: Rs. " + b.getTotalFare());
                }
            }
        } catch (Exception e) {}

        try (java.io.PrintWriter w = new java.io.PrintWriter(new java.io.FileWriter(dir + "schedules_report.txt"))) {
            w.println("--- TRAIN SCHEDULES ---");
            if (db.getAllSchedules() != null) {
                for (com.sathish.thodar.data.dto.request.admin.ScheduleRequest s : db.getAllSchedules()) {
                    w.println("Train ID: " + s.getTrainId() + " | Date: " + com.sathish.thodar.util.ParseHelper.epochToDateString(s.getJourneyDateEpoch()) + " | Status: " + s.getStatus());
                }
            }
        } catch (Exception e) {}

        // TRAIN-WISE BOOKINGS REPORT TXT
        try (java.io.PrintWriter w = new java.io.PrintWriter(new java.io.FileWriter(dir + "train_wise_bookings_report.txt"))) {
            w.println("===============================================================================================================================================================");
            w.println("                                                            THODAR RAILWAYS - TRAIN WISE BOOKINGS REPORT                                                       ");
            w.println("===============================================================================================================================================================");

            w.println(String.format("%-15s | %-3s | %-8s | %-20s | %-12s | %-12s | %-12s | %-10s | %-10s | %-10s | %-10s",
                    "PassengerName", "Age", "Train No", "Train Name", "From", "To", "Travel Date", "Dep Time", "Arr Time", "Seat No", "Status"));
            w.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");

            if (db.getAllTickets() != null) {
                Set<String> printedRows = new HashSet<>();

                for (com.sathish.thodar.data.dto.request.passenger.BookingRequest b : db.getAllTickets()) {
                    com.sathish.thodar.data.dto.request.admin.ScheduleRequest s = db.getScheduleById(b.getScheduleId());
                    if (s == null) continue;

                    com.sathish.thodar.data.dto.request.admin.TrainSetupRequest t = db.getTrainById(s.getTrainId());
                    if (t == null) continue;

                    String travelDate = com.sathish.thodar.util.ParseHelper.epochToDateString(s.getJourneyDateEpoch());
                    String bTimeStr = (s.getTimetable() != null && b.getBoardingStation() != null) ? s.getTimetable().get(b.getBoardingStation()) : "N/A";
                    String dTimeStr = (s.getTimetable() != null && b.getDropStation() != null) ? s.getTimetable().get(b.getDropStation()) : "N/A";

                    String fromStation = b.getBoardingStation() != null ? b.getBoardingStation().split("/")[0] : "N/A";
                    String toStation = b.getDropStation() != null ? b.getDropStation().split("/")[0] : "N/A";

                    if (b.getPassengers() != null) {
                        for (com.sathish.thodar.data.dto.request.passenger.BookingRequest.PassengerDetail pd : b.getPassengers()) {

                            String row = String.format("%-15s | %-3d | %-8s | %-20s | %-12s | %-12s | %-12s | %-10s | %-10s | %-10s | %-10s",
                                    pd.name,
                                    pd.age,
                                    t.getTrainNumber(),
                                    t.getTrainName(),
                                    fromStation,
                                    toStation,
                                    travelDate,
                                    bTimeStr.replace("Dep:", "").trim(),
                                    dTimeStr.replace("Arr:", "").trim(),
                                    pd.currentCoachSeat != null ? pd.currentCoachSeat : "N/A",
                                    pd.currentStatus != null ? pd.currentStatus : b.getStatus().toString()
                            );

                            if (printedRows.add(row)) {
                                w.println(row);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}

        System.out.println("Text Reports  generated successfully");
    }

    private boolean loadAllData() {
        try {
            String dir = fileModel.getBackupDirectory();
            if (!dir.endsWith("/") && !dir.endsWith("\\")) {
                dir += "/";
            }

            db.setAllTrains((java.util.List) loadFromFile(dir + fileModel.getTrainsFile()));
            db.setAllSchedules((java.util.List) loadFromFile(dir + fileModel.getSchedulesFile()));
            db.setAllTickets((java.util.List) loadFromFile(dir + fileModel.getTicketsFile()));

            try {
                db.setAllUsers((java.util.List) loadFromFile(dir + "users.dat"));
            } catch (FileNotFoundException e) {}

            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (Exception e) {
            System.out.println("❌ Data Load Error: " + e.getMessage());
            return false;
        }
    }

    private Object loadFromFile(String filePath) throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) throw new FileNotFoundException();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return ois.readObject();
        }
    }

    public void autoLoadOnStartup() {
        System.out.println("Checking for previous data backups...");
        if (loadAllData()) {
            System.out.println(" Previous data restored successfully.");
        } else {
            System.out.println("No previous data found. Starting fresh.");
        }
    }
}