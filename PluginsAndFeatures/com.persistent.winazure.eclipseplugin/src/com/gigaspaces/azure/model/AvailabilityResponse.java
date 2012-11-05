package com.gigaspaces.azure.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "AvailabilityResponse")
@XmlType(propOrder = { "result" })
public class AvailabilityResponse {
	
	private boolean result;
	
	@XmlElement(name = "Result")
	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

}
