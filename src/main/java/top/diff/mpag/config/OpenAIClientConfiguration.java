package top.diff.mpag.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIClientConfiguration extends FeignClientConfiguration {
  @Value("${openai-api.apiKey}")
  private String apiKey;

  @Bean
  public RequestInterceptor openAiRequestInterceptor() {
    return template -> template.header("Authorization", "Bearer " + apiKey);
  }
}
