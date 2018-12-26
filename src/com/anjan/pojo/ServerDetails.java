package com.anjan.pojo;

public class ServerDetails {
	
	private String hostName;
	private String userName;
	private String password;
	private int portNumber;
	private String fileLocation;
	private String srcPath;
	
	public ServerDetails(String hostName, String userName, String password, int portNumber, String fileLocation, String srcPath) {
		super();
		this.hostName = hostName;
		this.userName = userName;
		this.password = password;
		this.portNumber = portNumber;
		this.fileLocation = fileLocation;
		this.srcPath = srcPath;
	}

	public String getHostName() {
		return hostName;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public String getFileLocation() {
		return fileLocation;
	}
	
	public String getSrcPath(){
		return srcPath;
	}

	@Override
	public String toString() {
		return "Server Details [Host Name=" + hostName + ", User Name=" + userName + ", Password=" + password
				+ ", PortNumber=" + portNumber + ", File Location=" + fileLocation +"]";
	}
	
	
}
