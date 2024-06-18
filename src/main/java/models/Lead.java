package models;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import utils.BasicUtils;
import utils.DateTimeUtils;

@Entity
@Table(name = "`Lead`")
public class Lead {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(updatable = false, nullable = false)
	public String id = null;
	public String orgId = null;

	public String sfOwnerId = null;
	public String sfBranchId = null;
	public String sfCreatedById = null;
	public String sfLeadSource = null;
	public String lsLeadId = null;
	public String partnerReferenceId = null;
	public String salutation = null;
	public String firstName = null;
	public String middleName = null;
	public String lastName = null;
	public String countryCode = "+91";
	public String mobileNumber = null;
	public String emailId = null;
	public String status = null;
	public String subStatus = null;
	public String leadState = null;

	@ColumnDefault("0")
	public boolean isConvertedOnHFO = false;

	public String hfoLeadId = null;
	public String hfoAccountId = null;
	public String hfoContactId = null;
	public String hfoOpportunityId = null;
	public String hfoApplicantId = null;
	public String hfoLoanId = null;

	@ColumnDefault("0")
	public boolean isConverted = false;

	@ColumnDefault("0")
	public boolean hasConsented = false;

	public String sfLeadId = null;
	public String sfAccountId = null;
	public String sfContactId = null;
	public String sfOpportunityId = null;
	public String sfConnectorId = null;

	public String loanType = null;

	@ColumnDefault("-1")
	public double loanAmount = -1;

	@ColumnDefault("-1")
	public double propertyValue = -1;

	@ColumnDefault("-1")
	public double monthlyIncome = -1;

	@ColumnDefault("-1")
	public double monthlyIncomeInCash = -1;

	public String documentType = null;
	public String documentId = null;

	public String comment = null;
	public String remark = null;
	public String source = null;
	public String medium = null;
	public String campaign = null;

	@ColumnDefault("0")
	public boolean NHBNegativeFlagged = false;

	@Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
	public String createDatetime = DateTimeUtils.getCurrentDateTimeInIST();

	@Column(columnDefinition = "DATETIME")
	public String updateDatetime = DateTimeUtils.getCurrentDateTimeInIST();

	@Column(columnDefinition = "DATETIME")
	public String modifiedDatetime;

	@Column(columnDefinition = "DATETIME")
	public String appointmentDatetime;

	public String street = null;
	public String city = null;
	public String state = null;
	public String country = "India";
	public String postalCode = null;
	public String dob;
	public String gender;
	public String officialEmail;
	public String employmentType = null;
	public String salariedType = null;

	public String companyName = null; // Max : 128 Chars
	public String alternateContactNumber = null;

	@ColumnDefault("0")
	public boolean propertyIdentified = false;

	@ColumnDefault("0")
	public boolean mobileVerified = false;

	@ColumnDefault("0")
	public boolean itrAvailable = false;

	@ColumnDefault("-1")
	public int tenure = -1;

	public String getFullName() {

		String fullName = firstName;
		if (BasicUtils.isNotNullOrNA(middleName))
			fullName += " " + middleName;
		fullName += " " + lastName;

		return fullName;

	}

	public void addSFInfo(Lead sfLead) {

		sfLeadId = sfLead.sfLeadId;
		modifiedDatetime = sfLead.modifiedDatetime;
		emailId = sfLead.emailId;
		status = sfLead.status;
		subStatus = sfLead.subStatus;
		sfBranchId = sfLead.sfBranchId;
		sfOwnerId = sfLead.sfOwnerId;
		remark = sfLead.remark;
		isConverted = sfLead.isConverted;

	}

}