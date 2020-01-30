package com.me.utils;

import com.me.dao.UserDAO;
import com.me.pojo.Bill;
import com.me.pojo.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.codec.Base64;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class JSONUtils {

    @Autowired
    @Qualifier("userDAO")
    UserDAO userDAO;

    public User autherize(HttpServletRequest request) {
        String tokens = request.getHeader("Authorization");
        if (tokens != null && tokens.startsWith("Basic")) {
            String base64credentials = tokens.substring(5).trim();
            byte[] cred = Base64.decode(base64credentials.getBytes());
            String credentials = new String(cred);
            String[] values = credentials.split(":", 2);//username, password
            User u = userDAO.getUser(values[0], values[1]);
            return u;
        }
        return null;
    }

    public JSONObject JSONfromRequest(HttpServletRequest request) throws IOException {
        String data = "";
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        data = builder.toString();
        JSONObject js = new JSONObject(data);
        return js;
    }

    public User parseUser(JSONObject json, User u) {
        String fn = json.getString("first_name");
        String ln = json.getString("last_name");
        String password = json.getString("password");
        String email_address = json.getString("email_address");

        if (fn == null || ln == null || password == null || email_address == null) {
            return null;
        }

        if (fn != null) u.setFirst_name(fn);
        if (ln != null) u.setLast_name(ln);
        if (password != null) u.setPassword(password);
        if (email_address != null) u.setEmail_address(email_address);

        return u;
    }

    public Bill parseBill(JSONObject json, Bill bill) {
        String v = json.getString("vendor");
        String bd = json.getString("bill_date");
        String dd = json.getString("due_date");
        double ad = json.getDouble("amount_due");
        JSONArray categories = json.getJSONArray("categories");
        String stat = json.getString("paymentStatus");

        if (v == null || bd == null || dd == null || ad < 0.01 || categories == null || stat == null) return null;

        if (v != null) bill.setVendor(v);
        if (bd != null) {
            String[] bds = bd.split("-", 3);
            bill.setBill_date(LocalDate.of(Integer.parseInt(bds[0]), Integer.parseInt(bds[1]), Integer.parseInt(bds[2])));
        }
        if (dd != null) {
            String[] dds = dd.split("-", 3);
            bill.setDue_date(LocalDate.of(Integer.parseInt(dds[0]), Integer.parseInt(dds[1]), Integer.parseInt(dds[2])));
        }
        if (ad >= 0.01) bill.setAmount_due(ad);
        if (categories != null) {
            Set<String> set = new HashSet<>();
            for (int i = 0; i < categories.length(); i++) {
                set.add(categories.getString(i));
            }
            bill.setCategories(set);
        }
        if (stat != null) {
            bill.setPaymentStatus(Bill.status.valueOf(stat));
        }

        return bill;
    }
}
