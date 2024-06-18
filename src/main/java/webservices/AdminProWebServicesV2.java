package webservices;

import java.util.logging.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import manager.AdminUserManager;
import models.DefaultResponse;
import utils.Constants;
import utils.LocalResponse;
import utils.LoggerUtils;
import utils.OneResponse;
import v2.managers.ExternalPartnerManager;
import v2.managers.NotificationHelper;

@Path("/v2/as")
public class AdminProWebServicesV2 {

	private Logger logger = Logger.getLogger(AdminProWebServicesV2.class.getSimpleName());

	@Context
	private HttpServletRequest request;

	@PermitAll
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello() {

		logger.info("\n\n----------------------\n  Landing page invoked  \n----------------------\n\n");

		return "<html> " + "<title>" + "HFO RM Pro Admin Portal" + "</title>" + "<body><h1>"
				+ "Welcome to HFO RM Pro Admin Portal Web Services V2!" + "</h1></body>" + "</html> ";
	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@GET
	@Path("/authenticateClient")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response authenticateClient(@HeaderParam(Constants.ORG_ID) String orgId) {

		LoggerUtils.logMethodCall("authenticateClient");

		try {

			return new ExternalPartnerManager().authenticateClient(orgId);

		} catch (Exception e) {

			logger.info("Error while authenticateClient: " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();

		}

	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@GET
	@Path("/getUserId")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserId(@QueryParam(Constants.EMAIL_ID) String emailId) {

		LoggerUtils.logMethodCall("getUserId");

		try {

			AdminUserManager aManager = new AdminUserManager();
			String responseString = aManager.getUserId(emailId).toString();

			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("Error while getUserId: " + e.toString());
			e.printStackTrace();
			return Response.ok(new LocalResponse(e.getMessage()).toJson().toString(), MediaType.APPLICATION_JSON)
					.build();

		}

	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@POST
	@Path("/searchUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchUser(@HeaderParam(Constants.USER_ID) int userId, String body) {

		LoggerUtils.logMethodCall("searchUser");
		LoggerUtils.logBody(body);

		try {

			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.searchUser(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting user information: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@POST
	@Path("/getLocationInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLocationInfo(@HeaderParam(Constants.USER_ID) int userId, String body) {

		LoggerUtils.logMethodCall("getLocationInfo");
		LoggerUtils.logBody(body);

		try {

			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.getLocationInfo(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting rm user locatoin info: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@POST
	@Path("/getDashboard")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getDashboard(@HeaderParam(Constants.USER_ID) int userId, String body) {

		LoggerUtils.logMethodCall("getDashboard");
		LoggerUtils.logBody(body);

		try {

			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.getDashboard(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting dashboard info: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@POST
	@Path("/getActiveUsers")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getActiveUsers(@HeaderParam(Constants.USER_ID) int userId, String body) {

		LoggerUtils.logMethodCall("getActiveUsers");
		LoggerUtils.logBody(body);

		try {

			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.getActiveUsers(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting active user info: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@POST
	@Path("/getUserAndBranchInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserAndBranchInfo(@HeaderParam(Constants.USER_ID) int userId, String body) {

		LoggerUtils.logMethodCall("getUserAndBranchInfo");
		LoggerUtils.logBody(body);

		try {

			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.getUserAndBranchInfo(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting getUserAndBranch Info: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@POST
	@Path("/getPaymentInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getPaymentInfo(@HeaderParam(Constants.USER_ID) int userId, String body) {

		LoggerUtils.logMethodCall("getPaymentInfo");

		try {

			JSONObject bodyObject = new JSONObject(body);
			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.getPaymentInfo(userId, bodyObject).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting getPaymentInfo : " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@POST
	@Path("/updatePaymentStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePaymentStatus(@HeaderParam(Constants.USER_ID) int userId, String body) {

		LoggerUtils.logMethodCall("updatePaymentStatus");

		try {

			JSONObject bodyObject = new JSONObject(body);
			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.updatePaymentStatus(userId, bodyObject).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("Error while getting paymentStatusSuccessOrNot : " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@POST
	@Path("/pushNotification")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response pushNotification(@HeaderParam(Constants.USER_ID) int userId, String body) {

		LoggerUtils.logMethodCall("pushNotification");
//		LoggerUtils.logBody(body);

		try {

			JSONObject bodyObject = new JSONObject(body);
			NotificationHelper nHelper = new NotificationHelper(userId);

			return nHelper.pushNotification(bodyObject);

		} catch (Exception e) {

			logger.info("Error while pushing notification: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}

	@RolesAllowed(Constants.ADMIN_SERVICES)
	@POST
	@Path("/CrossNotification.push")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response asPushCrossNotification(@HeaderParam(Constants.ORG_ID) String orgId, String body) {

		LoggerUtils.logMethodCall("asPushCrossNotification");
		LoggerUtils.logBody(body);

		try {

			return new ExternalPartnerManager().pushCrossNotification(orgId, new JSONObject(body));

		} catch (Exception e) {
			logger.info("Error while asPushCrossNotification: " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();

		}

	}
	
	@RolesAllowed(Constants.ADMIN_SERVICES)
	@POST
	@Path("/Payment.Remote.updateStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePaymentStatus(@HeaderParam(Constants.ORG_ID) String orgId, String body) {

		LoggerUtils.logMethodCall("updatePaymentStatus");

		try {

			return new ExternalPartnerManager().updatePaymentStatus(new JSONObject(body));

		} catch (Exception e) {

			logger.info("Error while updatePaymentStatus : " + e.toString());
			e.printStackTrace();
			return new OneResponse().getDefaultFailureResponse();

		}

	}

}