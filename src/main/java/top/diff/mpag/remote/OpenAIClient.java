package top.diff.mpag.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.diff.mpag.config.OpenAIClientConfiguration;
import top.diff.mpag.remote.param.OpenAIChatReq;

@FeignClient(name = "openai-client", url = "${openai-api.url}", configuration = OpenAIClientConfiguration.class)
public interface OpenAIClient {

  @PostMapping(value = "/chat/completions", consumes = MediaType.APPLICATION_JSON_VALUE)
  String chat(@RequestBody OpenAIChatReq req);
}
