package com.sathish.thodar.features.booking;

import com.sathish.thodar.data.dto.entity.User;
import com.sathish.thodar.data.dto.request.admin.ScheduleRequest;
import com.sathish.thodar.data.dto.request.admin.TrainSetupRequest;
import com.sathish.thodar.data.dto.request.passenger.BookingRequest;
import com.sathish.thodar.data.dto.response.passenger.Transaction;
import com.sathish.thodar.data.dto.enums.TicketStatus;
import com.sathish.thodar.data.repository.ThodarDB;
import com.sathish.thodar.features.notification.NotificationView;
import com.sathish.thodar.features.filemanagement.FileView;
import com.sathish.thodar.util.ConsoleInput;

public class BookingView {

    private final BookingModel model = new BookingModel();
    private final ThodarDB db = ThodarDB.getInstance();
    private final NotificationView notifier = new NotificationView();

    // Main entry point for passenger
    public void showBookingMenu(User loggedInUser) {
        System.out.println("\n=================================");
        System.out.println("     TICKET BOOKING SYSTEM    ");
        System.out.println("=================================");

        // 1. Show Available Schedules
        System.out.println("Available Schedules:");
        for (ScheduleRequest s : db.getAllSchedules()) {
            TrainSetupRequest t = db.getTrainById(s.getTrainId());
            if (t != null) {
                System.out.println("ID: " + s.getId() + " | Train: " + t.getTrainName() + " (" + t.getTrainNumber() + ")");
            }
        }


        model.setSelectedScheduleId((long) ConsoleInput.getInt("\nEnter Schedule ID to Book (0 to cancel): "));
        if (model.getSelectedScheduleId() == 0) return;

        ScheduleRequest schedule = db.getScheduleById(model.getSelectedScheduleId());
        if (schedule == null) {
            System.out.println("❌ Invalid Schedule ID!");
            return;
        }

        model.setPassengerName(ConsoleInput.getString("Enter Passenger Name: "));

        System.out.println("Classes: 1. AC (₹1500) | 2. Sleeper (₹600) | 3. General (₹200)");
        int classChoice = ConsoleInput.getInt("Select Class (1/2/3): ");

        if (classChoice == 1) { model.setTicketClass("AC"); model.setTicketPrice(1500.0); }
        else if (classChoice == 2) { model.setTicketClass("Sleeper"); model.setTicketPrice(600.0); }
        else { model.setTicketClass("General"); model.setTicketPrice(200.0); }


        String[] seatAndStatus = generateAutoSeatAndStatus(schedule.getId(), model.getTicketClass());

        System.out.println("\n⏳ Processing Payment of ₹" + model.getTicketPrice() + "...");

        // Save Ticket
        BookingRequest newTicket = new BookingRequest();
        newTicket.setScheduleId(schedule.getId());
        newTicket.setUserId(loggedInUser.getId());
        newTicket.setPassengerId(loggedInUser.getId());

        //Dynamic Booking
        newTicket.setSeatNumber(seatAndStatus[0]);
        newTicket.setStatus(TicketStatus.valueOf(seatAndStatus[1]));
        newTicket.setBookingTime(System.currentTimeMillis());

        BookingRequest savedTicket = db.addTicket(newTicket);

        // Save Transaction
        Transaction trans = new Transaction(
                savedTicket.getPnrNumber(),
                loggedInUser.getId(),
                model.getTicketPrice(),
                "DEBIT",
                "Ticket Booking"
        );
        db.addTransaction(trans);


        System.out.println("TICKET BOOKED SUCCESSFULLY!");
        System.out.println("PNR: " + savedTicket.getPnrNumber() + " | Seat: " + savedTicket.getSeatNumber() + " | Status: " + savedTicket.getStatus());


        try {
            FileView fileManagement = new FileView();
            fileManagement.saveAllData();
        } catch (Exception e) {}

        TrainSetupRequest bookedTrain = db.getTrainById(schedule.getTrainId());
        if (loggedInUser.getEmail() != null) {
            notifier.sendDetailedBookingEmail(loggedInUser.getEmail(), savedTicket);
        }
    }


    // AUTO-SEAT ALLOCATION

    private String[] generateAutoSeatAndStatus(Long scheduleId, String ticketClass) {
        int existingBookings = 0;

        if (db.getAllTickets() != null) {
            for (BookingRequest ticket : db.getAllTickets()) {
                if (ticket.getScheduleId().equals(scheduleId) &&
                        ticket.getSeatNumber() != null &&
                        getTicketClassFromSeat(ticket.getSeatNumber()).equals(ticketClass)) {
                    existingBookings++;
                }
            }
        }

        int maxCnf = 0, maxRac = 0;
        String coachPrefix = "";

        switch (ticketClass.toUpperCase()) {
            case "AC": maxCnf = 54; maxRac = 10; coachPrefix = "B"; break;
            case "SLEEPER": maxCnf = 72; maxRac = 18; coachPrefix = "S"; break;
            case "GENERAL": maxCnf = 100; maxRac = 0; coachPrefix = "G"; break;
            default: maxCnf = 72; maxRac = 18; coachPrefix = "S"; break;
        }

        String allocatedSeat = "";
        String status = "";

        //  CONFIRM SEAT
        if (existingBookings < maxCnf) {
            int nextAvailable = existingBookings + 1;
            int coachNumber = ((nextAvailable - 1) / 72) + 1;
            int seatNumberInCoach = ((nextAvailable - 1) % 72) + 1;
            String berthType = calculateBerthType(seatNumberInCoach);

            allocatedSeat = coachPrefix + coachNumber + "/" + seatNumberInCoach + " (" + berthType + ")";
            status = "CNF";
        }
        //  RAC
        else if (existingBookings < (maxCnf + maxRac)) {
            int racNumber = existingBookings - maxCnf + 1;
            allocatedSeat = "RAC " + racNumber;
            status = "RAC";
        }
        // WAITLIST
        else {
            int wlNumber = existingBookings - (maxCnf + maxRac) + 1;
            allocatedSeat = "WL " + wlNumber;
            status = "WL";
        }

        return new String[]{allocatedSeat, status};
    }

    private String getTicketClassFromSeat(String seatNo) {
        if (seatNo.startsWith("B")) return "AC";
        if (seatNo.startsWith("S") && !seatNo.startsWith("SU") && !seatNo.startsWith("SL")) return "Sleeper";
        if (seatNo.startsWith("G") && !seatNo.startsWith("GN")) return "General";
        if (seatNo.contains("RAC") || seatNo.contains("WL")) return "Sleeper";
        return "Sleeper";
    }

    private String calculateBerthType(int seatNumber) {
        int positionInCabin = seatNumber % 8;
        switch (positionInCabin) {
            case 1: case 4: return "LB";
            case 2: case 5: return "MB";
            case 3: case 6: return "UB";
            case 7: return "SL";
            case 0: return "SU";
            default: return "UB";
        }
    }
}