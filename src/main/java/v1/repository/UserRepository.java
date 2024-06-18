package v1.repository;

import java.util.ArrayList;

import org.hibernate.Session;
import org.hibernate.Transaction;
import models.User;
import utils.HibernateUtil;
import utils.LoggerUtils;

public class UserRepository {

	public boolean saveUser(User user) {

		Transaction transaction = null;
		Session session = null;

		try {

			session = HibernateUtil.getSessionFactory().openSession();

			transaction = session.beginTransaction();
			session.saveOrUpdate(user);
			transaction.commit();

			return true;

		} catch (Exception e) {
			LoggerUtils.log("saveUser - Error : " + e.getMessage());
			e.printStackTrace();
			if (null != transaction)
				transaction.rollback();
			if (null != session)
				session.close();
		} finally {
			if (null != session)
				session.close();
		}

		return false;

	}

	public User findUserById(int id) {

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			return session.createQuery("from User where id = :id", User.class).setParameter("id", id).getSingleResult();

		} catch (Exception e) {
			LoggerUtils.log("findUserById: Error : " + e.getMessage());
			e.printStackTrace();
		}

		return null;

	}

	public ArrayList<User> getAllUsers() {

		final var allUsers = new ArrayList<User>();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			final var users = session.createQuery("from User where sessionPasscode is not null and length(sf_user_id) > 15 ", User.class).getResultList();
			
			allUsers.addAll(users);

		} catch (Exception e) {
			LoggerUtils.log("getAllUsers - Error : " + e.getMessage());
			
		}
		return allUsers;

	}
}
