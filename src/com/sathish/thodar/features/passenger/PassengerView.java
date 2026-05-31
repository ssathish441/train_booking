package com.sathish.thodar.features.passenger;

import com.sathish.thodar.util.ConsoleInput;
import com.sathish.thodar.util.ParseHelper;
import com.sathish.thodar.data.repository.ThodarDB;
import com.sathish.thodar.data.dto.enums.TicketClass;
import com.sathish.thodar.data.dto.enums.TicketQuota;
import com.sathish.thodar.data.dto.enums.TicketStatus;
import com.sathish.thodar.data.dto.request.admin.TrainSetupRequest;
import com.sathish.thodar.data.dto.request.admin.ScheduleRequest;
import com.sathish.thodar.data.dto.request.passenger.BookingRequest;
import com.sathish.thodar.data.dto.response.auth.AuthResponse;
import com.sathish.thodar.data.dto.response.passenger.LiveStatusResponse;
import com.sathish.thodar.data.dto.response.passenger.TicketSummaryResponse;
import com.sathish.thodar.data.dto.response.passenger.Transaction;
import com.sathish.thodar.features.support.SupportView;
import com.sathish.thodar.features.core.TrainService;
import com.sathish.thodar.data.dto.entity.User;
import com.sathish.thodar.features.notification.NotificationView;
import com.sathish.thodar.features.filemanagement.FileView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PassengerView {

    private final ThodarDB db = ThodarDB.getInstance();
    private final SupportView supportView = new SupportView();
    private final Random random = new Random();

    private final AuthResponse loggedInUser;
    private final User loggedInUserEntity;

    public PassengerView(AuthResponse loggedInUser, User loggedInUserEntity) {
        this.loggedInUser = loggedInUser;
        this.loggedInUserEntity = loggedInUserEntity;
    }

    public void showPassengerMenu() {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("       PASSENGER DASHBOARD       ");
            System.out.println("=================================");
            System.out.println("Wallet Balance: Rs. " + loggedInUserEntity.getWalletBalance());
            System.out.println("---------------------------------");
            System.out.println("1. Book Ticket");
            System.out.println("2. Cancel Ticket");
            System.out.println("3. Transaction History");
            System.out.println("4. View My Tickets");
            System.out.println("5. Support Helpdesk");
            System.out.println("6. Recharge Wallet");
            System.out.println("7. Journey Planner (Search Route)");
            System.out.println("8. Logout");


            String choice = ConsoleInput.getString("Choice: ").trim();

            switch (choice) {
                case "1":
                    handleBookTicket();
                    break;
                case "2":
                    handleCancelTicket();
                    break;
                case "3":
                    viewTransactionHistory();
                    break;
                case "4":
                    handleViewMyTickets();
                    break;
                case "5":
                    handlePassengerSupport();
                    break;
                case "6":
                    handleRechargeWallet();
                    break;

                case "7":
                    new com.sathish.thodar.features.journeyplanning.RouteView().showSearchScreen();
                    break;

                case "8":
                    System.out.println("Passenger logged out.");
                    return;
                default:
                    System.out.println("[ERROR] Invalid option.");
            }
        }
    }

    private void handleRechargeWallet() {
        double amt = ConsoleInput.getDouble("Enter amount to recharge (Rs): ");

        if (amt > 0) {
            loggedInUserEntity.setWalletBalance(loggedInUserEntity.getWalletBalance() + amt);
            db.addTransaction(new Transaction("WALLET", loggedInUser.getId(), amt, "CREDIT", "Recharge"));
            System.out.println("Recharged Rs. " + amt + ". New Balance: Rs. " + loggedInUserEntity.getWalletBalance());
        } else {
            System.out.println("Invalid amount.");
        }
    }

    private void handleBookTicket() {
        System.out.println("\n--- SEARCH TRAINS ---");
        String fromIn = ConsoleInput.getString("From Station (Code/Name): ").trim();
        String toIn = ConsoleInput.getString("To Station (Code/Name): ").trim();

        if (fromIn.equalsIgnoreCase(toIn)) {
            System.out.println("Error: Source and Destination stations cannot be the same!");
            return;
        }

        String dateIn = ConsoleInput.getString("Journey Date (dd-MM-yyyy): ").trim();

        boolean routeFound = false;
        List<ScheduleRequest> availableSchedules = new ArrayList<>();
        List<TrainSetupRequest> availableTrains = new ArrayList<>();

        for (TrainSetupRequest t : db.getAllTrains()) {
            for (ScheduleRequest s : db.getSchedulesForTrain(t.getId())) {
                String schedDate = ParseHelper.epochToDateString(s.getJourneyDateEpoch());
                if (!schedDate.equals(dateIn)) {
                    continue;
                }

                List<String> r = t.getRouteStations();
                int fIdx = -1, tIdx = -1;

                for (int i = 0; i < r.size(); i++) {
                    String[] parts = r.get(i).split("/");
                    String code = parts[0].trim();
                    String name = parts.length > 1 ? parts[1].trim() : code;

                    if (code.equalsIgnoreCase(fromIn) || name.equalsIgnoreCase(fromIn))
                        fIdx = i;
                    if (code.equalsIgnoreCase(toIn) || name.equalsIgnoreCase(toIn))
                        tIdx = i;
                }

                if (fIdx != -1 && tIdx != -1 && fIdx < tIdx) {
                    routeFound = true;
                    availableSchedules.add(s);
                    availableTrains.add(t);
                }
            }
        }

        if (!routeFound) {
            System.out.println("\nNo trains found for the selected Route & Date.");
            return;
        }

        System.out.println("\nSelect Quota: 1. GENERAL | 2. TATKAL");
        TicketQuota quota = (ConsoleInput.getInt("Choice: ") == 2) ? TicketQuota.TATKAL : TicketQuota.GENERAL;

        List<LiveStatusResponse> results = new ArrayList<>();
        // 👉 Validation List for Smart Selection
        List<Long> validScheduleIds = new ArrayList<>();

        for (int i = 0; i < availableSchedules.size(); i++) {
            ScheduleRequest s = availableSchedules.get(i);
            TrainSetupRequest t = availableTrains.get(i);

            if (quota == TicketQuota.TATKAL) {
                long diff = s.getJourneyDateEpoch() - System.currentTimeMillis();
                long oneDayMs = 24L * 60 * 60 * 1000;
                if (diff > oneDayMs || diff < 0) {
                    continue;
                }
            }


            validScheduleIds.add(s.getId());

            List<String> r = t.getRouteStations();
            int fIdx = -1, tIdx = -1;
            String fName = "", tName = "";

            for (int j = 0; j < r.size(); j++) {
                String[] parts = r.get(j).split("/");
                String code = parts[0].trim();
                String name = parts.length > 1 ? parts[1].trim() : code;

                if (code.equalsIgnoreCase(fromIn) || name.equalsIgnoreCase(fromIn)) {
                    fIdx = j;
                    fName = code + "/" + name;
                }
                if (code.equalsIgnoreCase(toIn) || name.equalsIgnoreCase(toIn)) {
                    tIdx = j;
                    tName = code + "/" + name;
                }
            }

            LiveStatusResponse res = new LiveStatusResponse();
            res.scheduleId = s.getId();
            res.trainInfo = t.getTrainNumber() + " - " + t.getTrainName();
            res.route = fName + " to " + tName;
            res.date = dateIn;

            String boardFull = r.get(fIdx);
            String dropFull = r.get(tIdx);

            String boardTimeInfo = (s.getTimetable() != null && s.getTimetable().containsKey(boardFull))
                    ? s.getTimetable().get(boardFull)
                    : "Time not set";
            String dropTimeInfo = (s.getTimetable() != null && s.getTimetable().containsKey(dropFull))
                    ? s.getTimetable().get(dropFull)
                    : "Time not set";

            res.avail1A = TrainService.getAvail(s.getId(), TicketClass.AC_1A, boardFull, quota);
            res.avail2A = TrainService.getAvail(s.getId(), TicketClass.AC_2A, boardFull, quota);
            res.avail3A = TrainService.getAvail(s.getId(), TicketClass.AC_3A, boardFull, quota);
            res.availSL = TrainService.getAvail(s.getId(), TicketClass.SL, boardFull, quota);

            System.out.println("\n======================================================================");
            System.out.println("[" + res.scheduleId + "] " + res.trainInfo + " | Date: " + res.date);
            System.out.println("Boarding: " + fName + " -> " + boardTimeInfo);
            System.out.println("Dropping: " + tName + " -> " + dropTimeInfo);
            System.out.println("----------------------------------------------------------------------");

            List<String> availTexts = new ArrayList<>();
            if (!res.avail1A.equals("N/A"))
                availTexts.add("1A : " + String.format("%-15s", res.avail1A));
            if (!res.avail2A.equals("N/A"))
                availTexts.add("2A : " + String.format("%-15s", res.avail2A));
            if (!res.avail3A.equals("N/A"))
                availTexts.add("3A : " + String.format("%-15s", res.avail3A));
            if (!res.availSL.equals("N/A"))
                availTexts.add("SL : " + String.format("%-15s", res.availSL));

            for (int k = 0; k < availTexts.size(); k += 2) {
                if (k + 1 < availTexts.size()) {
                    System.out.println("  " + availTexts.get(k) + " |  " + availTexts.get(k + 1));
                } else {
                    System.out.println("  " + availTexts.get(k));
                }
            }
            System.out.println("======================================================================");

            results.add(res);
        }

        if (results.isEmpty()) {
            System.out.println("\n[No seats available under Tatkal. (Opens 1 day prior)]");
            return;
        }


        Long sId = 0L;
        if (validScheduleIds.isEmpty()) {
            System.out.println(" Error: Valid schedules could not be mapped.");
            return;
        } else if (validScheduleIds.size() == 1) {
            sId = validScheduleIds.get(0);
            System.out.println("\n Automatically selected Schedule ID: [" + sId + "]");
        } else {
            while (true) {
                sId = ConsoleInput.getLong("\nEnter Schedule ID to Book (0 to cancel): ");
                if (sId == 0) return;

                if (validScheduleIds.contains(sId)) {
                    break;
                } else {
                    System.out.println("Invalid Schedule ID! Please select a valid ID from the printed list above.");
                }
            }
        }

        int c = ConsoleInput.getInt("Select Class (1:1A, 2:2A, 3:3A, 4:SL): ");
        TicketClass tClass = (c == 1) ? TicketClass.AC_1A
                : (c == 2) ? TicketClass.AC_2A : (c == 3) ? TicketClass.AC_3A : TicketClass.SL;

        TrainSetupRequest selTrain = db.getTrainById(db.getScheduleById(sId).getTrainId());
        String fBoard = "";
        String fDrop = "";

        for (String station : selTrain.getRouteStations()) {
            String[] parts = station.split("/");
            if (parts[0].equalsIgnoreCase(fromIn) || (parts.length > 1 && parts[1].equalsIgnoreCase(fromIn))) {
                fBoard = station;
            }
            if (parts[0].equalsIgnoreCase(toIn) || (parts.length > 1 && parts[1].equalsIgnoreCase(toIn))) {
                fDrop = station;
            }
        }

        String availStatus = TrainService.getAvail(sId, tClass, fBoard, quota);
        if (availStatus.equals("REGRET") || availStatus.equals("N/A")) {
            System.out.println("\nNo seats available for the selected class.");
            return;
        }

        int count = ConsoleInput.getInt("\nEnter Passengers Count (Max 6): ");
        List<BookingRequest.PassengerDetail> paxList = new ArrayList<>();
        System.out.println("\n--- PASSENGER DETAILS ---");

        for (int i = 1; i <= count; i++) {
            BookingRequest.PassengerDetail pd = new BookingRequest.PassengerDetail();
            pd.name = ConsoleInput.getString("Passenger " + i + " Name: ");
            pd.age = ConsoleInput.getInt("Age: ");

            String gender;
            while (true) {
                gender = ConsoleInput.getString("Gender (M/F): ").trim().toUpperCase();
                if (gender.equals("M") || gender.equals("F")) {
                    break;
                }
                System.out.println("Invalid Input! Please enter 'M' or 'F'.");
            }
            pd.gender = gender;
            paxList.add(pd);
        }

        double baseFare = ((c == 1) ? 2000 : (c == 2) ? 1500 : (c == 3) ? 1000 : 500);
        double total = count * (quota == TicketQuota.TATKAL ? baseFare * 1.3 : baseFare);

        System.out.println("\n--- PAYMENT PAGE ---");
        System.out.println("Total Fare: Rs. " + total);
        System.out.println("1. UPI");
        System.out.println("2. Wallet (Current Balance: Rs. " + loggedInUserEntity.getWalletBalance() + ")");
        System.out.println("3. Credit / Debit Card");

        int payChoice = ConsoleInput.getInt("Select Payment Method (1-3): ");

        if (payChoice == 2 && loggedInUserEntity.getWalletBalance() < total) {
            System.out.println("\nInsufficient Wallet Balance! Please recharge from Passenger Menu.");
            return;
        }

        System.out.println("\nConnecting to Payment Gateway...");
        try {
            System.out.print("Processing");
            for (int i = 0; i < 4; i++) {
                Thread.sleep(700);
                System.out.print(".");
            }
            System.out.println();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        double payAmt = ConsoleInput
                .getDouble("\nPlease enter the exact amount (Rs. " + total + ") to confirm booking: ");

        if (payAmt == total) {
            if (payChoice == 2) {
                loggedInUserEntity.setWalletBalance(loggedInUserEntity.getWalletBalance() - total);
            }

            BookingRequest b = new BookingRequest();
            b.setUserId(loggedInUser.getId());
            b.setScheduleId(sId);
            b.setTicketClass(tClass);
            b.setQuota(quota);
            b.setBoardingStation(fBoard);
            b.setDropStation(fDrop);
            b.setPassengerCount(count);
            b.setPnrNumber("PNR" + (random.nextInt(90000) + 10000));

            for (BookingRequest.PassengerDetail pd : paxList) {
                b.addPassenger(pd);
            }

            b.setTotalFare(total);
            b.setStatus(TicketStatus.CNF);
            db.addTicket(b);
            TrainService.recalculateWaitlist(sId, tClass);

            String payMode = (payChoice == 1) ? "UPI" : (payChoice == 2) ? "WALLET" : "CARD";
            db.addTransaction(
                    new Transaction(b.getPnrNumber(), loggedInUser.getId(), total, "DEBIT", "Booking via " + payMode));

            System.out.println("\n[SUCCESS] Payment Received! Ticket Booked via " + payMode + ".");
            if (payChoice == 2) {
                System.out.println("Remaining Wallet Balance: Rs. " + loggedInUserEntity.getWalletBalance());
            }


            try {
                NotificationView notifier = new NotificationView();
                notifier.sendDetailedBookingEmail(loggedInUserEntity.getEmail(), b);
            } catch (Throwable e) {
                System.out.println(" Email skipped!");
                e.printStackTrace();
            }


            try {
                FileView fileManagement = new FileView();
                fileManagement.saveAllData();
            } catch (Exception e) {}

            printTicket(db.getTicketByPnr(b.getPnrNumber()));
        } else {
            System.out.println("\n[ERROR] Incorrect amount entered. Booking Cancelled!");
        }
    }

    private void handleCancelTicket() {
        System.out.println("\n--- CANCEL TICKET ---");
        String pnr = ConsoleInput.getString("Enter PNR Number to cancel: ");
        BookingRequest t = db.getTicketByPnr(pnr);

        if (t != null && t.getStatus() != TicketStatus.CAN) {
            double refund = t.getTotalFare() * 0.80;
            System.out.println("Refund Amount: Rs. " + refund + " (20% Fee applied)");

            if (ConsoleInput.getString("Confirm cancellation? (Y/N): ").equalsIgnoreCase("Y")) {
                t.setStatus(TicketStatus.CAN);

                for (BookingRequest.PassengerDetail pd : t.getPassengers()) {
                    pd.currentStatus = "CANCELLED";
                    pd.currentCoachSeat = "N/A";
                }

                Transaction bookingTxn = null;
                for (Transaction tx : db.getTransactionsByUserId(loggedInUser.getId())) {
                    if (tx.pnr.equals(pnr) && tx.type.equals("DEBIT")) {
                        bookingTxn = tx;
                        break;
                    }
                }

                String payMode = "WALLET";
                if (bookingTxn != null) {
                    if (bookingTxn.remark.contains("UPI")) {
                        payMode = "UPI";
                    } else if (bookingTxn.remark.contains("CARD")) {
                        payMode = "CARD";
                    }
                }

                if (payMode.equals("WALLET")) {
                    loggedInUserEntity.setWalletBalance(loggedInUserEntity.getWalletBalance() + refund);
                    System.out.println("Ticket Cancelled & Refunded to Wallet.");
                } else {
                    System.out.println(
                            "Ticket Cancelled. Refund of Rs." + refund + " initiated to original payment method ("
                                    + payMode + "). It will reflect in 3-5 working days.");
                }

                db.addTransaction(
                        new Transaction(pnr, loggedInUser.getId(), refund, "CREDIT", "Ticket Refund to " + payMode));
                TrainService.recalculateWaitlist(t.getScheduleId(), t.getTicketClass());

                try {
                    com.sathish.thodar.features.filemanagement.FileView fileManagement = new com.sathish.thodar.features.filemanagement.FileView();
                    fileManagement.saveAllData();
                } catch (Exception e) {}
            }
        } else {
            System.out.println("Invalid PNR or Ticket already cancelled.");
        }
    }

    private void viewTransactionHistory() {
        System.out.println("\n--- TRANSACTION HISTORY ---");
        List<Transaction> txns = db.getTransactionsByUserId(loggedInUser.getId());

        if (txns.isEmpty()) {
            System.out.println("No transactions recorded.");
            return;
        }

        System.out.println(
                String.format("%-12s | %-10s | %-8s | %-8s | %-15s", "TXN ID", "PNR", "AMT", "TYPE", "REMARK"));
        System.out.println("------------------------------------------------------------------");
        for (Transaction txn : txns) {
            System.out.println(String.format("%-12s | %-10s | %-8.2f | %-8s | %-15s", txn.tId, txn.pnr, txn.amount,
                    txn.type, txn.remark));
        }
    }

    private void handleViewMyTickets() {
        System.out.println("\n--- MY TICKETS ---");
        List<BookingRequest> myTickets = db.getTicketsForUser(loggedInUser.getId());

        if (myTickets.isEmpty()) {
            System.out.println("No tickets booked.");
            return;
        }

        for (BookingRequest t : myTickets) {
            printTicket(t);
        }
    }

    private void printTicket(BookingRequest t) {
        ScheduleRequest s = db.getScheduleById(t.getScheduleId());
        TrainSetupRequest train = db.getTrainById(s.getTrainId());

        TicketSummaryResponse res = new TicketSummaryResponse();
        res.pnrNumber = t.getPnrNumber();
        res.trainDetails = train.getTrainNumber() + " - " + train.getTrainName();

        String bTimeStr = (s.getTimetable() != null) ? s.getTimetable().get(t.getBoardingStation()) : null;
        String dTimeStr = (s.getTimetable() != null) ? s.getTimetable().get(t.getDropStation()) : null;

        String bDep = "Time N/A";
        if (bTimeStr != null) {
            if (bTimeStr.contains("|")) {
                for (String p : bTimeStr.split("\\|")) {
                    if (p.trim().startsWith("Dep:"))
                        bDep = p.trim();
                }
            } else {
                bDep = bTimeStr.trim();
            }
        }

        String dArr = "Time N/A";
        if (dTimeStr != null) {
            if (dTimeStr.contains("|")) {
                for (String p : dTimeStr.split("\\|")) {
                    if (p.trim().startsWith("Arr:"))
                        dArr = p.trim();
                }
            } else {
                dArr = dTimeStr.trim();
            }
        }

        res.routeDetails = t.getBoardingStation() + " (" + bDep + ") TO " + t.getDropStation() + " (" + dArr + ")";
        res.journeyDate = ParseHelper.epochToDateString(s.getJourneyDateEpoch());
        res.ticketClass = t.getTicketClass().toString();
        res.mainStatus = (t.getStatus() == TicketStatus.CAN) ? "CANCELLED" : "BOOKED";

        for (BookingRequest.PassengerDetail pd : t.getPassengers()) {
            TicketSummaryResponse.PassengerResponse pr = new TicketSummaryResponse.PassengerResponse();
            pr.name = pd.name;
            pr.age = pd.age;
            pr.gender = pd.gender;
            pr.bookingStatusInfo = pd.bookingStatus + " (" + pd.bookingCoachSeat + ")";
            pr.currentStatusInfo = pd.currentStatus + " (" + pd.currentCoachSeat + ")";
            res.passengers.add(pr);
        }

        System.out.println("\n==================================================================================================");
        System.out.println("PNR: " + res.pnrNumber + " | Train: " + res.trainDetails + " | Quota: " + t.getQuota());
        System.out.println("Route: " + res.routeDetails + " | Date: " + res.journeyDate);
        System.out.println("Class: " + res.ticketClass + " | Ticket Status: " + res.mainStatus);
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.println(String.format("%-15s | %-3s | %-6s | %-22s | %-22s", "Name", "Age", "Gender", "Booking Status", "Current Status"));
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (TicketSummaryResponse.PassengerResponse pr : res.passengers) {
            System.out.println(String.format("%-15s | %-3d | %-6s | %-22s | %-22s", pr.name, pr.age, pr.gender,
                    pr.bookingStatusInfo, pr.currentStatusInfo));
        }
        System.out.println("==================================================================================================");
    }

    private void handlePassengerSupport() {
        supportView.handlePassengerSupport(loggedInUser.getId());
    }

}