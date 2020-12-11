/*数据库更新日志 记录下更新人    更新日期  更新内容 */
/*金币场补给记录表*/
CREATE TABLE `t_gold_remedy` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `drawTime` datetime NOT NULL COMMENT '领取时间',
  `remedy` int(11) DEFAULT '0' COMMENT '补救金额',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*金币场玩家信息表*/
CREATE TABLE `t_gold_user` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(19) NOT NULL COMMENT '玩家id',
  `userName` varchar(100) DEFAULT NULL COMMENT '玩家名称',
  `userNickname` varchar(100) DEFAULT NULL COMMENT '玩家昵称',
  `playCount` int(9) NOT NULL COMMENT '局数',
  `playCountWin` int(9) NOT NULL COMMENT '胜局数',
  `playCountLose` int(9) NOT NULL COMMENT '败局数',
  `playCountEven` int(9) NOT NULL COMMENT '平局数',
  `freeGold` int(19) NOT NULL COMMENT '免费的金币',
  `Gold` int(19) NOT NULL COMMENT '付费的金币',
  `usedGold` int(19) NOT NULL COMMENT '消费的金币',
  `vipexp` bigint(20) NOT NULL DEFAULT '0' COMMENT 'vip经验',
  `exp` bigint(20) NOT NULL DEFAULT '0' COMMENT '经验值',
  `sex` int(2) NOT NULL DEFAULT '1' COMMENT '性别',
  `signature` text COMMENT '个性签名',
  `headimgurl` varchar(255) DEFAULT NULL COMMENT '头像',
  `headimgraw` varchar(255) DEFAULT '' COMMENT '原生头像',
  `extend` varchar(255) DEFAULT NULL COMMENT '扩展',
  `regTime` timestamp NULL DEFAULT NULL COMMENT '注册时间',
  `lastLoginTime` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `grade` int(9) DEFAULT '0' COMMENT '芒果跑得快段位',
  `gradeExp` int(9) DEFAULT '0' COMMENT '芒果跑得快段位经验值',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_userId` (`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*金币场房间表*/
CREATE TABLE `t_gold_room` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `currentCount` int(3) NOT NULL COMMENT '当前房间人数',
  `maxCount` int(3) NOT NULL COMMENT '最大人数',
  `serverId` int(10) NOT NULL COMMENT '服id',
  `currentState` varchar(20) NOT NULL COMMENT '当前状态（0：未开始，1：已开始，2：已结束）',
  `tableMsg` varchar(255) NOT NULL COMMENT '房间信息',
  `modeId` varchar(32) NOT NULL COMMENT '模式ID',
  `gameCount` int(3) NOT NULL COMMENT '局数',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifiedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`keyId`),
  KEY `idx_common` (`modeId`,`currentState`,`serverId`),
  KEY `idx_createdTime` (`createdTime`)
) ENGINE=InnoDB AUTO_INCREMENT=10000000 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='无房号金币场房间';
/*金币场房间玩家信息表*/
CREATE TABLE `t_gold_room_user` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `roomId` bigint(20) NOT NULL COMMENT '房间id',
  `userId` varchar(32) NOT NULL COMMENT '玩家ID',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gameResult` int(11) NOT NULL DEFAULT '0' COMMENT '结果',
  `logIds` varchar(512) DEFAULT NULL COMMENT 'logId多个以逗号隔开',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_room` (`roomId`,`userId`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `t_gold_user_firstmyth` (
  `recordDate` int(10) NOT NULL DEFAULT '0' COMMENT '记录日期',
  `userId` bigint(20) NOT NULL DEFAULT '0' COMMENT '玩家ID',
  `userName` varchar(60) NOT NULL DEFAULT '' COMMENT '玩家昵称',
  `record1` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日赢分最高',
  `record2` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日全关别人最多',
  `record3` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日输分最多',
  `record4` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日炸弹最多',
  `record5` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日组局游戏最多',
  `record6` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日飞机最多',
  `record7` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日被全关最多',
  `record8` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日赢分最高',
  `record9` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日自摸最多',
  `record10` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日输分最多',
  `record11` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日放炮最多',
  `record12` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日组局游戏最多',
  `record13` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日最多杠',
  `record14` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日点杠最多',
  `rewardRecord` varchar(255) DEFAULT NULL COMMENT '领奖记录',
  PRIMARY KEY (`recordDate`,`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

CREATE TABLE `roomgold_consume_statistics` (
  `consumeDate` date NOT NULL COMMENT '消耗时期',
  `commonGold` int(11) DEFAULT NULL COMMENT '付费金币数',
  `freeGold` int(11) DEFAULT NULL COMMENT '免费金币数',
  `freeGoldSum` int(11) DEFAULT '0' COMMENT '免费金币剩余总数',
  `commonGoldSum` int(11) DEFAULT '0' COMMENT '收费金币剩余总数',
  PRIMARY KEY (`consumeDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* ------hyz gslogin数据库更新  2017-11-10 代开表*/
ALTER TABLE daikai_table add COLUMN `assisCreateNo` VARCHAR (255) DEFAULT NULL COMMENT '群助手创房编号';
ALTER TABLE daikai_table add COLUMN `assisGroupNo` VARCHAR (255) DEFAULT NULL COMMENT '群助手创房群编号';

/*-----------------------------刘平 2017-11-2 陇南摆叫麻将 玩家牌局返利活动记录 */
CREATE TABLE `activity_game_rebate` (
  `userId` bigint(20) DEFAULT NULL COMMENT '玩家ID',
  `name` varchar(60) DEFAULT NULL COMMENT '昵称',
  `wanfaId` int(11) DEFAULT NULL COMMENT '玩法ID',
  `number` int(11) DEFAULT NULL COMMENT '达标局数',
  `gameTime` datetime DEFAULT NULL COMMENT '游戏时间',
  `payBindId` int(8) DEFAULT '0' COMMENT '邀请码ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*------------------------- gslogin数据库更新 操作*/
CREATE TABLE `t_action` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `actionType` varchar(20) NOT NULL COMMENT '操作类型（0代理操作，1正常消耗）',
  `actionNo` varchar(32) NOT NULL COMMENT '操作序号（代理操作建议存日期，正常消耗建议存房间id加局数）',
  `actionContent` varchar(256) NOT NULL COMMENT '操作内容',
  `beforeContent` varchar(256) DEFAULT NULL COMMENT '操作之前的值',
  `afterContent` varchar(256) DEFAULT NULL COMMENT '操作之后的值',
  `contentType` varchar(20) NOT NULL COMMENT '操作内容 的 类型',
  `userId` varchar(32) NOT NULL COMMENT '被操作的人',
  `actionUser` varchar(32) NOT NULL COMMENT '操作人',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `groupId` int(10) NOT NULL,
  PRIMARY KEY (`keyId`),
  KEY `idx` (`userId`,`contentType`,`actionType`,`createdTime`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*------------------------- gslogin数据库更新*/
ALTER TABLE user_firstmyth add COLUMN `gameType` int(10) NOT NULL DEFAULT 0 COMMENT '游戏类型';
ALTER TABLE user_firstmyth add COLUMN `groupId` bigint(20) NOT NULL DEFAULT 0 COMMENT '军团ID';
ALTER TABLE `user_firstmyth` DROP PRIMARY KEY ,ADD PRIMARY KEY (`recordDate`, `userId`, `gameType`, `groupId`);

/*----hyz gslogin数据库更新*/
ALTER TABLE `room`
MODIFY COLUMN `players`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '加入玩家id';

/*----hyz gslogin数据库更新 2017-12-23 活动接口使用 可不添加*/
CREATE TABLE `activity` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `beginTime` datetime NOT NULL COMMENT '活动开始时间',
  `endTime` datetime NOT NULL COMMENT '活动结束时间',
  `them` varchar(30) DEFAULT NULL COMMENT '活动主题',
  `showContent` text COMMENT '活动详细描述',
  `extend` varchar(255) DEFAULT NULL COMMENT '扩展内容，可以逗号分隔',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/*----lz 登录数据库更新 2018-01-08 t_third_relation现网必须要更新，有军团t_group_table要更新*/
CREATE TABLE `t_third_relation` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT,
  `userId` bigint(19) NOT NULL COMMENT '玩家ID',
  `thirdPf` varchar(32) NOT NULL COMMENT '第三方平台标识',
  `thirdId` varchar(256) NOT NULL DEFAULT '' COMMENT '第三方平台玩家Id',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `checkedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检查时间',
  `currentState` varchar(2) NOT NULL DEFAULT '1' COMMENT '是否有效',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_third` (`thirdId`,`thirdPf`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='第三方关系表';

ALTER TABLE `t_group_table`
ADD COLUMN `userId`  varchar(20) NULL COMMENT '创建人' AFTER `maxCount`,
ADD INDEX `idx_user` (`groupId`, `userId`, `currentState`) ;

/*---hyz 数据库更新 2018-01-15 有军团必须更新*/
ALTER TABLE t_table_user ADD COLUMN `isWinner` smallint(1) DEFAULT '0' COMMENT '是否大赢家';

/*----lz 登录数据库更新 2018-01-15*/
ALTER TABLE `t_table_record` ADD INDEX `idx_time` (`createdTime`, `groupId`) ;

/*----qr 登录数据库更新 2018-01-15彩票点击统计*/
CREATE TABLE `t_lottery_statistics` (
  `recordDate` int(10) NOT NULL DEFAULT '0' COMMENT '记录日期',
  `userId` bigint(20) NOT NULL DEFAULT '0' COMMENT '玩家ID',
  `userName` varchar(60) NOT NULL DEFAULT '' COMMENT '玩家名',
  `record1` int(10) NOT NULL DEFAULT '0' COMMENT '点彩票',
  `record2` int(10) NOT NULL DEFAULT '0' COMMENT '点下载APP',
  `record3` int(10) NOT NULL DEFAULT '0' COMMENT '兑换成功',
  PRIMARY KEY (`recordDate`,`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*---hyz 解散房间表 2018-01-24*/
CREATE TABLE `un_room_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `roomId` bigint(20) NOT NULL DEFAULT '0' COMMENT '房间id',
  `agencyId` bigint(20) DEFAULT '0' COMMENT '代理邀请码',
  `serverId` int(11) DEFAULT '0' COMMENT '服务器id',
  `players` varchar(128) DEFAULT NULL COMMENT '加入玩家id',
  `createTime` datetime DEFAULT NULL COMMENT '解散房间时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/*---lizhou 数据统计表 2018-01-30*/
CREATE TABLE `t_data_statistics` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dataDate` bigint(12) NOT NULL COMMENT '统计日期(最多可按分钟统计)',
  `dataCode` varchar(32) NOT NULL COMMENT '数据标识',
  `userId` varchar(20) NOT NULL COMMENT '玩家ID',
  `gameType` varchar(20) NOT NULL COMMENT '玩法',
  `dataType` varchar(20) NOT NULL COMMENT '数据类别',
  `dataValue` int(10) NOT NULL COMMENT '统计结果',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_unique` (`dataDate`,`dataCode`,`dataType`,`userId`) USING BTREE,
  KEY `idx_common` (`dataCode`,`dataType`,`dataDate`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8
;
/*---qinran 活动领取记录 2018-02-06*/
CREATE TABLE `activity_reward` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activityId` int(10) NOT NULL DEFAULT '0' COMMENT '活动ID',
  `userId` bigint(19) NOT NULL DEFAULT '0' COMMENT '玩家ID',
  `type` int(10) NOT NULL DEFAULT '0' COMMENT '类型 1钻石2现金红包',
  `state` int(10) NOT NULL DEFAULT '0' COMMENT '状态 1已领取',
  `rewardIndex` int(10) NOT NULL DEFAULT '0' COMMENT '奖励',
  `rewardDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `reward` varchar(100) DEFAULT '' COMMENT '奖励内容',
  `rewardNum` int(10) NOT NULL DEFAULT '0' COMMENT '奖励数',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/*---lz 活动领取记录 2018-02-25*/

CREATE TABLE `t_user_extend` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT,
  `userId` varchar(20) NOT NULL COMMENT '用户ID',
  `msgType` smallint(5) NOT NULL COMMENT '信息类别（可用于分区）',
  `msgKey` varchar(32) NOT NULL COMMENT '信息key',
  `msgValue` varchar(256) DEFAULT NULL COMMENT '信息vlue',
  `msgDesc` varchar(256) DEFAULT NULL COMMENT '描述（备用）',
  `msgState` varchar(2) NOT NULL COMMENT '状态（0不可用，1：可用）',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modifiedTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_unique` (`userId`,`msgType`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户扩展信息';

/* hyz 动态展示大厅代理信息*/
CREATE TABLE `agency_show` (
  `keyId` int(11) NOT NULL AUTO_INCREMENT,
  `weixin_name` varchar(255) DEFAULT NULL COMMENT '微信名',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

ALTER TABLE t_group_table ADD COLUMN `playedBureau` int(2) DEFAULT '0' COMMENT '实际打的局数';
ALTER TABLE t_group_table ADD COLUMN `players` varchar(255) DEFAULT NULL COMMENT '玩家的名称';
ALTER TABLE t_group_table ADD COLUMN `overTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '结束时间';

ALTER TABLE `t_gold_user`
ADD COLUMN `regTime`  timestamp NULL DEFAULT NULL COMMENT '注册时间',
ADD COLUMN `lastLoginTime`  timestamp NULL DEFAULT NULL COMMENT '最后登录时间';

/*积分钻石兑换信息表*/
CREATE TABLE `t_item_exchange` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` varchar(20) NOT NULL COMMENT '玩家Id',
  `itemType` varchar(20) NOT NULL COMMENT '商品类别',
  `itemId` varchar(20) NOT NULL COMMENT '商品id',
  `itemName` varchar(64) NOT NULL COMMENT '商品名称',
  `itemAmount` int(10) NOT NULL COMMENT '商品价值',
  `itemCount` int(10) NOT NULL COMMENT '商品数量',
  `itemGive` int(10) NOT NULL COMMENT '商品赠送数量',
  `itemMsg` varchar(1024) NOT NULL COMMENT '商品详细信息',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`keyId`),
  KEY `idx1` (`userId`,`itemType`,`createdTime`),
  KEY `idx2` (`createdTime`,`itemType`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='商品兑换信息表';

/*20180330*/
CREATE TABLE `t_resources_configs` (
  `keyId` int(9) NOT NULL AUTO_INCREMENT,
  `msgType` varchar(32) NOT NULL,
  `msgKey` varchar(64) NOT NULL,
  `msgValue` varchar(256) NOT NULL,
  `msgDesc` varchar(64) NOT NULL,
  `configTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_unique` (`msgType`,`msgKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='配置信息表';
/*201807181102*/
ALTER TABLE `user_inf` ADD COLUMN `preLoginTime`  timestamp NULL DEFAULT NULL COMMENT '上一次登陆时间（与logTime不在同一天）';
/*201807181102 cvs等配置文件*/
CREATE TABLE `t_base_config` (
  `keyId` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `msgType` varchar(32) CHARACTER SET utf8mb4 NOT NULL COMMENT '类型',
  `msgValue` varchar(256) CHARACTER SET utf8mb4 NOT NULL COMMENT '值(json数组)',
  PRIMARY KEY (`keyId`),
  KEY `idx_type` (`msgType`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*201807181102 俱乐部匹配模式*/
CREATE TABLE `t_group_match` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT,
  `groupCode` varchar(32) NOT NULL,
  `userId` varchar(20) NOT NULL,
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_userId` (`userId`),
  KEY `idx_group` (`groupCode`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;


/*201807211100芒果跑得快排行榜配置*/
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'rank_refresh', '1', '排行榜刷新开关', '2018-7-4 12:34:44');
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'rank_limit_num', '50', '排行榜上榜人数', '2018-7-4 12:34:44');

/*201807211100芒果跑得快七天登陆奖励配置*/
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'seven_sign_reward', '500,1000,1500,2000,2500,3000,20_11_70;31_10_29;41_10_1', '七日签到活动奖励配置', '2018-7-5 12:34:44');

/*201807211100小甘瓜分现金红包活动配置*/
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'red_bag_config', '5000,2018-7-11,2018-7-22,2018-7-11,2018-7-26,12:00:00,23:59:59,领取时间12:00:00-23:59:59,1、活动时间7.11-7.22，提现时间11日00:00-26日00:00;2、每日登陆可转动转盘领取红包一个，红包累计金额满5元才能提现;3、每日玩牌4局以上（含4局）可再领取红包一个;', '瓜分红包', '2018-7-5 12:34:44');
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'red_bag_reward_diamond_grade', '1:30,2:60,3:90,4:120,5:160', '瓜分红包', '2018-7-4 12:34:44');
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'red_bag_reward_grades', '0.18,0.28,0.38,0.58,0.68,0.88,1.28,1.58,1.88,2.88,8.88,18.88', '瓜分红包', '2018-7-4 12:34:44');
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'red_bag_reward_grade_login', '1:0.68:1.28,2:0.18:0.38,3:0.18:0.38,4:0.18:0.38,5:0.18:0.38,6:0.18:0.38,7:0.18:0.38', '瓜分红包', '2018-7-4 12:34:44');
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'red_bag_reward_grade_game', '1:0.58:1.28,2:0.28:0.88,3:0.28:0.88,4:0.28:0.88,5:0.28:0.88,6:0.28:0.88,7:0.58:1.28', '瓜分红包', '2018-7-4 12:34:44');

/*201807211100增加客服号配置*/
ALTER TABLE `t_resources_configs` MODIFY COLUMN `msgValue` text NOT NULL;
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'weixin_keFuHao', '陇南客服号_xiaogankefu008;兰州二报_lzmj166;兰州翻金_lzmj166;酒泉客服号_xiaogankefu888;武威客服号_wwmj222;陇西客服号_lxmj020&lxmj021;平凉客服号_plmj066;二报客服号_gsmj088;秦安客服号_qamj066&qamj088;滑水客服号_qymj066;咣咣客服号_mxmj066&mxmj088;客服微信号_xiaogankefu001&xiaogankefu002;张掖客服号_xiaogankefu020&xiaogankefu021;会牌客服号_xiaogankefu030&xiaogankefu031;微信公众号_小甘游戏;', '客服号填写格式：客服号_客服名&客服名;', '2018-07-24 10:30:17');

/*201807251100增加芒果跑得快段位配置*/
ALTER TABLE `t_gold_user`
ADD COLUMN `grade`  int(9) NULL DEFAULT 0 COMMENT '芒果跑得快段位',
ADD COLUMN `gradeExp`  int(9) NULL DEFAULT 0 COMMENT '芒果跑得快段位经验值';

/*201807251100增加芒果跑得快段位配置   游戏后台数据库system_common_info表执行*/
insert into system_common_info values ("mangguoResetJiFenTime", "2018-08-11 00:00:00");
insert into system_common_info values ("isMangGuoJiFenReset", "0");

/*201807251100增加芒果跑得快积分日志*/
CREATE TABLE `t_jifen_recordlog` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` varchar(20) NOT NULL COMMENT '玩家Id',
  `jifen` int(10) NOT NULL COMMENT '获得积分数',
  `sourceType` int(10) NOT NULL COMMENT '获得积分来源',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;

/*201807251100增加芒果跑得快渠道授权表*/
CREATE TABLE `mangguo_authorization` (
  `unionId` varchar(255) NOT NULL DEFAULT '' COMMENT 'unionId',
  `pf` varchar(255) DEFAULT NULL COMMENT '芒果渠道名称',
  `createTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`unionId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*201807261100 玩家用户表增加channel字段（渠道来源 芒果跑得快）*/
ALTER TABLE `user_inf`
ADD COLUMN `channel` varchar(255) NULL COMMENT '渠道来源 芒果跑得快';

/*201808041100 增加现金红包记录*/
CREATE TABLE `redbag_info` (
  `userId` bigint(20) NOT NULL COMMENT '玩家ID',
  `redBagType` int(11) DEFAULT 0 COMMENT '红包类型 1钻石 2现金红包',
  `redbag` float(11,2) DEFAULT '0.00' COMMENT '领取的红包金额',
  `receiveDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取红包时间',
  `drawDate` timestamp NULL DEFAULT NULL COMMENT '提现时间',
  `sourceType` int(11) DEFAULT NULL COMMENT '红包来源',
  `sourceTypeName` varchar(60) NOT NULL COMMENT '红包来源名',
  PRIMARY KEY (`userId`,`redBagType`,`receiveDate`),
  KEY `user_date` (`userId`,`redBagType`,`receiveDate`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='现金红包记录';

/*201808041100 增加小甘瓜分现金红包记录*/
CREATE TABLE `activity_redbag` (
  `userId` bigint(20) NOT NULL COMMENT '玩家ID',
  `receiveDate` varchar(20) NOT NULL COMMENT '领取红包时间 yyyy-mm-dd',
  `gameNum` int(11) DEFAULT NULL COMMENT '今日玩牌局数',
  `receiveNum` int(11) DEFAULT NULL COMMENT '当天已领取红包次数',
  `receiveRecords` text COMMENT '玩家当天红包领取记录',
  `loginRedBag` float(11,2) DEFAULT '0.00' COMMENT '登陆红包',
  `gameRedBag` float(11,2) DEFAULT '0.00' COMMENT '打牌红包',
  `lastReceiveTime` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`userId`,`receiveDate`),
  KEY `user_date` (`userId`,`receiveDate`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='瓜分现金红包活动记录(玩家数据)  KEY==>玩家ID_领取红包时间';

CREATE TABLE `t_redbag_system` (
  `createdTime` datetime DEFAULT NULL COMMENT '创建奖池时间',
  `dayPoolNum` float(11,2) DEFAULT NULL COMMENT '现金红包奖池(每日凌晨0点重置)',
  `receiveRecords` text COMMENT '现金红包领取记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='瓜分现金红包活动系统数据';

/**比赛场**/
CREATE TABLE `t_match` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `serverId` int(9) NOT NULL COMMENT '服ID',
  `tableMsg` varchar(256) NOT NULL COMMENT '创房参数',
  `matchType` varchar(32) NOT NULL COMMENT '类别',
  `tableCount` int(3) NOT NULL COMMENT '房间人数',
  `matchProperty` varchar(20) NOT NULL COMMENT '1：人满即开，时间：定时开',
  `matchRule` varchar(128) NOT NULL COMMENT '竞技规则：（eg：36_30;1_1_21,2_3_12,3_3_6,4_3_3,5_3_1）',
  `currentCount` int(5) NOT NULL COMMENT '当前人数',
  `minCount` int(5) NOT NULL COMMENT '最少人数',
  `maxCount` int(5) NOT NULL COMMENT '最大人数',
  `currentState` varchar(8) NOT NULL COMMENT '当前状态（0，未开始，1_*正在进行第几场，2已结束，3未开局过期结束）',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `finishedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '完成时间',
  `matchPay` varchar(32) NOT NULL DEFAULT '0' COMMENT '报名费',
  `matchName` varchar(64) DEFAULT NULL COMMENT '比赛场名称',
  `matchDesc` varchar(256) DEFAULT NULL COMMENT '描述信息',
  `restTable` int(4) NOT NULL DEFAULT '0' COMMENT '剩下的桌数',
  `matchExt` varchar(1024) DEFAULT NULL COMMENT '扩展信息',
  `startTime` bigint(19) NOT NULL DEFAULT '0' COMMENT '比赛开始时间',
  PRIMARY KEY (`keyId`),
  KEY `idx_common` (`matchType`,`currentState`),
  KEY `idx_server` (`serverId`,`currentState`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `t_match_user` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `matchId` bigint(19) NOT NULL COMMENT '比赛场Id',
  `matchType` varchar(32) NOT NULL COMMENT '类别',
  `userId` varchar(20) NOT NULL COMMENT '玩家Id',
  `currentState` varchar(8) NOT NULL COMMENT '当前状态(0准备、1开局、2结束、3被解散、4出局)',
  `currentNo` int(3) NOT NULL DEFAULT '0' COMMENT '当前局数',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifiedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `currentScore` int(10) NOT NULL DEFAULT '0' COMMENT '分数',
  `userRank` int(4) NOT NULL DEFAULT '0' COMMENT '排名',
  `userAward` varchar(256) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '奖励信息',
  `awardState` varchar(8) NOT NULL DEFAULT '0' COMMENT '奖励领取状态（0无奖励，1未领取，2已领取）',
  `reliveCount` int(2) DEFAULT '0' COMMENT '复活次数',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_unique` (`matchId`,`userId`),
  KEY `idx_userId` (`userId`,`currentState`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/**玩家房卡获取/消耗日志记录**/
CREATE TABLE `user_card_record` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(20) NOT NULL COMMENT '玩家ID',
  `freeCard` int(11) NOT NULL COMMENT '玩家当前免费房卡数',
  `cards` int(11) NOT NULL COMMENT '玩家当前房卡数',
  `addFreeCard` int(11) NOT NULL COMMENT '玩家本次操作(消耗/获得)免费房卡数',
  `addCard` int(11) NOT NULL COMMENT '玩家本次操作(消耗/获得)免费房卡数',
  `recordType` int(1) NOT NULL COMMENT '操作类型(1消耗  0获得)',
  `playType` int(11) NOT NULL COMMENT '操作所属玩法ID 0表示不属于玩法类操作',
  `sourceType` int(11) NOT NULL DEFAULT '0' COMMENT '操作来源',
  `sourceName` varchar(100) DEFAULT NULL COMMENT '操作来源名',
  `createTime` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`userId`,`createTime`),
  KEY `idx_date` (`createTime`,`sourceType`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/**海外百人玩法**/
ALTER TABLE table_inf MODIFY COLUMN `players` text DEFAULT NULL COMMENT '玩家信息';

/**海外龙虎斗玩家日志记录**/
CREATE TABLE `t_lhd_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `userId` bigint(20) NOT NULL COMMENT '玩家ID',
  `betInfo` text COMMENT '下注信息',
  `betGold` int(11) NOT NULL COMMENT '下注金额',
  `result` tinyint(3) NOT NULL COMMENT '对局结果 1龙 2虎 3和',
  `winGold` int(11) NOT NULL DEFAULT '0' COMMENT '输赢金币数',
  `createTime` datetime DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1185 DEFAULT CHARSET=utf8;

/**user_inf 增加非金币场房间总局数统计**/
ALTER TABLE `user_inf`
ADD COLUMN `totalBureau` bigint(20) NULL DEFAULT 0 COMMENT '非金币场房间总局数';
update `user_inf` set `totalBureau` = `totalCount`;

/** 信用分**/
ALTER TABLE `t_group_user`
ADD COLUMN `credit` int(11) NULL DEFAULT 0 COMMENT '玩家信用分值' AFTER `userGroup`;

ALTER TABLE `t_group_table`
ADD COLUMN `type` int(11) NULL DEFAULT 1 COMMENT '房间类型：1：普通房，2：信用房' AFTER `tableId`;

ALTER TABLE `user_playlog`
ADD COLUMN `type` int(11) NULL DEFAULT 1 COMMENT '房间类型：1：普通房，2：信用房' AFTER `maxPlayerCount`;

CREATE TABLE `t_group_credit_log` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(11) DEFAULT '0' COMMENT '用户id',
  `optUserId` bigint(11) DEFAULT '0' COMMENT '操作员id：默认为0',
  `tableId` bigint(20) DEFAULT '0' COMMENT '牌桌id',
  `credit` int(11) DEFAULT '0' COMMENT '信用分值',
  `curCredit` int(11) DEFAULT NULL COMMENT '操作后信用分',
  `type` int(11) DEFAULT NULL COMMENT '类型：1：管理加减分，2：佣金，3：牌局',
  `flag` int(11) DEFAULT '0' COMMENT '是否有效：0否，1是',
  `createdTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`keyId`),
  KEY `groupId_credit` (`groupId`,`credit`,`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

ALTER TABLE `t_table_user`
ADD COLUMN `winLoseCredit` int(10) NULL DEFAULT 0 COMMENT '胜负信用分' AFTER `playResult`,
ADD COLUMN `commissionCredit` int(10) NULL DEFAULT 0 COMMENT '信用分佣金' AFTER `winLoseCredit`;

ALTER TABLE `user_inf` add COLUMN `photo` VARCHAR(255) NULL COMMENT '玩家个人相册';

-- 打筒子快乐四喜玩法
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay1_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay3_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay0_600', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay0_1000', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay0_1000', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay0_1000', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay1_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay3_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay0_600', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay0_1000', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay0_1000', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay0_1000', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay1_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay3_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay0_600', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay0_1000', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay0_1000', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay0_1000', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');

-- 信用分佣金分成模式
ALTER TABLE `t_group_relation`
ADD COLUMN `creditCommissionRate` int(11) NULL DEFAULT 0 COMMENT '信用分佣金分成模式,数据范围(0-10):1代表分成比例为小组长10%,群主90%,' AFTER `teamGroup`;

INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'admin_invite_by_id', '1', '管理员通过id邀请玩家进群');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'admin_diss_all_table', '1', '管理员解散所有房间');


-- 是否是打筒子app
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'isDtzApp', '1', '是否是打筒子app');
-- 新活动
INSERT INTO `t_base_config`(`msgType`, `msgValue`) VALUES ('ActivityConfigConfig', '["24","呼朋唤友","1","1","-1","1","2018-11-27 00:00:00","2038-10-3 23:59:59","2_4_6","8_10_2018-11-29 23:59:59","","17","1"]');
INSERT INTO `t_base_config`(`msgType`, `msgValue`) VALUES ('ActivityConfigConfig', '["25","老玩家回归","1","1","-1","2","2018-11-27 00:00:00","2038-10-3 23:59:59","15;10;7;20","1_1_10;2_1_20;3_1_30;4_1_30;5_1_30;6_1_30;7_2_0|1_2_1.88_200;2_2_2.88_200;3_2_3.88_200;4_2_5.88_150;5_2_6.88_100;6_2_7.88_100;7_2_8.88_50","","25","1"]');
INSERT INTO `t_base_config`(`msgType`, `msgValue`) VALUES ('ActivityConfigConfig', '["26","新人礼包","1","1","-1","3","2018-11-27 00:00:00","2038-10-3 23:59:59","1;2|2;1|3;3|4;1|5;1&1;10|2;10|3;20|4;20|5;20&7","20_1_20;50_1_30;100_1_50;160_2_10","","26","1"]');



-- 20181123 小组日志
ALTER TABLE `t_group_credit_log`
ADD COLUMN `userGroup` int(11) NULL DEFAULT -1 COMMENT '小组id' AFTER `flag`;
ALTER TABLE `t_table_user`
ADD COLUMN `userGroup` int(11) NULL DEFAULT -1 COMMENT '小组id' AFTER `isWinner`;

ALTER TABLE `t_group_credit_log`
ADD COLUMN `mode` int(11) NULL DEFAULT 0 COMMENT '是否正向数据:optUserId主动操作userId为正向' AFTER `userGroup`;
--分成比例由原在的1表示10%变为10表示10%,现在存的是实际百分比
update t_group_relation set creditCommissionRate = creditCommissionRate*10 where creditCommissionRate > 0;


-- 新增分成模式字段，1为分成模式A，2为分成模式B，3为分成模式C
ALTER TABLE `t_group`
ADD COLUMN `leaderSharingModel`  int(3) NOT NULL DEFAULT 1 COMMENT '分成模式，1为分成模式A，2为分成模式B，3为分成模式C' AFTER `groupMode`;
ALTER TABLE `t_group`
ADD COLUMN `groupExtConfig`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '赢家的分成模式以及比例,json格式' AFTER `leaderSharingModel`;
ALTER TABLE `t_group_user`
ADD COLUMN `creditPurse`  int(11) NULL DEFAULT 0 COMMENT '零钱包' AFTER `credit`;
ALTER TABLE `t_group_user`
ADD COLUMN `creditTotal`  int(11) NULL DEFAULT 0 COMMENT '总收入积分' AFTER `creditPurse`;

--俱乐部房间 添加表示房间名的字段 tableName
ALTER TABLE `t_group_table` ADD COLUMN `tableName` varchar(255) DEFAULT '' COMMENT '房间名' AFTER `tableId`;

-- 安化俱乐部多玩法 实时局数统计
ALTER TABLE `t_group_table`
ADD COLUMN `dealCount`  int(11) NULL DEFAULT 0 COMMENT '发牌次数' AFTER `playedBureau`;
-- 安化牌桌局数同步  注意 注意 注意  更新这个字段时 由于overTime会根据时间搓更新 也会更新到当前时间 会影响战绩的正常显示
update t_group_table set dealCount = playedBureau where currentState in("0","1");

-- 打筒子俱乐部优化
INSERT INTO t_resources_configs ( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'group_hall_all_tables', '1', '俱乐部大厅显示所有桌子');

-- 分享名片功能
CREATE TABLE `user_group_playlog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tableid` int(20) DEFAULT NULL COMMENT '房号',
  `userid` int(20) DEFAULT NULL COMMENT '创建人ID',
  `groupid` int(20) DEFAULT NULL COMMENT '所属亲友圈',
  `players` varchar(255) DEFAULT NULL COMMENT '玩家id',
  `count` int(20) DEFAULT NULL COMMENT '总局数',
  `score` varchar(255) DEFAULT NULL COMMENT '得分',
  `creattime` varchar(20) DEFAULT NULL COMMENT '创建房时间',
  `overtime` varchar(255) DEFAULT NULL COMMENT '结束时间',
  `playercount` int(11) DEFAULT NULL,
  `gamename` varchar(25) DEFAULT NULL COMMENT '游戏名',
  `totalCount` int(20) DEFAULT '0' COMMENT '游戏总局数',
  `diFenScore` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '算完底分的分',
  `diFen` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '底分',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8;

-- 打筒子智能补房配置，不需要修改的请不要配置
INSERT INTO t_resources_configs ( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'autoCreateTableCountLimit', '2', '俱乐部牌桌少于xx个时智能补房');
INSERT INTO t_resources_configs ( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'autoCreateTableCount', '2', '俱乐部智能补xx个牌桌');

-- 跑得快托管限制
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'autoTimeOutPdkNormal2', '20000', '跑得快普通场托管限制后倒计时时间20秒');

-- 快乐跑胡子增加半边天炸玩法 2人8局和12局
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count12_player2_pay0', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count12_player2_pay1', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count12_player2_pay3', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count8_player2_pay0', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count8_player2_pay1', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count8_player2_pay3', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');


--20190305 大联盟升级
ALTER TABLE t_group_user
ADD COLUMN `promoterLevel` int(5) NOT NULL DEFAULT '0' COMMENT '拉手级别：小组长下一级为1，依次往下，最下级成员为5',
ADD COLUMN `promoterName` varchar(100) NOT NULL DEFAULT '' COMMENT '拉手组名字',
ADD COLUMN `promoterId1` bigint(20) NOT NULL DEFAULT '0' COMMENT '一级推广员id',
ADD COLUMN `promoterId2` bigint(20) NOT NULL DEFAULT '0' COMMENT '二级推广员id',
ADD COLUMN `promoterId3` bigint(20) NOT NULL DEFAULT '0' COMMENT '三级推广员id',
ADD COLUMN `promoterId4` bigint(20) NOT NULL DEFAULT '0' COMMENT '四级推广员id';

ALTER TABLE `t_group`
ADD COLUMN `creditAllotMode` tinyint(1) NULL DEFAULT 1 COMMENT '分成模式：1大赢家分成，2参与分成' AFTER `isCredit`;

ALTER TABLE t_group_credit_log
ADD COLUMN `promoterId1` bigint(20) DEFAULT '0' COMMENT '一级拉手id' AFTER `userGroup`,
ADD COLUMN `promoterId2` bigint(20) DEFAULT '0' COMMENT '二级拉手id' AFTER `promoterId1`,
ADD COLUMN `promoterId3` bigint(20) DEFAULT '0' COMMENT '三级拉手id' AFTER `promoterId2`,
ADD COLUMN `promoterId4` bigint(20) DEFAULT '0' COMMENT '四级拉手id' AFTER `promoterId3`;

ALTER TABLE `t_group_table_config`
ADD COLUMN `creditMsg` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '信用房间配置' AFTER `createdTime`;

CREATE TABLE `t_group_credit_config` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL DEFAULT '0' COMMENT '俱乐部id',
  `preUserId` bigint(20) DEFAULT NULL COMMENT '上级玩家id',
  `userId` bigint(20) NOT NULL DEFAULT '0' COMMENT '玩家id',
  `configId` bigint(20) NOT NULL DEFAULT '0' COMMENT 't_group_table_config.id',
  `credit` int(11) NOT NULL DEFAULT '0' COMMENT 'credit值：固定数值，或百分比比例',
  `maxCreditLog` int(11) DEFAULT '0' COMMENT '记录修改时允许最大分值',
  `createdTime` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`) USING BTREE,
  KEY `groupId_index` (`groupId`) USING BTREE,
  KEY `preUserId_index` (`preUserId`) USING BTREE,
  KEY `userId_index` (`userId`) USING BTREE,
  KEY `configId_index` (`configId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--小组下成员为1级
update t_group_user set promoterLevel=1 where userRole = 2 and userGroup !='0';

-- 20190312大联盟升级
ALTER TABLE `t_group_user`
ADD COLUMN `creditCommissionRate` int(10) NOT NULL DEFAULT 0 COMMENT '上级给自己的赠送分分成比例，百分制整数' AFTER `promoterId4`;


ALTER TABLE `t_group_table`
ADD COLUMN `creditMsg` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '信用房间配置' AFTER `players`;

-- 20190320红中麻将的玩法扣钻信息
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player2_pay0', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player2_pay1', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player2_pay3', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player2_pay0', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player2_pay1', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player2_pay3', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player2_pay0', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player2_pay1', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player2_pay3', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player3_pay0', '14', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player3_pay1', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player3_pay3', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player3_pay0', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player3_pay1', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player3_pay3', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player3_pay0', '27', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player3_pay1', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player3_pay3', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player4_pay0', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player4_pay1', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player4_pay3', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player4_pay0', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player4_pay1', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player4_pay3', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player4_pay0', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player4_pay1', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player4_pay3', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');

-- 20190403欢乐金币场分享活动
ALTER TABLE `user_share`
ADD COLUMN `type` int(1) NULL DEFAULT 1 COMMENT '分享类型：1：普通分享（游戏分享），2：金币场分享' AFTER `userId`;

-- 20190410手机绑定及登录相关
alter table user_inf add  phonePw varchar(60)  comment '手机登录密码';
alter table user_inf add  phoneNum varchar(11) default null comment '手机号';
alter table user_inf add unique phone_unique (`phoneNum`);

CREATE TABLE `user_msg_verify`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NULL DEFAULT NULL,
  `verifyCode` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '短信验证码',
  `sendTime` datetime(0) NULL DEFAULT NULL COMMENT '发送时间',
  `phoneNum` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '\r\n验证手机号',
  `isUse` int(1) NULL DEFAULT 0 COMMENT '是否已经被使用过（验证成功，才会改变其状态值）',
  `ip` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'ip地址',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ip_unique`(`ip`) USING BTREE,
  UNIQUE INDEX `userId_unique`(`userId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用于记录短信验证相关信息' ROW_FORMAT = Dynamic;



-- 20190413亲友圈增加公告内容字段
 alter table  `t_group` add  `content` varchar(512) DEFAULT '' COMMENT '公告内容';


-- 20190419 安化亲友圈增加拒绝游戏邀请字段
ALTER TABLE t_group_user ADD COLUMN `refuseInvite` INT(2) DEFAULT '1' COMMENT '是否拒绝亲友圈游戏邀请: 0拒绝 1允许';

-- 2019/04/19安化跑得快游戏配置切牌倒计时配置
INSERT INTO t_resource_config(msgType, msgKey, msgValue, msgDesc) VALUES('ServerConfig','pdkQp_timeout',15000,'跑得快切牌倒计时');

-- 20190424 只仍白金岛项目需要创建
CREATE TABLE `bjd_data_statistics` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dataDate` bigint(12) NOT NULL COMMENT '统计日期(最多可按分钟统计)',
  `dataCode` varchar(32) NOT NULL COMMENT '数据标识',
  `userId` varchar(20) NOT NULL COMMENT '玩家ID',
  `gameType` varchar(20) NOT NULL COMMENT '玩法',
  `dataType` varchar(20) NOT NULL COMMENT '数据类别',
  `dataValue` int(10) NOT NULL COMMENT '统计结果',
  PRIMARY KEY (`keyId`) USING BTREE,
  UNIQUE KEY `idx_unique` (`dataDate`,`dataCode`,`dataType`,`userId`) USING BTREE,
  KEY `idx_common` (`dataCode`,`dataType`,`dataDate`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- 20190424 只仍白金岛项目需要创建
CREATE TABLE `bjd_group_newer_bind` (
  `userId` bigint(11) NOT NULL,
  `groupId` bigint(11) DEFAULT NULL,
  PRIMARY KEY (`userId`),
  KEY `idx_uid` (`userId`) USING BTREE,
  KEY `idx_gid` (`groupId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--20190425 login更新，短信验证签名配置信息
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'smsConfig', '联盛科技', '短信验证配置：签名模板');


--20190506 设置小组分成人数下限
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'groupIncomeNum', '2', '小组参与分成最低人数');

-- 20190508 信用分日志增加包间名字
ALTER TABLE `t_group_credit_log`
ADD COLUMN `roomName` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '包间名字' AFTER `createdTime`;

-- 20190709 战绩记录表增加通用信息字段
ALTER TABLE `user_playlog`
ADD COLUMN `generalExt` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT '通用信息：json格式' AFTER `type`;

-- 修改现有玩法配置的名字为包间名字
update t_group_table_config c , t_group g set c.tableName=g.groupName where c.groupId=g.groupId and c.parentGroup=g.parentGroup;

-- 俱乐部玩法配置列表接口数据精简,目前打筒子用
INSERT INTO `t_resources_configs`(msgType`, `msgKey`, `msgValue`, `msgDesc``) VALUES ('ServerConfig', 'groupTableConfigListSimplify', '1', '俱乐部玩法配置列表接口数据精简');

ALTER TABLE `t_group`
ADD COLUMN `switchInvite` tinyint(1) NULL DEFAULT 1 COMMENT '亲友圈邀请人进群是否需要对方同意：0不需要，1需要' AFTER `content`;

-- 20191104
ALTER TABLE `user_inf`
ADD COLUMN `playCount1` int(11) NULL DEFAULT 0 COMMENT '玩家进入游戏后玩的总大局数（包含金币场）' AFTER `playState`;

ALTER TABLE `user_card_consume`
ADD COLUMN `playType` int(11) NULL COMMENT '玩法' AFTER `consumeGold`;


-- 20191123 活动奖励记录
CREATE TABLE `log_activity_reward` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) DEFAULT NULL,
  `activityType` int(11) DEFAULT NULL,
  `rewardType` int(11) DEFAULT NULL,
  `rewardValue` int(11) DEFAULT NULL,
  `createdTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`keyId`),
  KEY `idx_uid_type_time` (`userId`,`activityType`,`createdTime`) USING BTREE,
  KEY `idx_type_time` (`activityType`,`createdTime`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='活动奖励表';