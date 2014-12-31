package gov.usgs.cida.microservices.config;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
public class ServiceConfig {

	private static final Logger logger = LoggerFactory.getLogger(ServiceConfig.class);
	private String name; //required
	private int port; //required
	private String id; //optional
	private long ttl; // optional
	private String[] tags; //optional
	// TODO- private HealthCheck 

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	protected void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	protected void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the ttl
	 */
	public long getTtl() {
		return ttl;
	}

	/**
	 * @param ttl the ttl to set
	 */
	protected void setTtl(long ttl) {
		this.ttl = ttl;
	}

	/**
	 * @return the tags
	 */
	public String[] getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	protected void setTags(String[] tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
