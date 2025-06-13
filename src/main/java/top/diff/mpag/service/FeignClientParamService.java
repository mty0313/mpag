package top.diff.mpag.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.diff.mpag.common.CustomAppId;
import top.diff.mpag.entity.RemoteServerInfo;
import top.diff.mpag.remote.param.WeixinMPParams;

@Service
public class FeignClientParamService {
  @Autowired
  private RemoteServerInfoService remoteServerInfoService;

  public <T> T getFeignParam(String appId, Class<T> paramClazz) throws Exception {
    // todo 不同的appId设置不同的参数,用更好的方式
    RemoteServerInfo serverInfo = remoteServerInfoService.getServerInfoByAppId(appId);
    if (null == serverInfo) {
      return null;
    }
    if (CustomAppId.WeixinMP.name().equals(appId)) {
      String[] weiMPDevInfo = serverInfo.getAccessToken().split(";");
      WeixinMPParams params = new WeixinMPParams();
      params.setServerUrl(serverInfo.getServerUrl());
      params.setAppId(weiMPDevInfo[0]);
      params.setAppSecret(weiMPDevInfo[1]);
      RemoteServerInfo weixinMPAccessToken = remoteServerInfoService.getServerInfoByAppId(CustomAppId.WeixinMPAccessToken.name());
      if (null != weixinMPAccessToken) {
        params.setAccessToken(weixinMPAccessToken.getAccessToken());
      }
      return (T) params;
    }
    return null;

  }
}
