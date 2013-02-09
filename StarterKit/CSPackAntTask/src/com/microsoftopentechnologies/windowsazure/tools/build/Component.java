/*
 Copyright 2013 Microsoft Open Technologies, Inc.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.microsoftopentechnologies.windowsazure.tools.build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.tools.ant.BuildException;

/**
 * 
 * A Class representing component element within workerrole element
 * 
 */
public class Component {
	private String importSrc;
	private String importAs;
	private ImportMethod importMethod = ImportMethod.NONE;
	private DeployMethod deployMethod = DeployMethod.NONE;
	private DeployMethod cloudMethod = DeployMethod.NONE;
	private String deployDir;
	private String cloudSrc;
	private String cloudKey;
	private WorkerRole role;
	
	/**
	 * Constructor
	 */
	public Component(WorkerRole role) {
		this.role = role;
	}

	/**
	 * Returns WorkerRole that contains this component
	 * @return
	 */
	public WorkerRole getRole() {
		return role;
	}
	
	/**
	 * Sets/gets the deployment method in the cloud if different from deploymethod
	 * @param cloudMethod
	 */
	public void setCloudMethod(String cloudMethod) {
		if (cloudMethod == null) {
			throw new BuildException("Missing cloud method");
		}

		if(cloudMethod.equalsIgnoreCase("exec")) {
			this.cloudMethod = DeployMethod.EXEC;
		} else if(cloudMethod.equalsIgnoreCase("copy")) {
			this.cloudMethod = DeployMethod.COPY;
		} else if(cloudMethod.equalsIgnoreCase("unzip")) {
			this.cloudMethod = DeployMethod.UNZIP;
		} else if(cloudMethod.equalsIgnoreCase("none")) {
			this.cloudMethod = DeployMethod.NONE;
		} else {
			throw new BuildException("Unsupported cloud method: " + cloudMethod);			
		}
	}
	public DeployMethod getCloudMethod() {
		if(this.cloudMethod == DeployMethod.NONE) {
			return this.deployMethod;
		} else {
			return this.cloudMethod;
		}
	}
	
	/**
	 * Sets/gets the storage account access key to use when deploying in the cloud if the download comes from a private blob
	 * @param key
	 */
	public void setCloudKey(String key) {
		this.cloudKey = key;
	}
	public String getCloudKey() {
		return this.cloudKey;
	}
	
	/**
	 * Sets/gets the URL to download component from when deploying in cloud (not local)
	 * @param url
	 */
	public void setCloudSrc(String url) {
		this.cloudSrc = url;
	}
	public URL getCloudSrc() {
		if(this.cloudSrc == null) {
			return null;
		}
		try {
			URI uri = new URI(this.cloudSrc);
			return uri.toURL();
		} catch (URISyntaxException e) {
			throw new BuildException("Cloud source URL not valid: " + this.cloudSrc);
		} catch (MalformedURLException e) {
			throw new BuildException("Cloud source URL not valid: " + this.cloudSrc);
		}
	}
	
	/**
	 * Sets importas attribute
	 * 
	 * @param importAs
	 */
	public void setImportAs(String importAs) {
		this.importAs = importAs;
	}

	/**
	 * Returns the name of the file or directory that the component will be
	 * imported as, relative to approot
	 * 
	 * @return
	 */
	public String getImportAs() {
		if(this.importAs != null && !this.importAs.isEmpty()) {
			// If import destination has been specified, then return it; treat empty string as null
			return this.importAs;
		} else if(this.importMethod == ImportMethod.NONE) {
			// If no import method and no import destination, then return import source
			return this.importSrc;
		} else if(this.importMethod == ImportMethod.COPY) {
			// If import method is Copy and no import destination, then assume the import source's file name 
			File srcPath = new File(this.importSrc);
			return srcPath.getName();
		} else if(this.importMethod == ImportMethod.ZIP) {
			// If import method is Zip and no import destination, then assume the import souce's file name plus .zip
			File srcPath = new File(this.importSrc);
			return srcPath.getName() + ".zip";
		} else {
			return null;
		}
	}

	/**
	 * Gets the effective file name of the downloaded component
	 * @return
	 */
	public String getCloudDownloadAs() {
		
		URI uri;
		try {
			if(null == (uri = this.getCloudSrc().toURI())) {
				return null;
			}
		} catch (URISyntaxException e) {
			return null;
		}

		String path = uri.getPath().substring(1);
		String[] pathParts = path.split("/");
		return pathParts[pathParts.length-1];
	}
	
	/**
	 * Sets deploymethod attribute
	 * 
	 * @param deployMethod
	 */
	public void setDeployMethod(String deployMethod) {
		if (deployMethod == null) {
			throw new BuildException("Missing deployment method");
		}

		if(deployMethod.equalsIgnoreCase("exec")) {
			this.deployMethod = DeployMethod.EXEC;
		} else if(deployMethod.equalsIgnoreCase("copy")) {
			this.deployMethod = DeployMethod.COPY;
		} else if(deployMethod.equalsIgnoreCase("unzip")) {
			this.deployMethod = DeployMethod.UNZIP;
		} else if(deployMethod.equalsIgnoreCase("none")) {
			this.deployMethod = DeployMethod.NONE;
		} else {
			throw new BuildException("Unsupported deployment method: " + deployMethod);			
		}
	}

	/**
	 * Gets deploymethod setting
	 * 
	 * @return
	 */
	public DeployMethod getDeployMethod() {
		return this.deployMethod;
	}

	/**
	 * Sets importmethod attribute
	 * 
	 * @param importMethod
	 */
	public void setImportMethod(String importMethod) {
		if (importMethod == null) {
			throw new BuildException("Missing import method");
		} else if(importMethod.equalsIgnoreCase("none")) {
			this.importMethod = ImportMethod.NONE;
		} else if(importMethod.equalsIgnoreCase("copy")) {
			this.importMethod = ImportMethod.COPY;
		} else if(importMethod.equalsIgnoreCase("zip")) {
			this.importMethod = ImportMethod.ZIP;
		} else if(importMethod.equalsIgnoreCase("auto")) {
			this.importMethod = ImportMethod.AUTO;
		} else {
			throw new BuildException("Unsupported import method: " + importMethod);
		}
	}

	/**
	 * Gets importmethod setting
	 * 
	 * @return
	 */
	public ImportMethod getImportMethod() {
		return this.importMethod;
	}

	/**
	 * Sets importsrc attribute
	 * 
	 * @param srcpath
	 */
	public void setImportSrc(String srcPath) {
		this.importSrc = srcPath;
	}

	/**
	 * Gets the importsrc attribute
	 * 
	 * @return
	 */
	public String getImportSrc() {
		return this.importSrc;
	}

	/**
	 * Sets deployto attribute
	 * 
	 * @param srcpath
	 */
	public void setDeployDir(String deployPath) {
		if(deployPath.isEmpty())
			deployPath = null;
		this.deployDir = deployPath;
	}

	/**
	 * Gets the deployto attribute
	 * 
	 * @return
	 */
	public String getDeployDir() {
		if(this.deployDir != null && !this.deployDir.isEmpty()) {
			return this.deployDir;
		} else {
			return "."; // Return current directory by default
		}
	}
	
	/** Allows the use of an arbitrary type attribute setting on a component by external tools, but the Ant extension currently 
	 * has no functionality associated with it
	 * 
	 * @param type
	 */
	public void setType(String type) {
		return;
	}

	/**
	 * Returns the container name based on the URL, if this is a private Windows Azure Blob 
	 * @return
	 */
	private String getCloudContainer() {
		URL url;
		URI uri;
		String path;
		String[] pathParts;
		
		try {
			if(null == (url = this.getCloudSrc()) || this.getCloudKey() == null) {
				return null;
			} else if(null == (uri = url.toURI())) {
				return null;
			} else if(null == (path = uri.getPath()) || path.isEmpty()) {
				return null;
			} else if(null == (pathParts = path.substring(1).split("/"))) {
				return null;
			} else if(pathParts.length < 1) {
				return null;
			} else {
				return pathParts[0];
			}
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/**
	 * Returns the blob name based on the URL, if this isa private Windows Azure Blob
	 * @return
	 */
	private String getCloudBlob() {
		URL url;
		URI uri;
		String path, containerName;
		
		try {
			if(null == (url = this.getCloudSrc()) || this.getCloudKey() == null) {
				return null;
			} else if(null == (uri = url.toURI())) {
				return null;
			} else if(null == (path = uri.getPath()) || path.isEmpty()) {
				return null;
			} else if(null == (containerName = getCloudContainer())) {
				return null;
			} else {
				return path.substring(containerName.length()+2);
			}
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/** Returns the name of the storage account based on the URL, if this is a privatre Windows Azure Blob
	 * @return
	 */
	private String getCloudStorage() {
		URL url;
		String hostName;
		String[] hostNameParts;
		
		if(null == (url = this.getCloudSrc()) || this.getCloudKey() == null) {
			return null;
		} else if(null == (hostName = url.getHost())) {
			return null;
		} else if(null == (hostNameParts = hostName.split("\\."))) {
			return null;
		} else if(hostNameParts.length < 1) {
			return null;
		} else {
			return hostNameParts[0];
		}
	}
	
	/**
	 * Verifies whether the download indicated by cloudsrc exists
	 * @return
	 */
	private boolean verifyDownloadPublic() {
		try {
			HttpURLConnection connection = (HttpURLConnection) this.getCloudSrc().openConnection();
			connection.setRequestMethod("HEAD");
			if(200 == connection.getResponseCode()) {
				return true;
			} else {
	    		return false;
			}
			
		} catch (IOException e) {
			return false;
		}
	}
	
	/** Verifies whether the blob pointed to by cloudSrc actually exists
	 * @return
	 */
	private boolean verifyDownloadPrivate() {
		
		// Extract blob info
		String containerName, blobName, storageName;
		
		if(null == (containerName = getCloudContainer())) {
			return false;
		} else if(null == (blobName = getCloudBlob())) {
			return false;			
		} else if(null == (storageName = getCloudStorage())) {
			return false;
		}

		// Create WASH process
		Runtime runtime = Runtime.getRuntime();
		String washCommandline = String.format("%s%s%s%s%s", role.getAppRootDir(), File.separator, WindowsAzurePackage.DEFAULT_UTIL_SUBDIR, File.separator, WindowsAzurePackage.UTIL_WASH_FILENAME);
		Process washProc;
		try {
			washProc = runtime.exec(washCommandline);
		} catch (IOException e) {
			return false;
		}
		
		OutputStream outStream = washProc.getOutputStream();
		InputStream inStream = washProc.getInputStream();
		InputStream errStream = washProc.getErrorStream();
		
		// Process the initial prompt
		if(null == WindowsAzurePackage.expectStreamResponse(inStream, errStream, inStream)) {
			processStreamDestroy(inStream, outStream, errStream, washProc);
			return false;
		}
		
		// Write command
		String command = String.format("blob use \"%s\" \"%s\" \"%s\" \"%s\"\n",
				blobName,
				containerName,
				storageName,
				this.getCloudKey());
		
		try {
			outStream.write(command.getBytes());
			outStream.flush();
		} catch (IOException e) {
			processStreamDestroy(inStream, outStream, errStream, washProc);
			return false;
		}
		
		// Get response
		if(null == WindowsAzurePackage.expectStreamResponse(errStream, errStream, inStream)) {
			processStreamDestroy(inStream, outStream, errStream, washProc);
			return true;
		} else {
			processStreamDestroy(inStream, outStream, errStream, washProc);
			return false;
		}
	}
	
	/** Destroys the process and cleans up the pipeline streams
	 * @param inStream
	 * @param outStream
	 * @param errStream
	 * @param process
	 */
	private void processStreamDestroy(InputStream inStream, OutputStream outStream, InputStream errStream, Process process) {
		try {
			if(inStream != null) {
				inStream.close();
			}
			if(outStream !=null) {
				outStream.close();
			}
			if(errStream !=null) {
				errStream.close();
			}
		} catch(IOException e) {
			;
		}
		
		if(process != null) {
			process.destroy();
		}
	}
	
	/**
	 * Returns the component deployment commandline
	 * @param destFile
	 * @param deployMethod
	 * @param deployPath
	 * @return
	 */
	public String createComponentDeployCommandLine() {
		String importedPath;
		String cmdLineTemplate; 
		String deployPath = getDeployDir();
		DeployMethod method;
		WindowsAzurePackage wapackage = role.getPackage();
		
		// If building for the cloud, let cloudmethod override deploymethod if specified
		if(wapackage.getPackageType() == PackageType.local) {
			method = getDeployMethod();
			importedPath = getImportAs();	
		} else {
			method = getCloudMethod();
			if(getCloudSrc() == null) {
				importedPath = getImportAs();					
			} else {
				importedPath = getCloudDownloadAs();
			}
		} 
		
		File destFile = new File(importedPath);

		switch(method)
		{
			case COPY:
				// Support for deploy method: copy - ensuring non-existent target directories get created as needed
				cmdLineTemplate = "if exist \"$destName\"\\* (echo d | xcopy /y /e \"$destName\" \"$deployPath\\$destName\") else (echo f | xcopy /y \"$destName\" \"$deployPath\\$destName\")";
				return cmdLineTemplate
						.replace("$destName", destFile.getName())
						.replace("$deployPath", deployPath);
			case UNZIP:
				// Support for deploy method: unzip return
				cmdLineTemplate = "cscript /NoLogo $utilSubdir\\$unzipFilename \"$destName\" $deployPath";
				return cmdLineTemplate
						.replace("$utilSubdir", WindowsAzurePackage.DEFAULT_UTIL_SUBDIR)
						.replace("$unzipFilename", WindowsAzurePackage.UTIL_UNZIP_FILENAME)
						.replace("$destName", destFile.getName())
						.replace("$deployPath", deployPath);

			case EXEC:
				// Support for deploy method: exec
				StringBuilder s = new StringBuilder("start \"Windows Azure\" ");
				
				// If deploy dir specified, treat it as a change directory request
				if(deployPath != null) {
					s.append("/D\"");
					s.append(deployPath);
					s.append("\" ");
				}
				s.append(importedPath);
				return s.toString();
			case NONE:
				// Ignore if deploymethod is NONE
				return null;
			default:
				throw new BuildException("Unsupported deployment method");
		}
	}
	
	/**
	 * Returns command line for downloading from public location
	 * @return
	 */
	private String createPublicDownloadCommandLine() {
		if(getCloudSrc() == null) {
			return null;
		} 

		String cmd = String.format("cmd /c %s%s%s file download \"%s\" \"%s\"", 
				WindowsAzurePackage.DEFAULT_UTIL_SUBDIR, 
				File.separator, 
				WindowsAzurePackage.UTIL_WASH_FILENAME, 
				getCloudSrc(),
				this.getCloudDownloadAs());
		return cmd;
	}
	
	/**
	 * Returns command line for downloading from private blob
	 * @return
	 */
	private String createBlobDownloadCommandLine() {
		if(getCloudSrc() == null || getCloudKey() == null) {
			return null;
		}
		
		try {
			// Extract storage account, container, blob and file names
			String storeName = getCloudSrc().getHost().split("\\.")[0];
			URI uri = getCloudSrc().toURI();
			String path = uri.getPath().substring(1);
			String[] pathParts = path.split("/");
			String containerName = pathParts[0];
			String blobName = path.substring(containerName.length()+1);
			String fileName = this.getCloudDownloadAs();
			if(pathParts.length < 2 || containerName.isEmpty() || blobName.isEmpty() || storeName.isEmpty() || fileName.isEmpty()) {
				throw new BuildException("\tNot a valid blob URL: " + getCloudSrc().toExternalForm());
			} 

			return String.format("cmd /c %s%s%s blob download \"%s\" \"%s\" %s %s \"%s\"", 
					WindowsAzurePackage.DEFAULT_UTIL_SUBDIR, 
					File.separator, 
					WindowsAzurePackage.UTIL_WASH_FILENAME, 
					fileName, 
					blobName, 
					containerName, 
					storeName, 
					getCloudKey());
			
		} catch (URISyntaxException e) {
			throw new BuildException("\tNot valid component URL: " + getCloudSrc().toExternalForm());
		}
	}

	/**
	 * Returns component download command line
	 * @return
	 */
	public String createDownloadCommandLine() {
		if(role.getPackage().getPackageType() == PackageType.local || getCloudSrc() == null) {
			// Nothing to download if building for emulator or no cloudsrc URL
			return null;
		
		} else if(getCloudKey() == null) {
			// Download from public location
			return createPublicDownloadCommandLine();

		} else {
			// Download from private blob
			return createBlobDownloadCommandLine();
		}
	}

	/**
	 * Verifies availability of the download
	 * @return
	 */
	public void verifyDownload() {
		WindowsAzurePackage waPackage = role.getPackage();
		URL cloudSrc = getCloudSrc();
		if(!waPackage.getVerifyDownloads() || cloudSrc == null) {
			return;
		} else if(this.getCloudKey() == null) {
			// Verify public download
			waPackage.log("Verifying download availability (" + cloudSrc.toExternalForm() + ")...");
			if(!verifyDownloadPublic()) {
				waPackage.log("warning: Failed to confirm download availability! Make sure the URL is correct (" + cloudSrc.toExternalForm() + ").", 1);							
			}			
		} else {
			// Verify private download
			waPackage.log("Verifying blob availability (" + cloudSrc.toExternalForm() + ")...");			
			if(!verifyDownloadPrivate()) {
				waPackage.log("warning: Failed to confirm blob availability! Make sure the URL and/or the access key is correct (" + cloudSrc.toExternalForm() + ").", 1);
			}
		}
	}
	
	/**
	 * Validates component import settings
	 */
	public void verifyImportSettings() {
		// Validate parameters
		if (getImportSrc() == null && getImportMethod() != ImportMethod.NONE) {
			// Missing import source
			throw new BuildException("Missing import source");
		} else if (getImportAs() == null) {
			// Missing importAs name
			throw new BuildException(String.format("Missing import destination for component '%s'", getImportSrc()));
		} else if (getImportMethod() == null) {
			// Missing import method
			throw new BuildException(String.format("Missing import method for component '%s'", getImportAs()));
		}
	}

	/**
	 * Ensures the component's deployment settings are ok and it is ready to be deployed
	 */
	public void verifyDeploySettings() {
		if (role.getAppRootDir() == null) {
			throw new BuildException("Missing component or approot due to an unknown internal error");
		}

		WindowsAzurePackage wapackage = role.getPackage();
		
		// Determine deploy method depending on cloud vs emulator
		DeployMethod deployMethod;
		if(wapackage.getPackageType() == PackageType.local) {
			deployMethod = getDeployMethod();
		} else { 
			deployMethod = getCloudMethod();
		}
		
		ImportMethod importMethod = getImportMethod();
		File deployFile = new File(role.getAppRootDir(), getImportAs());

		// Ensure default value for deploy method
		if (importMethod == ImportMethod.ZIP && deployMethod == DeployMethod.EXEC) {
			// It doesn't make sense to call exec on a zip
			throw new BuildException(String.format("Deployment method '%s' cannot be used with the import method '%s' for component '%s'", deployMethod.toString().toLowerCase(), importMethod.toString().toLowerCase(), deployFile));

		} else if (deployMethod == null) {
			// Missing deploy method is a problem
			throw new BuildException(String.format("Missing deployment method for component '%s'", deployFile));

		} else if (!deployFile.exists() && deployMethod != DeployMethod.EXEC && (wapackage.getPackageType() == PackageType.local || getCloudSrc() == null)) {
			// Validate that deployment already exists in approot, unless its deployment method is EXEC, in which case skip this check, since it could be an arbitrary commandline
			throw new BuildException(String.format("Cannot find component '%s'", deployFile));

		} else if (getDeployDir() == null && (deployMethod == DeployMethod.COPY || deployMethod ==  DeployMethod.UNZIP)) {
			// Missing deploy directory for COPY or UNZIP (not required for EXEC and NONE)
			throw new BuildException(String.format("Missing deployment directory for component '%s'", getImportAs()));
		}
	}
	
	/**
	 * Verifies component import into the approot
	 * @param component
	 * @param approot
	 */
	public void verifyImportSucceeded() {
		WindowsAzurePackage wapackage = role.getPackage();
		if (role.getAppRootDir() == null) {
			throw new BuildException("Internal failure for unknown reason");
		} else if (getImportAs() == null) {
			wapackage.log(String.format("\tNothing to import for component '%s'", getImportSrc()));
		} else if(getCloudSrc() != null && wapackage.getPackageType() == PackageType.cloud) {
			// Don't verify component when building for the cloud if it has a cloud deployment url specified
			wapackage.log(String.format("\tNot importing component '%s' because it will be downloaded during deployment in the cloud", getImportAs()));
		} else if (getImportMethod() != ImportMethod.NONE) {
			// Confirm that the file actually got imported into the approot, unless import method is NONE
			String fileName = getImportAs();
			
			// Strip out command line parameters if any, but for deploymethod=EXEC only
			if(fileName != null && getDeployMethod() == DeployMethod.EXEC) {
				fileName = fileName.split(" ")[0]; 
			}
			
			File destFile = new File(role.getAppRootDir(), fileName);
			if (destFile.exists()) {
				wapackage.log(String.format("\tImported as '%s' from \"%s\"", fileName, getImportSrc()));
			} else {
				throw new BuildException(String.format("Failed importing component '%s' as '%s' into 'approot\\%s'", getImportSrc(), getImportMethod(), fileName));
			}
		}
	}
	
	/**
	 * Imports a component into the role's approot
	 * @param component
	 * @param approotDir
	 */
	public void doImport() {
		File approotDir = role.getAppRootDir();
		WindowsAzurePackage wapackage = role.getPackage();
		
		// Ignore no import method
		ImportMethod importMethod = getImportMethod();
		if(importMethod == ImportMethod.NONE) {
			return;
		}

		String fileName = getImportAs();

        // Strip out command line parameters if any, but only for deploymethod=EXEC
		if(getDeployMethod() == DeployMethod.EXEC && fileName != null) {
			fileName = fileName.split(" ")[0];
		}
        File destFile = new File(approotDir, fileName);
        
		// When building for the cloud if cloud source is specified, delete the component if it exists and import method isn't none; and don't import
		if(getCloudSrc() != null && wapackage.getPackageType() == PackageType.cloud && getImportMethod() != ImportMethod.NONE) {
			if(destFile.exists()) {
				WindowsAzurePackage.deleteDirectory(destFile);
			}
			return;
		}
		
		File srcFile = new File(getImportSrc());

		// If relative path, make it relative to approot
		if (!srcFile.isAbsolute()) {
			srcFile = new File(approotDir, srcFile.getPath());
		}

		if (!srcFile.exists())
			throw new BuildException(String.format("Failed to find component \"%s\"", srcFile.getPath()));

		if (importMethod == ImportMethod.COPY) {
			// Component import method: copy
			wapackage.copyFile(srcFile, destFile);

		} else if (importMethod == ImportMethod.ZIP) {
			// Component import method: zip
			wapackage.zipFile(srcFile, destFile);
		}
	}
}