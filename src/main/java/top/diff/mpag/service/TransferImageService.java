package top.diff.mpag.service;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import top.diff.mpag.common.CustomAppId;
import top.diff.mpag.remote.WeixinMPClient;
import top.diff.mpag.remote.param.WeixinMPAddMaterialResponse;
import top.diff.mpag.remote.param.WeixinMPImageUploadResponse;
import top.diff.mpag.utils.GuavaCache;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@Service
@Slf4j
public class TransferImageService {
  @Autowired
  private DynamicFeignClientService dynamicFeignClientService;

  public String downloadAndUploadToWeChat(String webUrl) {
    if (StringUtils.isBlank(webUrl)) {
      return null;
    }
    // 先取缓存
    String uploadWeChatUrlCache = GuavaCache.getString(webUrl);
    if (StringUtils.isNotBlank(uploadWeChatUrlCache)) {
      return uploadWeChatUrlCache;
    }
    try {
      MultipartFile multipartFile = createMultipartFileFromUrl(webUrl);
      WeixinMPClient client = dynamicFeignClientService.getClient(WeixinMPClient.class, CustomAppId.WeixinMP.name());
      // 上传
      String uploadRes = client.uploadImage(multipartFile);
      WeixinMPImageUploadResponse uploadResponse = JSON.to(WeixinMPImageUploadResponse.class, uploadRes);
      if (null != uploadResponse && uploadResponse.success()) {
        String uploadWeChatUrl = uploadResponse.getUrl();
        if (StringUtils.isNotBlank(uploadWeChatUrl)) {
          // 设置缓存, 减少重复上传
          GuavaCache.put(webUrl, uploadWeChatUrl);
          GuavaCache.put(uploadWeChatUrl, webUrl);
          return uploadWeChatUrl;
        }
      }
      return null;
    } catch (Exception e) {
      log.error("处理图片失败: " + e.getMessage(), e);
      return null;
    }
  }

  public String downloadAndUploadToWeChatMaterial(String webUrl) {
    if (StringUtils.isBlank(webUrl)) {
      return null;
    }
    try {
      MultipartFile multipartFile = createMultipartFileFromUrl(webUrl);
      WeixinMPClient client = dynamicFeignClientService.getClient(WeixinMPClient.class, CustomAppId.WeixinMP.name());
      // 上传
      String uploadRes = client.addMaterial("image", multipartFile);
      WeixinMPAddMaterialResponse uploadResponse = JSON.to(WeixinMPAddMaterialResponse.class, uploadRes);
      if (null != uploadResponse && uploadResponse.success()) {
        return uploadResponse.getMediaId();
      }
      return null;
    } catch (Exception e) {
      throw new RuntimeException("处理图片失败: " + e.getMessage(), e);
    }
  }

  private MultipartFile createMultipartFileFromUrl(String webUrl) throws IOException {
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
    return new CommonsMultipartFile(fileItem);
  }




}