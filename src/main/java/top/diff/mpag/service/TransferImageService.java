package top.diff.mpag.service;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import top.diff.mpag.common.CustomAppId;
import top.diff.mpag.remote.WeixinMPClient;
import top.diff.mpag.remote.param.WeixinMPImageUploadResponse;
import top.diff.mpag.utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

@Service
public class TransferImageService {
  @Autowired
  private DynamicFeignClientService dynamicFeignClientService;

  public String downloadAndUploadToWeChat(String webUrl) {
    if (StringUtils.isBlank(webUrl)) {
      return null;
    }
    try {
      // 下载图片
      URL url = new URL(webUrl);
      URLConnection connection = url.openConnection();
      connection.setRequestProperty("User-Agent", "Mozilla/5.0");
      InputStream inputStream = connection.getInputStream();

      // 读取内容
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      int len;
      while ((len = inputStream.read(buffer)) != -1) {
        baos.write(buffer, 0, len);
      }
      byte[] fileContent = baos.toByteArray();

      // 获取 contentType 和扩展名
      String contentType = connection.getContentType();
      String ext = contentType != null && contentType.contains("/") ? contentType.split("/")[1] : "jpg";
      String fileName = "image." + ext;

      // 创建 MultipartFile
      FileItem fileItem = new DiskFileItem("media", contentType, false, fileName, fileContent.length, new File(System.getProperty("java.io.tmpdir")));
      try (OutputStream os = fileItem.getOutputStream()) {
        os.write(fileContent);
      }
      MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
      WeixinMPClient client = dynamicFeignClientService.getClient(WeixinMPClient.class, CustomAppId.WeixinMP.name());
      // 上传
      String uploadRes = client.uploadImage(multipartFile);
      WeixinMPImageUploadResponse uploadResponse = JSON.to(WeixinMPImageUploadResponse.class, uploadRes);
      if (null != uploadResponse && uploadResponse.success()) {
        return uploadResponse.getUrl();
      }
      return null;
    } catch (Exception e) {
      throw new RuntimeException("处理图片失败: " + e.getMessage(), e);
    }
  }
}