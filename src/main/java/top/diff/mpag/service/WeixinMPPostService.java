package top.diff.mpag.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.diff.mpag.common.CustomAppId;
import top.diff.mpag.entity.WeixinMPDraftPost;
import top.diff.mpag.job.params.WeixinMPAfterDraft;
import top.diff.mpag.mapper.WeixinMPDraftPostMapper;
import top.diff.mpag.remote.WeixinMPClient;
import top.diff.mpag.remote.param.*;
import top.diff.mpag.service.params.WeixinMPDraftCreateAndPost;
import top.diff.mpag.utils.DateUtil;
import top.diff.mpag.utils.GuavaCache;
import top.diff.mpag.utils.RegexUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WeixinMPPostService {
  @Autowired
  private DynamicFeignClientService feignClientService;
  @Autowired
  private WeixinMPDraftPostMapper draftPostMapper;
  @Autowired
  private BarkService barkService;
  @Value("${bark.weixinMP.draftPostedNotifyDevices:\"\"}")
  private String draftPostedNotifyDevices;
  @Autowired
  private TransferImageService transferImageService;

  /**
   * 创建和发布草稿
   */
  public WeixinMPDraftCreateAndPost draftCreateAndPost(WeixinMPDraftCreateRequest request, WeixinMPAfterDraft afterDraft) {
    if (!request.valid()) {
      log.error("草稿箱创建参数不合法: {}", JSON.toJSONString(request));
      return null;
    }
    // 生成封面图
    generateArticleThumb(request);
    WeixinMPClient client = feignClientService.getClient(WeixinMPClient.class, CustomAppId.WeixinMP.name());
    String response = client.createDraft(request);
    WeixinMPDraftCreateResponse responseEntity = JSON.to(WeixinMPDraftCreateResponse.class, response);
    if (null == responseEntity || !StringUtils.hasText(responseEntity.getMediaId())) {
      log.error("草稿创建请求失败: {}", JSON.toJSONString(responseEntity));
      return null;
    }
    String mediaId = responseEntity.getMediaId();
    log.info("草稿创建请求成功: " + mediaId);
    String publishId = "None";
    if (afterDraft.isPost2MpNews()) {
      // 发布草稿到图文, 成功会返回publishId用于轮询是否成功
      publishId = postDraft(mediaId).getPublishId();
      if (StringUtils.hasText(publishId)) {
        WeixinMPDraftPost draftPost = new WeixinMPDraftPost(UUID.randomUUID().toString(),
            mediaId, publishId, false);
        draftPostMapper.insert(draftPost);
      }
    }
    if (afterDraft.isSend2All()) {
      // 群发草稿中的内容
      WeixinMPSend2AllResponse send2AllResponse = weixinMPSend2All(mediaId);
      if (send2AllResponse != null && send2AllResponse.success()) {
        log.info("群发任务提交成功: mediaId: {}, msgId: {}", mediaId, send2AllResponse.getMsgId());
      } else {
        log.error("群发任务提交失败: {}", JSON.toJSONString(send2AllResponse));
      }
    }
    // 推送bark通知手动处理
    String[] draftPostedNotifyDevices = this.draftPostedNotifyDevices.split(",");
    if (CollectionUtils.isEmpty(Arrays.asList(draftPostedNotifyDevices))) {
      log.info("Bark推送设备列表为空忽略推送");
    }
    for (String device : draftPostedNotifyDevices) {
      barkService.pushMsg(device, "每日推文更新", DateUtil.toStandardYMD(new Date()));
    }
    return new WeixinMPDraftCreateAndPost(afterDraft.isPost2MpNews(), afterDraft.isSend2All(), mediaId, publishId);
  }

  /**
   * 生成文章头图
   */
  private void generateArticleThumb(WeixinMPDraftCreateRequest request) {
    List<WeixinMPDraftCreateRequest.ArticlesDTO> articles = request.getArticles();
    for (WeixinMPDraftCreateRequest.ArticlesDTO article : articles) {
      String content = article.getContent();
      String imgTagUrl = RegexUtil.parseImgTag(content);
      if (!StringUtils.hasText(imgTagUrl)) {
        continue;
      }
      // 从缓存获取原图,进行上传
      String rawImageUrl = GuavaCache.getString(imgTagUrl);
      String mediaId = transferImageService.downloadAndUploadToWeChatMaterial(rawImageUrl);
      if (StringUtils.hasText(mediaId)) {
        article.setThumbMediaId(mediaId);
      }
    }
  }

  /**
   * 根据publishId查询是否发布成功
   */
  public boolean publishQuery(String publishId) {
    if (!StringUtils.hasText(publishId)) {
      log.warn("没有publishId退出查询");
      return false;
    }
    WeixinMPClient client = feignClientService.getClient(WeixinMPClient.class, CustomAppId.WeixinMP.name());
    String response = client.queryPublishResult(new WeixinMPPublishResultQueryRequest(publishId));
    WeixinMPPublishResultQueryResponse responseEntity = JSON.to(WeixinMPPublishResultQueryResponse.class, response);
    if (null == responseEntity) {
      log.warn("WeixinMPPublishResultQueryResponse转换失败: {}", response);
      return false;
    }
    if (!StringUtils.hasText(responseEntity.getPublishId())) {
      log.warn("queryPublishResult没有返回publish_id, 查询参数: {}", publishId);
      return false;
    }
    if (!responseEntity.publishSucceeded()) {
      log.warn("发布失败: {}, {}", publishId, response);
    }
    return responseEntity.publishSucceeded();
  }

  public List<WeixinMPDraftPost> queryByWrapper(QueryWrapper<WeixinMPDraftPost> wrapper) {
    return draftPostMapper.selectList(wrapper);
  }

  public void updateDraftPost(WeixinMPDraftPost draftPost) {
    draftPostMapper.updateById(draftPost);
  }

  public WeixinMPDraftPost getByPublishId(String publishId) {
    QueryWrapper<WeixinMPDraftPost> wrapper = new QueryWrapper<>();
    wrapper.eq("publish_id", publishId);
    return draftPostMapper.selectOne(wrapper);
  }

  /**
   * 发布草稿, 任务提交成功则返回publish_id
   */
  private WeixinMPDraftPost postDraft(String mediaId) {
    WeixinMPClient client = feignClientService.getClient(WeixinMPClient.class, CustomAppId.WeixinMP.name());
    String response = client.draftPost(new WeixinMPDraftPostRequest(mediaId));
    WeixinMPDraftPostResponse responseEntity = JSON.to(WeixinMPDraftPostResponse.class, response);
    WeixinMPDraftPost draftPost = new WeixinMPDraftPost(UUID.randomUUID().toString(),
        mediaId, null, false);
    if (null != responseEntity && responseEntity.success()) {
      log.info("图文发布成功: {}", responseEntity.getPublishId());
      draftPost.setPublishId(responseEntity.getPublishId());
      return draftPost;
    }
    log.info("图文发布失败: {}", mediaId);
    return draftPost;
  }

  private WeixinMPSend2AllResponse weixinMPSend2All(String mediaId) {
    WeixinMPClient client = feignClientService.getClient(WeixinMPClient.class, CustomAppId.WeixinMP.name());
    return client.send2All(new WeixinMPSend2AllRequest(mediaId));
  }
}
