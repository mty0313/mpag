package top.diff.mpag.remote.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class OpenAIChatReq {

  @JsonProperty("model")
  private String model = "deepseek-chat";
  @JsonProperty("messages")
  private List<MessagesDTO> messages = new ArrayList<>();
  @JsonProperty("stream")
  private boolean stream;

  @NoArgsConstructor
  @Data
  public static class MessagesDTO {
    @JsonProperty("role")
    private String role;
    @JsonProperty("content")
    private String content;
  }
}
