package top.diff.mpag.service;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.diff.mpag.common.GlobalException;
import top.diff.mpag.job.params.WeixinMPAfterDraft;
import top.diff.mpag.remote.param.WeixinMPDraftCreateRequest;
import top.diff.mpag.service.params.WeixinMPDraftCreateAndPost;

@Service
@Slf4j
public class WeixinMPDraftService {

  @Autowired
  private BarkService barkService;
  @Autowired
  private WeixinMPPostService postService;
  @Autowired
  private DynamicFeignClientService dynamicFeignClientService;
  @Value("${bark.weixinMP.draftPostedNotifyDevices:\"\"}")
  private String draftPostedNotifyDevices;

  public void createDraft(WeixinMPAfterDraft afterDraft) throws GlobalException {
    WeixinMPDraftCreateRequest draftCreateRequest = new WeixinMPDraftCreateRequest();
    if (null != draftCreateRequest) {
      WeixinMPDraftCreateAndPost draft = postService.draftCreateAndPost(draftCreateRequest, afterDraft);
      log.debug("draft is: {}", JSON.toJSONString(draft));
    }
  }
}
