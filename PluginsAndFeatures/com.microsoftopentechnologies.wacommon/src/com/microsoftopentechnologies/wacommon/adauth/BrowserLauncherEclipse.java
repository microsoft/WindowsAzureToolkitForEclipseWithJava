/**
 * Copyright (c) Microsoft Corporation
 * 
 * All rights reserved. 
 * 
 * MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.microsoftopentechnologies.wacommon.adauth;

import java.awt.Dimension;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoftopentechnologies.auth.browser.BrowserLauncher;

public class BrowserLauncherEclipse implements BrowserLauncher {
	Shell parentShell;

	public BrowserLauncherEclipse(Shell parentShell) {
		super();
		this.parentShell = parentShell;
	}

	@Override
	public ListenableFuture<Void> browseAsync(
			String url,
            String redirectUri,
            String callbackUrl,
            String windowTitle,
            boolean noShell) {

		// get the display from Eclipse
		Display display = PlatformUI.getWorkbench().getDisplay();
		Shell shell = (parentShell != null) ?
				new Shell(parentShell, SWT.PRIMARY_MODAL | SWT.CLOSE | SWT.TITLE | SWT.BORDER)
				: new Shell(display, SWT.PRIMARY_MODAL | SWT.CLOSE | SWT.TITLE | SWT.BORDER);
		shell.setText(windowTitle);
        Browser browser;
        ADAuthCodeCallback authCodeCallback = new ADAuthCodeCallback(display, shell, callbackUrl);
        
        shell.setLayout(new FillLayout());
        Monitor monitor = display.getPrimaryMonitor();
        Rectangle bounds = monitor.getBounds();
        Dimension size = new Dimension((int) (bounds.width * 0.40), (int) (bounds.height * 0.70));
        shell.setSize(size.width, size.height);
        shell.setLocation((bounds.width - size.width) / 2, (bounds.height - size.height) / 2);
        shell.setActive();

        try {
            browser = new org.eclipse.swt.browser.Browser(shell, SWT.NONE);
        } catch (SWTError err) {
            authCodeCallback.onFailed(Messages.browserErr + " \n" + err.getMessage());
            return Futures.immediateFuture(null);
        }

        BrowserLocationListener locationListener = new BrowserLocationListener(
                redirectUri, authCodeCallback);
        browser.addLocationListener(locationListener);

        if (noShell) {
            BrowserSilentProgressListener progressListener =
            		new BrowserSilentProgressListener(authCodeCallback);
            browser.addProgressListener(progressListener);
        }

        browser.setUrl(url);

        if (!noShell) {
            shell.open();
        }

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        // notify the caller that the window was closed
        try {
            httpRequest(new URI(callbackUrl).resolve("closed").toURL());
        } catch (IOException e) {
        	return Futures.immediateFailedFuture(e);
        } catch (URISyntaxException e) {
        	return Futures.immediateFailedFuture(e);
        }

        return Futures.immediateFuture(null);
	}
	
	private static void httpRequest(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.getResponseCode();
    }
	
	private static void showError(Shell shell, String msg) {
        MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
        msgBox.setMessage(msg);
        msgBox.setText("Error");
        msgBox.open();
    }
	
	private static class ADAuthCodeCallback implements AuthCodeCallback {
        private final Display display;
        private final String callbackUrl;
		private final Shell shell;

        public ADAuthCodeCallback(Display display, Shell shell, String callbackUrl) {
            this.display = display;
            this.shell = shell;
            this.callbackUrl = callbackUrl;
        }

        @Override
        public void onAuthCodeReceived(String authCode) {
            sendStatus("success", "data=" + authCode);
            shell.close();
        }

        @Override
        public void onFailed(String msg) {
            sendStatus("failed", msg);
            shell.close();
        }

        private void sendStatus(String status, String data) {
            try {
                httpRequest(new URL(callbackUrl + "?" +
                        URLEncoder.encode(String.format("status=%s&%s", status, data), "UTF-8")));
            } catch (MalformedURLException e) {
                // we shouldn't get here; if we do, well, too bad!
                showError(display.getActiveShell(), e.getMessage());
            } catch (IOException e) {
                // if we get here then it probably means that the user closed the IDE or the
                // web server in the IDE died somehow
                showError(display.getActiveShell(), Messages.ideErr);
            }
        }
    }
}
