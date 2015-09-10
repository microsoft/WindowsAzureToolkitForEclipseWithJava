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


package com.microsoftopentechnologies.windowsazure.tools.cspack;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.microsoftopentechnologies.windowsazure.tools.build.WindowsAzurePackage;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.Base64Converter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    /**
     * Calculate Sha256 hash
     */
    public static String calcHash(File file) throws IOException {
        int bufferSize = 16384;
        String output;
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte [] buffer = new byte[bufferSize];
            int sizeRead = -1;
            while ((sizeRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, sizeRead);
            }
            is.close();

            byte [] hash = null;
            hash = new byte[digest.getDigestLength()];
            hash = digest.digest();
            output = new Base64Converter().encode(hash);
        } catch (FileNotFoundException e) {
            throw new BuildException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new BuildException(e);
        }
        return output;
    }

    public static String calcHexHash(File file) throws IOException {
        int bufferSize = 16384;
        String output;
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte [] buffer = new byte[bufferSize];
            int sizeRead = -1;
            while ((sizeRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, sizeRead);
            }
            is.close();

            byte [] hash = null;
            hash = new byte[digest.getDigestLength()];
            hash = digest.digest();
            output = convertByteArrayToHexString(hash).toUpperCase();
        } catch (FileNotFoundException e) {
            throw new BuildException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new BuildException(e);
        }
        return output;
    }

    /**
     * Zip directory (source can't be a file)
     */
    public static void zipDirectory(File sourceDir, File zipFile, WindowsAzurePackage waPackage) {
        for (File file : sourceDir.listFiles()) {
            waPackage.zipFile(file, zipFile);
            delete(file);
        }
    }

    /**
     * Compresses the given directory and all its sub-directories into a ZIP file.
     * <p>
     * The ZIP file must not be a directory and its parent directory must exist.
     *
     * @param sourceDir
     *          root directory.
     * @param targetZipName
     *          name of ZIP file that will be created or overwritten.
     */
    public static void pack(File sourceDir, String targetZipName, boolean deleteSource) {
        if (!sourceDir.exists()) {
            throw new RuntimeException("Given file '" + sourceDir + "' doesn't exist!");
        }
        ZipOutputStream out = null;
        try {
            String[] filenames = sourceDir.list();
            if (filenames == null) {
                if (!sourceDir.exists()) {
                    throw new RuntimeException("Given file '" + sourceDir + "' doesn't exist!");
                }
                throw new IOException("Given file is not a directory '" + sourceDir + "'");
            }
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(new File(targetZipName))));
            pack(filenames, sourceDir, out, "");
            if (deleteSource) {
                for (String filename : filenames) {
                    delete(new File(sourceDir, filename));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * Compresses the given directory and all its sub-directories into a ZIP file.
     *
     * @param filenames
     *          files and directories names.
     * @param dir
     *          root directory.
     * @param out
     *          ZIP output stream.
     * @param pathPrefix
     *          prefix to be used for the entries.
     */
    private static void pack(String[] filenames, File dir, ZipOutputStream out, String pathPrefix) throws IOException {
        for (int i = 0; i < filenames.length; i++) {
            String filename = filenames[i];
            File file = new File(dir, filename);
            boolean isDir = file.isDirectory();
            String path = pathPrefix + file.getName();
            if (isDir) {
                path += "/";
            }
            // Create a ZIP entry
            ZipEntry zipEntry = new ZipEntry(path);
            if (!isDir) {
                zipEntry.setSize(file.length());
                zipEntry.setTime(file.lastModified());
            }
            out.putNextEntry(zipEntry);
            // Copy the file content
            if (!isDir) {
                copy(file, out);
            }
            out.closeEntry();
            // Traverse the directory
            if (isDir) {
                String[] dirFilenames = file.list();
                if (dirFilenames == null) {
                    if (!dir.exists()) {
                        throw new RuntimeException("Given file '" + dir + "' doesn't exist!");
                    }
                    throw new IOException("Given file is not a directory '" + dir + "'");
                }
                pack(dirFilenames, file, out, path);
            }
        }
    }

    /**
     * Copies the given file into an output stream.
     *
     * @param file input file (must exist).
     * @param out output stream.
     */
    public static void copy(File file, OutputStream out) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            byte[] buffer = new byte[4*1024];
            long count = 0;
            int n = 0;
            InputStream input = new BufferedInputStream(in);
            while (-1 != (n = input.read(buffer))) {
                out.write(buffer, 0, n);
                count += n;
            }
        }
        finally {
            closeQuietly(in);
        }
    }

    /**
     * Unconditionally close an <code>InputStream</code>.
     * @param stream the InputStream to close, may be null or already closed
     */
    public static void closeQuietly(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new RuntimeException("Failed to delete file: " + f);
    }

    public static void generateFileList(Collection<File> fileList, File node) {
        //add file only
        if (node.isFile()) {
            fileList.add(node.getAbsoluteFile());
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(fileList, new File(node, filename));
            }
        }
    }

    public static String generateUID(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());
        return uuid.toString().replaceAll("-", "");
    }

    static void createDirectory(String name) {
        File serviceDefinitionDirectory = new File(name);
        if (!serviceDefinitionDirectory.exists()) {
            serviceDefinitionDirectory.mkdir();
        }
    }

    public static void applyTemplateWithPath(String templateName, Object model, String deployPath) throws IOException {
        applyTemplate(templateName, model, deployPath + File.separator + templateName);
    }

    public static void applyTemplate(String templateName, Object model, String fileName) throws IOException {
//        System.out.println("templateName=" + templateName + "; fileName=" + fileName);
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName));
        try {
            Handlebars handlebars = new Handlebars();
            Template template = handlebars.compile("/templates/" + templateName);

            Context context = Context.newBuilder(model).resolver(JavaBeanValueResolver.INSTANCE).build();
            template.apply(context, fileWriter);
//            System.out.println(template.apply(context));
        } finally {
            fileWriter.flush();
            fileWriter.close();
        }
    }

    public static List<String> getJarEntries(String jarName, String entryName) throws IOException {
        List<String> files = new ArrayList<String>();
        System.out.println("entryName = " + entryName);
        JarURLConnection urlConnection = (JarURLConnection) new URL("jar:file:" + jarName + "!/" + entryName).openConnection();
//        URLConnection urlConnection = originUrl.openConnection();
            JarURLConnection jarConnection = ((JarURLConnection)urlConnection);
            JarFile jarFile = jarConnection.getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(jarConnection.getEntryName())) {
                    if (!entry.isDirectory()) {
                        files.add(entry.getName());
                    }
                }
            }
        return files;
    }

    public static void copyJarEntry(String source, File destination) throws IOException {
        InputStream stream = Utils.class.getResourceAsStream(source);
        if (stream == null) {
            throw new BuildException(source + " not found");
        }
        OutputStream resStreamOut = null;
        int readBytes;
        byte[] buffer = new byte[4096];
        try {
            resStreamOut = new FileOutputStream(destination);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } finally {
            stream.close();
            if (resStreamOut != null) {
                resStreamOut.close();
            }
        }
    }

    public static <T> T parseXmlFile(Class<T> type, String filePath) throws JAXBException {
        T instance = null;
        File file = new File(filePath);
        JAXBContext jaxbContext = JAXBContext.newInstance(type);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        instance = (T) jaxbUnmarshaller.unmarshal(file);
        return instance;
    }
}
