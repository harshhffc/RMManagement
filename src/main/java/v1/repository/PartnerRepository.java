package v1.repository;

import org.hibernate.Session;
import org.hibernate.Transaction;

import models.WhitelistedIP;
import models.admin.HFPartner;
import utils.HibernateUtil;
import utils.LoggerUtils;

public class PartnerRepository {
	
	public boolean savePartner(HFPartner partner) {

		Transaction transaction = null;
		Session session = null;

		try {

			session = HibernateUtil.getSessionFactory().openSession();
			
			transaction = session.beginTransaction();
			session.saveOrUpdate(partner);
			transaction.commit();
			session.close();

			return true;

		} catch (Exception e) {			
			LoggerUtils.log("savePartner - Error : " + e.getMessage());
			e.printStackTrace();
			if (transaction != null)
				transaction.rollback();
			if (null != session)
				session.close();
		}

		return false;

	}
	
	public HFPartner findPartnerById(String id) {

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			return session.createQuery("from HFPartner where id = :id", HFPartner.class)
					.setParameter("id", id)
					.getSingleResult()
					.updateServicesAllowed();

		}catch (Exception e) {
			LoggerUtils.log("findPartnerById - Error : " + e.getMessage());
		}

		return null;

	}
	
	public HFPartner findPartnerOrgId(String orgId) {

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			return session.createQuery("from HFPartner where orgId = :orgId", HFPartner.class)
					.setParameter("orgId", orgId)
					.getSingleResult()
					.updateServicesAllowed();

		}catch (Exception e) {
			LoggerUtils.log("findPartnerOrgId - Error : " + e.getMessage());
		}

		return null;

	}
	
	public boolean isPartnerIPAllowed(String orgId, String ipAddress) {
	
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			final var whitelistedIp = session.createQuery("from WhitelistedIP where orgId = :orgId and ipAddress = :ipAddress", WhitelistedIP.class)
					.setParameter("orgId", orgId)
					.setParameter("ipAddress", ipAddress)
					.getSingleResult();
			
			return (null != whitelistedIp && whitelistedIp.isActive); 

		}catch (Exception e) {
			LoggerUtils.log("isPartnerIPAllowed - Error : " + e.getMessage());
		}

		return false;
		
	}

}
