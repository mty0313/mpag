package top.diff.mpag.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.diff.mpag.common.CustomAppId;
import top.diff.mpag.common.R;
import top.diff.mpag.controller.params.UploadImageData;
import top.diff.mpag.controller.params.WxArticlesPost;
import top.diff.mpag.job.params.WeixinMPAfterDraft;
import top.diff.mpag.remote.WeixinMPClient;
import top.diff.mpag.remote.param.OpenAIChatReq;
import top.diff.mpag.remote.param.WeixinMPDraftCreateRequest;
import top.diff.mpag.service.DynamicFeignClientService;
import top.diff.mpag.service.TransferImageService;
import top.diff.mpag.service.WeixinMPPostService;
import top.diff.mpag.service.params.WeixinMPDraftCreateAndPost;
import top.diff.mpag.utils.DateUtil;

import java.util.ArrayList;
import java.util.Date;
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
  @Autowired
  private TransferImageService transferImageService;

  @PostMapping("/post")
  public WeixinMPDraftCreateAndPost post(@RequestBody WxArticlesPost post) {
    WeixinMPDraftCreateAndPost postResult = postService.draftCreateAndPost(buildFromReq(post), new WeixinMPAfterDraft(post2MpNews, send2All));
    return postResult;
  }

  @PostMapping("/postMedia")
  public String postMedia(@RequestBody MultipartFile file) {
    WeixinMPClient weixinMPClient = dynamicFeignClientService.getClient(WeixinMPClient.class, CustomAppId.WeixinMP.name());
    return weixinMPClient.addMaterial("image", file);
  }

  @PostMapping("/uploadImage")
  public R<String> uploadImage(@RequestBody UploadImageData imageData) {
    String wxmpUrl = transferImageService.downloadAndUploadToWeChat(imageData.getWebUrl());
    if (StringUtils.isNotBlank(wxmpUrl)) {
      return R.success(wxmpUrl);
    } else {
      return R.error();
    }
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

  private OpenAIChatReq buildOpenAIChatReq4Articles() {
    OpenAIChatReq req = new OpenAIChatReq();
    OpenAIChatReq.MessagesDTO system = new OpenAIChatReq.MessagesDTO();
    system.setRole("system");
    system.setContent("你是一个有用的人工智能助手, 你擅长搜集信息, 你擅长编写公众号文章.");
    req.getMessages().add(system);
    OpenAIChatReq.MessagesDTO user = new OpenAIChatReq.MessagesDTO();
    user.setRole("user");
    user.setContent(String.format("现在是%s, 请你帮我联网搜集下昨天的要闻, 搜集下最新的epic和steam免费游戏信息, 并且归纳为一篇适合发表的公众号文章.", DateUtil.toStandardYMD(new Date())));
    req.getMessages().add(user);
    return req;
  }
}
