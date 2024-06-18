package utils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import utils.Constants.ActionType;
import utils.Constants.Errors;

public class OneResponse {
	
	public OneResponse() {}
	
	public Response getAccessDeniedResponse() {
		return Response
				.status(401)
				.entity(
						new LocalResponse()
								.setStatus(false)
								.setMessage("Client authentication failed.")
								.setError(Errors.UNAUTHORIZED_ACCESS.value)
								.setAction(ActionType.AUTHENTICATE_AGAIN.stringValue)
								.toJson()
								.toString()
				)
				.build();
	}
	
	public Response getSuccessResponse(JSONObject successResponse) {
		return Response.ok(
				successResponse.toString(), 
				MediaType.APPLICATION_JSON
		).build();
	}
	
	public Response getFailureResponse(JSONObject failureResponse) {
		return Response
				.status(201)
				.entity(failureResponse.toString())
				.build();
	}
	
	public Response getDefaultFailureResponse() {
		return Response
				.status(500)
				.entity(new LocalResponse().toJson().toString())
				.build();
	}

}
