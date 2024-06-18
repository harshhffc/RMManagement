
package v2.managers;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Tag;

import models.Creds;
import utils.Constants;
import utils.LoggerUtils;
import utils.ProptertyUtils;
import utils.Constants.CredType;
import v1.repository.CommonRepository;

public class AmazonClient {

	private final AmazonS3 s3client;
	private final Regions clientRegion;
	private final String awsId;
	private final String awsKey;
	private static final String BUCKET_NAME_PROD = "homefirstindia-s3bucket";
	private static final String BUCKET_NAME_TEST = "hffc-teststaging-s3";
	private static Creds _creds = null;

	public static String getBasePath() {
		return "https://" + getBucketName() + ".s3.ap-south-1.amazonaws.com/";
	}

	private static void log(String value) {
		LoggerUtils.log("AmazonClient." + value);
	}

	public AmazonClient() throws Exception {

		var amazonCreds = amazonCreds();

		clientRegion = Regions.AP_SOUTH_1;

		awsId = ProptertyUtils.getKeyBearer().decrypt(amazonCreds.username);
		awsKey = ProptertyUtils.getKeyBearer().decrypt(amazonCreds.password);

		BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsId, awsKey);
		s3client = AmazonS3ClientBuilder.standard().withRegion(clientRegion)
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
	}

	private static Creds amazonCreds() throws Exception {

		if (null == _creds) {
			_creds = new CommonRepository().findCredsByPartnerName(Constants.PARTNER_AMAZON, CredType.PRODUCTION);

			if (null == _creds) {
				log("amazonCreds - failed to get Amazon Creds from DB.");
				throw new Exception("failed to get Amazon Creds from DB.");
			}

		}
		
		return _creds;

	}

	public enum S3BucketPath {

		PROFILE_IMAGES("RMManagement/Profile_picture"), DOCUMENT_IMAGES("RMManagement/Documents"),
		APPLICANT_IMAGES("RMManagement/Applicant_Profile_Picture"), MASK_IMAGES("RMManagement/Documents/Masked"),
		RESOURCE_NOTIFICATION("external/notification"), NOTIFICATION("RMManagement/Notification");

		public final String stringValue;

		S3BucketPath(String stringValue) {
			this.stringValue = stringValue;
		}

		public String fullPath() {
			return getBasePath() + this.stringValue + "/";
		}
	}

	private static String getBucketName() {

		if (Constants.IS_PRODUCTION)
			return BUCKET_NAME_PROD;
		else
			return BUCKET_NAME_TEST;

	}

	public boolean uploadImage(String fileName, String fileData, S3BucketPath bucketPath) throws Exception {

		try {

			MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
			byte[] bytes = Base64.decodeBase64(fileData.getBytes());
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(mimetypesFileTypeMap.getContentType(fileName));
			metadata.setContentLength(bytes.length);
			metadata.addUserMetadata("x-amz-meta-title", fileName);

			List<Tag> tags = new ArrayList<Tag>();
			if (bucketPath == S3BucketPath.DOCUMENT_IMAGES || bucketPath == S3BucketPath.MASK_IMAGES)
				tags.add(new Tag("Classification", "PII"));
			else if (bucketPath == S3BucketPath.PROFILE_IMAGES)
				tags.add(new Tag("Classification", "profile_picture"));
			else if (bucketPath == S3BucketPath.APPLICANT_IMAGES)
				tags.add(new Tag("Classification", "profile_picture"));
			else
				tags.add(new Tag("Classification", "default"));

			PutObjectRequest request = new PutObjectRequest(getBucketName(), bucketPath.stringValue + "/" + fileName,
					byteArrayInputStream, metadata);
			request.setTagging(new ObjectTagging(tags));

			s3client.putObject(request);

			LoggerUtils.log("==> File saved successfully in S3 with Name: " + fileName);

			return true;

		} catch (AmazonServiceException e) {
			// The call was transmitted successfully, but Amazon S3 couldn't process
			// it, so it returned an error response.
			e.printStackTrace();
		} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			e.printStackTrace();
		}

		return false;

	}

	public String getPublicURL(String fileName, S3BucketPath bucketPath, int minutes) {

		String publicUrl = Constants.NA;

		try {

			java.util.Date expiration = new java.util.Date();
			long expTimeMillis = expiration.getTime();
			expTimeMillis += 1000 * 60 * minutes;
			expiration.setTime(expTimeMillis);

			GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(getBucketName(),
					bucketPath.stringValue + "/" + fileName).withMethod(HttpMethod.GET).withExpiration(expiration);
			URL url = s3client.generatePresignedUrl(generatePresignedUrlRequest);

			publicUrl = url.toString();

		} catch (AmazonServiceException e) {
			// The call was transmitted successfully, but Amazon S3 couldn't process
			// it, so it returned an error response.
			e.printStackTrace();
		} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			e.printStackTrace();
		}

		return publicUrl;

	}

	public String getFullUrl(String fileName, S3BucketPath bucketPath) {

		if (null != fileName && !fileName.equalsIgnoreCase(Constants.NA))
			return s3client.getUrl(getBucketName(), bucketPath.stringValue + "/" + fileName).toString();
		else
			return Constants.NA;

	}

}
