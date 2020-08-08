package org.owpk.auth;

import javax.persistence.*;

public class UserDAO {
  private static final EntityManagerFactory entityManagerFactory =
      Persistence.createEntityManagerFactory("p-unit");
  private final EntityManager em = entityManagerFactory.createEntityManager();

  public User getUserByLoginAndPassword(String login, String pass) {
    Query query = em.createQuery("SELECT u FROM User u WHERE u.login = :login AND u.password_hash = :pass");
    query.setParameter("login", login);
    query.setParameter("pass", pass);
    User user = (User) query.getSingleResult();
    em.close();
    return user;
  }

  public void addUser(User user) {
    em.getTransaction().begin();
    em.persist(user);
    em.getTransaction().commit();
    em.close();
  }

}
