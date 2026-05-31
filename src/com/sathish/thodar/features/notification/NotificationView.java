package com.sathish.thodar.features.notification;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import com.sathish.thodar.data.dto.request.passenger.BookingRequest;
import com.sathish.thodar.data.repository.ThodarDB;
import com.sathish.thodar.data.dto.request.admin.ScheduleRequest;
import com.sathish.thodar.data.dto.request.admin.TrainSetupRequest;
import com.sathish.thodar.util.ParseHelper;

public class NotificationView {

    private final String ADMIN_EMAIL = "ssathish08052000@gmail.com";
    private final String APP_PASSWORD = "APP_PASSWORD";

    public void sendDetailedBookingEmail(String userEmail, BookingRequest b) {
        String subject = "Thodar Railways - TICKET CONFIRMED 🚆";
        String introMessage = "Your ticket has been successfully booked!";

        String body = buildDetailedEmailBody(b, introMessage);

        displaySystemPopup("SENDING DETAILED BOOKING EMAIL TO " + userEmail + "...");
        executeRealEmail(userEmail, subject, body);
    }

    public void sendWaitlistUpgradeEmail(String userEmail, BookingRequest b) {
        String subject = "Thodar Railways - STATUS UPGRADED 🟢";
        String introMessage = "Good news! Your ticket status has been upgraded.";

        String body = buildDetailedEmailBody(b, introMessage);

        displaySystemPopup("SENDING UPGRADE EMAIL TO " + userEmail + "...");
        executeRealEmail(userEmail, subject, body);
    }

    private String buildDetailedEmailBody(BookingRequest b, String introText) {
        ThodarDB db = ThodarDB.getInstance();
        ScheduleRequest s = db.getScheduleById(b.getScheduleId());
        TrainSetupRequest t = db.getTrainById(s.getTrainId());

        String travelDate = ParseHelper.epochToDateString(s.getJourneyDateEpoch());
        String bTimeStr = (s.getTimetable() != null && b.getBoardingStation() != null) ? s.getTimetable().get(b.getBoardingStation()) : "";
        String dTimeStr = (s.getTimetable() != null && b.getDropStation() != null) ? s.getTimetable().get(b.getDropStation()) : "";

        String bArr = "---", bDep = "---";
        if (bTimeStr.contains("|")) {
            String[] split = bTimeStr.split("\\|");
            bArr = split[0].replace("Arr:", "").trim();
            bDep = split[1].replace("Dep:", "").trim();
        } else if (bTimeStr.contains("Dep:")) {
            bDep = bTimeStr.replace("Dep:", "").trim();
        }

        String dArr = "---";
        if (dTimeStr.contains("|")) {
            String[] split = dTimeStr.split("\\|");
            dArr = split[0].replace("Arr:", "").trim();
        } else if (dTimeStr.contains("Arr:")) {
            dArr = dTimeStr.replace("Arr:", "").trim();
        }

        StringBuilder body = new StringBuilder();
        body.append("Dear Passenger,\n\n").append(introText).append("\n\n");

        body.append("PNR No: ").append(b.getPnrNumber())
                .append("   Travel Date: ").append(travelDate)
                .append("   Train NO: ").append(t.getTrainNumber())
                .append("   Train Name: ").append(t.getTrainName()).append("\n");

        body.append(String.format("From : %-25s Arr: %-5s | Dep: %-5s\n", b.getBoardingStation(), bArr, bDep));
        body.append(String.format("To   : %-25s Arr: %-5s\n\n", b.getDropStation(), dArr));

        String lineSeparator = "----------------------------------------------------------------------------------\n";
        body.append(lineSeparator);
        body.append(String.format("%-4s | %-15s | %-6s | %-12s | %-14s | %-14s\n", "Sno", "Passenger Name", "Gender", "SeatNo/WL", "Booking Status", "Current Status"));
        body.append(lineSeparator);

        // Passenger List Loop
        int sno = 1;
        if (b.getPassengers() != null) {
            for (BookingRequest.PassengerDetail pd : b.getPassengers()) {
                String seat = (pd.currentCoachSeat != null) ? pd.currentCoachSeat : "WL";
                String currentStat = (pd.currentStatus != null) ? pd.currentStatus : b.getStatus().toString();

                body.append(String.format("%-4d | %-15s | %-6s | %-12s | %-14s | %-14s\n",
                        sno++, pd.name, pd.gender, seat, b.getStatus().toString(), currentStat));
            }
        }
        body.append(lineSeparator).append("\n");
        body.append("Have a safe journey!\n- Thodar Railways Admin");

        return body.toString();
    }

    private void executeRealEmail(String toEmail, String subject, String messageText) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");

        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(ADMIN_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ADMIN_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);
            System.out.println("REAL EMAIL SENT SUCCESSFULLY TO " + toEmail + "!\n");

        } catch (MessagingException e) {
            System.out.println(" Email Sending Failed: " + e.getMessage() + "\n");
        }
    }

    private void displaySystemPopup(String alertMessage) {
        System.out.println("\n [SYSTEM NOTIFICATION] ");
        System.out.println(">> " + alertMessage);
    }
}