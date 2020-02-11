package com.me.controller;

import com.me.dao.BillDAO;
import com.me.dao.FileDAO;
import com.me.pojo.Bill;
import com.me.pojo.User;
import com.me.pojo.File;
import com.me.utils.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@RestController
@RequestMapping("v1/bill/*/file/**")
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

    private static String UPLOAD_DIR = "src/main/resources/tmp/";

    @RequestMapping(value = "v1/bill/{id}/file", method = RequestMethod.POST)
    public ResponseEntity createFile(@RequestHeader("Authorization") String auth, @RequestParam("file") MultipartFile file, @PathVariable("id") String id) throws IOException {
        User u = ju.autherize(auth);
        if (u == null) {
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(id, u);
        if (b == null) {
            return ResponseEntity.status(404).body("bill not found");
        }
        if (file.isEmpty()) {
            return ResponseEntity.status(400).body("No content");
        }
        byte[] bytes = file.getBytes();
        java.io.File newf = new java.io.File(UPLOAD_DIR + file.getOriginalFilename());
        newf.createNewFile();
        Path path = Paths.get("src/main/resources/tmp/" + file.getOriginalFilename());
        Files.write(path, bytes);


        //filedao
        File f = new File();
        f.setFile_name(file.getOriginalFilename());
        f.setUpload_date(LocalDate.now());
        f.setUrl(path.toString());
        f.setSize(bytes.length);
        f.setOwner_id(u);
        f.setBill_id(b);
        fileDAO.addFile(f);

        return ResponseEntity.status(201).body("File uploaded");

    }

    @RequestMapping(value = "v1/bill/{bid}/file/{fid}", method = RequestMethod.GET,produces = "application/json")
    public ResponseEntity getFile(@RequestHeader("Authorization") String auth, @PathVariable("bid") String bid, @PathVariable("fid") String fid) throws IOException {
        User u = ju.autherize(auth);
        if (u == null) {
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(bid, u);
        if (b == null) {
            return ResponseEntity.status(404).body("bill not found");
        }
        File f = fileDAO.getFile(fid, b, u);
        if (f == null) {
            return ResponseEntity.status(404).body("file not found");
        }
        return ResponseEntity.ok().body(f.toJSON().toString());
    }

    @RequestMapping(value = "v1/bill/{bid}/file/{fid}", method = RequestMethod.DELETE)
    public ResponseEntity deleteFile(@RequestHeader("Authorization") String auth, @PathVariable("bid") String bid, @PathVariable("fid") String fid) throws IOException {
        User u = ju.autherize(auth);
        if (u == null) {
            return ResponseEntity.status(401).body("unauthorized user");
        }
        Bill b = billDAO.getBill(bid, u);
        if (b == null) {
            return ResponseEntity.status(404).body("bill not found");
        }
        File f = fileDAO.getFile(fid, b, u);
        if (f == null) {
            return ResponseEntity.status(404).body("file not found");
        }
        fileDAO.deleteFile(f);
        return ResponseEntity.noContent().build();
    }
}
