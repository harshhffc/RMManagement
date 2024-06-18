package webservices;

import java.util.logging.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import models.DefaultResponse;
import utils.Constants;
import utils.LocalResponse;
import utils.LoggerUtils;
import utils.OneResponse;
import v2.managers.PaymentManager;
import v2.managers.UserManager;

@Path("/v2/RMManagementServices")
public class RMManagementWebServicesV2 {

	private Logger logger = Logger.getLogger(RMManagementWebServicesV2.class.getSimpleName());

	private void logMethod(String methodName) {
		LoggerUtils.logMethodCall("RMManagementServices V2 : " + methodName);
	}

	private void logBody(String methodName) {
		LoggerUtils.logBody("RMManagementServices V2 : " + methodName);
	}

	@PermitAll
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello() {

//		logger.info("\n\n----------------------\n  Landing page invoked  \n----------------------\n\n");

		return "<html> " + "<title>" + "HFFC RMManagement Portal" + "</title>" + "<body><h1>"
				+ "Welcome to HFFC RMManagement Portal Web Services!" + "</h1></body>" + "</html> ";
	}

	@RolesAllowed(Constants.CROWN_USER)
	@GET
	@Path("/retriveAndStoreLeaderboardHistory")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response retriveAndStoreLeaderboardHistory() {

		logMethod("retriveAndStoreLeaderboardHistory");

		try {

			UserManager uManager = new UserManager();
			UserManager.LeaderBoardHistory lbTask = uManager.new LeaderBoardHistory();
			Thread thread = new Thread(lbTask);
			thread.start();
			thread.join();

			return Response.ok(lbTask.getResponse().toString(), MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting storing leaderboard history: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@RolesAllowed(Constants.CROWN_USER)
	@GET
	@Path("/insertOrUpdateLeaderBoardHistory")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertOrUpdateLeaderBoardHistory() {

		logMethod("insertOrUpdateLeaderBoardHistory");

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.insertOrUpdateLeaderBoardHistory().toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while  current updating leader board history information: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}
	}

	@POST
	@Path("/getLeaderBoard")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLeaderBoard(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("getLeaderBoard");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			UserManager.LeaderBoardTask lbTask = uManager.new LeaderBoardTask(body);
			Thread thread = new Thread(lbTask);
			thread.start();
			thread.join();

			return Response.ok(lbTask.getResponse().toString(), MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting leader board: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/getProfileForUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLeaderProfileForUser(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("getProfileForUser");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			UserManager.LeaderProfileTask lbTask = uManager.new LeaderProfileTask(userSourceId, body);
			Thread thread = new Thread(lbTask);
			thread.start();
			thread.join();

			return Response.ok(lbTask.getResponse().toString(), MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting profile user: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/addNotificationToken")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addNotificationToken(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("addNotificationToken");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.addNotificationToken(userSourceId, body).toString();

			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while adding notification token: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}
	
	@POST
	@Path("/addApnsToken")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addApnsToken(String body, @HeaderParam(Constants.USER_SOURCE_ID) int userId) {
	
		
		LoggerUtils.logMethodCall("Add apns token");
		LoggerUtils.logBody(body);
		
		try {
			
			UserManager userManager = new UserManager();
		
			String responseString = userManager.addApnsToken(userId, body).toString();
			
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			  
		} catch (Exception e) {
			
			logger.info("error while adding apns token: " + e.toString());
			e.printStackTrace();
			return Response.ok(new LocalResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}

	@POST
	@Path("/getUserDashboardData")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserDashboardData(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("getUserDashboardData");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			UserManager.UserDashboardTask lbTask = uManager.new UserDashboardTask(userSourceId, body);
			Thread thread = new Thread(lbTask);
			thread.start();
			thread.join();

			return Response.ok(lbTask.getResponse().toString(), MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting user dashboard data: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/addAppsData")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addAppsData(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("addAppsData");

		try {

			JSONObject bodyObject = new JSONObject(body);
			UserManager userManager = new UserManager();
			String resp = userManager.addInstalledAppInfo(userSourceId, bodyObject).toString();

			return Response.ok(resp, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while adding apps info: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/setProfileImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setProfileImage(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("setProfileImage");

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.setProfileImage(userSourceId, body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while setting users profile image: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/addUpdateTaskAndActivity")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addUpdateTaskAndActivity(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("addUpdateTaskAndActivity");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.addUpdateTaskAndActivity(userSourceId, body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while Adding / Updateing task and acticity: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/updateApplicantAndFetchVerifiedDetails")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateApplicantAndFetchVerifiedDetails(String body) {

		logMethod("updateApplicantAndFetchVerifiedDetails");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.updateApplicantAndFetchVerifiedDetails(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while updating update applicant fetch document: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/setApplicantProfilePicture")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setApplicantProfilePicture(String body) {

		logMethod("setApplicantProfilePicture");

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.setApplicantProfilePicture(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while setting applicant or co-applicant profile picture: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/generateOTP")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response generateOTP(String body) {

		LoggerUtils.logMethodCall("Generate OTP");
		LoggerUtils.logBody(body);

		try {

			UserManager userManager = new UserManager();
			String responseString = userManager.generateOTP(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while generating OTP: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/verifyMobileNumber")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response verifyOTP(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		LoggerUtils.logMethodCall("Verify OTP");
		LoggerUtils.logBody(body);

		try {

			UserManager userManager = new UserManager();
			String responseString = userManager.verifyMobileNumber(userSourceId, body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while verifying mobile number: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/resendOTP")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response resendOTP(String body) {

		LoggerUtils.logMethodCall("Resend OTP");
		LoggerUtils.logBody(body);

		try {

			UserManager userManager = new UserManager();
			String responseString = userManager.resendOTP(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while resending OTP: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@RolesAllowed(Constants.CROWN_USER)
	@GET
	@Path("/syncRegionMap")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response syncRegionMap() {

		logMethod("syncRegionMap");

		try {
			UserManager uManager = new UserManager();
			String responseString = uManager.insertOrUpdateRegionMap().toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while  current updating region map information: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}
	}

//	================== Notification Methods Implementation ================

	@POST
	@Path("/getNotifications")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getNotifications(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("getNotifications");
		LoggerUtils.logBody(body);

		try {

			UserManager manager = new UserManager();

			String responseString = manager.getNotifications(userSourceId, new JSONObject(body)).toString();

			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while getting notifications: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/updateNotificationStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateNotificationStatus(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("updateNotificationStatus");
		LoggerUtils.logBody(body);

		try {

			UserManager manager = new UserManager();

			String responseString = manager.updateNotificationStatus(userSourceId, new JSONObject(body)).toString();

			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while setting notification status: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@GET
	@Path("/getUnreadNotificationCount")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUnreadNotificationCount(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId) {

		logMethod("getUnreadNotificationCount");

		try {

			UserManager manager = new UserManager();

			String responseString = manager.getUnreadNotificationCount(userSourceId).toString();

			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while getting unread notification count: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@GET
	@Path("/getBankPickList")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getBankPickList(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId) {

		logMethod("getBankPickList");

		try {

			UserManager manager = new UserManager();

			String responseString = manager.getBankPickList().toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while getting getBankPickList: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/customerLookUp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response customerLookUp(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("customerLookUp");
		LoggerUtils.logBody(body);

		try {

			UserManager manager = new UserManager();

			String responseString = manager.customerLookUp(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while getting getBankPickList: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/initializePayment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response initializePayment(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		LoggerUtils.logMethodCall("initializePayment");
		LoggerUtils.logBody(body);

		try {

			PaymentManager pManager = new PaymentManager();
			String responseString = pManager.initalizePayment(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while initializing payment: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@GET
	@Path("/getPendingPayments")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getPendingPayments(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId) {

		LoggerUtils.logMethodCall("getPendingPayments");

		try {

			PaymentManager pManager = new PaymentManager();
			String responseString = pManager.getPendingPayments(userSourceId).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getPendingPayments: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/customerLookUpGlobal")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response customerLookUpGlobal(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("customerLookUpGlobal");
		LoggerUtils.logBody(body);

		try {

			UserManager manager = new UserManager();

			String responseString = manager.customerLookUpGlobal(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while getting customerLookUpGlobal: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/sendPaymentLink")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendPaymentLink(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("sendPaymentLink");
		logBody(body);

		try {

			return new UserManager().sendPaymentLink(userSourceId, new JSONObject(body));

		} catch (Exception e) {

			logger.info("Error while sendPaymentLink : " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();

		}

	}

	@POST
	@Path("/getPayments")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getPayments(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("getPayments");
		logBody(body);

		try {

			return new UserManager().getPayments(userSourceId, new JSONObject(body));

		} catch (Exception e) {

			logger.info("Error while getPayments : " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();

		}

	}

	@POST
	@Path("/searchPayment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchPayment(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("searchPayment");
		logBody(body);

		try {

			return new UserManager().searchPayment(userSourceId, new JSONObject(body));

		} catch (Exception e) {

			logger.info("Error while searchPayment : " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();

		}

	}

	@POST
	@Path("/generateFailedReceipt")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response generateFailedReceipt(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("generateFailedReceipt");

		try {

			return new UserManager().generateFailedReceipt(userSourceId, new JSONObject(body));

		} catch (Exception e) {

			logger.info("Error while generateFailedReceipt : " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();

		}

	}

	@GET
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(@HeaderParam(Constants.USER_SOURCE_ID) int userId) {

		logMethod("logout");

		try {

			return new UserManager().logout(userId);

		} catch (Exception e) {
			logger.info("logout - Error while logging out: " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();
		}
	}
	
	@GET
	@Path("/getLoanDetails/{loanAccountNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoanDetails(
			@HeaderParam(Constants.USER_SOURCE_ID) int userId,
			@PathParam("loanAccountNumber") String loanAccountNumber) {
		
		logMethod("getLoanDetails");
		
		try {
			
			return new UserManager().getLoanDetails(loanAccountNumber);
			
		} catch (Exception e) {
			logger.info("getLoanDetails - Error while getting loan details: " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();
		}
	}

	@RolesAllowed(Constants.CROWN_USER)
	@GET
	@Path("/syncSfUserDetail")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response syncSfUserDetail() {

		logMethod("syncSfUserDetail");

		try {

			return new UserManager().syncSfUserDetail();

		} catch (Exception e) {
			logger.info("Error while syncSfUserDetail: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}
	}

	@POST
	@Path("/requestCall")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response clickToCall(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		logMethod("requestCall");
		LoggerUtils.logBody(body);

		try {
			return new UserManager().initiateCall(userSourceId, body);

		} catch (Exception e) {
			logger.info("Error while requestCall: " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();
		}

	}

}
