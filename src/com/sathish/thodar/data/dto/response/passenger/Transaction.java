package com.sathish.thodar.data.dto.response.passenger;

import java.io.Serializable;

public class Transaction implements Serializable {


    public String tId;
    public String pnr;
    public Long userId;
    public Double amount;
    public String type;
    public String remark;


    public Transaction(String pnr, Long userId, Double amount, String type, String remark) {
        this.tId = "TXN" + (System.currentTimeMillis() % 1000000);
        this.pnr = pnr;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.remark = remark;
    }


    public Transaction() {
    }
}