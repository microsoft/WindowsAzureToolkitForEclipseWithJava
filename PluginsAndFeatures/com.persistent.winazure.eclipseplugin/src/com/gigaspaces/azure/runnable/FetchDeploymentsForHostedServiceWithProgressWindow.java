package com.gigaspaces.azure.runnable;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.model.HostedService;
import com.gigaspaces.azure.rest.InvalidThumbprintException;
import com.gigaspaces.azure.rest.RestAPIException;
import com.gigaspaces.azure.tasks.AccountCachingExceptionEvent;
import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.util.PublishData;
import com.gigaspaces.azure.wizards.Messages;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;
import com.persistent.util.MessageUtil;

public class FetchDeploymentsForHostedServiceWithProgressWindow extends AccountActionRunnable implements Runnable {

	private String hostedServiceName;
	private HostedService hostedService;
	
	private final static int TASKS = 100;
	
	public FetchDeploymentsForHostedServiceWithProgressWindow(PublishData data, Shell shell) {
		super(data, shell);
	}

	@Override
	public void run() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, this);
			dialog.close();
		} catch (InvocationTargetException e) {
			MessageUtil.displayErrorDialog(shell, com.gigaspaces.azure.wizards.Messages.fetchingDeploymentsTitle, e.getMessage());
			Activator.getDefault().log(Messages.error, e);
		} catch (InterruptedException e) {
		}			
	}

	@Override
	public void doTask() {
		
		try {
			this.hostedService = WizardCacheManager.getHostedServiceWithDeployments(hostedServiceName);
		} 
		catch (RestAPIException e) {
			AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
			event.setException(e);
			event.setMessage(e.getMessage());
			onRestAPIError(event);
			Activator.getDefault().log(Messages.error, e);
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (CommandLineException e) {
			AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
			event.setException(e);
			event.setMessage(e.getMessage());
			onRestAPIError(event);
			Activator.getDefault().log(Messages.error, e);
		} catch (InvalidThumbprintException e) {
			AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
			event.setException(e);
			event.setMessage(e.getMessage());
			onRestAPIError(event);
			Activator.getDefault().log(Messages.error, e);
		} catch(WACommonException e) {
			Activator.getDefault().log(Messages.error, e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		 
		monitor.beginTask("Fetching Deployments For " + hostedServiceName, TASKS);
		
		Thread thread = doAsync();

		while (wait.get() == true) {
			if (monitor.isCanceled()) {
				thread.interrupt();
				throw new InterruptedException();
			}
			Thread.sleep(1000);
			monitor.worked(1);
		}
		if (error.get() == true) {
			monitor.worked(TASKS);
			monitor.done();
			throw new InvocationTargetException(exception, errorMessage);
		}
		monitor.worked(TASKS);
		monitor.done();
		thread.join();
	}


	public String getHostedServiceName() {
		return hostedServiceName;
	}

	public void setHostedServiceName(String hostedServiceName) {
		this.hostedServiceName = hostedServiceName;
	}

	public HostedService getHostedService() {
		return hostedService;
	}

	public void setHostedService(HostedService hostedService) {
		this.hostedService = hostedService;
	}
	
	

}
