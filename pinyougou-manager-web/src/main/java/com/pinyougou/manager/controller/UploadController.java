package com.pinyougou.manager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.pinyougou.common.utils.FastDFSClient;

import entity.Result;

@RestController
public class UploadController {
	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;// 文件上传地址

	@RequestMapping("/upload")
	public Result upload(MultipartFile file) {
		String filename = file.getOriginalFilename();
		//获取文件扩展名
		String extName = filename.substring(filename.lastIndexOf(".")+1);
		try {
			//创建一个客户端执行上传
			FastDFSClient fastDFSClient=new FastDFSClient("classpath:config/fdfs_client.conf");
			
			String path = fastDFSClient.uploadFile(file.getBytes(),extName);
			
			//拼接返回的url和ip地址
			String url=FILE_SERVER_URL+path;
			return new Result(true,url);
		} catch (Exception e) {
			return new Result(false ,"上传失败");
		}
	}
}
