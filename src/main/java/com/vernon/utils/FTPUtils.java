package com.vernon.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FTPUtils {
	
	private static final Log LOGGER = LogFactory.getLog(FTPUtils.class);
	public static final String UPLOAD_ERROR_MESSAGE = "errormsg";
	public static final String UPLOAD_FILE_URL = "fileurl";
	public static final String UPLOAD_FILE_STATUS = "status";
	public static final String FILE_MD5 = "filemd5";
	public static final String FILE_SIZE = "filesize";
	
	public static final String SUCCESS = "1";
	public static final String FAILURE = "0";



	private FTPUtils(){
		
	}

	
	public static Map<String, String> upload(MultipartFile file, String dir,String ip,String user,String password,String downUrl){

		// 文件下载地址
		downUrl = downUrl.replaceAll("(\\\\|/)*$", "");

		// 文件名称生成策略（日期时间+uuid ）
		UUID uuid = UUID.randomUUID();
		Date d = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String formatDate = format.format(d);
		// 获取文件的扩展名
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		// 文件名
		String fileName = formatDate + "-" + uuid + "." + extension;

		Map<String, String> maps = new HashMap<String, String>();
		maps.put(UPLOAD_FILE_STATUS, FAILURE);
		if(StringUtils.isEmpty(ip)
				|| StringUtils.isEmpty(user)
				|| StringUtils.isEmpty(password)
				|| StringUtils.isEmpty(downUrl)){
			LOGGER.error("ftp相关配置为空，ip=" + ip + ",user=" + user + ",password=" + password + ",downurl=" + downUrl);
			maps.put(UPLOAD_ERROR_MESSAGE, "ftp相关配置为空");
			return maps;
		}
		
		FTPManager ftpManager = null;
		InputStream input = null;
		boolean result = true;
		try {
			input = file.getInputStream();
			//计算文件大小
			ftpManager = new FTPManager(ip, user, password);
			result = ftpManager.connectServer();
			if (result) {
				result = ftpManager.upFile(dir, fileName, input);
			}
		} catch(Exception e) {
			result = false;
			LOGGER.error("ftp处理过程异常:", e);
		} finally {
			IOUtils.closeQuietly(input);
			ftpManager.disConnectServer();
		}
		
		if(!result) {
			maps.put(UPLOAD_ERROR_MESSAGE, "ftp上传失败");
			return maps;
		}
		
		
		maps.put(UPLOAD_FILE_STATUS, SUCCESS);
		maps.put(UPLOAD_FILE_URL, downUrl + "/"+dir + fileName);
		
		return maps;
	}
}
