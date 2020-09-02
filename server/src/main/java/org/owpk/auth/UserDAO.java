package org.owpk.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;

public class UserDAO {
  private final Logger log = LogManager.getLogger(UserDAO.class.getName());
  private static final EntityManagerFactory entityManagerFactory =
      Persistence.createEntityManagerFactory("p-unit");
  private final EntityManager em = entityManagerFactory.createEntityManager();

  public User getUserByLoginAndPassword(String login, String pass) {
    Query query = em.createQuery("SELECT u FROM User u WHERE u.login = :login AND u.password_hash = :pass");
    query.setParameter("login", login);
    query.setParameter("pass", pass);
    User user = null;
    try {
      user = (User) query.getSingleResult();
    } catch (NoResultException e) {
      log.error(e.getMessage());
    }
    return user;
  }

  public User getUserByLogin(String login) {
    Query query = em.createQuery("SELECT u FROM User u WHERE u.login = :login");
    query.setParameter("login", login);
    User user = null;
    try {
      user = (User) query.getSingleResult();
    } catch (NoResultException e) {
      log.error(e.getMessage());
    }
    return user;
  }

  public void addUser(User user) {
    em.getTransaction().begin();
    em.persist(user);
    em.getTransaction().commit();
  }

}
