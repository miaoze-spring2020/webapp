package com.me.dao;

import com.me.pojo.Bill;
import com.me.pojo.File;
import com.me.pojo.User;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

@Component("fileDAO")
public class FileDAO extends DAO{
    public void addFile(File file){
        try{
            begin();
            getSession().save(file);
            commit();
        }catch(HibernateException e){
            rollback();
            e.printStackTrace();
        }
    }

    public File getFile(String fid, Bill b, User u){
        try{
            begin();
            Criteria c = getSession().createCriteria(File.class);
            c.add(Restrictions.eq("owner_id",u))
                    .add(Restrictions.eq("bill_id",b))
                    .add(Restrictions.eq("id",fid));
            File f = (File)c.uniqueResult();
            commit();
            return f;
        }catch(HibernateException e){
            rollback();
            e.printStackTrace();
        }
        return null;
    }

    public void deleteFile(File f){
        try{
            begin();
            getSession().delete(f);
            commit();
        }catch(HibernateException e){
            rollback();
            e.printStackTrace();
        }
    }
}
