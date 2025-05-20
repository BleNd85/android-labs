package com.example.lab1;

import java.util.Date;
import java.util.UUID;

public class Transaction {
    private UUID id;
    private Double amount;
    private String description;
    private String category;
    private Date date;
    private Type type;

    public Transaction(UUID id, Double amount, String description, String category, Date date, Type type) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
        this.type = type;
    }

    public enum Type {
        EXPENSE,
        INCOME
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
