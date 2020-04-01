package com.me.controller;

import com.me.dao.BillDAO;
import com.me.pojo.Bill;
import com.me.pojo.File;
import com.me.pojo.User;
import com.me.timer.TimerAPI;
import com.me.utils.JSONUtils;
import com.me.utils.PollingTask;
import com.me.utils.S3Utils;
import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/bill/**", "/bills/**"})
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
    PollingTask pollingTask;

    @Autowired
    private StatsDClient statsDClient;

    private static final Logger logger = LogManager.getLogger(BillController.class);

    @Autowired
    @Qualifier("timerAPI")
    TimerAPI timerAPI;

    @RequestMapping(value = "/bill/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createBill(@RequestBody String bill, @RequestHeader(value = "Authorization", required = false) String auth) {
        logger.info("enter create bill api");
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
        logger.info("enter get bills api");
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
        logger.info("enter get bill api");
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
        logger.info("enter update bill api");
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
        logger.info("enter delete bill api");
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

    @RequestMapping(value = "/bills/due/{x}", method = RequestMethod.GET)
    public ResponseEntity getDue(@PathVariable("x") String x, @RequestHeader(value = "Authorization", required = false) String auth) {
        logger.info("enter get bills due api");
        timerAPI.start();
        statsDClient.incrementCounter("endpoint.bills.http.get.due");

        User u = ju.autherize(auth);
        if (u == null) {
            timerAPI.recordTimeToStatdD("bills.get.time");
            return ResponseEntity.status(401).body("unauthorized user");
        }
        long xday;
        try {
            xday = Long.parseLong(x);
            if(xday <= 0){
                return ResponseEntity.status(400).body("number must be greater than 0");
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body("invalid day number");
        }
        try {
            LocalDate today = LocalDate.now();
            LocalDate dueXDate = LocalDate.now().plusDays(xday);
            List<Bill> bills = billDAO.getAllBillsTime(u, today, dueXDate);
            JSONArray ja = new JSONArray();
            for (Bill b : bills) {
                ja.put(b.toJSON());
            }
            JSONObject messagejo = new JSONObject();
            messagejo.put("username",u.getEmail_address());
            messagejo.put("bills",ja);
            pollingTask.post(messagejo.toString());
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Failed to send request to Amazon SQS");
        }
        timerAPI.recordTimeToStatdD("bills.get.due.time");
        return ResponseEntity.ok().body("The bills are sent to this email address: " + u.getEmail_address());
    }
}
