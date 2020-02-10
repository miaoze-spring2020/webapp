package com.me.dao;

import com.me.pojo.Bill;
import com.me.pojo.User;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("billDAO")
public class BillDAO extends DAO {


    public void createBill(Bill bill) {
        try {
            begin();
            getSession().save(bill);
            commit();
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
    }

    public Bill updateBill(Bill bill) {
        try {
            begin();
            getSession().update(bill);
            commit();

            return bill;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        return null;
    }

    public void deleteBill(Bill bill) {
        try {
            begin();
            getSession().delete(bill);
            commit();
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
    }

    public Bill getBill(String bid, User user) {
        try {
            begin();
            Criteria c = getSession().createCriteria(Bill.class);
            c.add(Restrictions.eq("owner", user))
            .add(Restrictions.eq("id",bid));
            Bill bill = (Bill)c.uniqueResult();
            commit();

            return bill;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        return null;
    }

    public List<Bill> getAllBills(User user) {
        begin();
        Criteria c = getSession().createCriteria(Bill.class);
        c.add(Restrictions.eq("owner", user));
        List<Bill> bills = c.list();
        commit();
        return bills;
    }
}
