package com.me.dao;

import com.me.pojo.Bill;
import com.me.pojo.File;
import com.me.pojo.User;
import com.me.timer.TimerSQL;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("fileDAO")
public class FileDAO extends DAO {

    @Autowired
    @Qualifier("timerSQL")
    TimerSQL timerSQL;

    public void addFile(File file) {
        timerSQL.start();
        try {
            begin();
            getSession().save(file);
            commit();
            timerSQL.recordTimeToStatdD("create.file.success");
            return;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("create.file.fail");
    }

    public File getFile(String fid, Bill b, User u) {
        timerSQL.start();
        try {
            begin();
            Criteria c = getSession().createCriteria(File.class);
            c.add(Restrictions.eq("owner_id", u))
                    .add(Restrictions.eq("bill_id", b))
                    .add(Restrictions.eq("id", fid));
            File f = (File) c.uniqueResult();
            commit();
            timerSQL.recordTimeToStatdD("get.file.success");
            return f;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("get.file.fail");
        return null;
    }

    public void deleteFile(File f) {
        timerSQL.start();
        try {
            begin();
            getSession().delete(f);
            commit();
            timerSQL.recordTimeToStatdD("delete.file.success");
            return;
        } catch (HibernateException e) {
            rollback();
            e.printStackTrace();
        }
        timerSQL.recordTimeToStatdD("delete.file.fail");
    }
}
