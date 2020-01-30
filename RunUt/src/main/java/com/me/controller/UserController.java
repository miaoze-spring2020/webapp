package com.me.controller;

import com.me.dao.UserDAO;
import com.me.pojo.User;
import com.me.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/v1/user/**")
public class UserController {
    @Autowired
    @Qualifier("jsonUtils")
    JSONUtils ju;

    @Autowired
    @Qualifier("userDAO")
    UserDAO userDAO;

    @RequestMapping(value = "/v1/user", method = RequestMethod.POST, headers = "Accept=application/json")
    public void createUser(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {

        JSONObject js = ju.JSONfromRequest(request);

        if (!js.has("first_name") || !js.has("last_name") || !js.has("email_address") || !js.has("password")) {
            response.sendError(400, "Bad Request");
        } else {
            String fn = (String) js.get("first_name");
            String ln = (String) js.get("last_name");
            String password = (String) js.get("password");
            String email = (String) js.get("email_address");


            //validate username:
            String email_reg = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
            if (!email.matches(email_reg)) {
                response.getWriter().write("Invalid username(must be in email format)");
                response.setStatus(400);
                return;
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
                response.getWriter().write("Invalid Password(must contain 1 Uppercase ,1 Lower case, digits and special characters)");
                response.setStatus(400);
                return;
            }

            //create user:
            UserDAO userDAO = new UserDAO();
            User u = userDAO.createUser(fn, ln, password, email);
            if (u != null) {
                response.setStatus(201);
                response.setContentType("application/json");
                response.getWriter().write(u.toJSON().toString());
            } else {
                response.setStatus(400);
            }
        }
    }

    @RequestMapping(value = "/v1/user/self", method = RequestMethod.GET, headers = "Accept=application/json")
    public void getUserInfo(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        User u = ju.autherize(request);
        if (u != null) {
            response.setStatus(200);
            response.setContentType("application/json");
            response.getWriter().write(u.toJSON().toString());
            return;
        }
        response.setStatus(401);
    }

    @RequestMapping(value = "/v1/user/self", method = RequestMethod.PUT, headers = "Accept=application/json")
    public void updateUserInfo(HttpServletRequest request, HttpServletResponse response) {
        try {
            JSONObject js = new JSONUtils().JSONfromRequest(request);

            User u = ju.autherize(request);
            if (u == null) {
                response.setStatus(401);
                return;
            }
            User modifiedu = ju.parseUser(js, u);
            if (modifiedu == null) {
                response.setStatus(400);
                return;
            }
            new UserDAO().updateUser(modifiedu);
            response.setStatus(204);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
