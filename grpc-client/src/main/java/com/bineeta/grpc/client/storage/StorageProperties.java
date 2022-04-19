package com.bineeta.grpc.client.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	/**
	 * Folder location for storing files
	 */
	// local windows env
	private String location = "D:\\localdata\\outputdata";
	// cloud linux env
	//private String location = "//localdata//outputdata";

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
