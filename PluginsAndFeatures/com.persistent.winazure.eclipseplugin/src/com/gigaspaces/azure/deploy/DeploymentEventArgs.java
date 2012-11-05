/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.gigaspaces.azure.deploy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.Locale;

public class DeploymentEventArgs extends EventObject {

	private static final long serialVersionUID = 1757673237718513593L;

	private String id;
	private String deployMessage;
	private int deployCompleteness;
	private Date startTime;
	private RequestStatus status;

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
		if (deployMessage.equals(com.gigaspaces.azure.wizards.Messages.deploymentCanceled)) {
			return deployMessage;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(Messages.dateFormatEventArgs, Locale.getDefault()); 
		String format = String.format(Messages.toStringFormat, dateFormat.format(getStartTime()), getDeployMessage());
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
	public RequestStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(RequestStatus status) {
		this.status = status;
	}
}
