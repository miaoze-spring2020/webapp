package com.me.dao;

import com.me.pojo.User;
import com.me.timer.StopWatch;
import com.me.timer.TimerSQL;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("userDAO")
public class UserDAO extends DAO {

    @Autowired
    @Qualifier("timerSQL")
    private TimerSQL timerSQL;

    public User createUser(String fn, String ln, String pwd, String email) {
        timerSQL.start();
        try {
            begin();
            int x = getSession().createCriteria(User.class).add(Restrictions.eq("email_address", email)).list().size();
            if (x >= 1) {
                timerSQL.recordTimeToStatdD("create.user.fail");
                return null;
            }


            User u = new User();
            u.setFirst_name(fn);
            u.setLast_name(ln);
            u.setEmail_address(email);
            u.setAccount_created(LocalDate.now());
            u.setAccount_updated(LocalDate.now());

            String hashed = BCrypt.hashpw(pwd, BCrypt.gensalt());
            u.setPassword(hashed);

            getSession().save(u);

            commit();
            timerSQL.recordTimeToStatdD("create.user.success");
            return u;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("create.user.fail");
        return null;
    }

    public User getUser(String username, String password) {
        timerSQL.start();
        try {
            begin();
            Criteria criteria = getSession().createCriteria(User.class);

            criteria.add(Restrictions.eq("email_address", username));
            User u = (User) criteria.uniqueResult();

            if (u == null) {
                timerSQL.recordTimeToStatdD("get.user.null");
                return null;
            }

            boolean match = BCrypt.checkpw(password, u.getPassword());

            commit();

            if (match) {
                timerSQL.recordTimeToStatdD("get.user.success");
                return u;
            } else {
                timerSQL.recordTimeToStatdD("get.user.fail");
                return null;
            }
        }catch (HibernateException e){
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("get.user.fail");
        return null;
    }

    public void updateUser(User user) {
        timerSQL.start();
        try {
            begin();
            getSession().update(user);
            commit();
            timerSQL.recordTimeToStatdD("update.user.success");
            return;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("update.user.fail");
    }

}
