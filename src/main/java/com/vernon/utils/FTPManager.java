package com.vernon.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.util.StringUtils;

public class FTPManager {
    private FTPClient ftpclient;
    private String ipAddress;
    private int ipPort = 21;
    private String userName;
    private String passWord;
    private static final String Encod = "UTF-8";
    private static final Log logger = LogFactory.getLog(FTPManager.class);

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getIpPort() {
        return this.ipPort;
    }

    public void setIpPort(int ipPort) {
        this.ipPort = ipPort;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return this.passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public FTPManager() {
    }

    public FTPManager(String ip, int port, String username, String password) throws Exception {
        this.ipAddress = new String(ip);
        this.ipPort = port;
        this.userName = new String(username);
        this.passWord = new String(password);
    }

    public FTPManager(String ip, String username, String password) throws Exception {
        this.ipAddress = new String(ip);
        this.userName = new String(username);
        this.passWord = new String(password);
    }

    public boolean connectServer() {
        boolean flag = true;

        try {
            this.ftpclient = new FTPClient();
            this.ftpclient.setControlEncoding("UTF-8");
            this.ftpclient.setConnectTimeout(1000000);
            this.ftpclient.connect(this.ipAddress, this.ipPort);
            this.ftpclient.login(this.userName, this.passWord);
            int reply = this.ftpclient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                this.ftpclient.disconnect();
                logger.debug("FTP 服务拒绝连接！");
                flag = false;
            }
        } catch (SocketException var3) {
            flag = false;
            logger.error(ExceptionUtils.getStackTrace(var3));
            logger.error("登录ftp服务器 " + this.ipAddress + " 失败,连接超时！");
        } catch (IOException var4) {
            flag = false;
            logger.error(ExceptionUtils.getStackTrace(var4));
            logger.error("登录ftp服务器 " + this.ipAddress + " 失败，FTP服务器无法打开！");
        }

        return flag;
    }

    public void disConnectServer() {
        try {
            if (this.ftpclient != null && this.ftpclient.isConnected()) {
                this.ftpclient.logout();
                this.ftpclient.disconnect();
            }
        } catch (Exception var2) {
            logger.error(var2, var2);
        }

    }

    public boolean makeDirectory(String dir) {
        boolean flag = true;

        try {
            flag = this.ftpclient.makeDirectory(dir);
            if (flag) {
                logger.debug("make Directory " + dir + " succeed");
            } else {
                logger.debug("make Directory " + dir + " false");
            }
        } catch (Exception var4) {
            flag = false;
            logger.error(ExceptionUtils.getStackTrace(var4));
        }

        return flag;
    }

    public List<String> fileNames(String fullPath) throws Exception {
        ArrayList list = new ArrayList();

        try {
            String[] names = this.ftpclient.listNames();
            if (names == null) {
                return list;
            }

            for(int i = 0; i < names.length; ++i) {
                list.add(names[i]);
            }
        } catch (Exception var5) {
            logger.error(ExceptionUtils.getStackTrace(var5));
        }

        return list;
    }

    public String[] getRemoteFiles(String path) {
        String[] files = null;

        try {
            boolean x = this.ftpclient.changeWorkingDirectory(path);
            if (x) {
                files = this.ftpclient.listNames();
            }
        } catch (Exception var4) {
            logger.error(ExceptionUtils.getStackTrace(var4));
        }

        return files;
    }

    public boolean upFile(String path, String filename, InputStream input) {
        boolean flag = false;

        try {
            this.ftpclient.setFileType(2);
            this.ftpclient.setFileTransferMode(10);
            this.ftpclient.changeWorkingDirectory("/");
            if (!StringUtils.isEmpty(path)) {
                String[] paths = path.split("/|\\\\");

                for(int i = 0; i < paths.length; ++i) {
                    String p = paths[i];
                    if (!StringUtils.isEmpty(p)) {
                        this.ftpclient.makeDirectory(p);
                        this.ftpclient.changeWorkingDirectory(p);
                    }
                }
            }

            this.ftpclient.setDataTimeout(1000000);
            this.ftpclient.enterLocalPassiveMode();
            flag = this.ftpclient.storeFile(filename, input);
        } catch (Exception var8) {
            logger.error(ExceptionUtils.getStackTrace(var8));
        }

        return flag;
    }

    public boolean deleteFile(String pathAndFileName) {
        boolean flag = false;

        try {
            flag = this.ftpclient.deleteFile(pathAndFileName);
        } catch (IOException var4) {
            logger.error(ExceptionUtils.getStackTrace(var4));
        }

        return flag;
    }

    public boolean isExist(String path, String fileName) throws Exception {
        boolean isExist = false;
        String[] fileNames = null;

        try {
            fileNames = this.getRemoteFiles(path);
        } catch (Exception var6) {
            var6.printStackTrace();
            return isExist;
        }

        if (fileNames != null) {
            for(int i = 0; i < fileNames.length; ++i) {
                if (fileName.equals(fileNames[i])) {
                    isExist = true;
                    break;
                }
            }
        }

        return isExist;
    }

    public byte[] downFile(String SourceFileName) {
        ByteArrayOutputStream byteOut = null;
        InputStream ftpIn = null;

        try {
            this.ftpclient.setFileType(2);
            this.ftpclient.enterLocalPassiveMode();
            this.ftpclient.setFileTransferMode(10);
            byteOut = new ByteArrayOutputStream();
            ftpIn = this.ftpclient.retrieveFileStream(SourceFileName);
            if (ftpIn == null) {
                Object var12 = null;
                return (byte[])var12;
            } else {
                byte[] buf = new byte[204800];
                boolean var5 = false;

                int bufsize;
                while((bufsize = ftpIn.read(buf, 0, buf.length)) != -1) {
                    byteOut.write(buf, 0, bufsize);
                }

                byte[] var6 = byteOut.toByteArray();
                return var6;
            }
        } catch (IOException var10) {
            logger.error(ExceptionUtils.getStackTrace(var10));
            return null;
        } finally {
            IOUtils.closeQuietly(byteOut);
            IOUtils.closeQuietly(ftpIn);
        }
    }

    public static void main(String[] args) throws Exception {
        FTPManager fUp = new FTPManager("172.16.82.220", "ydhl", "123456");
        fUp.connectServer();
        FileInputStream fin = new FileInputStream("d:\\testupload.txt");
        fUp.upFile("/test", "test.txt", fin);
        fUp.deleteFile("/test/test.txt");
        fUp.getRemoteFiles("/");
        List<String> list = fUp.fileNames("/");

        for(int i = 0; i < list.size(); ++i) {
            System.out.println(((String)list.get(i)).toString());
        }

        fUp.downFile("/test/test.txt");
        fin.close();
        fUp.disConnectServer();
        System.out.println("程序运行完成！");
    }
}
