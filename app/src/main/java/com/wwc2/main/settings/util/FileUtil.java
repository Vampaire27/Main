package com.wwc2.main.settings.util;

/**
 * Created by swd1 on 17-7-26.
 */

import android.os.StatFs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author IBM
 */

public class FileUtil {


    /**
     * save file accroding to physical directory infomation
     *
     * @param physicalDir physical directory
     * @param fname       file name of destination
     * @param istream     input stream of destination file
     * @return
     */

    public static boolean SaveFileByPhysicalDir(String physicalPath,
                                                InputStream istream) {

        boolean flag = false;
        try {
            OutputStream os = new FileOutputStream(physicalPath);
            int readBytes = 0;
            byte buffer[] = new byte[8192];
            while ((readBytes = istream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, readBytes);
            }
            os.close();
            flag = true;
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return flag;
    }

    public static boolean CreateDirectory(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        return true;
    }


    public static void saveAsFileOutputStream(String physicalPath, String content) {
        File file = new File(physicalPath);
        boolean b = file.getParentFile().isDirectory();
        if (!b) {
            File tem = new File(file.getParent());
            // tem.getParentFile().setWritable(true);
            tem.mkdirs();// 创建目录
        }
        //Log.info(file.getParent()+";"+file.getParentFile().isDirectory());
        FileOutputStream foutput = null;
        try {
            foutput = new FileOutputStream(physicalPath);

            foutput.write(content.getBytes("UTF-8"));
            //foutput.write(content.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } finally {
            try {
                foutput.flush();
                foutput.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        //Log.info("文件保存成功:"+ physicalPath);
    }


    /**
     * COPY文件
     *
     * @param srcFile String
     * @param desFile String
     * @return boolean
     */
    public boolean copyToFile(String srcFile, String desFile) {
        File scrfile = new File(srcFile);
        if (scrfile.isFile() == true) {
            int length;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(scrfile);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            File desfile = new File(desFile);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(desfile, false);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            desfile = null;
            length = (int) scrfile.length();
            byte[] b = new byte[length];
            try {
                fis.read(b);
                fis.close();
                fos.write(b);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            scrfile = null;
            return false;
        }
        scrfile = null;
        return true;
    }

    /**
     * COPY文件夹
     *
     * @param sourceDir String
     * @param destDir   String
     * @return boolean
     */
    public boolean copyDir(String sourceDir, String destDir) {
        File sourceFile = new File(sourceDir);
        String tempSource;
        String tempDest;
        String fileName;
        File[] files = sourceFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            fileName = files[i].getName();
            tempSource = sourceDir + "/" + fileName;
            tempDest = destDir + "/" + fileName;
            if (files[i].isFile()) {
                copyToFile(tempSource, tempDest);
            } else {
                copyDir(tempSource, tempDest);
            }
        }
        sourceFile = null;
        return true;
    }

    /**
     * 删除指定目录及其中的所有内容。
     *
     * @param dir 要删除的目录
     * @return 删除成功时返回true，否则返回false。
     */
    public boolean deleteDirectory(File dir) {
        File[] entries = dir.listFiles();
        int sz = entries.length;
        for (int i = 0; i < sz; i++) {
            if (entries[i].isDirectory()) {
                if (!deleteDirectory(entries[i])) {
                    return false;
                }
            } else {
                if (!entries[i].delete()) {
                    return false;
                }
            }
        }
        if (!dir.delete()) {
            return false;
        }
        return true;
    }


    /**
     * File exist check
     *
     * @param sFileName File Name
     * @return boolean true - exist<br>
     * false - not exist
     */
    public static boolean checkExist(String sFileName) {

        boolean result = false;

        try {
            File f = new File(sFileName);

            //if (f.exists() && f.isFile() && f.canRead()) {
            if (f.exists() && f.isFile()) {
                result = true;
            } else {
                result = false;
            }
        } catch (Exception e) {
            result = false;
        }

        /* return */
        return result;
    }

    /**
     * Get File Size
     *
     * @param sFileName File Name
     * @return long File's size(byte) when File not exist return -1
     */
    public static long getSize(String sFileName) {

        long lSize = 0;

        try {
            File f = new File(sFileName);

            //exist
            if (f.exists()) {
                if (f.isFile() && f.canRead()) {
                    lSize = f.length();
                } else {
                    lSize = -1;
                }
                //not exist
            } else {
                lSize = 0;
            }
        } catch (Exception e) {
            lSize = -1;
        }
       /* return */
        return lSize;
    }

    /**
     * File Delete
     *
     * @param sFileName File Nmae
     * @return boolean true - Delete Success<br>
     * false - Delete Fail
     */
    public static boolean deleteFromName(String sFileName) {

        boolean bReturn = true;

        try {
            File oFile = new File(sFileName);

            //exist
            if (oFile.exists()) {
                //Delete File
                boolean bResult = oFile.delete();
                //Delete Fail
                if (bResult == false) {
                    bReturn = false;
                }

                //not exist
            } else {

            }

        } catch (Exception e) {
            bReturn = false;
        }

        //return
        return bReturn;
    }

    /**
     * File Unzip
     *
     * @param sToPath  Unzip Directory path
     * @param sZipFile Unzip File Name
     */
    @SuppressWarnings("rawtypes")
    public static void releaseZip(String sToPath, String sZipFile) throws Exception {

        if (null == sToPath || ("").equals(sToPath.trim())) {
            File objZipFile = new File(sZipFile);
            sToPath = objZipFile.getParent();
        }
        ZipFile zfile = new ZipFile(sZipFile);
        Enumeration zList = zfile.entries();
        ZipEntry ze;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {

            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                continue;
            }

            OutputStream os =
                    new BufferedOutputStream(
                            new FileOutputStream(getRealFileName(sToPath, ze.getName())));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
    }

    /**
     * getRealFileName
     *
     * @param baseDir     Root Directory
     * @param absFileName absolute Directory File Name
     * @return java.io.File     Return file
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static File getRealFileName(String baseDir, String absFileName) throws Exception {

        File ret = null;

        List dirs = new ArrayList();
        StringTokenizer st = new StringTokenizer(absFileName, System.getProperty("file.separator"));
        while (st.hasMoreTokens()) {
            dirs.add(st.nextToken());
        }

        ret = new File(baseDir);
        if (dirs.size() > 1) {
            for (int i = 0; i < dirs.size() - 1; i++) {
                ret = new File(ret, (String) dirs.get(i));
            }
        }
        if (!ret.exists()) {
            ret.mkdirs();
        }
        ret = new File(ret, (String) dirs.get(dirs.size() - 1));
        return ret;
    }

    /**
     * copyFile
     *
     * @param srcFile    Source File
     * @param targetFile Target file
     */
    @SuppressWarnings("resource")
    static public void copyFile(String srcFile, String targetFile) throws IOException {

        FileInputStream reader = new FileInputStream(srcFile);
        FileOutputStream writer = new FileOutputStream(targetFile);

        byte[] buffer = new byte[1024];
        int len;

        try {
            reader = new FileInputStream(srcFile);
            writer = new FileOutputStream(targetFile);

            while ((len = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
        }
    }

    /**
     * renameFile
     *
     * @param srcFile    Source File
     * @param targetFile Target file
     */
    static public void renameFile(String srcFile, String targetFile) throws IOException {
        try {
            copyFile(srcFile, targetFile);
            deleteFromName(srcFile);
        } catch (IOException e) {
            throw e;
        }
    }


    public static void writeAppend(String tivoliMsg, String logFileName) {
        try {
            byte[] bMsg = tivoliMsg.getBytes();
            FileOutputStream fOut = new FileOutputStream(logFileName, true);
            fOut.write(bMsg);
            fOut.flush();
            fOut.getFD().sync();
            fOut.close();
        } catch (IOException e) {
            //throw the exception
        }

    }

    public static void write(String tivoliMsg, String logFileName) {
        try {
            byte[] bMsg = tivoliMsg.getBytes();
            FileOutputStream fOut = new FileOutputStream(logFileName);
            fOut.write(bMsg);
            fOut.flush();
            fOut.getFD().sync();
            fOut.close();
        } catch (IOException e) {
            //throw the exception
        }

    }

    public static void write(byte[] bMsg, String logFileName) {
        try {
            FileOutputStream fOut = new FileOutputStream(logFileName);
            fOut.write(bMsg);
            fOut.getFD().sync();
            fOut.close();
        } catch (IOException e) {
            //throw the exception
        }

    }

    /**
     * This method is used to log the messages with timestamp,error code and the method details
     *
     * @param errorCd    String
     * @param className  String
     * @param methodName String
     * @param msg        String
     */
    public static void writeLog(String logFile, String batchId, String exceptionInfo) {

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.JAPANESE);

        Object args[] = {df.format(new Date()), batchId, exceptionInfo};

        String fmtMsg = MessageFormat.format("{0} : {1} : {2}", args);

        try {

            File logfile = new File(logFile);
            if (!logfile.exists()) {
                logfile.createNewFile();
            }

            FileWriter fw = new FileWriter(logFile, true);
            fw.write(fmtMsg);
            fw.write(System.getProperty("line.separator"));

            fw.flush();
            fw.close();

        } catch (Exception e) {
        }
    }

    public static String readTextFile(String realPath) throws Exception {
        File file = new File(realPath);
        if (!file.exists()) {
            System.out.println("File not exist!");
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(realPath), "UTF-8"));
        String temp;
        String txt = "";
        while ((temp = br.readLine()) != null) {
            txt += temp;
        }
        br.close();
        return txt;
    }

    public static long getAvailableSize(String path)
    {
        StatFs fileStats = new StatFs(path);
        fileStats.restat(path);
        return (long) fileStats.getAvailableBlocks() * fileStats.getBlockSize();
    }
//zhongyang.hu add for copy DIR
    /**
     * Internal copy file method.
     *
     * @param srcFile  the validated source file, must not be <code>null</code>
     * @param destFile  the validated destination file, must not be <code>null</code>
     * @param preserveFileDate  whether to preserve the file date
     * @throws IOException if an error occurs
     */
    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream input = new FileInputStream(srcFile);
        try {
            FileOutputStream output = new FileOutputStream(destFile);
            try {
                IOUtils.copy(input, output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }

        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" +
                    srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }

    /**
     * Internal copy directory method.
     *
     * @param srcDir  the validated source directory, must not be <code>null</code>
     * @param destDir  the validated destination directory, must not be <code>null</code>
     * @param filter  the filter to apply, null means copy all directories and files
     * @param preserveFileDate  whether to preserve the file date
     * @param exclusionList  List of files and directories to exclude from the copy, may be null
     * @throws IOException if an error occurs
     * @since Commons IO 1.1
     */
    private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter,
                                        boolean preserveFileDate, List<String> exclusionList) throws IOException {
        if (destDir.exists()) {
            if (destDir.isDirectory() == false) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (destDir.mkdirs() == false) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
            if (preserveFileDate) {
                destDir.setLastModified(srcDir.lastModified());
            }
        }
        if (destDir.canWrite() == false) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        // recurse
        File[] files = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir);
        }
        for (int i = 0; i < files.length; i++) {
            File copiedFile = new File(destDir, files[i].getName());
            if (exclusionList == null || !exclusionList.contains(files[i].getCanonicalPath())) {
                if (files[i].isDirectory()) {
                    doCopyDirectory(files[i], copiedFile, filter, preserveFileDate, exclusionList);
                } else {
                    doCopyFile(files[i], copiedFile, preserveFileDate);
                }
            }
        }
    }


    /**
     * Copies a filtered directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.
     * <p>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     *
     * <h4>Example: Copy directories only</h4>
     *  <pre>
     *  // only copy the directory structure
     *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
     *  </pre>
     *
     * <h4>Example: Copy directories and txt files</h4>
     *  <pre>
     *  // Create a filter for ".txt" files
     *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *  // Create a filter for either directories or ".txt" files
     *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     *  // Copy using the filter
     *  FileUtils.copyDirectory(srcDir, destDir, filter, false);
     *  </pre>
     *
     * @param srcDir  an existing directory to copy, must not be <code>null</code>
     * @param destDir  the new directory, must not be <code>null</code>
     * @param filter  the filter to apply, null means copy all directories and files
     * @param preserveFileDate  true if the file date of the copy
     *  should be the same as the original
     *
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.4
     */
    public static void copyDirectory(File srcDir, File destDir,
                                     FileFilter filter, boolean preserveFileDate) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (srcDir.exists() == false) {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        }
        if (srcDir.isDirectory() == false) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<String>(srcFiles.length);
                for (int i = 0; i < srcFiles.length; i++) {
                    File copiedFile = new File(destDir, srcFiles[i].getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
    }



    /**
     * Copies a whole directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.
     * <p>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     *
     * @param srcDir  an existing directory to copy, must not be <code>null</code>
     * @param destDir  the new directory, must not be <code>null</code>
     * @param preserveFileDate  true if the file date of the copy
     *  should be the same as the original
     *
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.1
     */
    public static void copyDirectory(File srcDir, File destDir,
                                     boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, null, preserveFileDate);
    }

    /**
     * Copies a whole directory to a new location preserving the file dates.
     * <p>
     * This method copies the specified directory and all its child
     * directories and files to the specified destination.
     * The destination is the new location and name of the directory.
     * <p>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     *
     * @param srcDir  an existing directory to copy, must not be <code>null</code>
     * @param destDir  the new directory, must not be <code>null</code>
     *
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.1
     */

    public static void copyDirectory(File srcDir, File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

 //END
}