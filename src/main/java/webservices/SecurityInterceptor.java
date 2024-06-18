package webservices;

import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import manager.AdminUserManager;
import models.DefaultResponse;
import models.admin.ClientRequest;
import models.admin.PartnerLog;
import services.UserLogger;
import totalkyc.DocumentKYCHelper;
import utils.Constants;
import utils.DateTimeUtils;
import utils.LoggerUtils;
import v2.managers.SecurityManager;
import v2.managers.UserManager;

@Provider
public class SecurityInterceptor implements ContainerRequestFilter {

	@Context
	private ResourceInfo resourceInfo;

	@Context
	private HttpServletRequest request;

	@Override
	public void filter(ContainerRequestContext requestContext) {

		try {

			Method method = resourceInfo.getResourceMethod();
			String methodName = method.getName();
			String className = resourceInfo.getResourceClass().getName();
			
			String ipAddress = request.getHeader("X-FORWARDED-FOR");
			if (null == ipAddress) 
				ipAddress = request.getRemoteAddr();
			
			LoggerUtils.log("Interceptor - Remote IP : " + ipAddress);
			LoggerUtils.log("Interceptor - Calling method : " + methodName + " | Calling class: " + className);

			if (method.isAnnotationPresent(PermitAll.class)) {
				LoggerUtils.log("Interceptor - PermitAll access authorized.");
				return;
			} else if (method.isAnnotationPresent(RolesAllowed.class)) {

				RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
				Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));

				if (rolesSet.contains(Constants.KYC_USER)) {

					LoggerUtils.log("Interceptor - initializing KYC User access check...");

					if (!isKYCUserAllowed(requestContext, methodName)) {
						LoggerUtils.log("Interceptor - KYC User access has been denied.");
						requestContext.abortWith(getAccessDeniedResponse());
					} else {
						LoggerUtils.log("Interceptor - KYC User access authorized.");
					}

				} else if (rolesSet.contains(Constants.ADMIN)) {

					LoggerUtils.log("Interceptor - initializing Admin access check...");

					if (!isAdminUserAllowed(requestContext, methodName)) {
						LoggerUtils.log("Interceptor - Admin access has been denied.");
						requestContext.abortWith(getAccessDeniedResponse());
					} else {
						LoggerUtils.log("Interceptor - Admin access authorized.");
					}

				} else if (rolesSet.contains(Constants.CROWN_USER)) {

					LoggerUtils.log("Interceptor - initializing CROWN User access check...");

					if (!isCrownUserAllowed(requestContext, methodName)) {
						LoggerUtils.log("Interceptor - CROWN access has been denied.");
						requestContext.abortWith(getAccessDeniedResponse());
					} else {
						LoggerUtils.log("Interceptor - CROWN access authorized.");
					}

				} else if (rolesSet.contains(Constants.ADMIN_SERVICES)) {
					
					Response epResponse = authenticatePartnerAdminServices(ipAddress, requestContext, methodName);

					if (epResponse.getStatus() == 200) {
						LoggerUtils.log("Interceptor - Admin partner access authorized.");
					} else {
						LoggerUtils.log("Interceptor - Admin partner access has been denied.");
						requestContext.abortWith(epResponse);
					}

				}


				return;

			} else {

				LoggerUtils.log("Interceptor - initializing Default access check...");

				if (!isUserAllowed(requestContext, methodName)) {
					LoggerUtils.log("Interceptor - Default access has been denied.");
					requestContext.abortWith(getAccessDeniedResponse());
				} else {
					LoggerUtils.log("Interceptor - Default access authorized.");
				}

				return;

			}

		} catch (Exception e) {
			LoggerUtils.log("Error while interception APIs: " + e.getMessage());
			e.printStackTrace();
			requestContext.abortWith(getAccessDeniedResponse());
		}
	}

	private boolean isUserAllowed(final ContainerRequestContext requestContext, final String methodName)
			throws NoSuchAlgorithmException {

		String sessionPasscode = requestContext.getHeaderString(Constants.SESSION_PASSCODE);
		if (null == sessionPasscode)
			sessionPasscode = Constants.NA;

		String sourcePasscode = requestContext.getHeaderString(Constants.SOURCE_PASSCODE);
		if (null == sourcePasscode)
			sourcePasscode = Constants.NA;

		int userId = -1;
		String userSourceId = requestContext.getHeaderString(Constants.USER_SOURCE_ID);
		if (null == userSourceId)
			userSourceId = requestContext.getHeaderString(Constants.USER_ID);
		if (null != userSourceId)
			userId = Integer.parseInt(userSourceId);

		UserManager uManager = new UserManager();

		if (uManager.verifyUser(userId, sessionPasscode)) {
			LoggerUtils.log("Verifying user via ID and sessionPasscode");
			return true;
		} else if (AllowedMethodsWithSourcePasscode.contains(methodName)) {
			LoggerUtils.log("Verifying user via sourcePasscode");
			return uManager.verifySource(sourcePasscode);
		}

		return false;

	}

	private boolean isAdminUserAllowed(final ContainerRequestContext requestContext, final String methodName) {

		String sessionPasscode = requestContext.getHeaderString(Constants.SESSION_PASSCODE);
		if (null == sessionPasscode)
			sessionPasscode = Constants.NA;

		String sourcePasscode = requestContext.getHeaderString(Constants.SOURCE_PASSCODE);
		if (null == sourcePasscode)
			sourcePasscode = Constants.NA;

		int userId = -1;
		String userSourceId = requestContext.getHeaderString(Constants.USER_ID);
		if (null == userSourceId)
			userSourceId = requestContext.getHeaderString(Constants.USER_SOURCE_ID);
		if (null != userSourceId)
			userId = Integer.parseInt(userSourceId);

		AdminUserManager manager = new AdminUserManager();

		if (manager.verifyUser(userId, sessionPasscode)) {
			LoggerUtils.log("Verifying admin via ID and sessionPasscode");
			return true;
		} else if (AllowedAdminMethodsWithSourcePasscode.contains(methodName)) {
			LoggerUtils.log("Verifying admin via sourcePasscode");
			return manager.verifySource(sourcePasscode);
		}

		return false;

	}

	private boolean isKYCUserAllowed(final ContainerRequestContext requestContext, final String methodName) {

		LoggerUtils.log("Verifying KYC User via kyc auth token...");

		int userId = -1;
		String userSourceId = requestContext.getHeaderString(Constants.USER_SOURCE_ID);
		if (null != userSourceId)
			userId = Integer.parseInt(userSourceId);

		String kycAuthToken = requestContext.getHeaderString(Constants.KYC_AUTH_TOKEN);
		if (null == kycAuthToken)
			kycAuthToken = Constants.NA;

		DocumentKYCHelper dkHelper = new DocumentKYCHelper(userId);
		DefaultResponse authInfo = dkHelper.getTokenStatus(kycAuthToken);
		return authInfo.isSuccess;

	}

	private boolean isCrownUserAllowed(final ContainerRequestContext requestContext, final String methodName)
			throws NoSuchAlgorithmException {

		String crownPasscode = requestContext.getHeaderString(Constants.CROWN_PASSCODE);
		if (null == crownPasscode)
			crownPasscode = Constants.NA;

		UserManager uManager = new UserManager();

		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (null == ipAddress) 
			ipAddress = request.getRemoteAddr();		

		if (uManager.verifyMyCrown(crownPasscode, ipAddress)) {
			LoggerUtils.log("Verifying user via crownPasscode");
			return true;
		} else {
			return false;
		}

	}
	
	// <<<<<<<<< PARTNER USER AUTHENTICATION >>>>>>>>> //

			private Response authenticatePartnerAdminServices(String ipAddress, final ContainerRequestContext requestContext,
					final String methodName) throws Exception {
				
				String authorization = requestContext.getHeaderString(Constants.AUTHORIZATION);
				if (null == authorization)
					authorization = Constants.NA;

				String sessionPasscode = requestContext.getHeaderString(Constants.SESSION_PASSCODE);
				if (null == sessionPasscode)
					sessionPasscode = Constants.NA;

				String orgId = requestContext.getHeaderString(Constants.ORG_ID);
				if (null == orgId)
					orgId = Constants.NA;

				ClientRequest cRequest = new ClientRequest();
				cRequest.authorization = authorization;
				cRequest.orgId = orgId;
				cRequest.sessionPasscode = sessionPasscode;
				cRequest.ipAddress = ipAddress;
				cRequest.methodName = methodName;

				SecurityManager sManager = new SecurityManager();

				Response response;

				if (AllowedPartnerMethodsWithDefaultAccess.contains(methodName)) {
					LoggerUtils.log("Verifying Admin partner via Default Access");
					response = sManager.authenticateRequest(cRequest);
				} else {
					LoggerUtils.log("Verifying Admin partner via client creds and sessionPasscode");
					response = sManager.verifyExternalPartnerSession(cRequest);
				}

				PartnerLog pLog = new PartnerLog();
				pLog.ipAddress = ipAddress;
				pLog.datetime = DateTimeUtils.getCurrentDateTimeInIST();
				pLog.orgId = orgId;
				pLog.responseStatus = response.getStatus();
				pLog.serviceName = methodName;

				new UserLogger().logPartnerRequest(pLog);

				return response;

			}
	
			private ArrayList<String> AllowedMethodsWithSourcePasscode = new ArrayList<String>(
					Arrays.asList(
							"addUserInfo",
							"generateOTP",
							"verifyOTP",
							"resendOTP"
							)
				);
	
	private ArrayList<String> AllowedAdminMethodsWithSourcePasscode = new ArrayList<String>(
			Arrays.asList(
					"login"
			)
		);
	
	/*
	 * "getUserAndBranchInfo", "getActiveUsers", "getDashboard", "getLocationInfo",
	 * "getUser"
	 */
	
	private ArrayList<String> AllowedPartnerMethodsWithDefaultAccess = new ArrayList<String>(
			Arrays.asList("authenticateClient"));
	
	private Response getAccessDeniedResponse() {

		DefaultResponse lResponse = new DefaultResponse();
		lResponse.isSuccess = false;
		lResponse.message = "Unauthorized user!";

		return Response.status(401).entity(lResponse.toJson().toString()).build();
	}

}
