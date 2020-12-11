package com.sy599.game.qipai.klpaohuzi.constant;

import com.sy599.game.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

public class PaohuziConstant {
	
	public static boolean isTest = false;

	public static boolean isAutoMo = true;

	public static final int state_player_ready = 1;

	/** 快乐邵阳跑胡子 **/
	public static final int play_type_shaoyang = 30;
	
	/** 快乐邵阳剥皮 **/
	public static final int play_type_bopi = 31;
	
	/** 胡牌次数 */
    public static final int ACTION_COUNT_INDEX_HU = 0;
	/** 自摸次数 */
    public static final int ACTION_COUNT_INDEX_ZIMO = 1;
    /** 提次数 */
    public static final int ACTION_COUNT_INDEX_TI = 2;
    /** 跑次数 */
    public static final int ACTION_COUNT_INDEX_PAO = 3;

	// 跑胡子
	public static List<Integer> cardList = new ArrayList<>();

    public static final  Map<Integer,List<int[]>> paiZuMap = new HashMap<>();

	static {
		isTest="1".equals(ResourcesConfigsUtil.loadServerPropertyValue("test"));
		for (int i = 1; i <= 80; i++) {
			cardList.add(i);
		}
        for(int i = 1 ; i <= 10 ; i ++){
            paiZuMap.put(i,initPaiZu(i));
        }
        for(int i = 101 ; i <= 110 ; i ++){
            paiZuMap.put(i,initPaiZu(i));
        }
	}
	public static boolean isPlayBopi(int playType) {
		return playType ==  play_type_bopi;
	}
	public static boolean isPlaySyPhz(int playType) {
		return playType == play_type_shaoyang || playType == play_type_bopi;
	}


    /**
     * 托管时间15s
     */
    public static final int AUTO_TIME = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("auto_time"), 10 * 1000);

    /**
     * 托管操作时间3s
     */
    public static final int AUTO_PLAY_TIME = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("auto_play_time"), 2 * 1000);

    /**
     * 自动准备时间
     */
    public static final int AUTO_READY_TIME = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("auto_ready_time"), 10);

    public static List<int[]> getPaiZu(int val){
        return paiZuMap.get(val);
    }

    public static List<int[]> initPaiZu(int val) {
        List<int[]> res = new ArrayList<>();

        //三张一样
        res.add(new int[]{val, val, val});

        //二七十
        if (val % 100 == 2) {
            res.add(new int[]{val, val + 5, val + 8});
        } else if (val % 100 == 7) {
            res.add(new int[]{val - 5, val, val + 3});
        } else if (val % 100 == 10) {
            res.add(new int[]{val - 8, val - 3, val});
        }

        //做顺
        if (val % 100 == 10) {
            res.add(new int[]{val, val - 1, val - 2});
        } else if (val % 100 == 9) {
            res.add(new int[]{val, val - 1, val - 2});
            res.add(new int[]{val - 1, val, val + 1});
        } else if (val % 100 == 1) {
            res.add(new int[]{val, val + 1, val + 2});
        } else if (val % 100 == 2) {
            res.add(new int[]{val - 1, val, val + 1});
            res.add(new int[]{val, val + 1, val + 2});
        } else {
            res.add(new int[]{val - 2, val - 1, val});
            res.add(new int[]{val - 1, val, val + 1});
            res.add(new int[]{val, val + 1, val + 2});
        }

        //大小牌组合
        if (val > 100) {
            res.add(new int[]{val, val - 100, val - 100});
            res.add(new int[]{val, val, val - 100});
        } else {
            res.add(new int[]{val, val + 100, val + 100});
            res.add(new int[]{val, val, val + 100});
        }

        return res;
    }
}
