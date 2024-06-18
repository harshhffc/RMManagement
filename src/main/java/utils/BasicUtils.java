package utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Format;
import java.util.Base64;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;


public class BasicUtils {
	
	public BasicUtils(){}
	
	public BasicAuthCreds getClientCreds(String autherizationHeader) throws UnsupportedEncodingException {
		
		byte[] decodedBytes = Base64.getDecoder().decode(autherizationHeader.replaceFirst("Basic ", ""));
		String clientCredsString = new String(decodedBytes, Constants.UTF_8);
		StringTokenizer tokenizer = new StringTokenizer(clientCredsString, ":");
	
		String clientId = tokenizer.nextToken();
		String clientSecret = tokenizer.nextToken();
		
		return new BasicAuthCreds(clientId, clientSecret);
		
	}
	
	public static String getBase64(String value) {
		return Base64.getEncoder().encodeToString(value.getBytes());
	}
	
	public String getMD5Hash(String string) throws NoSuchAlgorithmException {
		
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashInBytes = md.digest(string.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
		
	}
	
	public static String getTheKey(String string) throws Exception {        
		StringBuilder sb = new StringBuilder();

		
        MessageDigest md = MessageDigest.getInstance("MD5");
        String thePasscode = string + ProptertyUtils.getMamasSpaghetti();
        byte[] hashInBytes = md.digest(thePasscode.getBytes(StandardCharsets.UTF_8));

        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
		
        
        return sb.toString();
		
	}
	
	public static JSONObject getSuccessTemplateObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(Constants.STATUS, Constants.SUCCESS);
		jsonObject.put(Constants.MESSAGE, Constants.NA);
		return jsonObject;
	}
	
	public static JSONObject getFailureTemplateObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(Constants.STATUS, Constants.FAILURE);
		jsonObject.put(Constants.MESSAGE, Constants.DEFAULT_ERROR_MESSAGE);
		return jsonObject;
	}
	
	public static String substringToLength(String string, int length) {
		
		if (null != string && string.length() > length) 
			return string.substring(0, length);
		
		return string;
	}
	
	public static JSONObject fetchJson(JSONObject json) {
		
		if (json.has("nameValuePairs"))
			return json.getJSONObject("nameValuePairs");
		
		return json;
		
	}
	
	public String getIPAddress(HttpServletRequest request) {
		
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (null == ipAddress) ipAddress = request.getRemoteAddr();
		
		return ipAddress; 
		
	}
	
	public static String getRandomKey() {
		
		Random random = new Random();
		double randomNumber = random.nextInt(999999); // Six 9s
		randomNumber += 213;
		return "YH8_" + (int)(randomNumber / 7) + "-8u" + (int)(randomNumber / 103) + "IK";
		
	}
	
	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}
	
	public static boolean isNotNullOrNA(String value) {
		return (null != value && !value.equalsIgnoreCase(Constants.NA) && !value.equalsIgnoreCase("Null") && !value.isEmpty());
	}
	
	public static String getBase64FromFile(String filePath) throws IOException {
		byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath));
		return Base64.getEncoder().encodeToString(fileContent);
	}
	
	public static String getCurrencyString(double amount) {

		Format format = com.ibm.icu.text.NumberFormat.getCurrencyInstance(new Locale("en", "in"));
		String currency = format.format(new BigDecimal(amount));
		return currency.replace("₹ ", "").replace("₹", "").replace(".00", "").trim();

	}
	
	public enum MimeMap {
		PDF("application/pdf", ".pdf"), PNG("image/png", ".png"), JPG("image/jpeg", ".jpg");
		public final String mime;
		public final String extention;
		private MimeMap(String mime, String extention) {
			this.mime = mime;
			this.extention = extention;
		}
		public static String mapMimetoExt(String mime) {
			for (MimeMap item : MimeMap.values()) {
				if (item.mime.equals(mime))
					return item.extention;
			}
			return "Unknown";
		}
		public static String mapExtToMime(String ext) {
			for (MimeMap item : MimeMap.values()) {
				if (item.extention.equals(ext))
					return item.mime;
			}
			return "Unknown";
		}
	}
	
	public static String getTruncatedDataFromEnd(String value, Integer size) {
		
		if (value.length() < size) return value;
		
		return value.substring(value.length() - size);
	}

}
