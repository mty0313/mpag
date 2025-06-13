package top.diff.mpag.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.diff.mpag.common.CustomAppId;
import top.diff.mpag.controller.params.WxArticlesPost;
import top.diff.mpag.job.params.WeixinMPAfterDraft;
import top.diff.mpag.remote.WeixinMPClient;
import top.diff.mpag.remote.param.WeixinMPDraftCreateRequest;
import top.diff.mpag.service.DynamicFeignClientService;
import top.diff.mpag.service.WeixinMPPostService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/article")
public class WxArticleController {
  @Autowired
  private WeixinMPPostService postService;
  @Autowired
  private DynamicFeignClientService dynamicFeignClientService;
  @Value("${weixin.mp.draft.post2MpNews:false}")
  private boolean post2MpNews;
  @Value("${weixin.mp.draft.send2All:false}")
  private boolean send2All;

  @PostMapping("/post")
  public void post(@RequestBody WxArticlesPost post) {
    postService.draftCreateAndPost(buildFromReq(post), new WeixinMPAfterDraft(post2MpNews, send2All));
  }

  @PostMapping("/postMedia")
  public String postMedia(@RequestBody MultipartFile file) {
    WeixinMPClient weixinMPClient = dynamicFeignClientService.getClient(WeixinMPClient.class, CustomAppId.WeixinMP.name());
    return weixinMPClient.addMaterial("image", file);
  }

  private WeixinMPDraftCreateRequest buildFromReq(WxArticlesPost article) {
    List<WeixinMPDraftCreateRequest.ArticlesDTO> articlesDTOList = new ArrayList<>();
    WeixinMPDraftCreateRequest request = new WeixinMPDraftCreateRequest();
    request.setArticles(articlesDTOList);
    for (WxArticlesPost.SimpleArticle a : article.getArticles()) {
      WeixinMPDraftCreateRequest.ArticlesDTO dto = WeixinMPDraftCreateRequest.buildArticle(a.getTitle(), a.getContent());
      articlesDTOList.add(dto);
    }
    return request;
  }
}
