package services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import models.KYCDocument;
import utils.BasicUtils;
import utils.Constants;
import utils.LoggerUtils;
import v2.managers.AmazonClient;
import v2.managers.AmazonClient.S3BucketPath;

public class ImageService {

	public static final String UPLOAD_FILE_SERVER = "/var/www/images/document_picture/";
	public static final String UPLOAD_FILE_LOCAL_SERVER = "/Users/sanjay/var/www/images/document_picture/"; // Sanjay
	//public static final String UPLOAD_FILE_LOCAL_SERVER = "/Users/appledeveloper/var/www/images/document_picture/"; // Rabit

	private Calendar calendar;
	private int count = 0;

	public ImageService() {
		calendar = Calendar.getInstance();
	}

	public void uploadMaskedAadhaarImageToS3(KYCDocument kycDocument) {

		calendar.add(Calendar.SECOND, 10);
		Date time = calendar.getTime();

		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {

				if (count < 3) {

					try {

						boolean frontSuccess = false;
						boolean backSuccess = false;

						if (BasicUtils.isNotNullOrNA(kycDocument.maskedFrontImageUrl))
							frontSuccess = uploadMaskedImageToS3(kycDocument.maskedFrontImageUrl,
									kycDocument.frontImageUrl);
						else {
							LoggerUtils.log("Invalid front image URL");
							frontSuccess = true;
						}

						if (BasicUtils.isNotNullOrNA(kycDocument.maskedBackImageUrl))
							backSuccess = uploadMaskedImageToS3(kycDocument.maskedBackImageUrl,
									kycDocument.backImageUrl);
						else {
							LoggerUtils.log("Invalid back image URL");
							backSuccess = true;
						}

						if (frontSuccess && backSuccess) {
							LoggerUtils.log("Successfully uploaded masked front and back image to S3");
							count = 0;
							timer.cancel();
						} else {
							LoggerUtils.log("Masked image upload failed");
							count++;
						}

					} catch (Exception e) {

						LoggerUtils.log("Error while uploading mask image to S3 : " + e.getMessage());
						e.printStackTrace();

						count++;
						LoggerUtils.log("Mask image upload request rescheduled, Iteration: " + count);

					}

				} else {

					LoggerUtils.log("Time's up! Failed to upload masked aadhaar image.");
					timer.cancel();

				}
			}

		}, time, 30000);

	}

	/*
	 * Download the masked image from url given by karza api and convert to base 64
	 * and then upload to AWS
	 */

	private Boolean uploadMaskedImageToS3(String downloadUrl, String fileName) throws Exception {

		String filePath = (Constants.IS_DB_IN_PRODUCTION ? UPLOAD_FILE_SERVER : UPLOAD_FILE_LOCAL_SERVER) + fileName;

		if (downloadImageFromUrl(downloadUrl, filePath)) {

			String base64Image = BasicUtils.getBase64FromFile(filePath);

			new File(filePath).delete();

			if (BasicUtils.isNotNullOrNA(base64Image))
				return new AmazonClient().uploadImage(fileName, base64Image, S3BucketPath.MASK_IMAGES);

		}

		return false;

	}

	private boolean downloadImageFromUrl(String downloadUrl, String filePath) {

		FileOutputStream fileOS = null;
		try {

			BufferedInputStream inputStream = new BufferedInputStream(new URL(downloadUrl).openStream());
			fileOS = new FileOutputStream(filePath);
			byte data[] = new byte[1024];
			int byteContent;
			while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
				fileOS.write(data, 0, byteContent);
			}
			fileOS.flush();
			fileOS.close();

			return true;

		} catch (Exception ioe) {
			LoggerUtils.log("Error while saving MASKED Document file: " + ioe.toString());
			ioe.printStackTrace();
			return false;
		}

	}

}
