package top.diff.mpag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.diff.mpag.remote.BarkClient;
import top.diff.mpag.remote.param.BarkResp;
import top.diff.mpag.utils.GuavaCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class BarkService {
  @Autowired
  private BarkClient barkClient;
  @Value("${bark.weixinMP.draftPostedNotifyDevices:\"\"}")
  private String draftPostedNotifyDevices;

  /**
   *
   * @param title 标题
   * @param content 内容
   * @param allowNoisy 允许吵闹: false: 24小时内只允许推送成功一次. true: 无限制推送
   */
  public void pushMsg(String title, String content, boolean allowNoisy) {
    try {
      String[] draftPostedNotifyDevices = this.draftPostedNotifyDevices.split(",");
      if (CollectionUtils.isEmpty(Arrays.asList(draftPostedNotifyDevices))) {
        log.info("Bark推送设备列表为空忽略推送");
      }
      for (String device : draftPostedNotifyDevices) {
        if (!allowNoisy) {
          Long lastPushed = GuavaCache.getLong(device);
          if (null != lastPushed) {
            log.info("{} 24小时内已经推送, 本次忽略", device);
            continue;
          }
        }
        BarkResp resp = barkClient.pushMsg(device, title, content);
        if (resp.isSuccess()) {
          GuavaCache.put(device, resp.getTimestamp());
        }
      }
    } catch (Exception e) {
      log.warn("Bark推送失败", e);
    }
  }
}
