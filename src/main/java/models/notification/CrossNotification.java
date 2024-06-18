package models.notification;

import java.util.ArrayList;

import utils.DateTimeUtils;

public class CrossNotification {

	public String id;
	public String orgId = null;
	public String origin = null;
	public ArrayList<String> targets = null;
	public String event = null;
	public RmNotification notification = null;
	public boolean isRouted = false;
	public String createDatetime = DateTimeUtils.getCurrentDateTimeInIST();
	public String updateDatetime = DateTimeUtils.getCurrentDateTimeInIST();

}
