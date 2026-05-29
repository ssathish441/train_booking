package com.sathish.thodar.data.dto.response.admin;

public class PassengerListResponse {
    public String seatNo;
    public String name;
    public int age;
    public String gender;
    public String currentStatus;

    
    public PassengerListResponse(String seatNo, String name, int age, String gender, String currentStatus) {
        this.seatNo = seatNo;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.currentStatus = currentStatus;
    }
}