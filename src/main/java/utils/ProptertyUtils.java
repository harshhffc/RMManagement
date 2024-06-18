package utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class ProptertyUtils {
	
	private static Properties prop = null;
	private static KeyBearer keyBearer = null;
	
	private static void initPropterties() {
		
		if (null == prop || null == keyBearer) {
			prop = new Properties();
			InputStream inputStream = null;
			try {
				String propFileName = "config.properties";
				inputStream = ProptertyUtils.class.getClassLoader().getResourceAsStream(propFileName);
				if (inputStream != null) {
					
					prop.load(inputStream);					
					
					keyBearer = new KeyBearer(
							getModifiedSpaghetti(),
							getModifiedSalt()
						);
					
				} else {
					throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
				}
			} catch (Exception e) {
				System.out.println("Exception while loading credentials: " + e.getMessage());
			} finally {
				try {
					inputStream.close();
				} catch (Exception e) {
					LoggerUtils.log("Error while closing input stream while getting propterties: " + e.getMessage());
				}
			}
		}
		
	}
	
	public static KeyBearer getKeyBearer() {
		initPropterties();
		return keyBearer;
	}
	
	public static String getMamasSpaghetti() throws Exception {
		initPropterties();
		return getModifiedSpaghetti();
	}
	
	private static String getModifiedSpaghetti() throws Exception {
		if (null != prop)
			return prop.getProperty(Keys.MAMAS_SPAGHETTI.value).substring(0, 12);
		else throw new NullPointerException("Prop is not initialized.");
	}
	
	public static String getMamasSalt() throws Exception {
		initPropterties();
		return prop.getProperty(Keys.MAMAS_SALT.value).substring(0, 18);
	}
	
	private static String getModifiedSalt() throws Exception {
		if (null != prop)
			return prop.getProperty(Keys.MAMAS_SALT.value).substring(0, 18);
		else throw new NullPointerException("Prop is not initialized.");
	}
	
	public static String getValurForKey(Keys key) throws Exception {
		
		if (key == Keys.MAMAS_SALT || key == Keys.MAMAS_SPAGHETTI) 
			throw new Exception("To get Mamas salt or spaghetti, please call their dedicated methods");
			
		initPropterties();	
		
		return keyBearer.decrypt(prop.getProperty(key.value));
		
	}
	
	public enum Keys {
		SF_TEST_LOGIN_URL("salesforce_test_LOGINURL"),
		SF_TEST_CLIENT_ID("salesforce_test_CLIENTID"),
		SF_TEST_CLIENT_SECRET("salesforce_test_CLIENTSECRET"),
		SF_TEST_USERNAME("salesforce_test_USERNAME"),
		SF_TEST_PASSWORD("salesforce_test_PASSWORD"),	
		
		SF_PROD_LOGIN_URL("salesforce_LOGINURL"),
		SF_PROD_CLIENT_ID("salesforce_CLIENTID"),
		SF_PROD_CLIENT_SECRET("salesforce_CLIENTSECRET"),
		SF_PROD_USERNAME("salesforce_USERNAME"),
		SF_PROD_PASSWORD("salesforce_PASSWORD"),
		
		SF_PREPROD_LOGIN_URL("salesforce_preprod_LOGINURL"),
		SF_PREPROD_CLIENT_ID("salesforce_preprod_CLIENTID"),
		SF_PREPROD_CLIENT_SECRET("salesforce_preprod_CLIENTSECRET"),
		SF_PREPROD_USERNAME("salesforce_preprod_USERNAME"),
		SF_PREPROD_PASSWORD("salesforce_preprod_PASSWORD"),		

		KARZA_TEST_API_KEY("KARZA_TEST_API_KEY"),	
		KARZA_PRODUCTION_API_KEY("KARZA_PRODUCTION_API_KEY"),
		
		NAOL_CODE("NAOL_CODE"),
		KEY_TO_THE_SOUCE("KEY_TO_THE_SOUCE"),
		RM_AHAM_BRAHMA("RM_AHAM_BRAHMA"),
		KEY_TO_THE_CRON("KEY_TO_MY_CRON"),
		MAMAS_SPAGHETTI("MAMAS_SPAGHETTI"),
		MAMAS_SALT("MAMAS_SALT"),
		
		S3_BUCKET_ACCESS_KEY("S3_BUCKET_ACCESS_KEY"),
		S3_BUCKET_SECRET_KEY("S3_BUCKET_SECRET_KEY"),
		
		AUTH_KEY("AUTH_KEY"),
		
		HFO_CLIENT_ID_TEST("HFO_CLIENT_ID_TEST"),
		HFO_CLIENT_SECRET_TEST("HFO_CLIENT_SECRET_TEST"),
		HFO_ORG_ID_TEST("HFO_ORG_ID_TEST"),
		
		HFO_CLIENT_ID_PROD("HFO_CLIENT_ID_PROD"),
		HFO_CLIENT_SECRET_PROD("HFO_CLIENT_SECRET_PROD"),
		HFO_ORG_ID_PROD("HFO_ORG_ID_PROD"),
		
		PROD_DB_PASS("PROD_DB_PASS");
		
		final String value;
		Keys(String value) {
			this.value = value;
		}
		
	}
	
}

