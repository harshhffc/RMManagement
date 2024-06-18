package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "`whitelisted_ip`")
public class WhitelistedIP {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(updatable = false, nullable = false)
	public String id = null;
	
	public String name;
	
	@Column(name = "org_id")
	public String orgId;
	
	@Column(name = "ip_address")
	public String ipAddress;
	
	@Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT false")
	public boolean isActive = false;
	
}
