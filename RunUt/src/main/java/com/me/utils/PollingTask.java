package com.me.utils;

import com.me.dao.BillDAO;
import com.me.pojo.Bill;
import com.me.pojo.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PollingTask {
    @Autowired
    @Qualifier("sqsUtils")
    SQSUtils sqsUtils;

    @Autowired
    @Qualifier("snsUtils")
    SNSUtils snsUtils;

    @Autowired
    @Qualifier("billDAO")
    BillDAO billDAO;

    public void post(User user, long xday) throws Exception{
        if(user == null || xday <= 0){
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate dueXDate = LocalDate.now().plusDays(xday);
        List<Bill> bills = billDAO.getAllBillsTime(user, today, dueXDate);
        JSONArray ja = new JSONArray();
        for (Bill b : bills) {
            ja.put(b.toJSON());
        }
        JSONObject messagejo = new JSONObject();
        messagejo.put("username",user.getEmail_address());
        messagejo.put("bills",ja);
        sqsUtils.sendMessage(messagejo.toString());
    }

    @Scheduled(fixedRate = 5000)
    public void poll(){
        String firstMessage = sqsUtils.retrieveMessage();
        snsUtils.publish(firstMessage);
    }

}
