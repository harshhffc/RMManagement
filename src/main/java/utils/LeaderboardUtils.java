package utils;

import models.LeaderboardFilter;

public class LeaderboardUtils {
	
	public static void applyTimeFilter(StringBuilder sb, String time, String objectName) {
		if (isTimeFilterApplicable(time))
			sb.append(" and " + objectName + ".datetime like ? ");
	}
	
	public static boolean isTimeFilterApplicable(String time) {
		return (!time.equalsIgnoreCase(LeaderboardFilter.ALL) && !time.equalsIgnoreCase(Constants.NA));
	}
	
	public static boolean isRegionFilterApplicable(String region) {
		return (!region.equalsIgnoreCase(LeaderboardFilter.ALL) && !region.equalsIgnoreCase(Constants.NA));
	}
	
	public enum Region {
		AP_T("AP&T"),
		CBE("CBE"),
		CENTRAL_MAHARASHTRA("Central Maharashtra"),
		CG_V("CG&V"),
		CHENNAI("Chennai"),
		KARNATAKA("Karnataka"),
		MD("MD"),
		MP("MP"),
		MUMBAI("Mumbai"),
		NCR("NCR"),
		NORTH_GUJRAT("North Gujarat"),
		RAJASTHAN("Rajasthan"),
		SAURASHTRA("Saurashtra"),
		SM_NK("SM&NK"),
		SOUTH_GUJRAT("South Gujarat"),
		PUNE("Pune"),
		UP("UP");
		
		public final String value;
		Region(String value) {
			this.value = value;
		}
		
		public static Region get(String value) {
			for (Region item: Region.values()) {
				if (item.value.equals(value)) return item;
			}
			return null;
		}
		
	}
	
	public enum RegionMap {
	    
		AP_T("AP&T","Andhra Pradesh & Telangana"),
		CBE("CBE","ROTN-Coimbatore"),
		CENTRAL_MAHARASHTRA("Central Maharashtra","Rest of Maharashtra"), //DOE
		CG_V("CG&V","Chhattisgarh & Vidarbha"),
		CHENNAI("Chennai","Chennai"),
		KARNATAKA("Karnataka", "Karnataka"),
		MD("MD","ROTN-Madurai"),
		MP("MP","Madhya Pradesh"),
		MUMBAI("Mumbai","Mumbai"),
		NCR("NCR","NCR"),
		NORTH_GUJRAT("North Gujarat", "North Gujarat"),
		RAJASTHAN("Rajasthan","Rajasthan"),
		SAURASHTRA("Saurashtra","Saurashtra"),
		SM_NK("SM&NK","South Maharashtra"),
		SOUTH_GUJRAT("South Gujarat","South Gujarat"),
		PUNE("Pune","Pune"),
		UP("UP","UP"); //DNE
		//Pune
		//Rest of Maharashtra
		//South Maharashtra
	    private final String old;
	    private final String newRegion;

	    private RegionMap(String old, String newRegion) {
	        this.old = old;
	        this.newRegion = newRegion;
	    }

	    public String getOldName() {
	        return old;
	    }

	    public String getNewRegionName() {
	        return newRegion;
	    }
	    public static String oldToNew(String old) {
				for (RegionMap item: RegionMap.values()) {
					if (item.newRegion.equals(old)) return item.old;
				}
				return "UnKnownRegion";
			
	    }
	    
	    public static String mapRegionToCluster(String region) {
			for (RegionMap item: RegionMap.values()) {
				if (item.newRegion.equals(region)) return item.old;
			}
			return "Unknown";
		
	    }
	    
	}

}
