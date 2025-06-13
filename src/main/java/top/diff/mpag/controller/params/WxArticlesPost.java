package top.diff.mpag.controller.params;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WxArticlesPost {
  List<SimpleArticle> articles = new ArrayList<>();

  @Data
  public static
  class SimpleArticle {
    private String title;
    private String content;
  }

}


