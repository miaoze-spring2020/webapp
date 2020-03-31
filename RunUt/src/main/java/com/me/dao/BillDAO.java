package com.me.dao;

import com.me.pojo.Bill;
import com.me.pojo.User;
import com.me.timer.TimerSQL;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component("billDAO")
public class BillDAO extends DAO {

    @Autowired
    @Qualifier("timerSQL")
    TimerSQL timerSQL;

    public void createBill(Bill bill) {
        timerSQL.start();
        try {
            begin();
            getSession().save(bill);
            commit();
            timerSQL.recordTimeToStatdD("create.bill.success");
            return;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("create.bill.fail");
    }

    public Bill updateBill(Bill bill) {
        timerSQL.start();
        try {
            begin();
            getSession().update(bill);
            commit();
            timerSQL.recordTimeToStatdD("update.bill.success");
            return bill;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("update.bill.fail");
        return null;
    }

    public void deleteBill(Bill bill) {
        timerSQL.start();
        try {
            begin();
            getSession().delete(bill);
            commit();
            timerSQL.recordTimeToStatdD("delete.bill.success");
            return;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("delete.bill.fail");
    }

    public Bill getBill(String bid, User user) {
        timerSQL.start();
        try {
            begin();
            Criteria c = getSession().createCriteria(Bill.class);
            c.add(Restrictions.eq("owner", user))
                    .add(Restrictions.eq("id", bid));
            Bill bill = (Bill) c.uniqueResult();
            commit();
            timerSQL.recordTimeToStatdD("get.bill.success");
            return bill;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("get.bill.fail");
        return null;
    }

    public List<Bill> getAllBills(User user) {
        timerSQL.start();
        try {
            begin();
            Criteria c = getSession().createCriteria(Bill.class);
            c.add(Restrictions.eq("owner", user));
            List<Bill> bills = c.list();
            commit();
            timerSQL.recordTimeToStatdD("get.bills.success");
            return bills;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("get.bills.fail");
        return null;
    }

    public List<Bill> getAllBillsTime(User user, LocalDate from, LocalDate to) {
        timerSQL.start();
        try {
            begin();
            Criteria c = getSession().createCriteria(Bill.class);
            c.add(Restrictions.eq("owner", user))
                    .add(Restrictions.gt("due_date", from))
                    .add(Restrictions.le("due_date", to));

            List<Bill> bills = c.list();
            commit();
            timerSQL.recordTimeToStatdD("get.bills.success");
            return bills;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("get.bills.fail");
        return null;
    }
}
