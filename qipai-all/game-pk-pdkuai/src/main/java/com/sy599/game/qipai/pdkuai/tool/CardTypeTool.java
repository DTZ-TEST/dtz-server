package com.sy599.game.qipai.pdkuai.tool;

import com.sy599.game.qipai.pdkuai.bean.PdkPlayer;
import com.sy599.game.qipai.pdkuai.bean.PdkTable;
import com.sy599.game.qipai.pdkuai.util.CardUtils;
import com.sy599.game.qipai.pdkuai.util.CardValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 规则 查询牌型 等等
 *
 * @author lc
 */
public class CardTypeTool {

    /**
     * @param from 是自己已有的牌
     * @param oppo 对手出的牌
     * @return
     */
    public static List<Integer> canPlay(List<Integer> from, List<Integer> oppo, boolean isDisCards, PdkPlayer player, PdkTable table) {
        return CardTypeTool.getBestAI2(from,oppo,false,table);
    }

    public static List<Integer> getBestAI2(List<Integer> curList, List<Integer> oppo, boolean nextDan,PdkTable table) {
        if (curList == null || curList.size() == 0) {
            return Collections.emptyList();
        }

        List<Integer> retList = new ArrayList<>();
        Map<Integer, Integer> map = CardTool.loadCards(curList);
        int val = 0;
        int count = map.size();

        if (oppo == null || oppo.size() == 0) {
            if (count == 1) {
                retList.addAll(curList);
                return retList;
            }

            int size = curList.size();
            switch (size) {
                case 2:
                    if (nextDan) {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            val = kv.getKey().intValue();
                        }
                        retList.add(CardTool.loadCards(curList, val).get(0));
                    } else {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            val = kv.getKey().intValue();
                            break;
                        }
                        retList.add(CardTool.loadCards(curList, val).get(0));
                    }
                    break;
                case 3:
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 2) {
                            val = kv.getKey().intValue();
                            retList.addAll(CardTool.loadCards(curList, val));
                            break;
                        }
                    }
                    if (retList.size() == 0) {
                        if (nextDan) {
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                val = kv.getKey().intValue();
                            }
                            retList.add(CardTool.loadCards(curList, val).get(0));
                        } else {
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                val = kv.getKey().intValue();
                                break;
                            }
                            retList.add(CardTool.loadCards(curList, val).get(0));
                        }
                    }
                    break;
                case 4:
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 3) {
                            retList.addAll(curList);
                            break;
                        }
                    }
                    if (retList.size() == 0) {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() == 2) {
                                val = kv.getKey().intValue();
                                retList.addAll(CardTool.loadCards(curList, val));
                                break;
                            }
                        }
                        if (retList.size() == 0) {
                            if (nextDan) {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    val = kv.getKey().intValue();
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            } else {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    val = kv.getKey().intValue();
                                    break;
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            }
                        }
                    }
                    break;
                case 5:
                    if (count == 2) {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() == 3) {
                                retList.addAll(curList);
                                break;
                            } else if (kv.getValue().intValue() == 4) {
                                if (nextDan) {
                                    val = kv.getKey().intValue();
                                    retList.addAll(CardTool.loadCards(curList, val));
                                }
                                break;
                            }
                        }
                        if (retList.size() == 0) {
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                val = kv.getKey().intValue();
                                break;
                            }
                            retList.add(CardTool.loadCards(curList, val).get(0));
                        }
                    } else if (count == 5) {
                        boolean isShun = true;
                        int pre = 0;
                        int current = 0;
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            val = kv.getKey().intValue();
                            if (current == 0) {
                                current = val;
                            } else {
                                if (pre == 0) {
                                    pre = current;
                                }
                                current = val;
                                if (current >= 15 || current - pre != 1) {
                                    isShun = false;
                                    break;
                                }
                            }
                        }

                        if (isShun) {
                            retList.addAll(curList);
                        } else {
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                if (kv.getValue().intValue() == 2) {
                                    val = kv.getKey().intValue();
                                    retList.addAll(CardTool.loadCards(curList, val));
                                    break;
                                }
                            }
                            if (retList.size() == 0) {
                                if (nextDan) {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        val = kv.getKey().intValue();
                                    }
                                    retList.add(CardTool.loadCards(curList, val).get(0));
                                } else {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        val = kv.getKey().intValue();
                                        break;
                                    }
                                    retList.add(CardTool.loadCards(curList, val).get(0));
                                }
                            }
                        }
                    } else {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() == 2) {
                                val = kv.getKey().intValue();
                                retList.addAll(CardTool.loadCards(curList, val));
                                break;
                            }
                        }
                        if (retList.size() == 0) {
                            if (nextDan) {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    val = kv.getKey().intValue();
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            } else {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    val = kv.getKey().intValue();
                                    break;
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            }
                        }
                    }
                    break;
                default:
                    if (count == size) {
                        boolean isShun = true;
                        int pre = 0;
                        int current = 0;
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            val = kv.getKey().intValue();
                            if (current == 0) {
                                current = val;
                            } else {
                                if (pre == 0) {
                                    pre = current;
                                }
                                current = val;
                                if (current >= 15 || current - pre != 1) {
                                    isShun = false;
                                    break;
                                }
                            }
                        }
                        if (isShun) {
                            retList.addAll(curList);
                        }
                    }else if (count*2==size){
                        boolean isShun = true;
                        int pre = 0;
                        int current = 0;
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue()!=2){
                                isShun=false;
                                break;
                            }
                            val = kv.getKey().intValue();
                            if (current == 0) {
                                current = val;
                            } else {
                                if (pre == 0) {
                                    pre = current;
                                }
                                current = val;
                                if (current >= 15 || current - pre != 1) {
                                    isShun = false;
                                    break;
                                }
                            }
                        }
                        if (isShun) {
                            retList.addAll(curList);
                        }
                    }

                    val = 0;
                    if (retList.size() == 0) {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            int c = kv.getValue().intValue();
                            if (c == 3) {
                                val = kv.getKey().intValue();
                                break;
                            }
                        }
                        if (val > 0) {
                            List<Integer> daiValues = new ArrayList<>();
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                int c = kv.getValue().intValue();
                                int val0 = kv.getKey().intValue();
                                if (c == 1) {
                                    daiValues.add(val0);
                                }
                            }
                            if (daiValues.size() >= 2) {
                                retList.addAll(CardTool.loadCards(curList, val));
                                retList.add(CardTool.loadCards(curList, daiValues.get(0).intValue()).get(0));
                                retList.add(CardTool.loadCards(curList, daiValues.get(1).intValue()).get(0));
                            } else {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    int c = kv.getValue().intValue();
                                    int val0 = kv.getKey().intValue();
                                    if (c == 2) {
                                        daiValues.add(val0);
                                    }
                                }
                                if (daiValues.size() >= 2) {
                                    retList.addAll(CardTool.loadCards(curList, val));
                                    retList.add(CardTool.loadCards(curList, daiValues.get(0).intValue()).get(0));
                                    retList.add(CardTool.loadCards(curList, daiValues.get(1).intValue()).get(0));
                                } else {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        int c = kv.getValue().intValue();
                                        int val0 = kv.getKey().intValue();
                                        if (c == 3 && val != val0) {
                                            daiValues.add(val0);
                                        }
                                    }

                                    if (daiValues.size()<2){
                                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                            int c = kv.getValue().intValue();
                                            int val0 = kv.getKey().intValue();
                                            if (c == 4) {
                                                retList = CardTool.loadCards(curList, val0);
                                            }
                                        }
                                    }

                                    if (retList.size()==0) {
                                        retList.addAll(CardTool.loadCards(curList, val));

                                        if (daiValues.size() > 1) {
                                            retList.add(CardTool.loadCards(curList, daiValues.get(0).intValue()).get(0));
                                            if (daiValues.size() > 2) {
                                                retList.add(CardTool.loadCards(curList, daiValues.get(1).intValue()).get(0));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (retList.size() == 0) {
                            val = 0;
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                int c = kv.getValue().intValue();
                                if (c == 2) {
                                    val = kv.getKey().intValue();
                                    break;
                                }
                            }
                            if (val == 0) {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    int c = kv.getValue().intValue();
                                    if (c == 3) {
                                        val = kv.getKey().intValue();
                                        break;
                                    }
                                }
                            }
                            if (val > 0) {
                                List<Integer> list0 = CardTool.loadCards(curList, val);
                                retList.add(list0.get(0));
                                retList.add(list0.get(1));
                            } else {
                                if (nextDan) {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        if (kv.getValue().intValue() == 4) {
                                            val = kv.getKey().intValue();
                                            retList.addAll(CardTool.loadCards(curList, val));
                                            break;
                                        } else {
                                            val = kv.getKey().intValue();
                                        }
                                    }
                                    if (retList.size() == 0) {
                                        retList.add(CardTool.loadCards(curList, val).get(0));
                                    }
                                } else {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        val = kv.getKey().intValue();
                                        break;
                                    }
                                    retList.add(CardTool.loadCards(curList, val).get(0));
                                }
                            }
                        }
                    }
            }
        } else {
            if (nextDan&&oppo.size()==1){
                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                    if (kv.getValue().intValue() == 4) {
                        val = kv.getKey().intValue();
                        retList.addAll(CardTool.loadCards(curList, val));
                        break;
                    } else {
                        val = kv.getKey().intValue();
                    }
                }
                if (retList.size()==0&&val>CardTool.loadCardValue(oppo.get(0))){
                    retList.add(CardTool.loadCards(curList,val).get(0));
                }
            }else{
                CardUtils.Result result = CardUtils.calcCardValue(CardUtils.loadCards(oppo),table.getSiDai());
                if (result.getType()>0){
                    List<CardValue> cardValueList = CardUtils.searchBiggerCardValues(CardUtils.loadCards(curList),result);
                    if (cardValueList!=null&&cardValueList.size()>0){
                        result = CardUtils.calcCardValue(cardValueList,table.getSiDai());
                        if ((result.getType()==11||result.getType()==22||result.getType()==33)&&result.getMax()>=15){
                            return retList;
                        }else{
                            retList = CardUtils.loadCardIds(cardValueList);
                        }
                    }
                }
            }
        }

        return retList;
    }

}

