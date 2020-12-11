package com.sy599.game.util;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GameUtil {
    /**
     * 防止用户出现在多个房间
     **/
    public static final Map<Long, String> USER_COMMAND_MAP = new ConcurrentHashMap<>();

    /**
     * 新版邵阳跑胡子
     **/
    public static final int play_type_syphz = 10;
    /**
     * 新版邵阳剥皮
     **/
    public static final int play_type_sybp = 11;

    /*** 轮流当庄十点半 */
    public static final int play_type_tenthirty_taketurn = 61;
    /**
     * 十点半房主霸王庄
     **/
    public static final int play_type_tenthirty_bawang = 62;

    /**
     * 十点半每局抢庄
     **/
    public static final int play_type_tenthirty_robwang = 63;
    /**
     * 十点半最小牌当庄
     **/
    public static final int play_type_tenthirty_lastwang = 64;

    /*** 三猴子轮流当庄 */
    public static final int play_type_three_taketurn = 65;
    /**
     * 三猴子每局抢庄
     **/
    public static final int play_type_three_robwang = 66;
    /**
     * 三猴子最小牌当庄
     **/
    public static final int play_type_three_lastwang = 67;
    /**
     * 三猴子房主霸王庄
     **/
    public static final int play_type_three_bawang = 68;

    /*** 把把抢庄斗牛 */
    public static final int play_type_dn_rob = 20;
    /*** 轮流抢庄斗牛 */
    public static final int play_type_dn_robtaketurns = 21;
    /*** 牛牛抢庄斗牛 */
    public static final int play_type_dn_robniuniu = 22;
    /**
     * 房主霸王庄斗牛
     **/
    public static final int play_type_dn_robbawang = 23;
    /**
     * 大牌为庄斗牛
     **/
    public static final int play_type_dn_robfirst = 24;
    /**
     * 闲家推注玩法
     **/
    public static final int play_type_dn_pushBet = 25;
    /**
     * 新增把把暗牌抢庄斗牛
     */
    public static final int play_type_dn_robanpai = 26;

    /**
     * 快乐邵阳跑胡子
     **/
    public static final int play_type_kl_shaoyang = 30;
    /**
     * 快乐邵阳剥皮
     **/
    public static final int play_type_kl_bopi = 31;
    /**
     * 邵阳跑胡子
     **/
    public static final int play_type_shaoyang = 32;

    /**
     * 邵阳剥皮
     **/
    public static final int play_type_bopi = 33;

    /**
     * 甘肃扬沙子
     **/
    public static final int play_type_ysz = 81;

    /**
     * 甘肃麻将
     */
    public static final int play_type_gsmj = 101;
    /**
     * 谷仓麻将
     */
    public static final int play_type_gucang = 105;

    /**
     * 陇南摆叫麻将玩法ID
     **/
    public static final int game_type_majiang_lnbj = 102;
    /**
     * 陇南摆叫麻将三人玩法ID
     **/
    public static final int game_type_majiang_lnbj_3 = 108;
    /**
     * 陇南摆叫麻将两人玩法ID
     **/
    public static final int game_type_majiang_lnbj_2 = 109;

    /**
     * 卡二条麻将玩法ID
     **/
    public static final int game_type_majiang_kaertiao = 103;

    /**
     * 卡二条麻将二人玩法ID
     **/
    public static final int game_type_majiang_kaertiao_2 = 110;

    /**
     * 兰州会牌麻将玩法ID
     **/
    public static final int game_type_majiang_lzhp = 104;

    /**
     * 张掖麻将玩法ID
     **/
    public static final int game_type_majiang_zhangye = 106;

    /**
     * 兰州会牌麻将玩法ID
     **/
    public static final int game_type_majiang_qyhs = 107;

    /**
     * 岷县咣咣麻将玩法ID
     **/
    public static final int game_type_majiang_minXianGG = 111;

    /**
     * 秦安麻将玩法ID
     **/
    public static final int game_type_majiang_qamj = 112;

    /**
     * 三人斗地主
     **/
    public static final int ddz_three = 91;
    /**
     * 二人斗地主
     **/
    public static final int ddz_two = 92;
    /**
     * 赖子玩法
     **/
    public static final int ddz_three_niggle = 93;

    /** 快乐四喜：2人玩法 **/
    public static final int play_type_2PERSON_4Xi = 210;
    /** 快乐四喜：3人玩法 **/
    public static final int play_type_3PERSON_4Xi = 211;
    /** 快乐四喜：4人玩法 **/
    public static final int play_type_4PERSON_4Xi = 212;

    /**
     * 3副牌玩法
     **/
    public static final int play_type_3POK = 113;
    /**
     * 4副牌玩法
     **/
    public static final int play_type_4POK = 114;
    /**
     * 三人3副牌玩法
     **/
    public static final int play_type_3PERSON_3POK = 115;
    /**
     * 三人4副牌玩法
     **/
    public static final int play_type_3PERSON_4POK = 116;
    /**
     * 两人3副牌玩法
     **/
    public static final int play_type_2PERSON_3POK = 117;
    /**
     * 两人4副牌玩法
     **/
    public static final int play_type_2PERSON_4POK = 118;


    /**
     * 15张玩法
     **/
    public static final int play_type_15 = 15;
    /**
     * 16张玩法
     **/
    public static final int play_type_16 = 16;

    /**
     * yz15张玩法
     **/
    public static final int play_type_yz15 = 17;
    /**
     * yz16张玩法
     **/
    public static final int play_type_yz16 = 18;
    /**
     * 娄底放炮罚
     */
    public static final int ldfpf=199;

    /**
     * 永州扯胡子
     **/
    public static final int play_type_3_2_yongzhou = 37;
    public static final int play_type_3_4_yongzhou = 38;
    public static final int play_type_4_2_yongzhou = 35;
    public static final int play_type_4_4_yongzhou = 36;
    public static final int play_type_3_3_yongzhou = 39;
    public static final int play_type_4_3_yongzhou = 40;
    public static final int play_type_2_2_yongzhou = 41;
    public static final int play_type_2_3_yongzhou = 42;
    public static final int play_type_2_4_yongzhou = 43;

    /**
     * 祁阳六胡抢
     **/
    public static final int play_type_2_0_six = 44;
    public static final int play_type_2_1_six = 45;
    public static final int play_type_2_2_six = 46;
    public static final int play_type_3_0_six = 47;
    public static final int play_type_3_1_six = 48;
    public static final int play_type_3_2_six = 49;
    public static final int play_type_4_0_six = 50;
    public static final int play_type_4_1_six = 51;
    public static final int play_type_4_2_six = 52;

    /**
     * 湘西三皮
     **/
    public static final int play_type_sp = 121;

    /**
     * 转转麻将
     **/
    public static final int play_type_zhuanzhuan = 1;
    /**
     * 长沙麻将
     **/
    public static final int play_type_changesha = 2;
    /**
     * 红中麻将
     **/
    public static final int play_type_hongzhong = 3;
    /**
     * 安化麻将
     **/
    public static final int play_type_anhua = 4;
    /**
     * 长春麻将
     **/
    public static final int play_type_ccmj = 5;

    /**
     * 半边天炸玩法
     **/
    public static final int play_type_bbtz = 131;

    /**
     * 沅江鬼胡子玩法
     **/
    public static final int play_type_yjghz = 39;
   
    /***
     * 常德跑胡子
     */
    public static final int play_type_changdephz =  53;
 

    /**
     * 沅江鬼胡子八软息玩法
     **/
    public static final int play_type_yjghzbrx = 54;
    /**
     * 王者麻将(沅江)
     **/
    public static final int play_type_yuanjiang = 6;
    /**
     * 王者跑得快15张(沅江)
     **/
    public static final int play_type_pdk_yj15 = 15;
    /**
     * 王者跑得快16张(沅江)
     **/
    public static final int play_type_pdk_yj16 = 16;

    /**
     * 千分 三个人三副牌(去掉3,4)
     **/
    public static final int play_type_qianfen3_3_70 = 70;

    /**
     * 千分 三个人三副牌(去掉3,4,6,7)
     **/
    public static final int play_type_qianfen3_3_71 = 71;

    /**
     * 千分 二个人三副牌(去掉3,4)
     **/
    public static final int play_type_qianfen2_3_72 = 72;

    /**
     * 千分 二个人三副牌(去掉3,4,6,7)
     **/
    public static final int play_type_qianfen2_3_73 = 73;

    /**
     * 划水麻将玩法ID
     **/
    public static final int game_type_majiang_plhs = 500;

    /**
     * 静宁打经麻将玩法ID
     **/
    public static final int game_type_majiang_jndj = 501;

    /**
     * 天水麻将玩法ID
     **/
    public static final int game_type_majiang_tsmj = 502;

    /**
     * 金昌划水麻将玩法ID
     **/
    public static final int game_type_majiang_jchs = 503;

    /**
     * 陇西麻将玩法ID
     **/
    public static final int game_type_majiang_longximj = 504;

    /**
     * 武威麻将玩法ID
     **/
    public static final int game_type_majiang_wuweimj = 505;

    /**
     * 酒泉嘉峪关悄悄胡玩法ID
     **/
    public static final int game_type_majiang_jqmj_jyg = 506;

    /**
     * 酒泉三报麻将玩法ID
     **/
    public static final int game_type_majiang_jqmj_sanbao = 507;

    /**
     * 酒泉挑经玩法ID
     **/
    public static final int game_type_majiang_jqmj_tiaojing = 508;

    /**
     * 酒泉二报玩法ID
     **/
    public static final int game_type_majiang_jqmj_erbao = 509;

    /**
     * 兰州二报玩法ID
     **/
    public static final int game_type_majiang_lz_erbao = 510;

    /**
     * 兰州翻金玩法ID
     **/
    public static final int game_type_majiang_lz_fanjin = 511;

    /**
     * 山西斗地主
     **/
    public static final int game_type_shangxi_doudizhu = 600;
    /**
     * 临汾斗地主
     **/
    public static final int game_type_linfen_doudizhu = 601;
    /**
     * 二人斗地主
     **/
    public static final int game_type_er_doudizhu = 602;
    /**
     * 运城四人斗地主
     **/
    public static final int game_type_yuncheng_doudizhu = 603;

    /**
     * 龙虎斗
     **/
    public static final int game_type_longhudou = 640;
    /**
     * 二八杠
     **/
    public static final int game_type_erbagang = 641;

    public static final int game_type_caishaizi = 642;

    /**
     * 安化斗地主3人玩法
     **/
    public static final int game_type_ah_ddz91 = 91;

    /**
     * 安化斗地主2人玩法
     **/
    public static final int game_type_ah_ddz92 = 92;

    /**
     * 安化跑得快玩法ID
     **/
    public static final int game_type_ah_pdk15 = 15;

    /**
     * 安化跑得快玩法ID
     **/
    public static final int game_type_ah_pdk16 = 16;

    /**
     * 安化转转麻将玩法ID
     **/
    public static final int game_type_ah_zzmj = 1;

    /**
     * 安化红中麻将玩法ID
     **/
    public static final int game_type_ah_hzmj = 3;

    /**
     * 安化长沙麻将玩法ID
     **/
    public static final int game_type_ah_csmj = 2;

    /**
     * 安化麻将玩法ID
     **/
    public static final int game_type_ah_mj = 4;

    /**
     * 安化斗牛玩法ID
     **/
    public static final int game_type_ah_dn20 = 20;

    /**
     * 安化斗牛玩法ID
     **/
    public static final int game_type_ah_dn23 = 23;

    /**
     * 安化斗牛玩法ID
     **/
    public static final int game_type_ah_dn22 = 22;

    /**
     * 安化斗牛玩法ID
     **/
    public static final int game_type_ah_dn21 = 21;

    /**
     * 安化长春麻将玩法ID
     **/
    public static final int game_type_ah_ccmj = 5;

    /**
     * 安化跑胡子玩法ID
     **/
    public static final int game_type_ah_phz = 31;

    /**
     * 安化邵阳跑胡子玩法ID
     **/
    public static final int game_type_ah_syphz = 32;

    /**
     * 安化剥皮玩法ID
     **/
    public static final int game_type_ah_sybp = 33;

    /**
     * 安化娄底跑胡子玩法ID
     **/
    public static final int game_type_ah_loudi_phz = 34;

    /**
     * 安化桂林跑胡子玩法ID
     **/
    public static final int game_type_ah_guilin_phz = 35;

    /**
     * 安化三公玩法ID
     **/
    public static final int game_type_ah_sg42 = 42;

    /**
     * 安化三公玩法ID
     **/
    public static final int game_type_ah_sg41 = 41;
    /**
     * 双扣玩法2-A游戏ID
     */
    public static final int GAME_SJ_TO_A_ID = 200;
    /**
     * 双扣玩法2-6游戏ID
     */
    public static final int GAME_SJ_TO_6_ID = 201;
    /**
     * 双扣玩法2-10游戏ID
     */
    public static final int GAME_SJ_TO_10_ID = 202;
    /**
     * 西北嘉峪关麻将
     */
    public static final int GAME_WEST_JIAYUGUAN_ID = 203;
    /**
     * 西北酒泉麻将
     */
    public static final int GAME_WEST_JIUQUAN_ID = 204;

    /***--------------------转转 麻将------------------------------------**/
    public static final int game_type_zzmj = 220;
    /***--------------------红中 麻将------------------------------------**/
    public static final int game_type_hzmj = 221;
    /***--------------------长沙 麻将------------------------------------**/
    public static final int game_type_csmj = 222;
    /***--------------------邵阳 麻将------------------------------------**/
    public static final int game_type_symj = 223;
    
    /***--------------------保山 麻将------------------------------------**/
    public static final int game_type_bsmj = 225;


    public static boolean isPlayNewSyPhz(int playType) {
        return playType == play_type_syphz;
    }

    public static boolean isPlayNewSyBp(int playType) {
        return playType == play_type_sybp;
    }


    public static boolean isPlayYjGame() {
        return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("game_wz"));
    }

    public static boolean isPlayAhGame() {
        return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("game_ah"));
    }

    public static boolean isPlayAhDdz(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_ddz91 || playType == game_type_ah_ddz92);
    }

    public static boolean isPlayAhPdk(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_pdk15 || playType == game_type_ah_pdk16);
    }

    public static boolean isPlayAhZzOrHzMj(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_zzmj || playType == game_type_ah_hzmj);
    }

    public static boolean isPlayAhCsMj(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_csmj);
    }

    public static boolean isPlayAhMj(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_mj);
    }

    public static boolean isPlayAhDn(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_dn20 || playType == game_type_ah_dn21 || playType == game_type_ah_dn22 || playType == game_type_ah_dn23);
    }

    public static boolean isPlayAhCcMj(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_ccmj);
    }

    public static boolean isPlayAhPhz(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_phz);
    }

    public static boolean isPlayAhSyPhz(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_syphz || playType == game_type_ah_sybp);
    }
    public static boolean isPlayBSMj(int playType) {
        return playType == game_type_bsmj;
    }

    public static boolean isPlayAhLdPhz(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_loudi_phz);
    }

    public static boolean isPlayAhGlPhz(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_guilin_phz);
    }

    public static boolean isPlayAhSg(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_sg41 || playType == game_type_ah_sg42);
    }

    /**
     * 是否是玩千分游戏
     *
     * @param playType
     * @return
     */
    public static final boolean isPlayQianFen(int playType) {
        return playType == play_type_qianfen3_3_70 || playType == play_type_qianfen3_3_71 || playType == play_type_qianfen2_3_72 || playType == play_type_qianfen2_3_73;
    }

    public static boolean isPlayYjGhz(int playType) {
        if (isPlayYjGame()) {
            return playType == play_type_yjghz; //|| playType == play_type_yjghzbrx
        }
        return false;
    }

    public static boolean isPlayYjMj(int playType) {
        if (isPlayYjGame()) {
            return playType == play_type_yuanjiang;
        }
        return false;
    }

    public static boolean isPlayYjPdk(int playType) {
        if (isPlayYjGame()) {
            return playType == play_type_pdk_yj15 || playType == play_type_pdk_yj16;
        }
        return false;
    }


    public static boolean isPlayBbtz(int playType) {
        if (playType == play_type_bbtz) {
            return true;
        }
        return false;
    }

    public static boolean isPlayNn(int playType) {
        return SharedConstants.isKingOfBull() && (playType == play_type_dn_robniuniu || playType == play_type_dn_robtaketurns || playType == play_type_dn_rob || playType == play_type_dn_robbawang || playType == play_type_dn_robfirst
                || playType == play_type_dn_pushBet);
    }

    public static boolean isPlayDn(int playType) {
        return playType == play_type_dn_robniuniu || playType == play_type_dn_robtaketurns || playType == play_type_dn_rob || playType == play_type_dn_robbawang || playType == play_type_dn_robfirst
                || playType == play_type_dn_pushBet;
    }

    public static boolean isPlayBopi(int playType) {
        return playType == play_type_bopi || playType == play_type_kl_bopi;
    }

    public static boolean isPlaySyPhz(int playType) {
        return playType == play_type_shaoyang || playType == play_type_bopi || playType == play_type_kl_shaoyang || playType == play_type_kl_bopi;
    }
    
    
    public static boolean isPlayCdPhz(int playType) {
        return playType== play_type_changdephz;
    }
    
    

    public static boolean isPlayTenthirty(int playType) {
        return playType == play_type_tenthirty_taketurn || playType == play_type_tenthirty_bawang || playType == play_type_tenthirty_robwang || playType == play_type_tenthirty_lastwang;
    }

    public static boolean isPlayThreeMonkeys(int playType) {
        return playType == play_type_three_taketurn || playType == play_type_three_robwang;
    }

    public static boolean isPlayGSMajiang(int playType) {
        return (playType == play_type_gsmj || playType == play_type_gucang);
    }

    public static boolean isPlayDdz(int playType) {
        return playType == ddz_three || playType == ddz_two || playType == ddz_three_niggle;
    }

    public static boolean isLdfpf(int playType) {
        return playType==ldfpf;
    }

    public static boolean isPlayDtz(int playType) {
        switch (playType) {
            case play_type_3POK:
            case play_type_4POK:
            case play_type_3PERSON_3POK:
            case play_type_3PERSON_4POK:
            case play_type_2PERSON_3POK:
            case play_type_2PERSON_4POK:
            case play_type_2PERSON_4Xi:
            case play_type_3PERSON_4Xi:
            case play_type_4PERSON_4Xi:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPlayPdk(int playType) {
        if (playType == play_type_15 || playType == play_type_16) {
            return true;
        }
        return false;
    }

    public static boolean isPlayYzPdk(int playType) {
        if (playType == play_type_yz15 || playType == play_type_yz16) {
            return true;
        }
        return false;
    }

    public static boolean isPlaySp(int playType) {
        return playType == play_type_sp;
    }

    public static boolean isPlayMajiang(int playType) {
        if (playType == play_type_hongzhong || playType == play_type_zhuanzhuan) {
            return true;
        }
        return false;
    }

    public static boolean isPlayCSMajiang(int playType) {
        if (playType == play_type_changesha) {
            return true;
        }
        return false;
    }

    public static boolean isPlayAhMajiang(int playType) {
        if (playType == play_type_anhua) {
            return true;
        }
        return false;
    }

    public static boolean isPlayCCMajiang(int playType) {
        if (playType == play_type_ccmj) {
            return true;
        }
        return false;
    }

    /**
     * 是否百人玩法
     *
     * @param playType
     * @return
     */
    public static boolean isPlayBaiRenWanfa(int playType) {
        if (playType == game_type_longhudou || playType == game_type_erbagang) {
            return true;
        }
        return false;
    }

    /**
     * 速创建房间模式下检测智能创房
     *
     * @param groupId
     * @param playerClass
     */
    public final static void autoCreateGroupTable(final String groupId, final Class<? extends Player> playerClass) {
    	autoCreateGroupTable(groupId, playerClass, 0);
    }

    private static final Map<String, Object> lockMap = new ConcurrentHashMap<>();

    public static Object getAutoCreateLock(String groupId) {
        Object res = lockMap.get(groupId);
        if (res != null) {
            return res;
        } else {
            synchronized (lockMap) {
                res = lockMap.get(groupId);
                if (res != null) {
                    return res;
                } else {
                    res = new Object();
                    lockMap.put(groupId, res);
                    return res;
                }
            }
        }
    }
    
    public final static void autoCreateGroupTable(final String groupId, final Class<? extends Player> playerClass, long configId) {
        final long now = System.currentTimeMillis();
        LogUtil.msg("autoCreateGroupTable|submit|" + now + "|" + groupId + "|" + playerClass.getSimpleName() + "|" + configId);
        TaskExecutor.SINGLE_EXECUTOR_SERVICE_GROUP.execute(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    LogUtil.msg("autoCreateGroupTable|run|"+now+"|" + groupId + "|" + playerClass.getSimpleName() + "|" + configId);
                    //快速创建房间模式下检测智能创房 - 智能补房
                    GroupInfo group = GroupDao.getInstance().loadGroupInfo(groupId, "0");
                    if (group == null) {
                        return;
                    }
                    if (group.getExtMsg() == null || !group.getExtMsg().contains("+a")) {
                    	return;
                    }
                    
                    GroupUser master = GroupDao.getInstance().loadGroupMaster(group.getGroupId().toString());
                    if (master == null) {
                        return;
                    }
                    boolean bl = true;
                    Player player = PlayerManager.getInstance().getPlayer(master.getUserId());
                    if (player == null) {
                        RegInfo user = UserDao.getInstance().selectUserByUserId(master.getUserId());
                        if (user != null && playerClass != null) {
                            player = ObjectUtil.newInstance(playerClass);
                            player.loadFromDB(user);
                        } else {
                            bl = false;
                        }
                    }
                    if (!bl) {
                        return;
                    }

                    List<GroupInfo> groupRooms = GroupDao.getInstance().loadAllGroupRoom(groupId);
                    if (groupRooms != null && groupRooms.size() > 0) {
                        //包间模式
                        if (configId > 0) {
                            GroupTableConfig groupTableConfig = GroupDao.getInstance().loadGroupTableConfig(configId);
                            if(groupTableConfig == null || !"1".equals(groupTableConfig.getConfigState())){
                                LogUtil.msg("autoCreateGroupTable|error|5|" + now + "|" + groupId + "|" + playerClass.getSimpleName() + "|" + configId);
                                return;
                            }
                            Integer count = GroupDao.getInstance().loadGroupRoomTableCount(group.getGroupId(), "0", groupTableConfig.getGroupId().toString());
                            int autoCreateTableCountLimit = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoCreateTableCountLimit", 5);
                            if (count == null || count.intValue() >= autoCreateTableCountLimit) {
                                LogUtil.msg("autoCreateGroupTable|fail|6|" + now + "|" + groupId + "|" + playerClass.getSimpleName() + "|" + configId + "|" + count + "|" + autoCreateTableCountLimit + "|" + groupTableConfig.getGroupId());
                                return;
                            }
                            createTable(player, groupId, groupTableConfig);
                        }else{
                            for (GroupInfo groupRoom : groupRooms) {
                                GroupTableConfig groupTableConfig = GroupDao.getInstance().loadLastGroupTableConfig(groupRoom.getGroupId(), group.getGroupId());
                                if (groupTableConfig == null) {
                                    return;
                                }
                                synchronized (getAutoCreateLock(groupId)) {
                                    Integer count = GroupDao.getInstance().loadGroupRoomTableCount(group.getGroupId(), "0", groupRoom.getGroupId().toString());
                                    int autoCreateTableCountLimit = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoCreateTableCountLimit",5);
                                    if (count == null || count.intValue() >= autoCreateTableCountLimit) {
                                        LogUtil.msg("autoCreateGroupTable|fail|8|" + now + "|" + groupId + "|" + playerClass.getSimpleName() + "|" + configId + "|" + count + "|" + autoCreateTableCountLimit + "|" + groupRoom.getGroupId());
                                        continue;
                                    }
                                    createTable(player, groupId, groupTableConfig);
                                }
                            }
                            player.writeComMessage(WebSocketMsgType.MULTI_CREATE_TABLE, 1, 0);
                        }

                    } else {
                    	
                    	//非包间模式
                    	List<GroupTableConfig> cfgList = new ArrayList<GroupTableConfig>();
                    	int type = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoCreateTableType", 0);
                    	if (configId > 0) {
                    		GroupTableConfig groupTableConfig = GroupDao.getInstance().loadGroupTableConfig(configId);
                    		if (groupTableConfig == null) {
                                return;
                            }
                            cfgList.add(groupTableConfig);
                    	}
                    	else {
                        	if (type == 1) {
                        		cfgList = GroupDao.getInstance().loadGroupTableConfig2(group.getGroupId(), 0);
                        		if (cfgList == null) {
                        			return;
                        		}
                        	}
                        	else {
                        		 GroupTableConfig groupTableConfig = GroupDao.getInstance().loadLastGroupTableConfig(group.getGroupId(), 0);
                                 if (groupTableConfig == null) {
                                     return;
                                 }
                                 cfgList.add(groupTableConfig);
                        	}
                    	}

                        synchronized (getAutoCreateLock(groupId)) {
                            
                        	if (configId > 0) {
                                //创建指定玩法的房间
                                synchronized (getAutoCreateLock(groupId)) {
                                    Integer curCount = GroupDao.getInstance().countGroupConfigTable(group.getGroupId(), "0", configId);
                                    int autoCreateTableCount = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoCreateTableCount", 10);
                                    int autoCreateTableCountLimit = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoCreateTableCountLimit", 5);
                                    if (curCount == null || curCount.intValue() < autoCreateTableCountLimit) {
                                        createTable(player, groupId, cfgList.get(0), autoCreateTableCount);
                                    }
                                }
                        	}else {
                        		if (type == 1) {
                                	//当任意设置的玩法空桌小于2时，自动补充空桌
                                	List<GroupTable> groupTableList = GroupDao.getInstance().loadGroupTablesGroupId(group.getGroupId(), "0");
                                	HashMap<Long, Integer> temp = new HashMap<Long, Integer>();
                                	if (groupTableList != null) {
                                		for (GroupTable groupTable : groupTableList) {
                                    		Integer count = temp.get(groupTable.getConfigId());
                                    		if (count == null) {
                                    			count = 1;
                                    		}
                                    		else {
                                    			count += 1;
                                    		}
                                    		temp.put(groupTable.getConfigId(), count);
                                    	}
                                	}
                                	for (GroupTableConfig groupTableConfig : cfgList) {
                                		Integer count = temp.get(groupTableConfig.getKeyId());
                                		if (count == null || count < 2) {
                                			createTable(player, groupId, groupTableConfig);
                                		}
                                                         	 
                                    }   
                                }else {
                                	Integer count = GroupDao.getInstance().loadGroupTableCount(group.getGroupId(), "0");
                                	if (count == null || count.intValue() >= 5) {
                                        return;
                                    }
                                	createTable(player, groupId, cfgList.get(0));
                                }
                        	}
                        }
                    }

                } catch (Throwable t) {
                    LogUtil.errorLog.error("autoCreateGroupTable Throwable:" + t.getMessage(), t);
                }finally {
                    LogUtil.msg("autoCreateGroupTable|timeUse|" + now + "|" + groupId + "|" + playerClass.getSimpleName() + "|" + configId + "|" + (System.currentTimeMillis() - start));
                }
            }

            public void createTable(Player player, String groupId, GroupTableConfig groupTableConfig) {
                createTable(player, groupId, groupTableConfig, 0);
            }

            public void createTable(Player player, String groupId, GroupTableConfig groupTableConfig, int autoCreateTableCount) {
                JsonWrapper json;
                List<Integer> intsList = null;
                List<String> strsList = null;
                try {
                    json = new JsonWrapper(groupTableConfig.getModeMsg());
                    intsList = GameConfigUtil.string2IntList(json.getString("ints"));
                    strsList = GameConfigUtil.string2List(json.getString("strs"));
                } catch (Throwable th) {
                } finally {
                    if ((intsList == null || intsList.size() == 0) && (strsList == null || strsList.size() == 0)) {
                        intsList = GameConfigUtil.string2IntList(groupTableConfig.getModeMsg());
                        strsList = new ArrayList<>();
                    }
                }
                try {
                    if (autoCreateTableCount <= 0) {
                        autoCreateTableCount = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoCreateTableCount", 10);
                    }
                    strsList.add(groupId);
                    strsList.add("" + autoCreateTableCount);
                    strsList.add("1");
                    strsList.add(String.valueOf(groupTableConfig.getKeyId()));
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("autoCreate", "1");
                    TableManager.getInstance().createTable(player, intsList, strsList, 0, 0, true, properties, null, null);
                    LogUtil.monitorLog.info("quick create table:count=" + autoCreateTableCount + ",groupId=" + groupId + ",modeId=" + groupTableConfig.getKeyId() + ",userId=" + player.getUserId());
                } catch (Throwable t) {
                    LogUtil.errorLog.error("autoCreateGroupTable Throwable:" + t.getMessage(), t);
                }
            }
        });
    }


    /**
     * 计算两点之间距离
     *
     * @param start
     * @param end
     * @return 米
     */
    public static double getDistance(String start, String end) {
        double la1 = NumberUtils.toDouble(start.split(",")[0]);
        double lo1 = NumberUtils.toDouble(start.split(",")[1]);
        double la2 = NumberUtils.toDouble(end.split(",")[0]);
        double lo2 = NumberUtils.toDouble(end.split(",")[1]);
        double lat1 = (Math.PI / 180) * la1;
        double lat2 = (Math.PI / 180) * la2;

        double lon1 = (Math.PI / 180) * lo1;
        double lon2 = (Math.PI / 180) * lo2;

        //地球半径
        double R = 6371;

        //两点间距离 km，如果想要米的话，结果*1000
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;
//        System.out.println("test get distance:"+ "start:"+start+",end:"+end+",d:"+ (int)d*1000);
        return (int) d * 1000;
    }

    /**
     * 获取牌局结束的条件值
     *
     * @param ints
     * @return
     */
    public static final int loadOverValue(List<Integer> ints) {
        int size;
        if (ints == null || (size = ints.size()) < 2) {
            return 0;
        }
        int playType = ints.get(1);
        int count = ints.get(0);
        if (isPlayQianFen(playType)) {
            return size >= 6 ? ints.get(5) : 0;
        } else if (isPlayDtz(playType)) {
            return size >= 4 ? ints.get(3) : 0;
        } else {
            return count;
        }
    }

    public static boolean isPlayZzMj(int playType){
        return playType == game_type_zzmj;
    }
    public static boolean isPlayHzMj(int playType){
        return playType == game_type_hzmj;
    }
    public static boolean isPlaySyMj(int playType){
        return playType == game_type_symj;
    }
    public static boolean isPlayCsMj(int playType){
        return playType == game_type_csmj;
    }
}
