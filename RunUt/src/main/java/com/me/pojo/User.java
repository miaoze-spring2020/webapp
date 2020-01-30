package com.me.pojo;


import org.hibernate.annotations.GenericGenerator;
import org.json.JSONObject;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table
public class User {
    @Id
    @Column
    @GenericGenerator(name = "systemUUID", strategy = "uuid")
    @GeneratedValue(generator = "systemUUID")
    private String id;

    @Column
    private String first_name;

    @Column
    private  String last_name;

    @Column
    private String password;

    @Column
    private String email_address;

    @Column
    private LocalDate account_created;

    @Column
    private LocalDate account_updated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public LocalDate getAccount_created() {
        return account_created;
    }

    public void setAccount_created(LocalDate account_created) {
        this.account_created = account_created;
    }

    public LocalDate getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(LocalDate account_updated) {
        this.account_updated = account_updated;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        json.put("id",id);
        json.put("first_name",first_name);
        json.put("last_name",last_name);
        json.put("email_address",email_address);
        json.put("account_created",account_created);
        json.put("account_updated",account_updated);

        return json;
    }
}
