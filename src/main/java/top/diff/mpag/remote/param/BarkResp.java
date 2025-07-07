package top.diff.mpag.remote.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
public class BarkResp {

  @JsonProperty("code")
  private Integer code;
  @JsonProperty("message")
  private String message;
  @JsonProperty("timestamp")
  private long timestamp;

  public BarkResp() {
    this.code = 500;
    this.message = "NonePushed";
    this.timestamp = (new Date()).getTime();
  }

  public boolean isSuccess() {
    return this.code == 200;
  }
}
