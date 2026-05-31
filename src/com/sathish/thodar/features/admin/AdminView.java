package com.sathish.thodar.features.admin;

import com.sathish.thodar.util.ConsoleInput;
import com.sathish.thodar.util.ParseHelper;
import com.sathish.thodar.data.repository.ThodarDB;
import com.sathish.thodar.data.repository.StationMaster;
import com.sathish.thodar.data.dto.response.Station;
import com.sathish.thodar.data.dto.enums.TicketClass;
import com.sathish.thodar.data.dto.enums.TicketQuota;
import com.sathish.thodar.data.dto.enums.TicketStatus;
import com.sathish.thodar.data.dto.enums.ScheduleStatus;
import com.sathish.thodar.data.dto.request.admin.TrainSetupRequest;
import com.sathish.thodar.data.dto.request.admin.ScheduleRequest;
import com.sathish.thodar.data.dto.request.passenger.BookingRequest;
import com.sathish.thodar.features.support.SupportView;
import com.sathish.thodar.features.reporting.ReportView;
import com.sathish.thodar.features.core.TrainService;
import com.sathish.thodar.data.dto.response.admin.PassengerListResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class AdminView {

    private final ThodarDB db = ThodarDB.getInstance();
    private final SupportView supportView = new SupportView();
    private final ReportView reportView = new ReportView();
    private final Random random = new Random();

    public void showAdminMenu() {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("         ADMIN DASHBOARD         ");
            System.out.println("=================================");
            System.out.println("1. Add Train & Route");
            System.out.println("2. Schedule Train (60 Days)");
            System.out.println("3. View Passenger Chart");
            System.out.println("4. Answer Support Queries");
            System.out.println("5. Generate Revenue Report");
            System.out.println("6. Simulate Dummy Bookings");
            System.out.println("7. Simulate Dummy Cancellation");
            System.out.println("8. Logout");

            String choice = ConsoleInput.getString("Choice: ").trim();

            switch (choice) {
                case "1":
                    handleAddTrain();
                    break;
                case "2":
                    handleAddSchedule();
                    break;
                case "3":
                    handleViewChart();
                    break;
                case "4":
                    handleAdminSupport();
                    break;
                case "5":
                    handleAdminReport();
                    break;
                case "6":
                    handleSimulateBookings();
                    break;
                case "7":
                    simulateDummyCancellation();
                    break;
                case "8":
                    System.out.println("Admin logged out.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void handleAddTrain() {
        System.out.println("\n--- ADD NEW TRAIN ---");
        String trainNo = ConsoleInput.getString("Enter 5-digit Train Number (e.g., 12631): ");
        boolean isDuplicate = false;
        if (db.getAllTrains() != null) {
            for (TrainSetupRequest existingTrain : db.getAllTrains()) {
                if (existingTrain.getTrainNumber().equalsIgnoreCase(trainNo)) {
                    isDuplicate = true;
                    break;
                }
            }
        }


        if (isDuplicate) {
            System.out.println(" Error: A train with number '" + trainNo + "' already exists in the system!");
            return;
        }
        String trainName = ConsoleInput.getString("Enter Train Name (e.g., Nellai Express): ");

        // STEP 1: SELECT BASE ROUTE
        System.out.println("\n--- STEP 1: SELECT BASE ROUTE ---");
        System.out.println("1. Chennai - Madurai (Chord Line - Via Ariyalur)");
        System.out.println("2. Chennai - Madurai (Main Line - Via Thanjavur)");
        System.out.println("3. Chennai - Coimbatore (West Coast Line)");
        System.out.println("4. Kerala South Line (Coimbatore to Trivandrum)");
        System.out.println("5. Kerala Malabar Line (Shoranur to Mangalore)");
        System.out.println("6. Coimbatore - Madurai (Via Palani)");
        System.out.println("7. Trichy - Rameswaram");
        int baseChoice = ConsoleInput.getInt("Choice: ");

        List<Station> masterRoute = new ArrayList<>();
        if (baseChoice == 1) masterRoute.addAll(StationMaster.SR_CHORD_LINE);
        else if (baseChoice == 2) masterRoute.addAll(StationMaster.SR_MAIN_LINE);
        else if (baseChoice == 3) masterRoute.addAll(StationMaster.SR_WEST_COAST_LINE);
        else if (baseChoice == 4) masterRoute.addAll(StationMaster.SR_KERALA_SOUTH_LINE);
        else if (baseChoice == 5) masterRoute.addAll(StationMaster.SR_MALABAR_LINE);
        else if (baseChoice == 6) masterRoute.addAll(StationMaster.SR_CBE_MDU_LINE);
        else if (baseChoice == 7) masterRoute.addAll(StationMaster.SR_TPJ_RMM_LINE);
        else {
            System.out.println("Invalid! Defaulting to Chord Line.");
            masterRoute.addAll(StationMaster.SR_CHORD_LINE);
        }


        if (baseChoice == 1 || baseChoice == 2 || baseChoice == 6) {
            System.out.println("\n--- STEP 2: SELECT ROUTE EXTENSION ---");
            System.out.println("1. No Extension (End at Madurai)");
            System.out.println("2. Extend to Tirunelveli");
            System.out.println("3. Extend to Kanyakumari");
            System.out.println("4. Extend to Tuticorin");
            System.out.println("5. Extend to Sengottai");
            System.out.println("6. Extend to Rameswaram");
            int extChoice = ConsoleInput.getInt("Choice: ");

            if (extChoice > 1 && extChoice <= 6) {
                masterRoute.remove(masterRoute.size() - 1);

                if (extChoice == 2) masterRoute.addAll(StationMaster.SR_EXT_TIRUNELVELI);
                else if (extChoice == 3) masterRoute.addAll(StationMaster.SR_EXT_KANYAKUMARI);
                else if (extChoice == 4) masterRoute.addAll(StationMaster.SR_EXT_TUTICORIN);
                else if (extChoice == 5) masterRoute.addAll(StationMaster.SR_EXT_SENGOTTAI);
                else if (extChoice == 6) masterRoute.addAll(StationMaster.SR_EXT_RAMESWARAM);
            }
        }

        // STEP 3: SOURCE & DESTINATION CUT
        System.out.println("\n--- STEP 3: SELECT SOURCE & DESTINATION ---");
        for (int i = 0; i < masterRoute.size(); i++) {
            System.out.println((i + 1) + ". " + masterRoute.get(i).toString());
        }

        int srcIdx = ConsoleInput.getInt("\nSelect Source Station Number: ") - 1;
        int dstIdx = ConsoleInput.getInt("Select Destination Station Number: ") - 1;

        if (srcIdx == dstIdx || srcIdx < 0 || dstIdx >= masterRoute.size() || srcIdx >= masterRoute.size()) {
            System.out.println(" [ERROR] Invalid Source or Destination selection!");
            return;
        }

        List<Station> finalStationRoute;
        if (srcIdx < dstIdx) {
            finalStationRoute = new ArrayList<>(masterRoute.subList(srcIdx, dstIdx + 1));
        } else {
            finalStationRoute = new ArrayList<>(masterRoute.subList(dstIdx, srcIdx + 1));
            Collections.reverse(finalStationRoute);
        }

        List<String> finalRouteStrings = finalStationRoute.stream()
                .map(station -> station.getStationCode() + "/" + station.getStationName())
                .collect(Collectors.toList());

        System.out.println("\n Route Generated: " + String.join(" -> ", finalRouteStrings));

        // STEP 4: COACH SETUP
        System.out.println("\n--- STEP 4: COACH SETUP ---");
        int sl = ConsoleInput.getInt("Enter Number of Sleeper (SL) Coaches: ");
        int ac3 = ConsoleInput.getInt("Enter Number of 3A Coaches: ");
        int ac2 = ConsoleInput.getInt("Enter Number of 2A Coaches: ");
        int ac1 = ConsoleInput.getInt("Enter Number of 1A Coaches: ");

        createAndSaveTrain(trainNo, trainName, finalRouteStrings, sl, ac3, ac2, ac1);

        // Auto-generate Return Train
        try {
            int num = Integer.parseInt(trainNo);
            String pairNum = (num % 2 == 0) ? String.valueOf(num - 1) : String.valueOf(num + 1);
            boolean pairExists = db.getAllTrains().stream().anyMatch(t -> t.getTrainNumber().equalsIgnoreCase(pairNum));

            if (!pairExists && ConsoleInput.getString("Auto-generate Return Train (" + pairNum + ")? (Y/N): ").equalsIgnoreCase("Y")) {
                List<String> route2 = new ArrayList<>(finalRouteStrings);
                Collections.reverse(route2);
                createAndSaveTrain(pairNum, trainName, route2, sl, ac3, ac2, ac1);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    private void createAndSaveTrain(String tNum, String tName, List<String> route, int sl, int ac3, int ac2, int ac1) {
        TrainSetupRequest t = new TrainSetupRequest();
        t.setTrainNumber(tNum);
        t.setTrainName(tName);
        t.setRouteStations(new ArrayList<>(route));
        t.setSourceStation(route.get(0));
        t.setDestinationStation(route.get(route.size() - 1));
        t.setSlCoaches(sl);
        t.setThirdAcCoaches(ac3);
        t.setSecondAcCoaches(ac2);
        t.setFirstAcCoaches(ac1);

        db.addTrain(t);
        System.out.println(
                "Train " + tNum + " Added! (" + t.getSourceStation() + " -> " + t.getDestinationStation() + ")");
    }

    private void handleAddSchedule() {
        if (db.getAllTrains().isEmpty()) {
            System.out.println("\nNo trains added yet!");
            return;
        }

        System.out.println("\n--- AVAILABLE TRAINS ---");
        for (TrainSetupRequest t : db.getAllTrains()) {
            System.out.println("ID: " + t.getId() + " | " + t.getTrainNumber() + " - " + t.getTrainName());
        }

        Long trainId = ConsoleInput.getLong("Enter Train ID to Schedule: ");
        TrainSetupRequest train = db.getTrainById(trainId);

        if (train == null) {
            System.out.println("Invalid Train ID.");
            return;
        }

        long dateEpoch;
        boolean isReschedule = false;
        ScheduleRequest existingSchedule = null;
        String rescheduleReason = null;

        while (true) {
            String dateStr = ConsoleInput.getString("Start Date (dd-MM-yyyy): ");
            Long parsedEpoch = ParseHelper.dateToEpoch(dateStr);
            if (parsedEpoch == null) {
                System.out.println("Invalid date format!");
                continue;
            }
            dateEpoch = parsedEpoch;

            if (dateEpoch < (System.currentTimeMillis() - 86400000L)) {
                System.out.println("Cannot schedule train for past dates!");
                continue;
            }

            boolean startConflict = false;
            for (ScheduleRequest s : db.getAllSchedules()) {
                if (s.getTrainId().equals(trainId) && s.getJourneyDateEpoch() == dateEpoch) {
                    startConflict = true;
                    existingSchedule = s;
                    break;
                }
            }

            if (startConflict) {
                String modify = ConsoleInput.getString(
                        "Train is already scheduled for this date. Modify the old schedule for specified date only? (Y/N): ");
                if (modify.equalsIgnoreCase("Y")) {
                    isReschedule = true;
                    rescheduleReason = ConsoleInput.getString("Reason for reschedule: ");
                    break;
                } else {
                    continue;
                }
            }

            boolean conflict = false;
            long oneDayMs = 86400000L;
            for (int i = 0; i < 60; i++) {
                long checkDate = dateEpoch + (i * oneDayMs);
                for (ScheduleRequest s : db.getAllSchedules()) {
                    if (s.getTrainId().equals(trainId) && s.getJourneyDateEpoch() == checkDate) {
                        conflict = true;
                        break;
                    }
                }
                if (conflict)
                    break;
            }

            if (conflict) {
                System.out.println(
                        "Train is already scheduled for one or more dates in this 60-day period! Please choose a different date.");
            } else {
                break;
            }
        }

        System.out.println("\n--- SET TIMETABLE FOR ROUTE ---");
        Map<String, String> timetable = new HashMap<>();
        List<String> stations = train.getRouteStations();

        for (int i = 0; i < stations.size(); i++) {
            String stn = stations.get(i);
            System.out.println("\nStation: " + stn);
            if (i == 0) {
                String dep;
                while (true) {
                    dep = ConsoleInput.getString("Departure Time (HH:mm): ");
                    if (dep.matches("([01]?\\d|2[0-3]):[0-5]\\d"))
                        break;
                    System.out.println(" Invalid format! Please enter time in HH:mm format.");
                }
                timetable.put(stn, "Dep: " + dep);
            } else if (i == stations.size() - 1) {
                String arr;
                while (true) {
                    arr = ConsoleInput.getString("Arrival Time (HH:mm): ");
                    if (arr.matches("([01]?\\d|2[0-3]):[0-5]\\d"))
                        break;
                    System.out.println(" Invalid format! Please enter time in HH:mm format.");
                }
                timetable.put(stn, "Arr: " + arr);
            } else {
                String arr, dep;
                while (true) {
                    arr = ConsoleInput.getString("Arrival Time (HH:mm): ");
                    dep = ConsoleInput.getString("Departure Time (HH:mm): ");

                    if (!arr.matches("([01]?\\d|2[0-3]):[0-5]\\d") || !dep.matches("([01]?\\d|2[0-3]):[0-5]\\d")) {
                        System.out.println(" Invalid format! Please enter time in HH:mm format.");
                        continue;
                    }

                    String[] aParts = arr.split(":");
                    String[] dParts = dep.split(":");
                    int arrMins = Integer.parseInt(aParts[0]) * 60 + Integer.parseInt(aParts[1]);
                    int depMins = Integer.parseInt(dParts[0]) * 60 + Integer.parseInt(dParts[1]);

                    if (depMins < arrMins) {
                        if (arrMins >= 23 * 60 && depMins <= 1 * 60) {
                            break;
                        } else {
                            System.out.println(" Invalid! Departure time cannot be earlier than Arrival time.");
                        }
                    } else {
                        break;
                    }
                }
                timetable.put(stn, "Arr: " + arr + " | Dep: " + dep);
            }
        }

        if (isReschedule) {
            existingSchedule.setTimetable(new HashMap<>(timetable));
            existingSchedule.setRescheduleReason(rescheduleReason);
            System.out.println("Train schedule updated successfully for the specified date!");
        } else {
            System.out.println("\nGenerating bulk schedules for the next 60 Days ");
            long oneDayMs = 86400000L;

            for (int i = 0; i < 60; i++) {
                ScheduleRequest s = new ScheduleRequest();
                s.setTrainId(trainId);
                s.setJourneyDateEpoch(dateEpoch + (i * oneDayMs));
                s.setTimetable(new HashMap<>(timetable));
                s.setStatus(ScheduleStatus.SCHEDULED);
                db.addSchedule(s);
            }

            System.out.println("Train Scheduled successfully for 60 consecutive days!");
        }
    }

    private void handleSimulateBookings() {
        System.out.println("\n--- DUMMY BOOKINGS ---");

        List<ScheduleRequest> allSchedules = db.getAllSchedules();
        if (allSchedules.isEmpty()) {
            System.out.println("No schedules found! Please schedule a train first.");
            return;
        }

        int printCount = Math.min(15, allSchedules.size());
        System.out.println("[Showing first " + printCount + " available schedules]");
        for (int i = 0; i < printCount; i++) {
            ScheduleRequest s = allSchedules.get(i);
            TrainSetupRequest t = db.getTrainById(s.getTrainId());
            System.out.println("ID: " + s.getId() + " | " + t.getTrainNumber() + " - " + t.getTrainName() + " | Date: "
                    + ParseHelper.epochToDateString(s.getJourneyDateEpoch()));
        }

        Long sId = ConsoleInput.getLong("\nEnter Schedule ID to inject traffic: ");
        ScheduleRequest sched = db.getScheduleById(sId);

        if (sched == null) {
            System.out.println("Invalid Schedule ID.");
            return;
        }

        TrainSetupRequest train = db.getTrainById(sched.getTrainId());
        System.out.println(
                "Generating dummy bookings to simulate realistic availability for Schedule ID: " + sId + "...");

        for (TicketClass tc : TicketClass.values()) {
            int capacity = TrainService.getCnfCapacity(train, tc);
            if (capacity > 0) {
                bookDummySeats(sId, tc, train.getSourceStation(), (int) (capacity * (0.2 + random.nextDouble())));
                if (train.getRouteStations().size() > 6) {
                    String intermediate = train.getRouteStations().get(5);
                    bookDummySeats(sId, tc, intermediate, (int) (capacity * (0.1 + random.nextDouble() * 0.4)));
                }
            }
        }
        System.out.println("Dummy Seats Booked successfully!");
    }

    private void bookDummySeats(Long sId, TicketClass tClass, String station, int count) {
        int booked = 0;
        double price = (tClass == TicketClass.AC_1A) ? 2000
                : (tClass == TicketClass.AC_2A) ? 1500 : (tClass == TicketClass.AC_3A) ? 1000 : 500;
        List<BookingRequest> dummyBookings = new ArrayList<>();
        String[] genders = { "M", "F" };

        while (booked < count) {
            int toBook = random.nextInt(4) + 1;
            if (booked + toBook > count) {
                toBook = count - booked;
            }

            BookingRequest d = new BookingRequest();
            d.setUserId(999L);
            d.setScheduleId(sId);
            d.setTicketClass(tClass);
            d.setQuota(TicketQuota.GENERAL);
            d.setBoardingStation(station);
            d.setPassengerCount(toBook);
            d.setStatus(TicketStatus.CNF);
            d.setTotalFare(price * toBook);

            for (int i = 0; i < toBook; i++) {
                BookingRequest.PassengerDetail pd = new BookingRequest.PassengerDetail();
                pd.name = "Dummy_" + random.nextInt(100);
                pd.age = 20 + random.nextInt(40);
                pd.gender = genders[random.nextInt(2)];
                d.addPassenger(pd);
            }
            dummyBookings.add(d);
            booked += toBook;
        }

        for (BookingRequest b : dummyBookings) {
            db.addTicket(b);
        }

        TrainService.recalculateWaitlist(sId, tClass);
    }

    private void handleViewChart() {
        System.out.println("\n--- AVAILABLE SCHEDULES ---");
        List<ScheduleRequest> allSchedules = db.getAllSchedules();

        if (allSchedules.isEmpty()) {
            System.out.println("No schedules found!");
            return;
        }

        int printCount = Math.min(10, allSchedules.size());
        System.out.println("[Showing first " + printCount + " schedules. Check DB for rest]");

        for (int i = 0; i < printCount; i++) {
            ScheduleRequest s = allSchedules.get(i);
            TrainSetupRequest t = db.getTrainById(s.getTrainId());
            System.out.println("ID: " + s.getId() + " | " + t.getTrainNumber() + " - " + t.getTrainName() + " | Date: "
                    + ParseHelper.epochToDateString(s.getJourneyDateEpoch()));
        }

        Long sId = ConsoleInput.getLong("\nEnter Schedule ID: ");
        ScheduleRequest sched = db.getScheduleById(sId);

        if (sched == null) {
            System.out.println("Schedule Not Found!");
            return;
        }

        TrainSetupRequest train = db.getTrainById(sched.getTrainId());

        for (TicketClass tc : TicketClass.values()) {
            if (TrainService.getCnfCapacity(train, tc) <= 0)
                continue;

            System.out.println("\n============================================================");
            System.out.println("          PASSENGER CHART - " + tc);
            System.out.println("============================================================");
            System.out.println(
                    String.format("%-10s | %-15s | %-4s | %-6s | %-10s", "Seat No", "Name", "Age", "Gender", "Status"));
            System.out.println("------------------------------------------------------------");

            List<PassengerListResponse> chartList = new ArrayList<>();

            for (BookingRequest t : db.getAllTickets()) {
                if (t.getScheduleId().equals(sId) && t.getTicketClass() == tc && t.getStatus() != TicketStatus.CAN) {
                    for (BookingRequest.PassengerDetail pd : t.getPassengers()) {
                        PassengerListResponse paxDTO = new PassengerListResponse(
                                pd.currentCoachSeat, pd.name, pd.age, pd.gender, pd.currentStatus);
                        chartList.add(paxDTO);
                    }
                }
            }

            if (chartList.isEmpty()) {
                System.out.println("No passengers booked in " + tc);
            } else {
                for (PassengerListResponse pax : chartList) {
                    System.out.println(String.format("%-10s | %-15s | %-4d | %-6s | %-10s",
                            pax.seatNo, pax.name, pax.age, pax.gender, pax.currentStatus));
                }
            }
            System.out.println("============================================================");
        }
    }

    private void handleAdminSupport() {
        supportView.handleAdminSupport();
    }

    private void handleAdminReport() {
        System.out.println("\n--- REVENUE REPORT ---");
        double rev = 0;
        int count = 0;

        for (BookingRequest t : db.getAllTickets()) {
            if (t.getStatus() != TicketStatus.CAN && t.getTotalFare() != null) {
                rev += t.getTotalFare();
                count++;
            }
        }

        reportView.generateReport(ParseHelper.epochToDateString(System.currentTimeMillis()), count, rev);
    }

    private void simulateDummyCancellation() {
        System.out.println("\nProcessing Dummy Cancellation");
        int cancelledSeats = 0;

        for (BookingRequest t : db.getAllTickets()) {

            if (t.getUserId() == 999L && t.getStatus() != TicketStatus.CAN && random.nextInt(35) < 15) {
                t.setStatus(TicketStatus.CAN);
                TrainService.recalculateWaitlist(t.getScheduleId(), t.getTicketClass());
                cancelledSeats += t.getPassengerCount();
            }
        }

        if (cancelledSeats > 0) {
            System.out.println(" " + cancelledSeats + " dummy passengers cancelled. Waitlists upgraded!");
        } else {
            System.out.println("No cancellations generated in this run.");
        }
    }
}