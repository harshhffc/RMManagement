package webservices;

import java.util.logging.Logger;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import manager.AdvancedTaskManager;
import manager.PaymentManager;
import manager.UserManager;
import models.DefaultResponse;
import utils.Constants;
import utils.LoggerUtils;
import utils.OneResponse;

@Path("/v1/RMManagementServices")
public class RMManagementWebServices {

	private Logger logger = Logger.getLogger(RMManagementWebServices.class.getSimpleName());

	private void logMethod(String methodName) {
		LoggerUtils.logMethodCall("RMManagementServices V1 : " + methodName);
	}

	@Context
	private HttpServletRequest request;

	@PermitAll
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello() {

//		logger.info("\n\n----------------------\n  Landing page invoked  \n----------------------\n\n");

		return "<html> " + "<title>" + "HFFC RMManagement Portal" + "</title>" + "<body><h1>"
				+ "Welcome to HFFC RMManagement Portal Web Services!" + "</h1></body>" + "</html> ";
	}

	@POST
	@Path("/addLocationInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addLocationInfo(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		LoggerUtils.logMethodCall("addLocationInfo");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.addUserLocationInfo(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while adding user location information: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/addUserInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addUserInfo(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		LoggerUtils.logMethodCall("addUserInfo");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			String ipAddress = request.getRemoteAddr();
			String responseString = uManager.addUserInfo(body, ipAddress).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while adding user information: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/getAvailableDocuments")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAvailableDocuments(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		LoggerUtils.logMethodCall("getAvailableDocuments");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.getAvailableDocuments(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while getting all available documents: " + e.toString());
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
			logger.info("error while initializing payment: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/updatePayment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePayment(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		LoggerUtils.logMethodCall("updatePayment");
		LoggerUtils.logBody(body);

		try {

			PaymentManager pManager = new PaymentManager();
			String responseString = pManager.updatePayment(body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while updating payment status: " + e.toString());
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
			logger.info("error while updating payment status: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@Deprecated
	@POST
	@Path("/getLeaderBoard")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLeaderBoard(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		LoggerUtils.logMethodCall("getLeaderBoard");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			UserManager.LeaderBoardTask lbTask = uManager.new LeaderBoardTask(body);
			Thread thread = new Thread(lbTask);
			thread.start();
			thread.join();

			return Response.ok(lbTask.getResponse().toString(), MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while getting leader board: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/addUpdateApplicantInformation")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addUpdateApplicantInformation(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId,
			String body) {

		LoggerUtils.logMethodCall("addUpdateApplicantInformation");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.addUpdateApplicantInformation(userSourceId, body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while add-updating applicant information: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/addUpdateCoApInformation")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addUpdateCoApInformation(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		LoggerUtils.logMethodCall("addUpdateCoApInformation");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.addUpdateCoApInformation(userSourceId, body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while add-updating co-applicant information: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@Deprecated
	@POST
	@Path("/getProfileForUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLeaderProfileForUser(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		LoggerUtils.logMethodCall("getProfileForUser");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			UserManager.LeaderProfileTask lbTask = uManager.new LeaderProfileTask(userSourceId, body);
			Thread thread = new Thread(lbTask);
			thread.start();
			thread.join();

			return Response.ok(lbTask.getResponse().toString(), MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while getting profile user: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@Deprecated
	@POST
	@Path("/setProfileImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setProfileImage(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId, String body) {

		LoggerUtils.logMethodCall("setProfileImage");
		LoggerUtils.logBody(body);

		try {

			UserManager uManager = new UserManager();
			String responseString = uManager.setProfileImage(userSourceId, body).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while setting users profile image: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@PermitAll
	@GET
	@Path("/syncBankInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response syncBankInfo(@HeaderParam(Constants.CROWN_PASSCODE) String sessionPasscode) {

		UserManager uManager = new UserManager();

		if (!uManager.verifyMyCrown(sessionPasscode)) {
			DefaultResponse errorResponse = new DefaultResponse();
			errorResponse.message = "Unauthorised user";
			return Response.status(401).entity(errorResponse.toJson().toString()).build();
		}

		LoggerUtils.logMethodCall("syncBankInfo");

		try {

			AdvancedTaskManager atManger = new AdvancedTaskManager();

			AdvancedTaskManager.UpdateBankInfoTask upTask = atManger.new UpdateBankInfoTask();
			Thread thread = new Thread(upTask);
			thread.start();
			thread.join();

			String responseString = upTask.getResponse().toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while updating bank info: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@POST
	@Path("/addSitePhotograph")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addSitePhotograph(String body, @HeaderParam(Constants.USER_SOURCE_ID) int userSourceId) {

		logMethod("addSitePhotograph");

		try {

			UserManager userManager = new UserManager();
			return userManager.addSitePhotograph(userSourceId, new JSONObject(body));

		} catch (Exception e) {

			logger.info("Error while addSitePhotograph : " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();

		}

	}

	@GET
	@Path("/getSitePhotographList")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getSitePhotographList(@HeaderParam(Constants.USER_SOURCE_ID) int userSourceId) {

		logMethod("getSitePhotographList");

		try {

			UserManager userManager = new UserManager();
			return userManager.getSitePhotographList(userSourceId);

		} catch (Exception e) {

			logger.info("Error while getSitePhotographList : " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();

		}

	}

}