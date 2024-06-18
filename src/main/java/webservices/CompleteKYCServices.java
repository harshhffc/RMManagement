package webservices;


import java.util.logging.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import models.DefaultResponse;
import totalkyc.DocumentKYCHelper;
import utils.Constants;
import utils.LoggerUtils;

@Path("/v1/CompleteKYCServices")
public class CompleteKYCServices {
	
	private Logger logger = Logger.getLogger(CompleteKYCServices.class.getSimpleName());
	
	@PermitAll
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello() {
		  
		logger.info("\n\n----------------------\n  Landing page invoked  \n----------------------\n\n");
		
	    return "<html> " + "<title>" + "HFFC CompleteKYC Portal" + "</title>"
	        + "<body><h1>" + "Welcome to HFFC CompleteKYC Portal Web Services!" + "</h1></body>" + "</html> ";
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/documentKYC")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response documentKYC(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {
		
		LoggerUtils.logMethodCall("documentKYC");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.documentKYC(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while performing documentKYC: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/mobileOTPGenerate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response mobileOTPGenerate(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {
		
		LoggerUtils.logMethodCall("mobileOTPGenerate");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.mobileOTPGenerate(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while generate OTP for mobile: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/mobileOTPAuth")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response mobileOTPAuth(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {
		
		LoggerUtils.logMethodCall("mobileOTPAuth");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.mobileOTPAuth(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while authenticating OTP for mobile: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/electricityBillAuth")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response electricityBillAuth(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {
		
		LoggerUtils.logMethodCall("electricityBillAuth");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.electricityBillAuth(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while authenticating electricity bill: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/telephoneBillAuth")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response telephoneBillAuth(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {
		
		LoggerUtils.logMethodCall("telephoneBillAuth");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.telephoneBillAuth(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while authenticating telephone bill: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/lpgIdAuth")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response lpgIdAuth(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {			
		
		LoggerUtils.logMethodCall("lpgIdAuth");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.lpgIdAuth(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while authenticating LPG ID: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/itrAuthentication")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response itrAuthentication(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {
		
		LoggerUtils.logMethodCall("itrAuthentication");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.itrAuthentication(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while ITR Authentication: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/vehicleRCAuthentication")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response vehicleRCAuthentication(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {		
		
		LoggerUtils.logMethodCall("vehicleRCAuthentication");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.vehicleRCAuthentication(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while Vehicle RC Authentication: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/epfGetOTP")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response epfGetOTP(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {	
		
		LoggerUtils.logMethodCall("epfGetOTP");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.epfGetOTP(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while EPF get OTP: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/epfGetPassbook")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response epfGetPassbook(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {		
		
		LoggerUtils.logMethodCall("epfGetPassbook");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.epfGetPassbook(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while EPF get passbook: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/documentOCR")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response documentOCR(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {		
		
		LoggerUtils.logMethodCall("documentOCR");
		
		try {

			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.performDocumentOCR(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while performing document OCR: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/itrvOCR")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response itrvOCR(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {		
		
		LoggerUtils.logMethodCall("itrvOCR");
		LoggerUtils.logBody(body);
		
		try {

			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.performITRvOCR(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while performing ITR-V OCR: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/epfUANLookup")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response epfUANLookup(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {
		
		LoggerUtils.logMethodCall("epfUANLookup");
		LoggerUtils.logBody(body);
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.epfUANLookup(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while EPF UAN Lookup: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/epfUanEmployerLookup")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response epfUanEmployerLookup(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {		
		
		LoggerUtils.logMethodCall("epfUanEmployerLookup");
		LoggerUtils.logBody(body);
		
		try {

			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.epfUANEmployerLookup(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while epf uan employer lookup: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.KYC_USER)
	@POST
	@Path("/gstAuth")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response gstAuth(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			String body) {		
		
		LoggerUtils.logMethodCall("gstAuth");
		LoggerUtils.logBody(body);
		
		
		try {
			
			DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
			String responseString = dkHelper.authenticateGst(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while performing gstAuth: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}

}
