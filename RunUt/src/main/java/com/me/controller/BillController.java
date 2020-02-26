package com.me.controller;

import com.me.dao.BillDAO;
import com.me.dao.FileDAO;
import com.me.pojo.Bill;
import com.me.pojo.File;
import com.me.pojo.User;
import com.me.utils.JSONUtils;
import com.me.utils.S3Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/v1/bill/", "/v1/bill/*", "/v1/bill*"})
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

    @RequestMapping(value = "/v1/bill/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createBill(@RequestBody String bill, @RequestHeader(value = "Authorization", required = false) String auth) {
        User u = ju.autherize(auth);
        if (u == null) {
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
            return ResponseEntity.status(201).body(b.toJSON().toString());
        }
        return ResponseEntity.status(400).body("invalid input");

    }

    @RequestMapping(value = "/v1/bills", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getBills(@RequestHeader(value = "Authorization", required = false) String auth) {
        User u = ju.autherize(auth);
        if (u == null) {
            return ResponseEntity.status(401).body("unauthorized user");
        }
        List<Bill> bills = billDAO.getAllBills(u);
        JSONArray ja = new JSONArray();
        for (Bill b : bills) {
            ja.put(b.toJSON());
        }
        return ResponseEntity.ok().body(ja.toString());

    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getBill(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable("id") String id) {
        User u = ju.autherize(auth);
        if (u == null) {
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(id, u);
        if (b != null) {
            return ResponseEntity.ok().body(b.toJSON().toString());
        }
        return ResponseEntity.status(404).body("No such Bill");
    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public ResponseEntity updateBill(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable("id") String id, @RequestBody String modi) {
        User u = ju.autherize(auth);
        if (u == null) {
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(id, u);
        if (b == null) {
            return ResponseEntity.status(404).body("no such bill");
        }
        JSONObject json = new JSONObject(new JSONTokener(new JSONObject(modi).toString()));

        Bill modifiedb = ju.parseBill(json, b);
        if (modifiedb == null) {
            return ResponseEntity.status(400).body("invalid input");
        }

        modifiedb = billDAO.updateBill(modifiedb);
        return ResponseEntity.status(200).body(modifiedb.toJSON().toString());

    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteBill(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable("id") String id) {
        User u = ju.autherize(auth);
        if (u == null) {
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(id, u);
        if (b == null) {
            return ResponseEntity.status(404).body("no such bill");
        }
        File f = b.getAttachment();
        if (f != null) {
            s3Utils.deleteFile(b.getId() + "_" + f.getFile_name());
        }
        billDAO.deleteBill(b);
        return ResponseEntity.noContent().build();
    }
}
