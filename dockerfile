FROM bellsoft/liberica-openjdk-alpine-musl:17
# 创建目录
RUN mkdir /mpag
WORKDIR /mpag

# 创建 resource 目录
RUN mkdir /server-temp

# 复制 Spring Boot JAR 文件和配置文件到容器中
COPY ./target/mpag-0.0.1-SNAPSHOT.jar /mpag

# 暴露端口
EXPOSE 8089

ENV DATABASE_URL=localhost:3306
ENV DATABASE_USER=user
ENV DATABASE_PASS=pass
ENV UPDATE_DATABASE=true
ENV BARK_SERVER=http://localhost
ENV BARK_DEVICE=""
ENV MP_POST_TO_MP_NEWS=false
ENV MP_SEND_TO_ALL=false
ENV MP_APP_ID=""
ENV MP_APP_SECRET=""

# 设置启动命令
CMD [ \
  "java", \
  "-Xmx512m", "-Xms512m", \
  "-Duser.timezone=GMT+08", \
  "-jar", "mpag-0.0.1-SNAPSHOT.jar", \
  "--spring.profiles.active=online", \
  "--spring.datasource.url=jdbc:mysql://${DATABASE_URL}/mpag?useUnicode=true&useSSL=false&characterEncoding=utf8", \
  "--spring.datasource.username=${DATABASE_USER}", \
  "--spring.datasource.password=${DATABASE_PASS}", \
  "--init.weixinmp.appId=${MP_APP_ID}", \
  "--init.weixinmp.appSecret=${MP_APP_SECRET}", \
  "--weixin.mp.draft.post2MpNews=${MP_POST_TO_MP_NEWS}", \
  "--weixin.mp.draft.send2All=${MP_SEND_TO_ALL}", \
  "--weixin.mp.draft.updateDatabase=${UPDATE_DATABASE}", \
  "--bark.serverUrl=${BARK_SERVER}", \
  "--bark.weixinMP.draftPostedNotifyDevices=${BARK_DEVICE}" \
]
