package com.me.pojo;

import org.hibernate.annotations.GenericGenerator;
import org.json.JSONObject;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table
public class File {
    @Id
    @Column
    @GenericGenerator(name = "systemUUID", strategy = "uuid")
    @GeneratedValue(generator = "systemUUID")
    private String id;

    @Column
    private String file_name;

    @Column
    private String url;

    @Column
    private LocalDate upload_date;

    @Column
    private long size;

    @OneToOne
    @JoinColumn
    private User owner_id;

    @OneToOne
    @JoinColumn
    private Bill bill_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(LocalDate upload_date) {
        this.upload_date = upload_date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public User getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(User owner_id) {
        this.owner_id = owner_id;
    }

    public Bill getBill_id() {
        return bill_id;
    }

    public void setBill_id(Bill bill_id) {
        this.bill_id = bill_id;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        json.put("id",id);
        json.put("url",url);
        json.put("file_name",file_name);
        json.put("upload_date",upload_date);

        return json;
    }
}
