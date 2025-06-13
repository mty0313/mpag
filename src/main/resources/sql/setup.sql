create table if not exists remote_server_info
(
    uuid         varchar(128) not null
        primary key,
    created      timestamp    null,
    modified     timestamp    null,
    access_token varchar(512) not null,
    app_id       varchar(128) not null,
    server_url   varchar(128) null comment '服务地址'
)
    charset = utf8mb3;

create table if not exists weixin_m_p_draft_post
(
    uuid       varchar(255)         null,
    media_id   varchar(255)         null comment '发布草稿返回的mediaId',
    published  tinyint(1) default 0 null comment '是否已发布',
    publish_id varchar(255)         null comment '草稿发布返回的发布ID'
)
    comment '微信公众号发布记录';