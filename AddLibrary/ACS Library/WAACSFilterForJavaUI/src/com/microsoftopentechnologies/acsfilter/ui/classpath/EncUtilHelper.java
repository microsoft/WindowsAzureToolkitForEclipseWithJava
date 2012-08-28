package com.microsoftopentechnologies.acsfilter.ui.classpath;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.Platform;
import com.microsoftopentechnologies.acsfilter.ui.activator.Activator;

public class EncUtilHelper {

    private static String encPath = String.format("%s%s%s%s%s", new File(Platform.getInstallLocation().getURL().getFile()).getPath().toString() ,
            File.separator, Messages.pluginFolder , File.separator, Messages.pluginId);
    /**
     * This method will encrypt the password using encutil.
     *
     * @param password :- Password to be encrypted.
     * @param certPath :- location of certificate file.
     * @return encrypted password.
     * @throws Exception
     * @throws IOException
     */
    public static String encryptPassword(String password, String certPath) throws Exception, IOException{
    	
    	// we dont include cmd.exe /C in the command since we are not invoking .bat or .cmd files. when invoking .exe files the cmd.exe is not needed 
    	// and causes problems with file paths.
    	
        String[] commandArgs = {encPath + File.separator + "encutil", "-encrypt" , "-text" , password , "-cert" , certPath };
        String encpwd = encInvocation(commandArgs);
        return encpwd;   	
     }

    /**
     * This method generates thumb print for given certificate.
     *
     * @param cerPath :- location of certificate file.
     * @return thumbprint.
     * @throws Exception
     * @throws IOException
     */
    public static String getThumbPrint(String cerPath) throws Exception, IOException {
    	
    	// we dont include cmd.exe /C in the command since we are not invoking .bat or .cmd files. when invoking .exe files the cmd.exe is not needed 
    	// and causes problems with file paths.

        String thumbprint = "";
        String[] commandArgs = {encPath + File.separator + "encutil", "-thumbprint" , "-cert" ,  cerPath };
        thumbprint = encInvocation(commandArgs);
        return thumbprint;
    }

    /**
     * This method creates a certificate for given password.
     *
     * @param certPath :- location of certificate file.
     * @param pfxPath :- location of pfx file.
     * @param alias :- User alias.
     * @param password :- alias password.
     * @return certificate.
     * @throws Exception
     * @throws IOException
     */
    public static String createCertificate(String certPath, String pfxPath ,String alias, String password) throws Exception, IOException {
    	
    	// we dont include cmd.exe /C in the command since we are not invoking .bat or .cmd files. when invoking .exe files the cmd.exe is not needed 
    	// and causes problems with file paths.
    	
        String newCertificate = "";
        String[] commandArgs = {encPath + File.separator + "encutil", "-create" , "-cert" ,  '"' + certPath + '"' , "-pfx" , pfxPath , "-alias", alias , "-pwd" , password};
        newCertificate = encInvocation(commandArgs);
        return newCertificate;
    }
    

    /**
     * This method is used for invoking the encutil.
     * @param command :- command to invoke enutil.
     * @return result :- depending on the method invocation.
     * @throws Exception
     * @throws IOException
     */
    private static String encInvocation(String[] command) throws
    Exception, IOException {
        String result = "";
        String error = "";
        Runtime runtime = Runtime.getRuntime();
        InputStream inputStream = null;
        InputStream errorStream = null;
        BufferedReader br = null;
        BufferedReader ebr = null;
        try {
            Process process = runtime.exec(command);
            inputStream = process.getInputStream();
            errorStream = process.getErrorStream();
            br = new BufferedReader(new InputStreamReader(inputStream));
            result = br.readLine();
            process.waitFor();
            ebr = new BufferedReader(new InputStreamReader(errorStream));
            error = ebr.readLine();
            if (error != null && (!error.equals(""))) {
                Activator.getDefault().log(error + command, null);
                throw new Exception(error , null);
            }
        } catch (Exception e) {
            Activator.getDefault().log(Messages.encUtilErrMsg, e);
            throw new Exception("Exception occurred while invoking encutil" , e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (errorStream != null) {
                errorStream.close();
            }
            if (br != null) {
                br.close();
            }
            if (ebr != null) {
                ebr.close();
            }
        }
        return result;
    }
}
