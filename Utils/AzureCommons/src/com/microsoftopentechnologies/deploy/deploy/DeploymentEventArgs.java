/**
* Copyright 2014 Microsoft Open Technologies, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.microsoftopentechnologies.deploy.deploy;

import com.microsoft.windowsazure.core.OperationStatus;
import com.microsoftopentechnologies.messagehandler.PropUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.Locale;

public class DeploymentEventArgs extends EventObject {
	
	String deploymentCanceled = PropUtil.getValueFromFile("deploymentCanceled");
	String dateFormatEventArgs = PropUtil.getValueFromFile("dateFormatEventArgs");
	String toStringFormat = PropUtil.getValueFromFile("toStringFormat");

	private static final long serialVersionUID = 1757673237718513593L;

	private String id;
	private String deployMessage;
	private int deployCompleteness;
	private Date startTime;
	private OperationStatus status;
	private String deploymentURL;

	public String getDeploymentURL() {
		return deploymentURL;
	}

	public void setDeploymentURL(String deploymentURL) {
		this.deploymentURL = deploymentURL;
	}

	public DeploymentEventArgs(Object source) {
		super(source);
	}

	public String getDeployMessage() {
		return deployMessage;
	}

	public void setDeployMessage(String deployMessage) {
		this.deployMessage = deployMessage;
	}

	public int getDeployCompleteness() {
		return deployCompleteness;
	}

	public void setDeployCompleteness(int deployCompleteness) {
		this.deployCompleteness = deployCompleteness;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@Override
	public String toString() {
		if (deployMessage.equals(deploymentCanceled)) {
			return deployMessage;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				dateFormatEventArgs, Locale.getDefault());
		String format = String.format(toStringFormat, dateFormat.format(getStartTime()), getDeployMessage());
		return format;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the status
	 */
	public OperationStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(OperationStatus status) {
		this.status = status;
	}
}
