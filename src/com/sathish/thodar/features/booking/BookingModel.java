package com.sathish.thodar.features.booking;

class BookingModel {
    private Long selectedScheduleId;
    private String passengerName;
    private String ticketClass; // AC, SL, GEN
    private String allocatedSeat;
    private Double ticketPrice;


    public Long getSelectedScheduleId() { return selectedScheduleId; }
    public void setSelectedScheduleId(Long selectedScheduleId) { this.selectedScheduleId = selectedScheduleId; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getTicketClass() { return ticketClass; }
    public void setTicketClass(String ticketClass) { this.ticketClass = ticketClass; }

    public String getAllocatedSeat() { return allocatedSeat; }
    public void setAllocatedSeat(String allocatedSeat) { this.allocatedSeat = allocatedSeat; }

    public Double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }
}