package models.admin;

import java.util.ArrayList;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Constants;
import utils.DateTimeUtils;
import utils.DateTimeUtils.DateTimeFormat;

public class KYCStats implements Comparable<KYCStats> {
	
	public String date = Constants.NA;
	public ArrayList<KYCDocStats> kycList = new ArrayList<>();
	
	public KYCStats() {}
	
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("date", date);
		
		JSONArray kycListArray = new JSONArray();
		
		for (KYCDocStats item: kycList) kycListArray.put(item.toJson());		
		json.put("kycList", kycListArray);
		
		return json;
		
	}
	
	@Override
	public int compareTo(KYCStats o) {
		
		try {
			Date lDate = DateTimeUtils.getDateFromString(date, DateTimeFormat.yyyy_MM_dd);
			Date rDate = DateTimeUtils.getDateFromString(o.date, DateTimeFormat.yyyy_MM_dd);
			if (lDate.before(rDate)) return -1;
	        else if(lDate.after(rDate)) return 1;
	        else return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		
	}

}
