package com.me.controller;

import com.me.dao.BillDAO;
import com.me.pojo.Bill;
import com.me.pojo.File;
import com.me.pojo.User;
import com.me.timer.TimerAPI;
import com.me.utils.JSONUtils;
import com.me.utils.S3Utils;
import com.timgroup.statsd.StatsDClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/bill/", "/bill/*", "/bill*"})
public class BillController {

    @Autowired
    @Qualifier("jsonUtils")
    JSONUtils ju;

    @Autowired
    @Qualifier("billDAO")
    BillDAO billDAO;

    @Autowired
    @Qualifier("s3Utils")
    S3Utils s3Utils;

    @Autowired
    private StatsDClient statsDClient;

    @Autowired
    @Qualifier("timerAPI")
    TimerAPI timerAPI;

    @RequestMapping(value = "/bill/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createBill(@RequestBody String bill, @RequestHeader(value = "Authorization", required = false) String auth) {
        timerAPI.start();
        statsDClient.incrementCounter("endpoint.bill.http.post");

        ResponseEntity re = null;
        User u = ju.autherize(auth);
        if (u == null) {
            timerAPI.recordTimeToStatdD("bill.post.time");
            return ResponseEntity.status(401).body("unauthorized user");
        }

        JSONObject json = new JSONObject(new JSONTokener(new JSONObject(bill).toString()));

        Bill b = ju.parseBill(json, new Bill());
        //set time / owner
        if (b != null) {
            b.setOwner(u);
            b.setCreated_ts(LocalDateTime.now());
            b.setUpdated_ts(LocalDateTime.now());
            billDAO.createBill(b);
            timerAPI.recordTimeToStatdD("bill.post.time");
            return ResponseEntity.status(201).body(b.toJSON().toString());
        }
        timerAPI.recordTimeToStatdD("bill.post.time");
        return ResponseEntity.status(400).body("invalid input");
    }

    @RequestMapping(value = "/bills", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getBills(@RequestHeader(value = "Authorization", required = false) String auth) {
        timerAPI.start();
        statsDClient.incrementCounter("endpoint.bills.http.get");

        User u = ju.autherize(auth);
        if (u == null) {
            timerAPI.recordTimeToStatdD("bills.get.time");
            return ResponseEntity.status(401).body("unauthorized user");
        }
        List<Bill> bills = billDAO.getAllBills(u);
        JSONArray ja = new JSONArray();
        for (Bill b : bills) {
            ja.put(b.toJSON());
        }
        timerAPI.recordTimeToStatdD("bills.get.time");
        return ResponseEntity.ok().body(ja.toString());

    }

    @RequestMapping(value = "/bill/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getBill(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable("id") String id) {
        timerAPI.start();
        statsDClient.incrementCounter("endpoint.bill.http.get");

        User u = ju.autherize(auth);
        if (u == null) {
            timerAPI.recordTimeToStatdD("bill.get.time");
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(id, u);
        if (b != null) {
            timerAPI.recordTimeToStatdD("bill.get.time");
            return ResponseEntity.ok().body(b.toJSON().toString());
        }
        timerAPI.recordTimeToStatdD("bill.get.time");
        return ResponseEntity.status(404).body("No such Bill");
    }

    @RequestMapping(value = "/bill/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public ResponseEntity updateBill(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable("id") String id, @RequestBody String modi) {
        timerAPI.start();
        statsDClient.incrementCounter("endpoint.bill.http.put");
        User u = ju.autherize(auth);
        if (u == null) {
            timerAPI.recordTimeToStatdD("bill.put.time");
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(id, u);
        if (b == null) {
            timerAPI.recordTimeToStatdD("bill.put.time");
            return ResponseEntity.status(404).body("no such bill");
        }
        JSONObject json = new JSONObject(new JSONTokener(new JSONObject(modi).toString()));

        Bill modifiedb = ju.parseBill(json, b);
        if (modifiedb == null) {
            timerAPI.recordTimeToStatdD("bill.put.time");
            return ResponseEntity.status(400).body("invalid input");
        }

        modifiedb = billDAO.updateBill(modifiedb);
        timerAPI.recordTimeToStatdD("bill.put.time");
        return ResponseEntity.status(200).body(modifiedb.toJSON().toString());

    }

    @RequestMapping(value = "/bill/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteBill(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable("id") String id) {
        statsDClient.incrementCounter("endpoint.bill.http.delete");

        timerAPI.recordTimeToStatdD("bill.delete.time");

        User u = ju.autherize(auth);
        if (u == null) {
            timerAPI.recordTimeToStatdD("bill.delete.time");
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(id, u);
        if (b == null) {
            timerAPI.recordTimeToStatdD("bill.delete.time");
            return ResponseEntity.status(404).body("no such bill");
        }
        File f = b.getAttachment();
        if (f != null) {
            s3Utils.deleteFile(id + "_" + f.getFile_name());
        }
        billDAO.deleteBill(b);
        timerAPI.recordTimeToStatdD("bill.delete.time");
        return ResponseEntity.noContent().build();
    }
}
