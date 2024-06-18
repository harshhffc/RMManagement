package utils;

import java.util.Properties;  
import javax.mail.*;  
import javax.mail.internet.*;  

public class MailUtils {
	
	private static MailUtils instance = null;
	private MailUtils() {}
	public static MailUtils getInstance() {
		if (instance == null)
			instance = new MailUtils();
		return instance;
	}
	
	public boolean sendDefaultMail(String subject, String emailMessage, String... users) {
		
		final String user="developer.naol@homefirstindia.com";    
		
		//Get the session object  
		Properties props = new Properties();  
		props.setProperty("mail.transport.protocol", "smtp");     
	    props.setProperty("mail.host", "smtp.gmail.com");  
	    props.put("mail.smtp.auth", "true");  
	    props.put("mail.smtp.port", "465");  
	    props.put("mail.debug", "true");  
	    props.put("mail.smtp.socketFactory.port", "465");  
	    props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");  
	    props.put("mail.smtp.socketFactory.fallback", "false");
		
	    try {	    
	    
	    	final String naolCode = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.NAOL_CODE);
	    	
			Session session = Session.getDefaultInstance(props,  
					new javax.mail.Authenticator() {  
						protected PasswordAuthentication getPasswordAuthentication() {  		    
							return new PasswordAuthentication(user, naolCode);  
						}  
					}
			);    
			
			InternetAddress[] recipients = new InternetAddress[users.length];
			for (int i = 0; i < users.length; i++) {
				recipients[i] = new InternetAddress(users[i]); 
			}
			
			MimeMessage message = new MimeMessage(session);  
			message.setFrom(new InternetAddress(user, "Naol"));  
		    message.addRecipients(Message.RecipientType.TO, recipients);  
		    message.setSubject(subject);  
		    message.setContent(emailMessage, "text/plain");  
		       
		    //send the message    
		    Transport.send(message);  
		  
		    LoggerUtils.log("==> Mail sent successfully...");  
		   
		} catch (Exception e) {
			System.out.println("\n\nFailed to send message: " + e.toString());
			e.printStackTrace();
		}    
		
		return false;
	}

}