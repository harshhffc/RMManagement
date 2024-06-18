package v1.repository;

import java.util.ArrayList;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;

import models.PaymentInfo;
import utils.HibernateUtil;
import utils.LoggerUtils;

public class PaymentInfoRepository {
	
	private void log(String value) {
		LoggerUtils.log("PaymentRepository." + value);
	}

	public boolean savePaymentInfo(PaymentInfo paymentInfo) {

		Transaction transaction = null;
		Session session = null;

		try {

			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			session.saveOrUpdate(paymentInfo);
			transaction.commit();
			session.close();

			return true;

		} catch (Exception e) {
			log("savePaymentInfo - Error : " + e.getMessage());
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
	
	public PaymentInfo findPaymentInfoByTransId(String transId) {

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			return session.createQuery("from PaymentInfo where transaction_id = :transId", PaymentInfo.class)
					.setParameter("transId", transId)
					.getSingleResult();

		}catch (Exception e) {
			log("findPaymentInfoByTransId: Error : " + e.getMessage());
			e.printStackTrace();
		}

		return null;

	}
	
	public ArrayList<PaymentInfo> getAllPayments(int limit, int offset, int userId) {

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			return (ArrayList<PaymentInfo>) session.createQuery("from PaymentInfo where user_id = :userId "
					+ "and payment_status in ('success', 'initiated', 'cashInitiated')"
					+ "order by initial_datetime desc", PaymentInfo.class)
					.setParameter("userId", userId)
					.setFirstResult(offset)
					.setMaxResults(limit)
					.getResultList();

		} catch (Exception e) {
			LoggerUtils.log("getAllPayments: Error : " + e.getMessage());
			e.printStackTrace();
		}

		return new ArrayList<PaymentInfo>();

	}
	
	public ArrayList<PaymentInfo> getPendingPayments(int limit, int offset, int userId) {

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			return (ArrayList<PaymentInfo>) session.createQuery("from PaymentInfo where user_id = :userId "
					+ "and payment_status in ('initiated')"
					+ "and (receipt_number is null or receipt_number = 'NA')"
					+ "order by initial_datetime desc", PaymentInfo.class)
					.setParameter("userId", userId)
					.setFirstResult(offset)
					.setMaxResults(limit)
					.getResultList();

		} catch (Exception e) {
			LoggerUtils.log("getPendingPayments: Error : " + e.getMessage());
			e.printStackTrace();
		}

		return new ArrayList<PaymentInfo>();

	}
	
	public ArrayList<PaymentInfo> searchPayment(int userId, String searchKey) {

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			return (ArrayList<PaymentInfo>) session.createQuery("from PaymentInfo where user_id = :userId "
					+ "and payment_status in ('success', 'initiated', 'cashInitiated') and opportunity_name like :searchKey "
					+ "order by initial_datetime desc", PaymentInfo.class)
					.setParameter("userId", userId)
					.setParameter("searchKey",  MatchMode.ANYWHERE.toMatchString(searchKey))
					.getResultList();

		} catch (Exception e) {
			LoggerUtils.log("searchPayment: Error : " + e.getMessage());
			e.printStackTrace();
		}

		return new ArrayList<PaymentInfo>();

	}

}
