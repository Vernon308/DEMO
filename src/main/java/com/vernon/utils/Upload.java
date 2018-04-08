package com.vernon.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


/**
 * 上传文件工具类
 * @author qunding
 */
public class Upload {

    /**
     * 上传文件
     *
     * @param request
     * @param response
     * @param serverPath 服务器地址:(http://172.16.5.102:8090/)
     * @param path       文件路径（不包含服务器地址：upload/）
     * @return
     */
    public static String upload(Client client, MultipartFile file, HttpServletRequest request, HttpServletResponse response, String serverPath, String path) throws IOException {
        // 文件名称生成策略（日期时间+uuid ）
        UUID uuid = UUID.randomUUID();
        Date d = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String formatDate = format.format(d);
        // 获取文件的扩展名
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        // 文件名
        String fileName = formatDate + "-" + uuid + "." + extension;
        //相对路径
        String relaPath = path + fileName;

        String dir = serverPath + path.substring(0, path.lastIndexOf("/"));
        File fileLocation = new File(dir);
        if (!fileLocation.exists()) {
            boolean mkdirs = fileLocation.mkdirs();
            System.out.println(mkdirs);
        }
        // 另一台tomcat的URL（真实路径）
        String realPath = serverPath + relaPath;

        File uploadFile = new File(dir,fileName);
        try {
        CommonsMultipartFile cf = (CommonsMultipartFile) file;
        DiskFileItem fi = (DiskFileItem) cf.getFileItem();
        File formalFile = fi.getStoreLocation();
        OutputStream out = new FileOutputStream(uploadFile);
        InputStream in = new FileInputStream(formalFile);
        byte[] buffer = new byte[1024*1024];
        int length;
        while((length=in.read(buffer))>0){
            out.write(buffer,0,length);
        }
        in.close();
        out.close();
        return fileName + ";" + relaPath + ";" + realPath;
        } catch (FileNotFoundException ex) {
            System.out.println("上传失败!");
            ex.printStackTrace();
            return "";
        } catch (IOException ex) {
            System.out.println("上传失败!");
            ex.printStackTrace();
            return "";
        }


//        // 设置请求路径
//        WebResource resource = client.resource(realPath);
//
//        // 发送开始post get put（基于put提交）
//
//        try {
//            resource.put(String.class, file.getBytes());
//            return fileName+";"+relaPath+";"+realPath;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "";
//        }


    }


}