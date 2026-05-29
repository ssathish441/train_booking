package com.sathish.thodar.features.support;

import java.util.ArrayList;
import java.util.List;
import com.sathish.thodar.util.ConsoleInput;

public class SupportView {
    
    private static final List<SupportModel> supportTickets = new ArrayList<>();
    private static long supportPk = 0L;

    public void handlePassengerSupport(Long userId) {
        System.out.println("\n--- SUPPORT HELPDESK ---");
        System.out.println("1. Raise new issue");
        System.out.println("2. View my issues");

        if (ConsoleInput.getInt("Choice: ") == 1) {
            SupportModel query = new SupportModel();
            query.setUserId(userId);
            query.setIssueDescription(ConsoleInput.getString("Describe your issue: "));
            query.setQueryId(++supportPk);
            query.setIsResolved(false);
            supportTickets.add(query);
            System.out.println("\n[SUCCESS] Ticket raised! Admin will reply soon.");
        } else {
            boolean hasIssues = false;
            for (SupportModel t : supportTickets) {
                if (t.getUserId().equals(userId)) {
                    displaySupportTicket(t);
                    hasIssues = true;
                }
            }
            if (!hasIssues) {
                System.out.println("\nYou have no support tickets.");
            }
        }
    }

    public void handleAdminSupport() {
        System.out.println("\n--- USER SUPPORT TICKETS ---");
        boolean hasQueries = false;

        for (SupportModel ticket : supportTickets) {
            hasQueries = true;
            displaySupportTicket(ticket);

            if (!ticket.getIsResolved()) {
                String reply = ConsoleInput.getString("Enter Reply (or press Enter to skip): ");
                if (!reply.trim().isEmpty()) {
                    ticket.setAdminReply(reply);
                    ticket.setIsResolved(true);
                    System.out.println("Reply sent.");
                }
            }
        }

        if (!hasQueries) {
            System.out.println("No pending support queries.");
        }
    }

    public void showTicketSubmissionSuccess(Long queryId) {
        System.out.println("\n Support ticket submitted!");
        System.out.println("Your Ticket ID is: " + queryId);
        System.out.println("Our admin will review and reply shortly.");
    }

    private void displaySupportTicket(SupportModel ticket) {
        System.out.println("\n--- SUPPORT TICKET [" + ticket.getQueryId() + "] ---");
        System.out.println("Issue: " + ticket.getIssueDescription());
        
        if (ticket.getIsResolved()) {
            System.out.println("Status: RESOLVED");
            System.out.println("Admin Reply: " + ticket.getAdminReply());
        } else {
            System.out.println("Status: PENDING");
            System.out.println("Admin Reply: Waiting for response...");
        }
        System.out.println("--------------------------------");
    }
}