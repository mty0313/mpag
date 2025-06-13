package top.diff.mpag.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import top.diff.mpag.common.R;
import top.diff.mpag.config.FeignClientConfiguration;

@FeignClient(name = "bark-client", url = "${bark.serverUrl}", configuration = FeignClientConfiguration.class)
public interface BarkClient {
  /**
   * 推送消息
   * @param device
   * @param title
   * @param content
   * @return
   */
  @PostMapping("/{device}/{title}/{content}")
  R<String> pushMsg(@PathVariable("device") String device, @PathVariable("title") String title,
      @PathVariable("content") String content);

}
