package com.gigaspaces.azure.model;

import java.util.List;

public class CertificateUploadList {

	List<CertificateUpload> list;

	public CertificateUploadList() {
		super();
	}

	public CertificateUploadList(List<CertificateUpload> list) {
		super();
		this.list = list;
	}

	public List<CertificateUpload> getList() {
		return list;
	}

	public void setList(List<CertificateUpload> list) {
		this.list = list;
	}
}
