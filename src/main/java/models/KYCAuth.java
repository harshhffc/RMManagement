package models;

import utils.Constants;

public class KYCAuth {
	
	public int id = -1;
	public String entityName = Constants.NA;
	public String authToken = Constants.NA;
	public boolean isValid = false;
	public int tokenCount = -1;
	public String creationDatetime = Constants.NA;
	public String refreshDatetime = Constants.NA;
	
	public KYCAuth() {}

}
