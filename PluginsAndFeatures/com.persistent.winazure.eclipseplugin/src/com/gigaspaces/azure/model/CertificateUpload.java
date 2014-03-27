package com.gigaspaces.azure.model;

public class CertificateUpload {
	String name;
	String thumbprint;
	String pfxPath;
	String pfxPwd;

	public CertificateUpload() {
		super();
	}

	public CertificateUpload(
			String name,
			String thumbprint,
			String pfxPath,
			String pfxPwd) {
		super();
		this.name = name;
		this.thumbprint = thumbprint;
		this.pfxPath = pfxPath;
		this.pfxPwd = pfxPwd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getThumbprint() {
		return thumbprint;
	}

	public void setThumbprint(String thumbprint) {
		this.thumbprint = thumbprint;
	}

	public String getPfxPath() {
		return pfxPath;
	}

	public void setPfxPath(String pfxPath) {
		this.pfxPath = pfxPath;
	}

	public String getPfxPwd() {
		return pfxPwd;
	}

	public void setPfxPwd(String pfxPwd) {
		this.pfxPwd = pfxPwd;
	}
}
