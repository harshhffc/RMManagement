package models;

import org.json.JSONObject;

import v2.managers.AmazonClient;
import v2.managers.AmazonClient.S3BucketPath;

public class LeaderBoardItem implements Comparable<LeaderBoardItem> {

	public User rmUser = new User();
	public int kycDocumentCount = 0;
	public int utilityBillCount = 0;
	public int vehicleRCCount = 0;
	public int epfCount = 0;
	public int gstinCount = 0;
	public int itrvCount = 0;
	public int paymentCount = 0;
	public int createdApCount = 0;
	public int convertedApCount = 0;
	public int coApCount = 0;
	public int bankInfoFetchedCount = 0;
	public int visitCompletedCount = 0;
	public int total = 0;
	public int points = 0;
	public double rating = 0.0;
	public int rank = 0;

	public LeaderBoardItem() {
	}

	public JSONObject toJson() {

		JSONObject json = new JSONObject();

		AmazonClient amzClient;
		try {
			amzClient = new AmazonClient();

			JSONObject userObject = new JSONObject();
			userObject.put("id", rmUser.id);
			userObject.put("displayName", rmUser.displayName);
			userObject.put("sfUserId", rmUser.sfUserId);
			userObject.put("emailId", rmUser.email);
			userObject.put("profileImageUrl", rmUser.profileImageUrl);
			userObject.put("fullProfileImageUrl", amzClient.getFullUrl(rmUser.profileImageUrl, S3BucketPath.PROFILE_IMAGES));

			json.put("rmUser", userObject);
			json.put("kycDocumentCount", kycDocumentCount);
			json.put("utilityBillCount", utilityBillCount);
			json.put("vehicleRCCount", vehicleRCCount);
			json.put("epfCount", epfCount);
			json.put("gstinCount", gstinCount);
			json.put("itrvCount", itrvCount);
			json.put("paymentCount", paymentCount);
			json.put("createdApCount", createdApCount);
			json.put("convertedApCount", convertedApCount);
			json.put("coApCount", coApCount);
			json.put("bankInfoFetchedCount", bankInfoFetchedCount);
			json.put("visitCompletedCount", visitCompletedCount);

			json.put("total", total);
			json.put("points", points);
			json.put("rating", rating);
			json.put("rank", rank);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

	public int calculateDefaultPoints() {

		points = kycDocumentCount + getPointsFromCount(utilityBillCount, 2) + getPointsFromCount(vehicleRCCount, 2)
				+ getPointsFromCount(epfCount, 2) + getPointsFromCount(gstinCount, 2) + getPointsFromCount(itrvCount, 2)
				+ getPointsFromCount(paymentCount, 1) + getPointsFromCount(createdApCount, 1)
				+ getPointsFromCount(convertedApCount, 2) + getPointsFromCount(coApCount, 1)
				+ getPointsFromCount(bankInfoFetchedCount, 3) + getPointsFromCount(visitCompletedCount, 1);

		return points;
	}

	public int calculatePointsWithWeightage(ScoreWeightage weightage) {

		points = getPointsFromCount(kycDocumentCount, weightage.kycDocument)  
				+ getPointsFromCount(utilityBillCount, weightage.utilityBill)
				+ getPointsFromCount(vehicleRCCount, weightage.vehicleRC) 
				+ getPointsFromCount(epfCount, weightage.epf)
				+ getPointsFromCount(gstinCount, weightage.gstin) 
				+ getPointsFromCount(itrvCount, weightage.itr)
				+ getPointsFromCount(paymentCount, weightage.payment)
				+ getPointsFromCount(createdApCount, weightage.leadCreated)
				+ getPointsFromCount(convertedApCount, weightage.leadConverted)
				+ getPointsFromCount(coApCount, weightage.leadCreated)
				+ getPointsFromCount(bankInfoFetchedCount, weightage.bankStatement)
				+ getPointsFromCount(visitCompletedCount, weightage.visitCompleted);

		return points;
	}

	public double calculateRating(double max) {

		double current = (double) points;
		double rating = ((current / max) * 100) / 20;
		this.rating = rating <= 5 ? rating : 5;

		return rating;

	}

	private int getPointsFromCount(int dataCount, int pointMultiplier) {
		return dataCount * pointMultiplier;
	}

	public void addValues(LeaderBoardItem leader) {
		rmUser = leader.rmUser;
		kycDocumentCount += leader.kycDocumentCount;
		utilityBillCount += leader.utilityBillCount;
		vehicleRCCount += leader.vehicleRCCount;
		epfCount += leader.epfCount;
		gstinCount += leader.gstinCount;
		itrvCount += leader.itrvCount;
		paymentCount += leader.paymentCount;
		createdApCount += leader.createdApCount;
		convertedApCount += leader.convertedApCount;
		coApCount += leader.coApCount;
		bankInfoFetchedCount += leader.bankInfoFetchedCount;
		visitCompletedCount += leader.visitCompletedCount;
		total += leader.total;
		points += leader.points;
	}

	@Override
	public int compareTo(LeaderBoardItem o) {

		if (points < o.points)
			return 1;
		else if (points > o.points)
			return -1;
		else
			return 0;

	}
	
	

}
