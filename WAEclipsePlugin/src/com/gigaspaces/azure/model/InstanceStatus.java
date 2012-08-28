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
package com.gigaspaces.azure.model;

public enum InstanceStatus {

	Ready(Messages.ready),

	Busy(Messages.busy),

	Initializing(Messages.init),

	Stopping(Messages.stopping),

	Stopped(Messages.stopped),

	Unresponsive(Messages.unresponsive),

	RoleStateUnknown(Messages.roleStateUnknown),

	CreatingVM(Messages.creatingVm),

	StartingVM(Messages.startingVm),

	CreatingRole(Messages.creatingRole),

	StartingRole(Messages.startingRole),

	ReadyRole(Messages.readyRole),

	BusyRole(Messages.busyRole),

	StoppingRole(Messages.stoppingRole),

	StoppingVM(Messages.stoppingVm),

	DeletingVM(Messages.deletingVm),

	StoppedVM(Messages.stoppedVm),

	RestartingRole(Messages.restartVm),

	CyclingRole(Messages.cyclingVm),

	FailedStartingVM(Messages.failedStartingVm),

	UnresponsiveRole(Messages.unresponsiveRole);

	private String instanceStatus;

	InstanceStatus(String instanceStatus) {
		this.instanceStatus = instanceStatus;
	}
	
	public String getInstanceStatus() {
		return instanceStatus;
	}
}
