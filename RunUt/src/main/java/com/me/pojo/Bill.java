package com.me.pojo;


import org.hibernate.annotations.GenericGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table
public class Bill {
    @Id
    @Column
    @GenericGenerator(name = "systemUUID", strategy = "uuid")
    @GeneratedValue(generator = "systemUUID")
    private String id;

    @Column
    private LocalDateTime created_ts;

    @Column
    private LocalDateTime updated_ts;

    @ManyToOne
    @JoinColumn
    private User owner;

    @Column
    private String vendor;

    @Column
    private LocalDate bill_date;

    @Column
    private LocalDate due_date;

    @Column
    private double amount_due;

    @ElementCollection
    private Set<String> categories;

    @Column
    private status paymentStatus;

    @OneToOne(mappedBy = "bill_id" , cascade = CascadeType.ALL)
    private File attachment;

    public enum status{
        paid, due, past_due, no_payment_required;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public double getAmount_due() {
        return amount_due;
    }

    public void setAmount_due(double amount_due) {
        this.amount_due = amount_due;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public status getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(status paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getCreated_ts() {
        return created_ts;
    }

    public void setCreated_ts(LocalDateTime created_ts) {
        this.created_ts = created_ts;
    }

    public LocalDateTime getUpdated_ts() {
        return updated_ts;
    }

    public void setUpdated_ts(LocalDateTime updated_ts) {
        this.updated_ts = updated_ts;
    }

    public LocalDate getBill_date() {
        return bill_date;
    }

    public void setBill_date(LocalDate bill_date) {
        this.bill_date = bill_date;
    }

    public LocalDate getDue_date() {
        return due_date;
    }

    public void setDue_date(LocalDate due_date) {
        this.due_date = due_date;
    }

    public File getAttachment() {
        return attachment;
    }

    public void setAttachment(File attachment) {
        this.attachment = attachment;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        json.put("id",id);
        json.put("created_ts",created_ts);
        json.put("updated_ts",updated_ts);
        json.put("owner_id",owner.getId());
        json.put("vendor",vendor);
        json.put("bill_date",bill_date);
        json.put("due_date",due_date);
        DecimalFormat df = new DecimalFormat("0.00");
        json.put("amount_due",df.format(amount_due));
        JSONArray ja = new JSONArray();
        for(String s: categories){
            ja.put(s);
        }

        json.put("categories",ja);
        json.put("paymentStatus",paymentStatus);
        if(attachment != null) {
            json.put("attachment", attachment.toJSON());
        }else{
            json.put("attachment","no attachment");
        }

        return json;
    }
}
