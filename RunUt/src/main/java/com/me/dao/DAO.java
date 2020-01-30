package com.me.dao;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author miaoz
 */
public class DAO {

    private static final Logger log = Logger.getAnonymousLogger();

    private static final ThreadLocal sessionThread = new ThreadLocal();

    private static final SessionFactory sessionfactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();

    protected DAO() {
    }

    protected static Session getSession() {
        Session session = (Session) sessionThread.get();

        if (session == null) {
            session = sessionfactory.openSession();
            sessionThread.set(session);
        }
        return session;
    }

    protected static void begin() {
        getSession().beginTransaction();
    }

    protected static void commit() {
        if (getSession().getTransaction().isActive()) {
            getSession().getTransaction().commit();
        }
    }

    protected static void close() {
        getSession().close();
        sessionThread.set(null);
    }

    protected static void rollback() {
        try {
            getSession().getTransaction().rollback();
        } catch (HibernateException e) {
            log.log(Level.WARNING, "Cannot rollback", e);
        }
        try {
            getSession().close();
        } catch (HibernateException e) {
            log.log(Level.WARNING, "Cannot close", e);
        }
        sessionThread.set(null);
    }
}


