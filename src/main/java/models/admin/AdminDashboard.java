package models.admin;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class AdminDashboard {
	
	public LeadStat createdLead = new LeadStat();
	public LeadStat convertedLead = new LeadStat();
	public ArrayList<KYCStats> kycDocuments = new ArrayList<>();
	public ArrayList<KYCStats> utilityBills = new ArrayList<>();
	public ArrayList<KYCStats> incomeDocuments = new ArrayList<>();
	public ArrayList<KYCStats> otherDocuments = new ArrayList<>();
	public ArrayList<KYCStats> paymentList = new ArrayList<>();
	public double totalPaymentAmount = 0;
	
	public AdminDashboard() {}
	
	public JSONObject toJson() {
		
		JSONObject json =  new JSONObject();
		
		json.put("createdLead", createdLead.toJson());
		json.put("convertedLead", convertedLead.toJson());
		
		JSONArray kycDocumentArray = new JSONArray();
		for (KYCStats item: kycDocuments) kycDocumentArray.put(item.toJson());
		json.put("kycDocuments", kycDocumentArray);
		
		JSONArray utilityBillsArray = new JSONArray();
		for (KYCStats item: utilityBills) utilityBillsArray.put(item.toJson());
		json.put("utilityBills", utilityBillsArray);
		
		JSONArray incomeDocumentsArray = new JSONArray();
		for (KYCStats item: incomeDocuments) incomeDocumentsArray.put(item.toJson());
		json.put("incomeDocuments", incomeDocumentsArray);
		
		JSONArray otherDocumentsArray = new JSONArray();
		for (KYCStats item: otherDocuments) otherDocumentsArray.put(item.toJson());
		json.put("otherDocuments", otherDocumentsArray);
		
		JSONArray paymentsArray = new JSONArray();
		for (KYCStats item: paymentList) paymentsArray.put(item.toJson());
		json.put("paymentList", paymentsArray);
		
		json.put("totalPaymentAmount", totalPaymentAmount);
		
		return json;
		
	}

}
