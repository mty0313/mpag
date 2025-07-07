package top.diff.mpag.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.diff.mpag.common.GlobalException;
import top.diff.mpag.job.params.WeixinMPAfterDraft;
import top.diff.mpag.service.WeixinMPDraftService;
import top.diff.mpag.service.WeixinTokenService;

@Component
public class WeixinMPJob {

  @Value("${weixin.mp.draft.post2MpNews:false}")
  private boolean post2MpNews;
  @Value("${weixin.mp.draft.send2All:false}")
  private boolean send2All;

  @Autowired
  private WeixinTokenService weixinTokenService;
  @Autowired
  private WeixinMPDraftService weixinMPDraftService;

  @Scheduled(fixedRate = 7000000)
  public void tokenRefresh() {
    weixinTokenService.refreshAccessToken();
  }
}
