package com.me.dao;

import com.me.pojo.User;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDate;

public class UserDAO extends DAO{

    public User createUser(String fn, String ln, String pwd, String email){

        begin();
        int x = getSession().createCriteria(User.class).add(Restrictions.eq("email_address",email)).list().size();
        if(x >= 1){
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

        return u;
    }

    public User getUser(String username, String password) {
        begin();

        Criteria criteria = getSession().createCriteria(User.class);

        criteria.add(Restrictions.eq("email_address",username));
        User u = (User)criteria.uniqueResult();

        if(u == null) return null;

        boolean match = BCrypt.checkpw(password,u.getPassword());

        commit();

        if(match){
            return u;
        }else{
            return null;
        }

    }

    public void updateUser(User user){
        try {
            begin();
            getSession().update(user);
            commit();
        }catch (HibernateException e){
            e.printStackTrace();
        }
    }

}
