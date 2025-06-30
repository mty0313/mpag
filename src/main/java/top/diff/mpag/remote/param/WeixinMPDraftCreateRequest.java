package top.diff.mpag.remote.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class WeixinMPDraftCreateRequest {

  @JsonProperty("articles")
  private List<ArticlesDTO> articles = new ArrayList<>();

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  public static class ArticlesDTO {
    /**
     * 标题
     */
    @JsonProperty("title")
    private String title;
    /**
     * 作者
     */
    @JsonProperty("author")
    private String author = "MPAG";
    /**
     * 图文消息的摘要，仅有单图文消息才有摘要，多图文此处为空。如果本字段为没有填写，则默认抓取正文前54个字。
     */
    @JsonProperty("digest")
    private String digest;
    /**
     * 图文消息的具体内容，支持HTML标签，必须少于2万字符，小于1M，且此处会去除JS,涉及图片url必须来源 "上传图文消息内的图片获取URL"接口获取。外部图片url将被过滤。
     */
    @JsonProperty("content")
    private String content;
    /**
     * 图文消息的原文地址，即点击“阅读原文”后的URL
     */
    @JsonProperty("content_source_url")
    private String contentSourceUrl = "https://github.com/mty0313/mpag";
    /**
     * 图文消息的封面图片素材id（必须是永久MediaID）
     */
    @JsonProperty("thumb_media_id")
    private String thumbMediaId = "T-tQVwZE_VTJAKxQktbhae9CcIp1E0hRqOk3Lf_ELYoVN6Intpgr1Rv7qqlAvwSP";
    /**
     * Uint32 是否打开评论，0不打开(默认)，1打开
     */
    @JsonProperty("need_open_comment")
    private int needOpenComment = 1;
    /**
     * Uint32 是否粉丝才可评论，0所有人可评论(默认)，1粉丝才可评论
     */
    @JsonProperty("only_fans_can_comment")
    private int onlyFansCanComment = 1;
    /**
     * 封面裁剪为2.35:1规格的坐标字段。以原始图片（thumb_media_id）左上角（0,0），右下角（1,1）建立平面坐标系，经过裁剪后的图片，
     * 其左上角所在的坐标即为（X1,Y1）,右下角所在的坐标则为（X2,Y2），用分隔符_拼接为X1_Y1_X2_Y2，每个坐标值的精度为不超过小数点后6位数字。
     * 示例见下图，图中(X1,Y1) 等于（0.1945,0）,(X2,Y2)等于（1,0.5236），所以请求参数值为0.1945_0_1_0.5236。
     */
    @JsonProperty("pic_crop_235_1")
    private String picCrop2351;
    /**
     * 封面裁剪为1:1规格的坐标字段，裁剪原理同pic_crop_235_1，裁剪后的图片必须符合规格要求。
     */
    @JsonProperty("pic_crop_1_1")
    private String picCrop11;

    private boolean valid() {
      return StringUtils.hasText(this.title) && StringUtils.hasText(this.content);
    }
  }

  public static ArticlesDTO buildArticle(String title, String content) {
    ArticlesDTO dto = new ArticlesDTO();
    dto.setTitle(title);
    dto.setContent(content);
    return dto;
  }

  public boolean valid() {
    for (ArticlesDTO article : this.articles) {
      if (!article.valid()) {
        return false;
      }
    }
    return true;
  }
}
