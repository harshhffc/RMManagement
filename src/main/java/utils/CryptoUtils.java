package utils;

public class CryptoUtils {

	private static KeyBearer keyBearer = null;
	
	private static void init() {
		
		if (null != keyBearer) return;
		
		try {
			
			keyBearer = new KeyBearer(
					ProptertyUtils.getMamasSpaghetti(),
					ProptertyUtils.getMamasSpaghetti()
				);
						
		} catch (Exception e) {
			LoggerUtils.log("Error while initializing CryptoUtils: " + e.getMessage());
			e.printStackTrace();		
		}
		
	}
	
	public static String encrypt(String value) {
		init();
		try {			
			return keyBearer.encrypt(value);
		} catch (Exception e) {
			LoggerUtils.log("CryptoUtils - Failed to encrypt : " + e.getMessage());
			return value;
		}
	}
	
	public static String decrypt(String value) {
		init();
		try {			
			return keyBearer.decrypt(value);
		} catch (Exception e) {
			LoggerUtils.log("CryptoUtils - Failed to decrypt : " + e.getMessage());
			return value;
		}
	}
	
}
