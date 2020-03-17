package com.me.controller;

import com.me.dao.UserDAO;
import com.me.pojo.User;
import com.me.timer.TimerAPI;
import com.me.utils.JSONUtils;
import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/**")
public class UserController {
    @Autowired
    @Qualifier("jsonUtils")
    JSONUtils ju;

    @Autowired
    @Qualifier("userDAO")
    UserDAO userDAO;

    @Autowired
    private StatsDClient statsDClient;

    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Autowired
    @Qualifier("timerAPI")
    TimerAPI timerAPI;

    @RequestMapping(value = "/user", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity createUser(@RequestBody String user) {
        timerAPI.start();
        //log:
        logger.info("this is info log");
        logger.warn("this is warn log");
        statsDClient.incrementCounter("endpoint.user.http.post");

        JSONObject js = new JSONObject(new JSONTokener((new JSONObject(user)).toString()));

        if (!js.has("first_name") || !js.has("last_name") || !js.has("email_address") || !js.has("password")) {
            timerAPI.recordTimeToStatdD("user.post.time");
            return ResponseEntity.badRequest().body("Invalid Input");
        } else {
            String fn = (String) js.get("first_name");
            String ln = (String) js.get("last_name");
            String password = (String) js.get("password");
            String email = (String) js.get("email_address");


            //validate username:
            String email_reg = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
            if (!email.matches(email_reg)) {
                timerAPI.recordTimeToStatdD("user.post.time");
                return ResponseEntity.badRequest().body("Invalid username(must be in email format)");
            }

            //validate password:
            PasswordValidator pv = new PasswordValidator(new LengthRule(8, 10),
                    new CharacterRule(EnglishCharacterData.UpperCase, 1),
                    new CharacterRule(EnglishCharacterData.LowerCase, 1),
                    new CharacterRule(EnglishCharacterData.Digit),
                    new CharacterRule(EnglishCharacterData.Special));
            PasswordData pd = new PasswordData(password);
            RuleResult res = pv.validate(pd);
            if (!res.isValid()) {
                timerAPI.recordTimeToStatdD("user.post.time");
                return ResponseEntity.badRequest().body("Invalid Password(must contain 1 Uppercase ,1 Lower case, digits and special characters)");
            }

            //create user:
            User u = userDAO.createUser(fn, ln, password, email);
            timerAPI.recordTimeToStatdD("user.post.time");
            if (u != null) {
                return ResponseEntity.status(201).body(u.toJSON().toString());
            } else {
                return ResponseEntity.status(400).body("User Already exists");
            }
        }
    }

    @RequestMapping(value = "/user/self", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getUserInfo(@RequestHeader("Authorization") String auth) {
        timerAPI.start();
        statsDClient.incrementCounter("endpoint.user.http.get");

        User u = ju.autherize(auth);
        if (u != null) {
            timerAPI.recordTimeToStatdD("user.get.time");
            return ResponseEntity.ok().body(u.toJSON().toString());
        }
        timerAPI.recordTimeToStatdD("user.get.time");
        return ResponseEntity.status(401).body("Unauthorized user");
    }

    @RequestMapping(value = "/user/self", method = RequestMethod.PUT, consumes = "application/json")
    public ResponseEntity updateUserInfo(@RequestBody String modi, @RequestHeader("Authorization") String auth) {
        timerAPI.start();
        statsDClient.incrementCounter("endpoint.user.http.put");
        JSONObject js = new JSONObject(new JSONTokener((new JSONObject(modi)).toString()));

        User u = ju.autherize(auth);
        if (u == null) {
            timerAPI.recordTimeToStatdD("user.put.time");
            return ResponseEntity.status(401).body("unauthorized user");
        }
        User modifiedu = ju.parseUser(js, u);
        if (modifiedu == null) {
            timerAPI.recordTimeToStatdD("user.put.time");
            return ResponseEntity.status(400).body("invalid input");
        }
        new UserDAO().updateUser(modifiedu);
        timerAPI.recordTimeToStatdD("user.put.time");
        return ResponseEntity.noContent().build();
    }
}
