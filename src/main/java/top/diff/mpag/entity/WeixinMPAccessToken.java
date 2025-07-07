package top.diff.mpag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class WeixinMPAccessToken {

  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("expires_in")
  private Integer expiresIn;
  @JsonProperty("errcode")
  private Integer errcode;
  @JsonProperty("errmsg")
  private String errmsg;
}
