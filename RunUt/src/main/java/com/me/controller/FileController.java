package com.me.controller;

import com.me.dao.BillDAO;
import com.me.dao.FileDAO;
import com.me.pojo.Bill;
import com.me.pojo.User;
import com.me.pojo.File;
import com.me.timer.TimerAPI;
import com.me.utils.JSONUtils;
import com.me.utils.S3Utils;
import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/bill/*/file/**")
public class FileController {
    @Autowired
    @Qualifier("jsonUtils")
    JSONUtils ju;

    @Autowired
    @Qualifier("fileDAO")
    FileDAO fileDAO;

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

    private static final Logger logger = LogManager.getLogger(FileController.class);

    @RequestMapping(value = "/bill/{id}/file", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity createFile(@RequestHeader(name = "Authorization", required = false) String auth, @RequestParam(value = "file", required = false) MultipartFile file, @PathVariable("id") String id) {
        logger.info("enter create file api");
        timerAPI.start();
        statsDClient.incrementCounter("endpoint.file.http.post");

        User u = ju.autherize(auth);
        if (u == null) {
            timerAPI.recordTimeToStatdD("file.post.time");
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(id, u);
        if (b == null) {
            timerAPI.recordTimeToStatdD("file.post.time");
            return ResponseEntity.status(404).body("bill not found");
        }
        if (file == null || file.isEmpty()) {
            timerAPI.recordTimeToStatdD("file.post.time");
            return ResponseEntity.status(422).body("empty file");
        }

        //validate image suffix
        String filename = file.getOriginalFilename();
        int pos = filename.lastIndexOf('.');
        String suffix = filename.substring(pos + 1);

        if (!suffix.equalsIgnoreCase("jpeg") && !suffix.equalsIgnoreCase("jpg")
                && !suffix.equalsIgnoreCase("png") && !suffix.equalsIgnoreCase("pdf")) {
            timerAPI.recordTimeToStatdD("file.post.time");
            return ResponseEntity.status(422).body("invalid file format: " + suffix);
        }
        String newfilename = b.getId() + "_" + filename;

        //upload file
        String url = null;
        try {
            url = s3Utils.uploadFile(newfilename, file);
            if (url.equals("nobucket")) {
                timerAPI.recordTimeToStatdD("file.post.time");
                return ResponseEntity.status(405).body("Failed to upload file to server side: " + "bucket does not exist");
            }
            if (url.equals("exist")) {
                timerAPI.recordTimeToStatdD("file.post.time");
                return ResponseEntity.status(405).body("Failed to upload file to server side: " + "file already exists");
            }
        } catch (IOException e) {
            timerAPI.recordTimeToStatdD("file.post.time");
            return ResponseEntity.status(405).body("Failed to upload file to server side: " + e.getMessage());
        }

        //filedao
        File f = new File();
        f.setFile_name(filename);
        f.setUpload_date(LocalDate.now());
        f.setUrl(url);
        try {
            f.setSize(file.getBytes().length);
        } catch (IOException e) {
            s3Utils.deleteFile(newfilename);
            timerAPI.recordTimeToStatdD("file.post.time");
            return ResponseEntity.status(405).body("Failed to store file metadata, upload rollback: " + e.getMessage());
        }
        f.setOwner_id(u);
        f.setBill_id(b);
        b.setAttachment(f);
        billDAO.updateBill(b);
        fileDAO.addFile(f);

        timerAPI.recordTimeToStatdD("file.post.time");
        return ResponseEntity.status(201).body(f.toJSON().toString());

    }

    @RequestMapping(value = "/bill/{bid}/file/{fid}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getFile(@RequestHeader(name = "Authorization", required = false) String auth, @PathVariable("bid") String bid, @PathVariable("fid") String fid) {
        logger.info("enter get file api");
        timerAPI.start();
        statsDClient.incrementCounter("endpoint.file.http.get");
        User u = ju.autherize(auth);
        if (u == null) {
            timerAPI.recordTimeToStatdD("file.get.time");
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(bid, u);
        if (b == null) {
            timerAPI.recordTimeToStatdD("file.get.time");
            return ResponseEntity.status(404).body("bill not found");
        }
        File f = fileDAO.getFile(fid, b, u);
        if (f == null) {
            timerAPI.recordTimeToStatdD("file.get.time");
            return ResponseEntity.status(404).body("file not found");
        }
        timerAPI.recordTimeToStatdD("file.get.time");
        return ResponseEntity.ok().body(f.toJSON().toString());
    }

    @RequestMapping(value = "/bill/{bid}/file/{fid}", method = RequestMethod.DELETE)
    public ResponseEntity deleteFile(@RequestHeader(name = "Authorization", required = false) String auth, @PathVariable("bid") String bid, @PathVariable("fid") String fid) {
        logger.info("enter delete file api");
        timerAPI.start();
        statsDClient.incrementCounter("endpoint.file.http.delete");

        User u = ju.autherize(auth);
        if (u == null) {
            timerAPI.recordTimeToStatdD("file.delete.time");
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(bid, u);
        if (b == null) {
            timerAPI.recordTimeToStatdD("file.delete.time");
            return ResponseEntity.status(404).body("bill not found");
        }
        b.setAttachment(null);
        File f = fileDAO.getFile(fid, b, u);
        if (f == null) {
            timerAPI.recordTimeToStatdD("file.delete.time");
            return ResponseEntity.status(404).body("file not found");
        }
        String newfilename = bid + "_" + f.getFile_name();

        s3Utils.deleteFile(newfilename);
        fileDAO.deleteFile(f);
        timerAPI.recordTimeToStatdD("file.delete.time");
        return ResponseEntity.noContent().build();
    }
}
