package com.me.controller;

import com.me.dao.BillDAO;
import com.me.pojo.Bill;
import com.me.pojo.User;
import com.me.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping({"/v1/bill/**","/v1/bill*"})
public class BillController {

    @Autowired
    @Qualifier("jsonUtils")
    JSONUtils ju;

    @Autowired
    @Qualifier("billDAO")
    BillDAO billDAO;

    @RequestMapping(value = "/v1/bill/", method = RequestMethod.POST, headers = "Accept=application/json")
    public void createBill(HttpServletRequest request, HttpServletResponse response) {
        try {
            User u = ju.autherize(request);
            if (u == null) {
                response.setStatus(401);
                return;
            }

            JSONObject json = ju.JSONfromRequest(request);
            Bill b = ju.parseBill(json, new Bill());
            //set time / owner
            if (b != null) {
                b.setOwner(u);
                b.setCreated_ts(LocalDateTime.now());
                b.setUpdated_ts(LocalDateTime.now());
                billDAO.createBill(b);
                response.setStatus(201);
                response.setContentType("application/json");
                response.getWriter().write(b.toJSON().toString());
                return;
            }
            response.setStatus(400);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/v1/bills", method = RequestMethod.GET, headers = "Accept=application/json")
    public void getBills(HttpServletRequest request, HttpServletResponse response) throws IOException {

            User u = ju.autherize(request);
            if (u == null) {
                response.setStatus(401);
                return;
            }
            List<Bill> bills = new BillDAO().getAllBills(u);
            JSONArray ja = new JSONArray();
            for (Bill b : bills) {
                ja.put(b.toJSON());
            }
            response.setStatus(200);
            response.getWriter().write(ja.toString());

    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public void getBill(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) {
        try {
            User u = ju.autherize(request);
            if (u == null) {
                response.setStatus(401);
                return;
            }
            Bill b = new BillDAO().getBill(id);
            if (b != null) {
                response.setStatus(200);
                response.getWriter().write(b.toJSON().toString());
                return;
            }
            response.setStatus(404);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.PUT, headers = "Accept=application/json")
    public void updateBill(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) {
        try {
            User u = ju.autherize(request);
            if (u == null) {
                response.setStatus(401);
                return;
            }
            JSONObject json = ju.JSONfromRequest(request);
            Bill b = new BillDAO().getBill(id);
            if (b == null) {
                response.setStatus(404);
                return;
            }
            Bill modifiedb = ju.parseBill(json, b);
            if (modifiedb == null) {
                response.setStatus(400);
                return;
            }

            modifiedb = new BillDAO().updateBill(modifiedb);
            response.setStatus(200);
            response.setContentType("application/json");
            response.getWriter().write(modifiedb.toJSON().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public void deleteBill(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) {
        try {
            User u = ju.autherize(request);
            if (u == null) {
                response.setStatus(401);
                return;
            }
            Bill b = new BillDAO().getBill(id);
            if (b == null) {
                response.setStatus(404);
                return;
            }
            new BillDAO().deleteBill(b);
            response.setStatus(204);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
