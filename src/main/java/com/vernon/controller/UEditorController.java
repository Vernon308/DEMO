package com.vernon.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vernon.utils.FTPUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.vernon.baidu.ueditor.ActionEnter;
import com.vernon.utils.ResponseUtils;
import com.vernon.utils.Upload;
import com.sun.jersey.api.client.Client;

import net.sf.json.JSONObject;
/**
 * baidu-ueditor
 * @author qunding
 */
@Controller
@RequestMapping("/ueditor")
public class UEditorController {
    @Value(value="${ueditor}")    //后台图片保存地址
    private String ueditor;


    @Value(value="${ftp_ip}")
    private String ip;

    @Value(value="${ftp_user}")
    private String user;

    @Value(value="${ftp_password}")
    private String password;


    @Value(value="${ftp_download_url}")
    private String downUrl;

    /**
     * ueditor文件上传（上传到外部服务器）
     * @param request
     * @param response
     * @param action
     */
    @ResponseBody
    @RequestMapping(value="/ueditorUpload.do", method={RequestMethod.GET, RequestMethod.POST})
    public void editorUpload(HttpServletRequest request, HttpServletResponse response, String action) {
        response.setContentType("test/html");
        String rootPath = request.getSession().getServletContext().getRealPath("/");

        try {
            if("config".equals(action)){    //如果是初始化
                String exec = new ActionEnter(request, rootPath).exec();
                PrintWriter writer = response.getWriter();
                writer.write(exec);
                writer.flush();
                writer.close();
            }else if("uploadimage".equals(action) || "uploadvideo".equals(action) || "uploadfile".equals(action)){    //如果是上传图片、视频、和其他文件
                try {
                    MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
                    MultipartHttpServletRequest Murequest = resolver.resolveMultipart(request);
                    Map<String, MultipartFile> files = Murequest.getFileMap();//得到文件map对象
                    // 实例化一个jersey
                    Client client = new Client();

                    for(MultipartFile pic: files.values()){
                        JSONObject jo = new JSONObject();
                        long size = pic.getSize();    //文件大小
                        String originalFilename = pic.getOriginalFilename();  //原来的文件名
                        Map<String,String>map = FTPUtils.upload(pic,ueditor,ip,user,password,downUrl);
                        String uploadurl = map.get(FTPUtils.UPLOAD_FILE_URL);


                        if(!"".equals(uploadurl)){    //如果上传成功
                            jo.put("state", "SUCCESS");
                            jo.put("original", originalFilename);//原来的文件名
                            jo.put("size", size);//文件大小
                            jo.put("title", "");//随意，代表的是鼠标经过图片时显示的文字
                            jo.put("type", FilenameUtils.getExtension(pic.getOriginalFilename()));//文件后缀名
                            jo.put("url", uploadurl);//这里的url字段表示的是上传后的图片在图片服务器的完整地址（http://ip:端口/***/***/***.jpg）
                        }else{    //如果上传失败
                        }
                        //发送到前端解析
                        ResponseUtils.renderJson(response, jo.toString());
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
        }
    }
    @ResponseBody
    @RequestMapping(value="/add.do", method={RequestMethod.GET, RequestMethod.POST})
    public void add(HttpServletRequest request){
       // System.out.println("=====json====="+request.getParameter("content"));
    }
}