package v1.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;

import models.SecondaryInfo;
import models.User;
import models.notification.RegistrationKey;
import models.notification.RmNotification;
import models.notification.UserNotificationInfo;
import models.notification.UserNotificationKey;
import utils.BasicUtils;
import utils.Constants;
import utils.HibernateUtil;
import utils.LoggerUtils;
import utils.NotificationUtils;

public class NotificationRepository {

	private void log(String value) {
		LoggerUtils.log("NotificationRepository." + value);
	}

	public boolean saveRmNotification(RmNotification rmNotification) {

		Transaction transaction = null;
		Session session = null;

		try {

			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			session.saveOrUpdate(rmNotification);
			transaction.commit();
			session.close();

			return true;

		} catch (Exception e) {
			LoggerUtils.log("saveRmNotification - Error : " + e.getMessage());
			e.printStackTrace();
			if (transaction != null)
				transaction.rollback();
			if (null != session)
				session.close();
		} finally {
			if (null != session)
				session.close();
		}

		return false;

	}

	public RmNotification findConnectorNotificationById(int id) {

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			return session.createQuery("from RmNotification where id = :id", RmNotification.class)
					.setParameter("id", id).getSingleResult();

		} catch (Exception e) {
			LoggerUtils.log("findRmNotificationById - Error : " + e.getMessage());
		}

		return null;

	}

	public boolean saveUserNotificationInfo(UserNotificationInfo userNotificationInfo) {

		Transaction transaction = null;
		Session session = null;

		try {

			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			session.saveOrUpdate(userNotificationInfo);
			transaction.commit();
			session.close();

			return true;

		} catch (Exception e) {
			LoggerUtils.log("saveUserNotificationInfo - Error : " + e.getMessage());
			e.printStackTrace();
			if (transaction != null)
				transaction.rollback();
			if (null != session)
				session.close();
		} finally {
			if (null != session)
				session.close();
		}

		return false;

	}

	public boolean saveUserNotificationInfo(RmNotification cpNotification,
			ArrayList<RegistrationKey> registrationKeys) {

		Transaction transaction = null;
		Session session = null;

		try {

			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();

			String message = null;
			String title = null;

			if (cpNotification.audienceType.equals(NotificationUtils.AudienceType.PERSONALIZED.value)) {

				message = cpNotification.message;

				if (BasicUtils.isNotNullOrNA(cpNotification.bigMessage))
					message = cpNotification.bigMessage;

				if (BasicUtils.isNotNullOrNA(cpNotification.title))
					title = cpNotification.title;
			}

			for (var i = 0; i < registrationKeys.size(); i++) {

				final var rk = registrationKeys.get(i);

				final var uni = new UserNotificationInfo();

				uni.notificationId = cpNotification.id;
				uni.userId = rk.userId;
				uni.hasRead = false;
				uni.deviceId = rk.deviceId;
				uni.deviceType = rk.deviceType;
				uni.dynamicTitle = title;
				uni.dynamicMessage = message;

				session.saveOrUpdate(uni);

				if (i % Constants.HITBERNATE_BATCH_SIZE == 0) {
					session.flush();
					session.clear();
				}

			}

			transaction.commit();
			session.close();

			return true;

		} catch (Exception e) {
			LoggerUtils.log("saveUserNotificationInfo - Error : " + e.getMessage());
			e.printStackTrace();
			if (transaction != null)
				transaction.rollback();
			if (null != session)
				session.close();
		} finally {
			if (null != session)
				session.close();
		}

		return false;

	}

	@SuppressWarnings("unchecked")
	public ArrayList<UserNotificationKey> getUserNotificationKeysBySfId(String sfUserIds) {

		final var userNotificationKeys = new ArrayList<UserNotificationKey>();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			final var query = "select u.id, u.first_name, u.sf_user_id, si.fcm_key "
					+ " from RMManagementDB.user u "
					+ " left join (select user_id, fcm_key from RMManagementDB.user_secondary_info) si on si.user_id = u.id "
					+ " where sf_user_id in (" + sfUserIds + ")";

			final var hql = session.createNativeQuery(query);

			List<Object[]> unkObjects = hql.list();

			for (Object[] unkObject : unkObjects) {

				final var unKey = new UserNotificationKey();

				final var fcmKeyArrayString = (String) unkObject[3];

				if (null != fcmKeyArrayString && fcmKeyArrayString.startsWith("[")) {

					final var user = new User();

					user.id = (Integer) unkObject[0];
					user.firstName = (String) unkObject[1];
					user.sfUserId = (String) unkObject[2];

					unKey.user = user;

					final var keys = new ArrayList<RegistrationKey>();

					final var tokenJsonArray = new JSONArray(fcmKeyArrayString);
					for (int i = 0; i < tokenJsonArray.length(); i++) {
						keys.add(new RegistrationKey(user.id, tokenJsonArray.getJSONObject(i)));
					}

					unKey.registerationKeys = keys;

					userNotificationKeys.add(unKey);

				}

			}

		} catch (Exception e) {
			log("findConnectorNotificationById - Error : " + e.getMessage());
		}

		return userNotificationKeys;

	}

	public ArrayList<RegistrationKey> getAllRegistrationKeys() {

		final var rKeys = new ArrayList<RegistrationKey>();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			final var secondaryInfos = session.createQuery("from SecondaryInfo", SecondaryInfo.class).getResultList();

			for (var secondaryInfo : secondaryInfos) {

				if (null != secondaryInfo.fcmKey) {

					JSONArray tokenJsonArray = new JSONArray(secondaryInfo.fcmKey);

					for (int i = 0; i < tokenJsonArray.length(); i++) {
						rKeys.add(new RegistrationKey(secondaryInfo.userId, tokenJsonArray.getJSONObject(i)));
					}

				}

			}

		} catch (Exception e) {
			log("getAllRegistrationKeys - Error : " + e.getMessage());
		}

		return rKeys;

	}
	
}
