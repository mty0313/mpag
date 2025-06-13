package top.diff.mpag.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import top.diff.mpag.common.Assert;
import top.diff.mpag.common.CustomAppId;
import top.diff.mpag.common.GlobalException;
import top.diff.mpag.entity.RemoteServerInfo;
import top.diff.mpag.service.RemoteServerInfoService;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
@DependsOn("dataSourceInitializer")
@Slf4j
public class Initializer {
  @Autowired
  private RemoteServerInfoService remoteServerInfoService;
  @Autowired
  private WeixinMPJob weixinMPJob;
  @Value("${init.weixinmp.appId}")
  private String initWeixinMPAppId;
  @Value("${init.weixinmp.appSecret}")
  private String initWeixinMPAppSecret;
  @Value("${init.weixinmp.serverUrl:https://api.weixin.qq.com}")
  private String initWeixinMPServerUrl;

  @PostConstruct
  public void init() throws GlobalException {
    try {
      initServerParams();
      tokenRefresh();
    } catch (Exception e) {
      log.error("初始化任务失败: {}", e.getMessage());
    }

  }

  private void initServerParams() {
    // 运行时必须要有remoteServerInfo的参数，每次启动时都会更新
    Assert.notEmpty(initWeixinMPAppId, "initWeixinMPAppId");
    Assert.notEmpty(initWeixinMPAppSecret, "initWeixinMPAppSecret");
    Assert.notEmpty(initWeixinMPServerUrl, "initWeixinMPServerUrl");
    QueryWrapper<RemoteServerInfo> wrapper = new QueryWrapper<>();
    wrapper.in("app_id", List.of(CustomAppId.WeixinMP.name()));
    remoteServerInfoService.delete(wrapper);
    RemoteServerInfo weixinMP = new RemoteServerInfo(CustomAppId.WeixinMP,
        initWeixinMPAppId + ";" + initWeixinMPAppSecret, initWeixinMPServerUrl);
    remoteServerInfoService.insertServerInfo(weixinMP);
  }

  private void tokenRefresh() {
    weixinMPJob.tokenRefresh();
  }
}
