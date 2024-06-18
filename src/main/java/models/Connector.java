package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import utils.DateTimeUtils;

@Entity
@Table(name = "`Connector`")
public class Connector {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(updatable = false, nullable = false)
	public String id;

	public String orgId;

	@Column(unique = true)
	public String sfConnectorId;

	@Column(unique = true)
	public String sfConnectorCode;

	public String sfAccountId;
	public String sfContactId;

	public String salutation;
	public String firstName;
	public String middleName;
	public String lastName;
	public String fullName;
	public String gender;

	@Column(columnDefinition = "varchar(24) default 'Others'")
	public String religion = "Others";

	public String dateOfBirth;
	public String countryCode = "+91";

	@Column(unique = true)
	public String mobileNumber;

	@ColumnDefault("0")
	public boolean isMobileVerified = false;

	public String emailId;

	@ColumnDefault("0")
	public boolean isEmailVerified = false;

	@ColumnDefault("1")
	public boolean isEnabled = true;

	@ColumnDefault("0")
	public boolean isRegisteredOnApp = false;

	@ColumnDefault("0")
	public boolean isCasMigrated = false;

	public String imageUrl;
	public String category;
	public String subCategory;
	public String type;
	public String stage;
	
	public String source;

	public String panNumber;
	public String aadhaarNumber;

	public String branchSFId;
	public String branchName;
	public String branchDetailId;

	public String ownerSFId;
	public String ownerName;
	public String ownerDetailId;

	public String bankAccountId;
	
	public String gstin;

//	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "currentAddressId", referencedColumnName = "id")
//	public Address currentAddress;
//
//	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "permanentAddressId", referencedColumnName = "id")
//	public Address permanentAddress;

	@Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
	public String createDatetime = DateTimeUtils.getCurrentDateTimeInIST();

	@Column(columnDefinition = "DATETIME")
	public String updateDatetime = DateTimeUtils.getCurrentDateTimeInIST();
	
}
