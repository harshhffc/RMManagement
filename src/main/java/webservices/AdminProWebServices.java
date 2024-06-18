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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;
import manager.AdminUserManager;
import models.DefaultResponse;
import utils.Constants;
import utils.LoggerUtils;
import v2.managers.NotificationHelper;

@Path("/v1/AdminProServices")
public class AdminProWebServices {

	private Logger logger = Logger.getLogger(AdminProWebServices.class.getSimpleName());
	
	@Context
	private HttpServletRequest request;
	
	@PermitAll
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello() {
		  
		logger.info("\n\n----------------------\n  Landing page invoked  \n----------------------\n\n");
		
	    return "<html> " + "<title>" + "RM Pro Admin Portal" + "</title>"
	        + "<body><h1>" + "Welcome to RM Pro Admin Portal Web Services!" + "</h1></body>" + "</html> ";
	}
	
	@RolesAllowed(Constants.ADMIN)
	@POST
	@Path("/searchUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUser(
			@HeaderParam(Constants.USER_ID) int userId,
			String body) {	
		
		LoggerUtils.logMethodCall("searchUser");
		LoggerUtils.logBody(body);
		
		try {

			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.searchUser(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while getting user information: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.ADMIN)
	@POST
	@Path("/getLocationInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLocationInfo(
			@HeaderParam(Constants.USER_ID) int userId,
			String body) {		
		
		LoggerUtils.logMethodCall("getLocationInfo");
		LoggerUtils.logBody(body);
		
		try {

			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.getLocationInfo(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while getting rm user locatoin info: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.ADMIN)
	@POST
	@Path("/getDashboard")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getDashboard(
			@HeaderParam(Constants.USER_ID) int userId,
			String body) {
		
		LoggerUtils.logMethodCall("getDashboard");
		LoggerUtils.logBody(body);
		
		try {

			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.getDashboard(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while getting dashboard info: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.ADMIN)
	@POST
	@Path("/getActiveUsers")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getActiveUsers(
			@HeaderParam(Constants.USER_ID) int userId,
			String body) {		
		
		LoggerUtils.logMethodCall("getActiveUsers");
		LoggerUtils.logBody(body);
		
		try {

			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.getActiveUsers(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while getting active user info: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
	@RolesAllowed(Constants.ADMIN)
	@POST
	@Path("/getUserAndBranchInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserAndBranchInfo(
			@HeaderParam(Constants.USER_ID) int userId,
			String body) {
		
		LoggerUtils.logMethodCall("getUserAndBranchInfo");
		LoggerUtils.logBody(body);
		
		try {
			
			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.getUserAndBranchInfo(new JSONObject(body)).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			
		} catch (Exception e) {
			logger.info("error while getting getUserAndBranch Info: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}

    @RolesAllowed(Constants.ADMIN)
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(String body) {
		
		LoggerUtils.logMethodCall("login");
		
		try {
			
			JSONObject bodyObject = new JSONObject(body);
			String ipAddress = request.getRemoteAddr();
			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.performLogin(bodyObject, ipAddress).toString();
			
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			  
		} catch (Exception e) {
			
			logger.info("Error while admin user login: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
	
    @RolesAllowed(Constants.ADMIN)
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
			logger.info("error while getting getPaymentInfo : " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
		
	}
	
    @RolesAllowed(Constants.ADMIN)
	@POST
    @Path("/updatePaymentStatus")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
	public Response updatePaymentStatus(@HeaderParam(Constants.USER_ID) int userId, String body) {

		LoggerUtils.logMethodCall("updatePaymentStatus");

		try {

			JSONObject bodyObject = new JSONObject(body);
			AdminUserManager uManager = new AdminUserManager();
			String responseString = uManager.updatePaymentStatus(userId,bodyObject).toString();
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {
			logger.info("error while getting paymentStatusSuccessOrNot : " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();

		}

	}
    
    @RolesAllowed(Constants.ADMIN)
	@POST
	@Path("/pushNotification")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response pushNotification(
			@HeaderParam(Constants.USER_ID) int userId,
			String body
	) {
		
    	LoggerUtils.logMethodCall("pushNotification");
		LoggerUtils.logBody(body);
		
		try {
			
			JSONObject bodyObject = new JSONObject(body);
			NotificationHelper nHelper = new NotificationHelper(userId);
			
			String responseString = nHelper.pushNotification(bodyObject).toString();
			
			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();
			  
		} catch (Exception e) {
			
			logger.info("Error while pushing notification: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().toJson().toString(), MediaType.APPLICATION_JSON).build();
			
		}
		
	}
    
    @RolesAllowed(Constants.CROWN_USER)
	@POST
	@Path("/scheduleBirthdayNotification")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response scheduleCustomerBirthdayNotification(
			@HeaderParam(Constants.USER_ID) int userId,
			String body		
	) {

		LoggerUtils.logMethodCall("scheduleBirthdayNotification");

		try {

			NotificationHelper nHelper = new NotificationHelper(userId);
			String responseString = nHelper.scheduleBirthdayNotification(new JSONObject(body)).toString();

			return Response.ok(responseString, MediaType.APPLICATION_JSON).build();

		} catch (Exception e) {

			logger.info("error while scheduleBirthdayNotification: " + e.toString());
			e.printStackTrace();
			return Response.ok(new DefaultResponse().setError(e.getMessage()).toJson().toString(), MediaType.APPLICATION_JSON)
					.build();

		}

	}


}