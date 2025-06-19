package top.diff.mpag.controller.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class N8NFeeds {
  @JsonProperty("feeds")
  private List<FeedJson> feedJsons = new ArrayList<>();

  @Data
  public static class FeedJson {
    @JsonProperty("json")
    private Json json;
  }

  @Data
  public static class Json {
    @JsonProperty("title")
    private String title;
    @JsonProperty("pubDate")
    private String pubDate;
    @JsonProperty("type")
    private String type;
    @JsonProperty("contentSnippet")
    private String contentSnippet;
    @JsonProperty("image")
    private String image;
    @JsonProperty("rawImage")
    private String rawImage;
  }
}
