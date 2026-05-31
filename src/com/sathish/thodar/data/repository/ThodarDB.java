package com.sathish.thodar.data.repository;

import com.sathish.thodar.data.dto.entity.User;
import com.sathish.thodar.data.dto.request.admin.TrainSetupRequest;
import com.sathish.thodar.data.dto.request.admin.ScheduleRequest;
import com.sathish.thodar.data.dto.request.passenger.BookingRequest;
import com.sathish.thodar.data.dto.response.passenger.Transaction;
import com.sathish.thodar.data.dto.enums.TicketStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ThodarDB {

    private static ThodarDB instance = null;


    private final List<User> users = new ArrayList<>();
    private final List<TrainSetupRequest> trains = new ArrayList<>();
    private final List<ScheduleRequest> schedules = new ArrayList<>();
    private final List<BookingRequest> tickets = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private long userPk = 0L, trainPk = 0L, ticketPk = 0L;

    private ThodarDB() {}

    public static ThodarDB getInstance() {
        if (instance == null) {
            instance = new ThodarDB();
        }
        return instance;
    }

    

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public void setAllUsers(List<User> loadedUsers) {
        this.users.clear(); 
        if (loadedUsers != null) {
            this.users.addAll(loadedUsers);
        }
    }

    public User addUser(User user) {
        if (user == null || getUserByEmail(user.getEmail()) != null) {
            return null; // Email already exists
        }
        user.setId(++userPk);
        if (user.getRole() == null) {
            user.setRole("CUSTOMER");
        }
        users.add(user);
        return user;
    }

    public User getUserByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    public User authenticateUser(String email, String password) {
        User user = getUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

   

    public TrainSetupRequest addTrain(TrainSetupRequest train) {
        train.setId(++trainPk);
        trains.add(train);
        return train;
    }

    public List<TrainSetupRequest> getAllTrains() {
        return new ArrayList<>(trains);
    }

    public TrainSetupRequest getTrainById(Long id) {
        for (TrainSetupRequest t : trains) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        return null;
    }

    public void setAllTrains(List<TrainSetupRequest> loadedTrains) {
        this.trains.clear();
        if (loadedTrains != null) {
            this.trains.addAll(loadedTrains);
        }
    }

    
    //SCHEDULE MANAGEMENT
   

    public ScheduleRequest addSchedule(ScheduleRequest schedule) {
        schedules.add(schedule);
        return schedule;
    }

    public List<ScheduleRequest> getAllSchedules() {
        return new ArrayList<>(schedules);
    }

    public List<ScheduleRequest> getSchedulesForTrain(Long trainId) {
        List<ScheduleRequest> res = new ArrayList<>();
        for (ScheduleRequest s : schedules) {
            if (s.getTrainId().equals(trainId)) {
                res.add(s);
            }
        }
        return res;
    }

    public ScheduleRequest getScheduleById(Long id) {
        for (ScheduleRequest s : schedules) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    public void setAllSchedules(List<ScheduleRequest> loadedSchedules) {
        this.schedules.clear(); 
        if (loadedSchedules != null) {
            this.schedules.addAll(loadedSchedules);
        }
    }

    
    //  TICKET BOOKING MANAGEMENT
    
    public BookingRequest addTicket(BookingRequest ticket) {
        ticket.setId(++ticketPk);
        if (ticket.getPnrNumber() == null || ticket.getPnrNumber().trim().isEmpty()) {
            ticket.setPnrNumber(String.format(Locale.ROOT, "PNR%10d", ticketPk));
        }
        if (ticket.getStatus() == null) {
            ticket.setStatus(TicketStatus.CNF);
        }
        tickets.add(ticket);
        return ticket;
    }

    public List<BookingRequest> getAllTickets() {
        return new ArrayList<>(tickets);
    }

    public BookingRequest getTicketByPnr(String pnr) {
        for (BookingRequest t : tickets) {
            if (t.getPnrNumber() != null && t.getPnrNumber().equalsIgnoreCase(pnr)) {
                return t;
            }
        }
        return null;
    }

    public List<BookingRequest> getTicketsForUser(Long userId) {
        List<BookingRequest> res = new ArrayList<>();
        for (BookingRequest t : tickets) {
            if (t.getUserId().equals(userId)) {
                res.add(t);
            }
        }
        return res;
    }

    public void setAllTickets(List<BookingRequest> loadedTickets) {
        this.tickets.clear(); 
        if (loadedTickets != null) {
            this.tickets.addAll(loadedTickets);
        }
    }

    
   // TRANSACTION MANAGEMENT

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public List<Transaction> getTransactionsByUserId(Long userId) {
        List<Transaction> userTrans = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.userId.equals(userId)) {
                userTrans.add(t);
            }
        }
        return userTrans;
    }


}