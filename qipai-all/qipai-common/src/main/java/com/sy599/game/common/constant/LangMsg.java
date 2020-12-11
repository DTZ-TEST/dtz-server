package com.sy599.game.common.constant;

import com.sy599.game.db.dao.BaseConfigDao;
import com.sy599.game.db.dao.TableCheckDao;
import com.sy599.game.db.enums.DbEnum;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.helper.ResourceHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息提示
 */
public final class LangMsg extends ResourceHandler {
    public static final String code_cards_err = "code_cards_err";//房卡不足
    public static final String code_diamond_err = "code_diamond_err";//钻石不足
    public static final String code_0 = "code_0";//创建房间失败
    public static final String code_1 = "code_1";//没有找到该房间:{0}
    public static final String code_2 = "code_2";//请选择恢复房间[{0}]
    public static final String code_3 = "code_3";//参数错误
    public static final String code_4 = "code_4";//已加入了该房间
    public static final String code_5 = "code_5";//房间人数已满
    public static final String code_6 = "code_6";//人数未满
    public static final String code_7 = "code_7";//成员离线，无法出牌
    public static final String code_8 = "code_8";//该房间已被解散:[{0}]
    public static final String code_9 = "code_9";//该房间已进行到第[{0}]局，不能退出
    public static final String code_10 = "code_10";//牌局已开始，该房间不能退出
    public static final String code_11 = "code_11";//房主不能退出
    public static final String code_12 = "code_12";//跑得快没有找到该房间
    public static final String code_13 = "code_13";//麻将没有找到该房间
    public static final String code_14 = "code_14";//操作过于频繁
    public static final String code_15 = "code_15";//检查玩家操作失败，请重试{0}
    public static final String code_16 = "code_16";//操作失败
    public static final String code_17 = "code_17";//开房第一局出的牌必须包含黑桃三
    public static final String code_18 = "code_18";//局数错误{0}
    public static final String code_19 = "code_19";//比赛场不能解散
    public static final String code_20 = "code_20";//比赛场不能退出
    public static final String code_21 = "code_21";//牌局已开始，该房间不能加入
    public static final String code_22 = "code_22";//还有玩家未准备
    public static final String code_23 = "code_23";//杠后摸王不能过
    public static final String code_24 = "code_24";//杠牌失败！该牌杠后无法听牌！
    public static final String code_25 = "code_25";//比赛场不能创建房间
    public static final String code_26 = "code_26";//您当前不是托管状态，无需取消托管
    public static final String code_27 = "code_27";//您当前处于托管状态，无需设置托管
    public static final String code_28 = "code_28";//至少6人才能开始游戏
    public static final String code_29 = "code_29";//等待其他玩家操作
    public static final String code_30 = "code_30";//恭喜：[{0}]人品爆发，抽中[{1}]大奖！
    public static final String code_31 = "code_31";//服务器正在维护，10-30秒后恢复
    public static final String code_32 = "code_32";//没有找到正在打牌的房间
    public static final String code_33 = "code_33";//没有找到该玩家:{0}
    public static final String code_34 = "code_34";//该房间已经开始游戏，无法中途加入
    public static final String code_35 = "code_35";//第一轮必须出红桃4
    public static final String code_36 = "code_36";//还没有轮到您出牌
    public static final String code_37 = "code_37";//不能出
    public static final String code_38 = "code_38";//要不起
    public static final String code_39 = "code_39";//已经签过到了
    public static final String code_40 = "code_40";//已无牌可换
    public static final String code_41 = "code_41";//必须4人准备才能开始游戏
    public static final String code_42 = "code_42";//[{0}]房间存在玩家，不能解散
    public static final String code_43 = "code_43";//您无权解散房间
    public static final String code_44 = "code_44";//最后一局正在进行中，不许中途加入
    public static final String code_45 = "code_45";//道具发送间隔时间不能小于3秒
    public static final String code_46 = "code_46";//该房间不允许三带一
    public static final String code_47 = "code_47";//该房间不允许三带二
    public static final String code_48 = "code_48";//该牌局模式不存在
    public static final String code_49 = "code_49";//您不是该俱乐部成员，无法加入
    public static final String code_50 = "code_50";//群主钻石不足，无法创建房间
    public static final String code_51 = "code_51";//至少4人准备才能开始游戏
    public static final String code_52 = "code_52";//非俱乐部成员不能选择该模式
    public static final String code_53 = "code_53";//非俱乐部成员不能加入该房间
    public static final String code_54 = "code_54";//非群成员或者非指定群成员不能加入房间
    public static final String code_57 = "code_57";//房间已被{0}解散
    public static final String code_58 = "code_58";//您的客户端不支持该玩法
    public static final String code_59 = "code_59";//解散失败，请稍后再试!
    public static final String code_60 = "code_60";//该房间已被群主解散:[{0}]
    public static final String code_61 = "code_61";//至少4人才能开始游戏
    public static final String code_62 = "code_62";//您不是俱乐部成员无法创房！
    public static final String code_63 = "code_63";//您不是{0}俱乐部成员
    public static final String code_64 = "code_64";//信用分不够，需要{0}，您当前只有{1}
    public static final String code_65 = "code_65";//由于房间内玩家{0}的信用分低于最低要求分数{1}，房间自动解散！
    public static final String code_66 = "code_66";//由于长时间未准备，被踢出
    public static final String code_67 = "code_67";//需完成一局牌局才可领取
    public static final String code_68 = "code_68";//已经暂停开房，请联系群主或管理员
    public static final String code_200 = "code_200";//创建房间失败，未开启GPS
    public static final String code_201 = "code_201";//加入房间失败，未开启GPS
    public static final String code_202 = "code_202";//加入房间失败，距离过近!
    public static final String code_203 = "code_203";//精彩活动已过期！
    public static final String code_204 = "code_204";//未开局的游戏，暂不能解散
    public static final String code_205 = "code_205";//请先加入军团
    public static final String code_206 = "code_206";//消息太长
    public static final String code_207 = "code_207";//消息为空
    public static final String code_208 = "code_208";//未定义的操作指令
    public static final String code_209 = "code_209";//还未达成条件，暂不能领取！
    public static final String code_210 = "code_210";//还有玩家未准备，请稍后切牌
    public static final String code_211 = "code_211";//最多只能代开10个房间
    public static final String code_212 = "code_212";//您在别的游戏有未解散的房间！
    public static final String code_213 = "code_213";//商品不存在或已下架
    public static final String code_214 = "code_214";//商品项不存在
    public static final String code_215 = "code_215";//积分不足
    public static final String code_216 = "code_216";//兑换失败！兑换后剩余积分不能低于{0}
    public static final String code_217 = "code_217";//房间已解散
    public static final String code_218 = "code_218";//牌局未开始
    public static final String code_219 = "code_219";//您已领取过奖励！
    public static final String code_220 = "code_220";//条件不满足，领取失败！
    public static final String code_221 = "code_221";//该玩家正在打比赛场
    public static final String code_222 = "code_222";//应进入:{0}房间:{1}-{2}-{3}
    public static final String code_223 = "code_223";//您已经登陆游戏啦
    public static final String code_224 = "code_224";//没有抽奖机会了，继续玩游戏赚取抽奖机会！
    public static final String code_225 = "code_225";//请稍后再试
    public static final String code_226 = "code_226";//您已报名参赛！
    public static final String code_227 = "code_227";//请先完成您正在进行的牌局再来报名
    public static final String code_228 = "code_228";//太火爆了，人数已满，请参加下一场
    public static final String code_229 = "code_229";//请刷新再试
    public static final String code_230 = "code_230";//比赛已开始，暂时不能退出哦
    public static final String code_231 = "code_231";//您暂时没有可领取的钻石
    public static final String code_232 = "code_232";//你的IP为空，不能解散
    public static final String code_233 = "code_233";//该房间不存在，解散失败
    public static final String code_234 = "code_234";//当前牌局不存在相同IP玩家，解散失败
    public static final String code_235 = "code_235";//该房间有相同IP，解散房间成功
    public static final String code_236 = "code_236";//当前牌局不存在相同IP玩家，解散失败
    public static final String code_237 = "code_237";//您今日砸蛋次数已用完！
    public static final String code_238 = "code_238";//您没有砸蛋次数了！
    public static final String code_239 = "code_239";//没有找到该玩法:{0}
    public static final String code_240 = "code_240";//您正在比赛场中
    public static final String code_241 = "code_241";//未设置玩法，请联系群主
    public static final String code_242 = "code_242";//俱乐部不存在或已解散！
    public static final String code_243 = "code_243";//禁止成员开房，请联系群主或管理员
    public static final String code_244 = "code_244";//群主已开启群主支付
    public static final String code_245 = "code_245";//钻石不足！智能补房失败！
    public static final String code_246 = "code_246";//群主未开启群主支付
    public static final String code_247 = "code_247";//您已经拥有一个房间了！
    public static final String code_248 = "code_248";//已达到创房上限！
    public static final String code_249 = "code_249";//您已被踢下线
    public static final String code_250 = "code_250";//您超过{0}天未登录游戏，欢迎回归，获得{1}钻石！
    public static final String code_251 = "code_251";//创建失败，请重试！
    public static final String code_252 = "code_252";//您当前已下注噢，请稍后退出
    public static final String code_401 = "code_401";//已签过，不能重复签到
    public static final String code_402 = "code_402";//日期还未到，等待下次签到
    public static final String code_403 = "code_403";//金币低于{}，不能补签
    public static final String code_404 = "code_404";//累计奖励已经领完
    public static final String code_405 = "code_405";//连胜奖励已经领完
    public static final String code_406 = "code_406";//您还没有完成邀请，无法领取
    public static final String code_407 = "code_407";//今天补助已用完
    public static final String code_901 = "code_901";//金币场暂未开放
    public static final String code_902 = "code_902";//补助金暂未开放
    public static final String code_903 = "code_903";//金币不足
    public static final String code_904 = "code_904";//不能进入{0}
    public static final String code_905 = "code_905";//今日领取补助金已达上限
    public static final String code_906 = "code_906";//金币场聊天室暂未开放
    public static final String code_907 = "code_907";//您的金币太多了，请前往更高级的场次
    public static final String code_908 = "code_908";//该{0}不合法哦！请另取高名！
    public static final String code_909 = "code_909";//{0}暂未开放！
    public static final String code_910 = "code_910";//50元余额以上才可下注，请您先充值噢！
    public static final String other_err_78 = "other_err_78";//试炼NPC

    /**
     * 提示消息缓存
     */
    private static Map<String, String> langMap = new ConcurrentHashMap<>();

    static {

        langMap.put("code_cards_err","房卡不足");
        langMap.put("code_diamond_err","钻石不足");
        langMap.put("code_0","创建房间失败");
        langMap.put("code_1","没有找到该房间:{0}");
        langMap.put("code_2","请选择恢复房间[{0}]");
        langMap.put("code_3","参数错误");
        langMap.put("code_4","已加入了该房间");
        langMap.put("code_5","房间人数已满");
        langMap.put("code_6","人数未满");
        langMap.put("code_7","成员离线，无法出牌");
        langMap.put("code_8","该房间已被解散:[{0}]");
        langMap.put("code_9","该房间已进行到第[{0}]局，不能退出");
        langMap.put("code_10","牌局已开始，该房间不能退出");
        langMap.put("code_11","房主不能退出");
        langMap.put("code_12","跑得快没有找到该房间");
        langMap.put("code_13","麻将没有找到该房间");
        langMap.put("code_14","操作过于频繁");
        langMap.put("code_15","检查玩家操作失败，请重试{0}");
        langMap.put("code_16","操作失败");
        langMap.put("code_17","开房第一局出的牌必须包含黑桃三");
        langMap.put("code_18","局数错误{0}");
        langMap.put("code_19","比赛场不能解散");
        langMap.put("code_20","比赛场不能退出");
        langMap.put("code_21","牌局已开始，该房间不能加入");
        langMap.put("code_22","还有玩家未准备");
        langMap.put("code_23","杠后摸王不能过");
        langMap.put("code_24","杠牌失败！该牌杠后无法听牌！");
        langMap.put("code_25","比赛场不能创建房间");
        langMap.put("code_26","您当前不是托管状态，无需取消托管");
        langMap.put("code_27","您当前处于托管状态，无需设置托管");
        langMap.put("code_28","至少6人才能开始游戏");
        langMap.put("code_29","等待其他玩家操作");
        langMap.put("code_30","恭喜：[{0}]人品爆发，抽中[{1}]大奖！");
        langMap.put("code_31","服务器正在维护，10-30秒后恢复");
        langMap.put("code_32","没有找到正在打牌的房间");
        langMap.put("code_33","没有找到该玩家:{0}");
        langMap.put("code_34","该房间已经开始游戏，无法中途加入");
        langMap.put("code_35","第一轮必须出红桃4");
        langMap.put("code_36","还没有轮到您出牌");
        langMap.put("code_37","不能出");
        langMap.put("code_38","要不起");
        langMap.put("code_39","已经签过到了");
        langMap.put("code_40","已无牌可换");
        langMap.put("code_41","必须4人准备才能开始游戏");
        langMap.put("code_42","[{0}]房间存在玩家，不能解散");
        langMap.put("code_43","您无权解散房间");
        langMap.put("code_44","最后一局正在进行中，不许中途加入");
        langMap.put("code_45","道具发送间隔时间不能小于3秒");
        langMap.put("code_46","该房间不允许三带一");
        langMap.put("code_47","该房间不允许三带二");
        langMap.put("code_48","该牌局模式不存在");
        langMap.put("code_49","您不是该俱乐部成员，无法加入");
        langMap.put("code_50","群主钻石不足，无法创建房间");
        langMap.put("code_51","至少4人准备才能开始游戏");
        langMap.put("code_52","非俱乐部成员不能选择该模式");
        langMap.put("code_53","非俱乐部成员不能加入该房间");
        langMap.put("code_54","非群成员或者非指定群成员不能加入房间");
        langMap.put("code_57","房间已被{0}解散");
        langMap.put("code_58","您的客户端不支持该玩法");
        langMap.put("code_59","解散失败，请稍后再试!");
        langMap.put("code_60","该房间已被群主解散:[{0}]");
        langMap.put("code_61","至少4人才能开始游戏");
        langMap.put("code_62","您不是俱乐部成员无法创房！");
        langMap.put("code_63","您不是{0}俱乐部成员");
        langMap.put("code_64","信用分不够，需要{0}，您当前只有{1}");
        langMap.put("code_65","由于房间内玩家{0}的信用分低于最低要求分数{1}，房间自动解散！");
        langMap.put("code_66","由于长时间未准备，被踢出");
        langMap.put("code_67","需完成一局牌局才可领取");
        langMap.put("code_68","已经暂停开房，请联系群主或管理员");
        langMap.put("code_200","创建房间失败，未开启GPS");
        langMap.put("code_201","加入房间失败，未开启GPS");
        langMap.put("code_202","加入房间失败，距离过近!");
        langMap.put("code_203","精彩活动已过期！");
        langMap.put("code_204","未开局的游戏，暂不能解散");
        langMap.put("code_205","请先加入军团");
        langMap.put("code_206","消息太长");
        langMap.put("code_207","消息为空");
        langMap.put("code_208","未定义的操作指令");
        langMap.put("code_209","还未达成条件，暂不能领取！");
        langMap.put("code_210","还有玩家未准备，请稍后切牌");
        langMap.put("code_211","最多只能代开10个房间");
        langMap.put("code_212","您在别的游戏有未解散的房间！");
        langMap.put("code_213","商品不存在或已下架");
        langMap.put("code_214","商品项不存在");
        langMap.put("code_215","积分不足");
        langMap.put("code_216","兑换失败！兑换后剩余积分不能低于{0}");
        langMap.put("code_217","房间已解散");
        langMap.put("code_218","牌局未开始");
        langMap.put("code_219","您已领取过奖励！");
        langMap.put("code_220","条件不满足，领取失败！");
        langMap.put("code_221","该玩家正在打比赛场");
        langMap.put("code_222","应进入:{0}房间:{1}-{2}-{3}");
        langMap.put("code_223","您已经登陆游戏啦");
        langMap.put("code_224","没有抽奖机会了，继续玩游戏赚取抽奖机会！");
        langMap.put("code_225","请稍后再试");
        langMap.put("code_226","您已报名参赛！");
        langMap.put("code_227","请先完成您正在进行的牌局再来报名");
        langMap.put("code_228","太火爆了，人数已满，请参加下一场");
        langMap.put("code_229","请刷新再试");
        langMap.put("code_230","比赛已开始，暂时不能退出哦");
        langMap.put("code_231","您暂时没有可领取的钻石");
        langMap.put("code_232","你的IP为空，不能解散");
        langMap.put("code_233","该房间不存在，解散失败");
        langMap.put("code_234","当前牌局不存在相同IP玩家，解散失败");
        langMap.put("code_235","该房间有相同IP，解散房间成功");
        langMap.put("code_236","当前牌局不存在相同IP玩家，解散失败");
        langMap.put("code_237","您今日砸蛋次数已用完！");
        langMap.put("code_238","您没有砸蛋次数了！");
        langMap.put("code_239","没有找到该玩法:{0}");
        langMap.put("code_240","您正在比赛场中");
        langMap.put("code_241","未设置玩法，请联系群主");
        langMap.put("code_242","俱乐部不存在或已解散！");
        langMap.put("code_243","禁止成员开房，请联系群主或管理员");
        langMap.put("code_244","群主已开启群主支付");
        langMap.put("code_245","钻石不足！智能补房失败！");
        langMap.put("code_246","群主未开启群主支付");
        langMap.put("code_247","您已经拥有一个房间了！");
        langMap.put("code_248","已达到创房上限！");
        langMap.put("code_249","您已被踢下线");
        langMap.put("code_250","您超过{0}天未登录游戏，欢迎回归，获得{1}钻石！");
        langMap.put("code_251","创建失败，请重试！");
        langMap.put("code_252","您当前已下注噢，请稍后退出");
        langMap.put("code_901","金币场暂未开放");
        langMap.put("code_902","补助金暂未开放");
        langMap.put("code_903","金币不足");
        langMap.put("code_904","不能进入{0}");
        langMap.put("code_905","今日领取补助金已达上限");
        langMap.put("code_906","金币场聊天室暂未开放");
        langMap.put("code_907","您的金币太多了，请前往更高级的场次");
        langMap.put("code_908","该{0}不合法哦！请另取高名！");
        langMap.put("code_909","{0}暂未开放！");
        langMap.put("code_910", "50元余额以上才可下注，请您先充值噢！");
        langMap.put("other_err_78","试炼NPC");

    }

    public static final String getMsg(String msgKey, Object... o) {
        String msg = langMap.get(msgKey);
        if(msg == null) {
            LogUtil.errorLog.info("Lang msg not exist:" + msgKey);
            return "未知错误！";
        }
        String result = msg;
        if (o == null || o.length == 0) {
            return result;
        }
        for (int i = 0; i < o.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(o[i]));
        }
        return result;
    }

    public static final void loadLangMsg(String csv_path) {
        List<String[]> list;
        if(TableCheckDao.getInstance().checkTableExists0(DbEnum.LOGIN,"t_base_config")){
            list = BaseConfigDao.getInstance().loadValueList(BaseConfigDao.getInstance().selectAllByType("LangConfig"));
            if (list==null){
                list = readCSVResource(csv_path + "lang.csv", false);
            }
        }else{
            list = readCSVResource(csv_path + "lang.csv", false);
        }

        parseLangMsg(list);
    }

    private static final void parseLangMsg(List<String[]> csvList) {
        if(csvList.isEmpty())
            return;

        Map<String, String> langMap0 = new ConcurrentHashMap<>();

        for (String[] values : csvList) {
            int i = 0;
            String langKey = getStrValue(values, i++);
            String langMsg = getStrValue(values, i++);
            langMap0.put(langKey, langMsg);
        }

        langMap = langMap0;
        LogUtil.msgLog.info("load lang msg finished");
    }

    @Override
    public void reload(String resourceFileName) {
        List<String[]> list = readCSVResource(resourceFileName, false);
        if(list.isEmpty())
            return;
        parseLangMsg(list);
    }

}
