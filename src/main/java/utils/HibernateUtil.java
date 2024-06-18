
package utils;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import models.Creds;
import models.PaymentInfo;
import models.SecondaryInfo;
import models.User;
import models.WhitelistedIP;
import models.admin.HFPartner;
import models.notification.RmNotification;
import models.notification.UserNotificationInfo;
import utils.ProptertyUtils.Keys;


public class HibernateUtil {

	private static SessionFactory sessionFactory = null;
	
	public static SessionFactory getSessionFactory() throws Exception {
		
		if (sessionFactory == null) {
		
			Configuration config = new Configuration();
			
			Properties settings = new Properties();			
			settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");										
			
			if (Constants.IS_DB_IN_PRODUCTION) {
				
				settings.put(Environment.URL, "jdbc:mysql://rmm-db-instance.c5jfvga5d4bj.ap-south-1.rds.amazonaws.com:3306/RMManagementDB?useUnicode=true&characterEncoding=utf8&autoReconnect=true");
				settings.put(Environment.USER, "masternaol_rmm");
				settings.put(Environment.PASS, ProptertyUtils.getValurForKey(Keys.PROD_DB_PASS));
				
			} else {
			
				if (Constants.IS_STAGING) {
					
					settings.put(Environment.URL, "jdbc:mysql://127.0.0.1:3306/RMManagementDB?useUnicode=true&characterEncoding=utf8&autoReconnect=true");
					settings.put(Environment.USER, "naol");
					settings.put(Environment.PASS, "$Hffc9_SarikaML");
					
				} else {
					
					// p@radise7_mysql - rabit
					// Sarika@mysql7 - Sarika
					// Ranan#123 - Ranan
					// Sanjay14mysql - Sanjay
					// root 	- Nikul
					
					settings.put(Environment.URL, "jdbc:mysql://127.0.0.1:3306/RMManagementDB?useUnicode=true&characterEncoding=utf8&autoReconnect=true");
					settings.put(Environment.USER, "root");
					settings.put(Environment.PASS, "Sanjay14mysql"); // TODO: Change for your local


				}
				
			}
			
			settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL57Dialect");
			settings.put(Environment.SHOW_SQL, "false");
			settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
			settings.put(Environment.HBM2DDL_AUTO, "update");
			settings.put("hibernate.id.new_generator_mappings","false");
			settings.put("hibernate.jdbc.batch_size", Constants.HITBERNATE_BATCH_SIZE);
			
			config.setProperties(settings);
			config.addAnnotatedClass(HFPartner.class);
			config.addAnnotatedClass(RmNotification.class);
			config.addAnnotatedClass(UserNotificationInfo.class);
			config.addAnnotatedClass(WhitelistedIP.class);
			config.addAnnotatedClass(User.class);
			config.addAnnotatedClass(PaymentInfo.class);
			config.addAnnotatedClass(SecondaryInfo.class);
			config.addAnnotatedClass(Creds.class);
			
			ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(config.getProperties()).build();
			
			sessionFactory = config.buildSessionFactory(serviceRegistry);
			
		}
		
		return sessionFactory;
		
	}
	
}