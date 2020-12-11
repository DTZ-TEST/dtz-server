package com.sy599.game.qipai.doudizhu.tool;

import com.sy599.game.qipai.doudizhu.bean.Card_index;
import com.sy599.game.qipai.doudizhu.bean.Model;
import com.sy599.game.qipai.doudizhu.rule.CardType;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;

import java.util.*;

/**
 * 规则 查询牌型 等等
 *
 * @author hyz
 */
public class CardTypeTool {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(415, 315, 215, 314, 214, 114, 213, 309, 209, 313, 212, 411, 110,106,106, 405, 404, 403, 103));
        ArrayList<Integer> oppo = new ArrayList<>(Arrays.asList( 215, 115));

//        List<Integer> paiStr = departByNum(list);
        int magna = 9;
        List<Integer> purList = removeMagna(list, magna);
        setOrder(list);
        Model model = new Model();
        getTwo(list, model);
        getThree(list, model);
        getFour(list, model);
        getSingle(list, model);
        get123(list, model);
        get112233(model);
        get111222(model);
        getWangZha(list, model);
        Model model2 = new Model();
        // 补充癞子玩法里的牌型
        List<List<Integer>> numList = departByNumber(purList);
        addMagna(list, model, magna, model2, numList);
        Model modelSingle = new Model();
        CardTypeTool.checkModel(list, model2, modelSingle);
        System.out.println(model.a2);
        System.out.println(model2.a2);
        screen(list, model2, magna);
        System.out.println(model.a2);
        System.out.println(model2.a2);
        List<Integer> reso = getBestAI(list, oppo);
        List<Integer> res = getBestAI2(list, oppo, CardType.c2.getType(), magna);
        System.out.println(reso);
        System.out.println(res);
    }

    private static void get111222(Model model) {
        model.a111222.addAll(getPlane(model.a3));
    }

    private static void get112233(Model model) {
        model.a112233.addAll(getTwoTwo(model.a2));
    }

    private static void get123(List<Integer> purList, Model model) {
        model.a123.addAll(get123(purList));
    }

    private static void getFour(List<Integer> purList, Model model) {
        model.a4.addAll(getFour(purList));
    }

    private static void getThree(List<Integer> purList, Model model) {
        model.a3 = getMaxThreeLian(purList);
    }

    private static void getTwo(List<Integer> purList, Model model) {
        model.a2 = getTwo(purList);
    }

    public static boolean ifCanPlay(List<Integer> from, List<Integer> oppo, int magnaCard) {
        if (ifCanPlay(from, oppo)) {
            return true;
        }
        if (magnaCard <= 0 || magnaCard > 15) {
            return false;
        } else {
            // 正常打不起，看加上癞子牌是否打得起
            List<Integer> myCards = new ArrayList<>(from);
            List<Integer> copy = new ArrayList<>(from);
            List<Integer> hisCards = new ArrayList<>(oppo);
            List<Integer> magna = new ArrayList<>();
            // 先知道手上癞子牌的数量
            for (Integer card : myCards) {
                if (card % 100 == magnaCard) {
                    magna.add(card);
                    copy.remove(card);
                }
            }
            int len = magna.size();
            // 没有癞子牌或只有癞子牌是没法变的
            if (len == 0 || copy.isEmpty()) {
                return false;
            }
            // 按从大到小的顺序排序
            setOrder(myCards);
            // 按重复次数排序
            hisCards = getOrder2(hisCards);

            CardType cardType2 = jugdeType(hisCards);
            // 对手牌是王炸，肯定打不起
            if (cardType2 == CardType.c1617) {
                return false;
            }
            List<Integer> magnaCards = getMagnaCards(magnaCard);
            // 对手普通炸弹，则必须有四张癞子牌炸弹
            if (cardType2 == CardType.c4) {
                return myCards.containsAll(magnaCards);
            }
            // 对手普通牌型，先检查自己有没有癞子牌炸弹
            List<Integer> laiziBooms = getMaxLaiziBoom(myCards, magnaCard);
            if (!laiziBooms.isEmpty()) {
                return true;
            }
            // 对手普通牌型则必须手牌数大于等于对手出的牌数
            if (from.size() < oppo.size()) {
                return false;
            }
            // 单张 癞子牌不可变
            if (cardType2 == CardType.c1) {
                return false;
            }
            // 对子，只要有一张更大的单牌
            if (cardType2 == CardType.c2) {
                return getTrueValue(copy.get(0)) > getTrueValue(hisCards.get(0));
            }
            if (cardType2 == CardType.c3 || cardType2 == CardType.c31) {
                if (len == 1) {
                    List<Integer> twoList = getTwo(copy, 1);
                    return !twoList.isEmpty() && getTrueValue(twoList.get(0)) > getTrueValue(hisCards.get(0));
                } else if (len == 2) {
                    return getTrueValue(copy.get(0)) > getTrueValue(hisCards.get(0));
                }
            }
            if (cardType2 == CardType.c32) {
                if (getTrueValue(getMaxThree(myCards).get(0)) > getTrueValue(hisCards.get(0))) {
                    return true;
                }
                if (len == 1) {
                    List<Integer> twoList = getTwo(copy, 2);
                    if (twoList.size() == 2) {
                        return getTrueValue(twoList.get(0)) > getTrueValue(hisCards.get(0));
                    }
                } else if (len == 2) {
                    List<Integer> twoList = getTwo(copy, 1);
                    if (!twoList.isEmpty()) {
                        return getTrueValue(copy.get(0)) > getTrueValue(hisCards.get(0));
                    }
                }
            }
            // 顺子
            if (cardType2 == CardType.c123) {
                List<Integer> valueList = changeTrueValue(copy);
                if (len == 1) {
                    for (int j = 3; j < 15; ++j) {
                        if (valueList.contains(j)) {
                            continue;
                        }
                        List<Integer> afterChangeCards = new ArrayList<>(copy);
                        afterChangeCards.add(600 + j);
                        List<Integer> shunZi = getShunZi(afterChangeCards, hisCards.size());
                        if (!shunZi.isEmpty() && getTrueValue(shunZi.get(0)) > getTrueValue(hisCards.get(0))) {
                            return true;
                        }
                    }
                } else if (len == 2) {
                    for (int i = 3; i < 15; ++i) {
                        for (int j = 3; j < 15; ++j) {
                            if (valueList.contains(i) || valueList.contains(j) || i == j) {
                                continue;
                            }
                            List<Integer> afterChangeCards = new ArrayList<>(copy);
                            afterChangeCards.add(600 + i);
                            afterChangeCards.add(600 + j);
                            List<Integer> shunZi = getShunZi(afterChangeCards, hisCards.size());
                            if (!shunZi.isEmpty() && getTrueValue(shunZi.get(0)) > getTrueValue(hisCards.get(0))) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            // 连对
            if (cardType2 == CardType.c1122) {
                List<Integer> lianDui = getLianDui(copy, hisCards.size() / 2 - 1);
                if (lianDui.isEmpty() && len == 1) {
                    return false;
                }
                List<Integer> valueList = changeTrueValue(lianDui);
                List<Integer> canChangeList = new ArrayList<>();
                if (!lianDui.isEmpty()) {
                    int max = valueList.get(0);
                    int min = valueList.get(valueList.size() - 2);
                    for (int i = 1; i <= len; ++i) {
                        canChangeList.add(600 + max + i);
                        canChangeList.add(600 + min - i);
                    }
                    for (int card : canChangeList) {
                        if (card < 3 || card >= 15) {
                            continue;
                        }
                        List<Integer> afterChangeCards = new ArrayList<>(copy);
                        afterChangeCards.add(card);
                        List<Integer> newLianDui = getLianDui(afterChangeCards, hisCards.size() / 2);
                        if (!newLianDui.isEmpty() && getTrueValue(newLianDui.get(0)) > getTrueValue(hisCards.get(0))) {
                            return true;
                        }
                    }
                }
                if (len == 1) {
                    return false;
                }
                canChangeList.clear();
                List<Integer> twoList = getTwo(copy, copy.size() / 2);
                // 一对都没有无法组成连对
                if (twoList.isEmpty()) {
                    return false;
                }
                for (int i = 0; i < twoList.size() - 2; i += 2) {
                    if (getTrueValue(twoList.get(i)) - getTrueValue(twoList.get(i + 2)) == 2) {
                        canChangeList.add(600 + getTrueValue(twoList.get(i)) + 1);
                    }
                }
                if (!canChangeList.isEmpty()) {
                    for (int card : canChangeList) {
                        if (card < 3 || card >= 15) {
                            continue;
                        }
                        List<Integer> afterChangeCards = new ArrayList<>(copy);
                        afterChangeCards.add(card);
                        List<Integer> newLianDui = getLianDui(afterChangeCards, hisCards.size() / 2);
                        if (!newLianDui.isEmpty() && getTrueValue(newLianDui.get(0)) > getTrueValue(hisCards.get(0))) {
                            return true;
                        }
                    }
                }
            }

            // 飞机，飞机带单
            int type = cardType2.getType();
            if (cardType2 == CardType.c111222 || cardType2 == CardType.c11122234) {
                List<Integer> canChangeList = new ArrayList<>();
                // 获得自己所有的三张
                List<List<Integer>> threeList = getThree(copy, copy.size() / 3);
                if (threeList.size() < (cardType2 == CardType.c111222 ? hisCards.size() / 3 : hisCards.size() / 4)) {
                    return false;
                }
                List<Integer> threeValueList = new ArrayList<>();
                List<Integer> valueList = new ArrayList<>();
                for (List<Integer> aThreeList : threeList) {
                    threeValueList.add(getTrueValue(aThreeList.get(0)));
                }
                for (Integer aCopy : copy) {
                    valueList.add(getTrueValue(aCopy));
                }
                for (int i = 3; i < 15; i++) {
                    if (threeValueList.contains(i)) {
                        continue;
                    }
                    if (!valueList.contains(i)) {
                        continue;
                    }
                    canChangeList.add(i);
                }
                if (canChangeList.isEmpty()) {
                    return false;
                }
                if (!canChangeList.isEmpty()) {
                    for (int card : canChangeList) {
                        if (card < 3 || card >= 15) {
                            continue;
                        }
                        List<Integer> afterChangeCards = new ArrayList<>(copy);
                        afterChangeCards.add(card);
                        List<Integer> newLianDui = getLianDui(afterChangeCards, hisCards.size() / 2);
                        if (!newLianDui.isEmpty() && getTrueValue(newLianDui.get(0)) > getTrueValue(hisCards.get(0))) {
                            return true;
                        }
                    }
                }
                return false;
            }
            if (cardType2 == CardType.c1112223344) {
                List<List<Integer>> threeLianList = getThreeLian(copy, hisCards.size() / 5);
                // 不缺飞机
                if (!threeLianList.isEmpty()) {
                    for (List<Integer> feiji : threeLianList) {
                        if (getTrueValue(feiji.get(0)) > getTrueValue(hisCards.get(0))) {
                            List<Integer> twoList = getTwo(copy, 2);
                            if (twoList.size() / 2 == 1) {
                                return true;
                            }
                            if (twoList.size() / 2 == 0 && len == 2) {
                                return true;
                            }
                        }
                    }
                }
                // 缺飞机
                List<Integer> canChangeList = new ArrayList<>();
                // 获得自己所有的三张
                List<List<Integer>> threeList = getThree(copy, copy.size() / 3);
                if (threeList.size() < hisCards.size() / 5) {
                    return false;
                }
                List<Integer> threeValueList = new ArrayList<>();
                List<Integer> valueList = new ArrayList<>();
                for (List<Integer> aThreeList : threeList) {
                    threeValueList.add(getTrueValue(aThreeList.get(0)));
                }
                for (Integer aCopy : copy) {
                    valueList.add(getTrueValue(aCopy));
                }
                for (int i = 3; i < 15; i++) {
                    if (threeValueList.contains(i)) {
                        continue;
                    }
                    if (!valueList.contains(i)) {
                        continue;
                    }
                    canChangeList.add(i);
                }
                if (canChangeList.isEmpty()) {
                    return false;
                }
                if (!canChangeList.isEmpty()) {
                    for (int card : canChangeList) {
                        if (card < 3 || card >= 15) {
                            continue;
                        }
                        List<Integer> afterChangeCards = new ArrayList<>(copy);
                        afterChangeCards.add(card);
                        List<Integer> newLianDui = getLianDui(afterChangeCards, hisCards.size() / 2);
                        if (!newLianDui.isEmpty() && getTrueValue(newLianDui.get(0)) > getTrueValue(hisCards.get(0))) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        return false;
    }

    private static List<Integer> getMagnaCards(int magnaCard) {
        List<Integer> result = new ArrayList<>();
        result.add(100 + magnaCard);
        result.add(200 + magnaCard);
        result.add(300 + magnaCard);
        result.add(400 + magnaCard);
        return result;
    }

    private static List<Integer> changeTrueValue(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        List<Integer> result = new ArrayList<>();
        for (int i : list) {
            if (i < 100) {
                return list;
            }
            result.add(i % 100);
        }
        return result;
    }

    /**
     * 根据癞子牌的数量，检查出最大的癞子牌炸弹
     */
    private static List<Integer> getMaxLaiziBoom(List<Integer> myCards, int magnaCard) {
        List<Integer> maxBoom = new ArrayList<>();
        List<Integer> copy = new ArrayList<>(myCards);
        int len = 0;
        for (Integer card : myCards) {
            if (card % 100 == magnaCard) {
                ++len;
                copy.remove((Object) card);
            }
        }
        if (len == 0 || (len < 4 && copy.isEmpty())) {
            return maxBoom;
        }
        switch (len) {
            case 1:
                List<Integer> threeList = getMaxThreeLian(copy, 1);
                if (!threeList.isEmpty()) {
                    maxBoom.addAll(threeList);
                    maxBoom.add(threeList.get(0));
                }
                break;
            case 2:
                List<Integer> twoList = getTwo(copy, 1);
                if (!twoList.isEmpty()) {
                    maxBoom.addAll(twoList);
                    maxBoom.addAll(twoList);
                }
                break;
            case 3:
                for (int i = 1; i < 5; ++i) {
                    maxBoom.add(copy.get(0));
                }
                break;
            case 4:
                for (int i = 1; i < 5; ++i) {
                    maxBoom.add(i * 100 + magnaCard);
                }
                break;
        }
        return maxBoom;
    }

    /**
     * 根据癞子牌值或其他牌获得癞子牌
     */
    private static int getMagnaCard(int number) {
        if (number <= 0) {
            return 0;
        }
        int value = number;
        if (number > 100) {
            value = number % 100;
        }
        return 600 + value;
    }

    /**
     * 能否要的起
     *
     * @param from 自己的手牌
     * @param oppo 对手出的牌
     */
    public static boolean ifCanPlay(List<Integer> from, List<Integer> oppo) {
        List<Integer> myCards = new ArrayList<>(from);
        List<Integer> hisCards = new ArrayList<>(oppo);
        setOrder(myCards);
        hisCards = getOrder2(hisCards);
        // 获得我拥有的最大炸弹
        List<Integer> list = getMaxBoom(myCards);
        CardType cardType2 = jugdeType(hisCards);
        // 对手王炸
        if (cardType2 == CardType.c1617) {
            return false;
        }
        // 对手炸弹
        if (cardType2 == CardType.c4) {
            // 对手普通炸弹，尚可一搏
            return list.size() == 2 || (list.size() == 4 && getTrueValue(list.get(0)) > getTrueValue(hisCards.get(0)));
        }
        // 对手非炸 ，我有炸弹
        if (!list.isEmpty()) {
            return true;
        }
        // 对手非炸，我也没炸
        if (myCards.size() >= hisCards.size()) {
            // 我没炸弹，相同牌型有且最大值比对手大
            if (cardType2 == CardType.c1) {
                return getTrueValue(myCards.get(0)) > getTrueValue(hisCards.get(0));
            } else if (cardType2 == CardType.c2) {
                List<Integer> tempList = getMaxTwo(myCards, null);
                if (!tempList.isEmpty()) {
                    return getTrueValue(tempList.get(0)) > getTrueValue(hisCards.get(0));
                }
            } else if (cardType2 == CardType.c3 || cardType2 == CardType.c31) {
                List<Integer> tempList = getMaxThree(myCards);
                if (!tempList.isEmpty()) {
                    return getTrueValue(tempList.get(0)) > getTrueValue(hisCards.get(0));
                }
            } else if (cardType2 == CardType.c32) {
                List<Integer> tempList = getMaxThree(myCards);
                List<Integer> tempList2 = getTwo(myCards, 1, tempList);
                if (!tempList.isEmpty() && !tempList2.isEmpty()) {
                    return getTrueValue(tempList.get(0)) > getTrueValue(hisCards.get(0));
                }
            } else if (cardType2 == CardType.c123) {
                List<Integer> tempList = getShunZi(myCards, hisCards.size());
                if (!tempList.isEmpty()) {
                    return getTrueValue(tempList.get(0)) > getTrueValue(hisCards.get(0));
                }
            } else if (cardType2 == CardType.c1122) {
                // 连对
                List<Integer> tempList = getLianDui(myCards, hisCards.size() / 2);
                if (!tempList.isEmpty()) {
                    return getTrueValue(tempList.get(0)) > getTrueValue(hisCards.get(0));
                }
            } else if (cardType2 == CardType.c111222) {
                List<Integer> tempList = getMaxThreeLian(myCards, hisCards.size() / 3);
                if (!tempList.isEmpty()) {
                    return getTrueValue(tempList.get(0)) > getTrueValue(hisCards.get(0));
                }
            } else if (cardType2 == CardType.c11122234) {
                List<Integer> tempList = getMaxThreeLian(myCards, hisCards.size() / 4);
                if (!tempList.isEmpty()) {
                    return getTrueValue(tempList.get(0)) > getTrueValue(hisCards.get(0));
                }
            } else if (cardType2 == CardType.c1112223344) {
                List<Integer> tempList = getMaxThreeLian(myCards, hisCards.size() / 5);
                List<Integer> tempList2 = getTwo(myCards, 2, tempList);
                if (!tempList.isEmpty() && !tempList2.isEmpty()) {
                    return getTrueValue(tempList.get(0)) > getTrueValue(hisCards.get(0));
                }
            }
        }
        return false;
    }

    /**
     * 获得我手中的指定数目最大连对
     */
    private static List<Integer> getLianDui(List<Integer> myCards, int num) {

        List<Integer> result = new ArrayList<>();
        if (myCards.size() / 2 < num) {
            return result;
        }
        List<List<Integer>> lists = new ArrayList<>();
        for (int i = 0, len = myCards.size(); i < len; ++i) {
            if (getTrueValue(myCards.get(i)) > 14) {
                continue;
            }
            if (i + 1 >= len) {
                break;
            }
            if (getTrueValue(myCards.get(i)) == getTrueValue(myCards.get(i + 1))) {
                List<Integer> list = new ArrayList<>();
                list.add(myCards.get(i));
                list.add(myCards.get(i + 1));
                lists.add(list);
                i += 1;
            }
        }
        if (lists.size() < num) {
            return result;
        }
        for (int i = 0; i < lists.size() - num + 1; ++i) {
            boolean isLian = true;
            for (int j = i; j < i + num - 1; ++j) {
                if (getTrueValue(lists.get(j).get(0) - 1) != getTrueValue(lists.get(j + 1).get(0))) {
                    isLian = false;
                }
            }
            if (isLian) {
                for (int j = i; j < i + num; ++j) {
                    result.addAll(lists.get(j));
                }
                return result;
            }
        }
        return result;
    }

    /**
     * 获得指定牌张数的最大顺子（2以上除去）
     *
     * @param num 指定张数
     */
    private static List<Integer> getShunZi(List<Integer> myCards, int num) {
        List<Integer> list = new ArrayList<>();
        if (myCards.size() < num) {
            return list;
        }
        List<Integer> noSameList = new ArrayList<>();
        List<Integer> valueList = new ArrayList<>();
        // 得到没有重复值的集合
        for (Integer card : myCards) {
            // 不包含王和2
            if (getTrueValue(card) < 15) {
                if (!valueList.contains(getTrueValue(card))) {
                    valueList.add(card % 100);
                    noSameList.add(card);
                }
            }
        }
        if (noSameList.size() >= num) {
            for (int i = 0; i < noSameList.size() - num + 1; ++i) {
                if (valueList.get(i) - num + 1 == valueList.get(i + num - 1)) {
                    return noSameList.subList(i, i + num - 1);
                }
            }
        }
        return list;
    }

    /**
     * 获得手牌中所有的三张
     */
    public static List<Integer> getThree(List<Integer> myCards) {
        List<Integer> result = new ArrayList<>();
        List<List<Integer>> threeLists = getThree(myCards, myCards.size() / 3);
        if (!threeLists.isEmpty()) {
            for (List<Integer> list : threeLists) {
                result.addAll(list);
            }
        }
        return result;
    }

    /**
     * 获得手中最大的三张
     */
    public static List<Integer> getMaxThree(List<Integer> myCards) {
        List<Integer> result = new ArrayList<>();
        List<List<Integer>> threeLists = getThree(myCards, myCards.size() / 3);
        if (!threeLists.isEmpty())
            result = threeLists.get(0);
        return result;
    }

    /**
     * 返回指定数目的三张，如果小于指定数目则返回所有的三张
     */
    public static List<List<Integer>> getThree(List<Integer> myCards, int num) {
        List<List<Integer>> lists = new ArrayList<>();
        if (myCards == null || myCards.size() < 3) {
            return lists;
        }
        for (int i = 0, len = myCards.size(); i < len; ++i) {
            if (i + 2 >= len) {
                break;
            }
            if (getTrueValue(myCards.get(i)) == getTrueValue(myCards.get(i + 2))) {
                List<Integer> list = new ArrayList<>();
                list.add(myCards.get(i));
                list.add(myCards.get(i + 1));
                list.add(myCards.get(i + 2));
                lists.add(list);
                if (num == 1) {
                    return lists;
                }
                i += 2;
            }
        }
        return lists;
    }

    /**
     * 获得指定长度，所有的三连
     *
     * @param length 飞机的长度
     */
    public static List<List<Integer>> getThreeLian(List<Integer> myCards, int length) {
        List<List<Integer>> threeLianList = new ArrayList<>();
        List<List<Integer>> lists = new ArrayList<>();
        // 获得所有的三张
        lists = getThree(myCards, myCards.size() / 3);
        if (lists.size() < length) {
            return threeLianList;
        }

        for (int i = 0; i < lists.size() - length + 1; ++i) {
            if (getTrueValue(lists.get(i).get(0)) > 14) {
                continue;
            }
            boolean isThreeLian = true;
            for (int j = i; j < i + length - 1; ++j) {
                if (getTrueValue(lists.get(j).get(0) - 1) != getTrueValue(lists.get(j + 1).get(0))) {
                    isThreeLian = false;
                }
            }
            if (isThreeLian) {
                List<Integer> threeLian = new ArrayList<>();
                for (int j = i; j < i + length; ++j) {
                    threeLian.addAll(lists.get(j));
                }
                threeLianList.add(threeLian);
            }
        }
        return threeLianList;
    }

    /**
     * 获得我手上指定数目的三连的牌集(由大到小)
     */
    public static List<Integer> getMaxThreeLian(List<Integer> myCards, int num) {
        List<Integer> result = new ArrayList<>();
        if (myCards == null || myCards.size() < 3) {
            return result;
        }
        List<List<Integer>> lists;
        // 获得所有的三张
        lists = getThree(myCards, myCards.size() / 3);
        if (lists.size() < num) {
            return result;
        }
        for (int i = 0; i < lists.size() - num + 1; ++i) {
            if (getTrueValue(lists.get(i).get(0)) > 14) {
                continue;
            }
            boolean isThreeLian = true;
            for (int j = i; j < i + num - 1; ++j) {
                if (getTrueValue(lists.get(j).get(0) - 1) != getTrueValue(lists.get(j + 1).get(0))) {
                    isThreeLian = false;
                }
            }
            if (isThreeLian) {
                for (int j = i; j < i + num; ++j) {
                    result.addAll(lists.get(j));
                }
                return result;
            }
        }
        return result;
    }

    private static List<Integer> getTwo(List<Integer> myCards, int num) {
        return getTwo(myCards, num, null);
    }

    public static List<Integer> getMaxTwo(List<Integer> myCards, List<Integer> withoutList) {
        return getTwo(myCards, 1, withoutList);
    }

    /**
     * 获得我手上的指定数目的对子,如果不够则返回全部的对子
     */
    public static List<Integer> getTwo(List<Integer> myCards, int num, List<Integer> withoutList) {
        List<Integer> result = new ArrayList<>();
        if (myCards.size() < 2) {
            return result;
        }
        List<Integer> copy = new ArrayList<>(myCards);
        if (withoutList != null && !withoutList.isEmpty()) {
            copy.removeAll(withoutList);
        }
        for (int i = 0, len = copy.size() - 1; i < len; ++i) {
            if (getTrueValue(copy, i) == getTrueValue(copy, i + 1)) {
                List<Integer> pairs = new ArrayList<>();
                pairs.add(copy.get(i));
                pairs.add(copy.get(i + 1));
                result.addAll(pairs);
                if (result.size() / 2 >= num) {
                    return result;
                }
            }
        }
//        if (result.size() / 2 < num) {
//            return new ArrayList<>();
//        }
        return result;
    }

    /**
     * 获得集合中指定位置的值(使用前需保证myCards的存在下标为i位置的元素)
     */
    private static int getTrueValue(List<Integer> myCards, int i) {
        int value = 0;
        int originElement = myCards.get(i);
        if (originElement > 100) {
            value = myCards.get(i) % 100;
        } else {
            value = myCards.get(i);
        }
        return value;
    }

    /**
     * 获得最大的炸弹
     */
    private static List<Integer> getMaxBoom(List<Integer> myCards) {

        List<Integer> boom = new ArrayList<>();
        if (myCards == null || myCards.size() < 2) {
            return boom;
        }
        // 王炸
        if (getTrueValue(myCards.get(0)) == 17 && getTrueValue(myCards.get(1)) == 16) {
            boom.add(myCards.get(0));
            boom.add(myCards.get(1));
            return boom;
        }
        // 一般炸弹
        for (int i = 0, len = myCards.size(); i < len; ++i) {
            if (i + 3 >= len) {
                break;
            }
            if (getTrueValue(myCards.get(i)) == getTrueValue(myCards.get(i + 3))) {
                boom = new ArrayList<>();
                boom.add(myCards.get(i));
                boom.add(myCards.get(i + 1));
                boom.add(myCards.get(i + 2));
                boom.add(myCards.get(i + 3));
                return boom;
            }
        }
        return boom;
    }

    /**
     * 获得所有顺子
     *
     * @param num 连张数量
     * @return 返回达到数量要求的最长的连对
     */
    private static List<String> getLiandui(List<Integer> copy, List<String> paiStr, int num) {
        List<Integer> copy2 = new ArrayList<>(copy);
        String single = paiStr.get(0);
        if (!StringUtil.isBlank(single)) {
            List<Integer> singleList = stringToList(single);
            for (Integer value : singleList) {
                copy2.remove(findCardByValue(copy2, value));
            }
        }
        return getShunzi(copy2, num);
    }

    private static Integer findCardByValue(List<Integer> copy, Integer value) {
        if (copy == null || copy.isEmpty()) {
            return -1;
        }
        for (Integer card : copy) {
            if (getTrueValue(card) == value) {
                return card;
            }
        }
        return -1;
    }

    private static List<Integer> stringToList(String single) {
        String[] strs = single.split(",");
        List<Integer> result = new ArrayList<>();
        for (String str : strs) {
            result.add(Integer.parseInt(str));
        }
        return result;
    }

    /**
     * 获得所有顺子
     *
     * @param num 连张数量
     * @return 返回达到数量要求的最长的顺子
     */
    private static List<String> getShunzi(List<Integer> copy, int num) {
        List<Integer> notSameValueList = getNotSameValueList(copy);
        int size = notSameValueList.size();
        if (size < num) {
            return null;
        }
        List<String> result = null;
        setOrder(notSameValueList);
        for (int i = 0; i + num - 1 < size; i++) {
            int j = i;
            if (j + 1 >= size) {
                continue;
            }
            int count = 1;
            do {
                int val1 = notSameValueList.get(j);
                int val2 = notSameValueList.get(j + 1);
                if (val1 - 1 == val2) {
                    count++;
                    j++;
                } else {
                    break;
                }
            } while (j + 1 < size);
            if (count >= num) {
                StringBuilder sb = new StringBuilder("");
                for (int k = i; k < j + 1; ++k) {
                    sb.append(notSameValueList.get(k)).append(",");
                }
                sb.deleteCharAt(sb.lastIndexOf(","));
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(sb.toString());
                i = j;
            }
        }
        return result;
    }

    private static String listToString(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Integer value : list) {
            sb.append(value).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    private static List<Integer> getNotSameValueList(List<Integer> copy) {
        if (copy == null || copy.isEmpty()) {
            return null;
        }
        List<Integer> valueList = getValueList(copy);
        List<Integer> result = new ArrayList<>();
        for (Integer value : valueList) {
            if (!result.contains(value)) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * 分离得到癞子牌的数量和一个不含癞子牌的手牌 集合
     */
    private static int departMagna(List<Integer> copy, int magna) {
        List<Integer> copy2 = new ArrayList<>(copy);
        int count = 0;
        for (Integer card : copy2) {
            if (magna == getTrueValue(card)) {
                count++;
                copy.remove(card);
            }
        }
        return count;
    }

    /**
     * 得到所有的炸弹
     */
    private static List<String> getAllBoom(List<String> list) {
        List<String> boomList = new ArrayList<>();
        String boomStr = list.get(3);
        if (!StringUtil.isBlank(boomStr)) {
            String[] strs = boomStr.split(",");
            boomList.addAll(Arrays.asList(strs));
            return boomList;
        }
        return null;
    }

    private static void getWangZha(List<Integer> list, Model model) {
        model.a1617 = getWangZha(list);
    }

    private static List<String> getWangZha(List<Integer> list) {
        List<String> res = new ArrayList<>();
        Integer card1 = list.get(0);
        Integer card2 = list.get(1);
        if (getTrueValue(card1) == 17 && getTrueValue(card2) == 16) {
            res.add(card1 + "," + card2);
        }
        return res;
    }

    /**
     * 按个数分离
     */
    private static List<String> departByNum(List<Integer> my) {
        List<Integer> copy = new ArrayList<>(my);
        List<Integer> valueList = getValueList(copy);
        List<String> list = new ArrayList<>();

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        StringBuilder sb3 = new StringBuilder();
        StringBuilder sb4 = new StringBuilder();
        for (Integer value : valueList) {
            int index1 = valueList.indexOf(value);
            int index2 = valueList.lastIndexOf(value);
            int num = index2 - index1 + 1;
            if (num == 1) {
                sb1.append(value).append(",");
            } else if (num == 2) {
                sb2.append(value).append(",");
            } else if (num == 3) {
                sb3.append(value).append(",");
            } else if (num == 4) {
                sb4.append(value).append(",");
            }
        }
        list.add(sb1.length() == 0 ? "" : sb1.deleteCharAt(sb1.lastIndexOf(",")).toString());
        list.add(sb2.length() == 0 ? "" : sb2.deleteCharAt(sb2.lastIndexOf(",")).toString());
        list.add(sb3.length() == 0 ? "" : sb3.deleteCharAt(sb3.lastIndexOf(",")).toString());
        list.add(sb4.length() == 0 ? "" : sb4.deleteCharAt(sb4.lastIndexOf(",")).toString());
        return list;
    }

    /**
     * 按牌张数牌值分离
     */
    private static int[] depart(List<Integer> my) {
        int[] arr = new int[17];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = 0;
        }
        if (my == null || my.isEmpty()) {
            return arr;
        }
        List<Integer> valueList = getValueList(my);
        for (Integer value : valueList) {
            arr[value]++;
        }
        return arr;
    }

    private static List<Integer> getValueList(List<Integer> my) {
        if (my == null || my.isEmpty()) {
            return null;
        }
        List<Integer> valueList = new ArrayList<>();
        for (Integer card : my) {
            valueList.add(getTrueValue(card));
        }
        return valueList;
    }

    /**
     * 机器人判断出牌
     *
     * @param curList 自己已有的牌
     * @param oppo    对手出的牌
     * @return 出的牌
     */
    public static List<Integer> getBestAI2(List<Integer> curList, List<Integer> oppo, int oppoType, int magna) {
        // 从大到小排序
        setOrder(curList);
        List<Integer> list = new ArrayList<>(curList);
        Model model = new Model();
        List<Integer> purList = removeMagna(list, magna);
        setOrder(list);
        setOrder(purList);
        getTwo(list, model);
        getThree(list, model);
        getFour(list, model);
        getSingle(list, model);
        get123(list, model);
        get112233(model);
        get111222(model);
        getWangZha(list, model);
        Model model2 = new Model();
        // 补充癞子玩法里的牌型
        List<List<Integer>> numList = departByNumber(purList);
        addMagna(list, model, magna, model2, numList);
        screen(list, model, magna);

        List<Integer> showCardsList = null;
        Model myModel = null;
        int value = 0;
        int time = 99;
        if (oppo == null || oppo.isEmpty()) {
            myModel = getBestModel2(magna, list, model, myModel, value, time);
            showCardsList = getShowCardsList(myModel);
        } else {
            robotCheckPai(oppo, list);
            String showCards2 = getShowCards2(model, model2, oppoType, oppo, numList, list, magna);
            if (!showCards2.isEmpty()) {
                showCardsList = stringToList(showCards2);
            }
        }
        return showCardsList;
    }

    private static void robotCheckPai(List<Integer> oppo, List<Integer> list) {
        Map<Integer, Integer> valueMap = new HashMap<>();
        int sameCard = 0;
        for (Integer card : list) {
            if (oppo.contains(card)) {
                sameCard++;
            }
            int value = getTrueValue(card);
            if (valueMap.containsKey(value)) {
                int count = valueMap.get(value);
                count++;
                valueMap.put(value, count);
            } else {
                int count = 1;
                for (Integer card2 : oppo) {
                    if (getTrueValue(card2) == value) {
                        count++;
                    }
                }
                valueMap.put(value, count);
            }
        }
        if (sameCard>0) {
            System.out.println("你出老千！");
        }
        for (Integer key : valueMap.keySet()) {
            int count = valueMap.get(key);
            if (count>4) {
                System.out.println("竟然有"+count+"张"+key+"，你当我傻啊");
            }
        }
    }

    private static String getShowCards2(Model model, Model model2, int oppoType, List<Integer> oppo, List<List<Integer>> numList, List<Integer> my, int magna) {
        String res = "";
        List<String> biggerList = getBiggerList(model, model2, oppoType, oppo, my);
        if (biggerList.isEmpty()) {
            return res;
        } else {
            // 遍历能打得起的集合，选择出牌后重新计算权值与手术，取最优的解决方案
            int time = 99;
            int value = 0;
            Model myModel = null;
            for (String s : biggerList) {
                List<Integer> cardList = stringToList(s);
                List<Integer> copy = new ArrayList<>(my);
                for (Integer card : cardList) {
                    if (card/100 == 6) {
                        removeMagnaCard(copy, magna, 1);
                    }
                    copy.remove(card);
                }
                myModel = getBestModel2(magna, copy, model2, myModel, value, time);
                // 计算手数，计算权值
                int myTime = CardTypeTool.getTimes(myModel);
                if (myTime < time) {
                    time = myTime;
                    res = s;
                } else if (myTime == time && CardTypeTool.getCountValues(myModel) > value) {
                    value = CardTypeTool.getCountValues(myModel);
                    res = s;
                }
            }
            return  res;
//            // 取能打的起集合中最小的
//            String sb = biggerList.get(biggerList.size() - 1);
//            String strs[] = sb.split(",");
//            // 炸弹直接返回
//            if (strs.length == 4) {
//                res = sb;
//                return res;
//            }
//            // 带单带双特殊处理
//            if (oppoType == CardType.c31.getType()) {
//                List<Integer> oneList = numList.get(0);
//                if (!oneList.isEmpty()) {
//                    res = sb + "," + oneList.get(oneList.size() - 1);
//                }
//                if (StringUtil.isBlank(res)) {
//                    List<Integer> twoList = numList.get(1);
//                    if (!twoList.isEmpty()) {
//                        res = sb + "," + twoList.get(twoList.size() - 1);
//                    }
//                }
//                return res;
//            } else if (oppoType == CardType.c32.getType()) {
//                List<Integer> twoList = numList.get(1);
//                if (!twoList.isEmpty()) {
//                    res = sb + "," + twoList.get(twoList.size() - 1);
//                }
//                return res;
//            } else if (oppoType == CardType.c411.getType()) {
//                List<Integer> oneList = numList.get(0);
//                if (oneList.size() >= 2) {
//                    res = sb + "," + oneList.get(oneList.size() - 1) + "," + oneList.get(oneList.size() - 2);
//                }
//                if (StringUtil.isBlank(res)) {
//                    List<Integer> twoList = numList.get(1);
//                    if (!twoList.isEmpty()) {
//                        res = sb + "," + twoList.get(twoList.size() - 1) + "," + twoList.get(twoList.size() - 2);
//                    }
//                }
//                return res;
//            } else if (oppoType == CardType.c422.getType()) {
//                List<Integer> twoList = numList.get(1);
//                if (twoList.size() >= 2) {
//                    res = sb + "," + twoList.get(twoList.size() - 1) + "," + twoList.get(twoList.size() - 2);
//                }
//                return res;
//            } else if (oppoType == CardType.c11122234.getType()) {
//                List<Integer> oneList = numList.get(0);
//                if (oneList.size() >= 2) {
//                    res = sb + "," + oneList.get(oneList.size() - 1) + "," + oneList.get(oneList.size() - 2);
//                }
//                if (StringUtil.isBlank(res)) {
//                    List<Integer> twoList = numList.get(1);
//                    if (!twoList.isEmpty()) {
//                        res = sb + "," + twoList.get(twoList.size() - 1) + "," + twoList.get(twoList.size() - 2);
//                    }
//                }
//                return res;
//            } else if (oppoType == CardType.c1112223344.getType()) {
//                List<Integer> twoList = numList.get(1);
//                if (twoList.size() >= 2) {
//                    res = sb + "," + twoList.get(twoList.size() - 1) + "," + twoList.get(twoList.size() - 2);
//                }
//                return res;
//            } else {
//                return sb;
//            }
        }
    }

    /**
     * 移除癞子牌
     * @param copy  手牌
     * @param magna 癞子牌的值
     * @param num 移除的数量
     */
    private static int removeMagnaCard(List<Integer> copy, int magna, int num) {
        int i = 0;
        for (Integer card : copy) {
            if (getTrueValue(card) == magna) {
                copy.remove(card);
                ++i;
                if (i >= num) {
                    return i;
                }
            }
        }
        return i;
    }

    private static List<String> getBiggerList(Model model, Model model2, int oppoType, List<Integer> oppo, List<Integer> my) {
        List<String> res = new ArrayList<>();
        if (oppoType == CardType.c1617.getType()) {
            return res;
        } else {
            int oppoNum = oppo.size();
            if (oppoType == CardType.c1.getType()) {
                // 选出能打起的一张牌
                List<Integer> boomValueList = new ArrayList<>();
                List<String> biggerList = new ArrayList<>();
                if (!model.a4.isEmpty()) {
                    boomValueList = getBoomValueList(model.a4);
                }
                for (Integer card : my) {
                    if (getTrueValue(card) > oppo.get(0)) {
                        // 2以上炸弹不拆
                        if (boomValueList.contains(getTrueValue(card)) || getTrueValue(card) >= 15) {
                            continue;
                        }
                        biggerList.add(String.valueOf(card));
                    }
                }
                res.addAll(biggerList);
            } else if (oppoType == CardType.c2.getType()) {
                // 选出能打起的两张牌
                res.addAll(getCanPlayStrList(oppo, model.a2, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a2, oppoNum));
            } else if (oppoType == CardType.c3.getType()) {
                res.addAll(getCanPlayStrList(oppo, model.a3, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a3, oppoNum));
            } else if (oppoType == CardType.c31.getType()) {
                oppoNum = oppoNum - 1;
                res.addAll(getCanPlayStrList(oppo, model.a3, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a3, oppoNum));
            } else if (oppoType == CardType.c32.getType()) {
                oppoNum = oppoNum - 2;
                res.addAll(getCanPlayStrList(oppo, model.a3, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a3, oppoNum));
            } else if (oppoType == CardType.c411.getType()) {
                oppoNum = oppoNum - 2;
                res.addAll(getCanPlayStrList(oppo, model.a4, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a4, oppoNum));
            } else if (oppoType == CardType.c422.getType()) {
                oppoNum = oppoNum - 4;
                res.addAll(getCanPlayStrList(oppo, model.a4, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a4, oppoNum));
            } else if (oppoType == CardType.c123.getType()) {
                res.addAll(getCanPlayStrList(oppo, model.a123, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a123, oppoNum));
            } else if (oppoType == CardType.c1122.getType()) {
                res.addAll(getCanPlayStrList(oppo, model.a112233, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a112233, oppoNum));
            } else if (oppoType == CardType.c111222.getType()) {
                res.addAll(getCanPlayStrList(oppo, model.a111222, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a111222, oppoNum));
            } else if (oppoType == CardType.c11122234.getType()) {
                oppoNum = oppoNum * 3 / 4;
                res.addAll(getCanPlayStrList(oppo, model.a111222, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a111222, oppoNum));
            } else if (oppoType == CardType.c1112223344.getType()) {
                oppoNum = oppoNum * 3 / 5;
                res.addAll(getCanPlayStrList(oppo, model.a111222, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a111222, oppoNum));
            } else if (oppoType == CardType.c0.getType()) {
                LogUtil.e("getShowCardsList2 err-->oppoType:" + oppoType);
                return res;
            }
            if (res.isEmpty()) {
                res.addAll(getCanPlayStrList(oppo, model.a4, oppoNum));
                res.addAll(getCanPlayStrList(oppo, model2.a4, oppoNum));
            }
        }
        return res;
    }

    private static List<Integer> getBoomValueList(List<String> a4) {
        List<Integer> result = new ArrayList<>();
        for (String str : a4) {
            String[] strs = str.split(",");
            int cardValue = getTrueValue(Integer.parseInt(strs[0]));
            if (!result.contains(cardValue)) {
                result.add(cardValue);
            }
        }
        return result;
    }

    private static List<String> getCanPlayStrList(List<Integer> oppo, List<String> li, int num) {
        int maxOppoCard = oppo.get(0);
        List<String> result = new ArrayList<>();
        for (String s : li) {
            String[] strs = s.split(",");
            if (strs.length == num) {
                int card = Integer.parseInt(strs[0]);
                if (getTrueValue(card)>getTrueValue(maxOppoCard)) {
                    result.add(s);
                }
            } else if (strs.length > num) {
                for (int i = 0; i + num< strs.length; ++i) {
                    int card = Integer.parseInt(strs[i]);
                    if (getTrueValue(card)>getTrueValue(maxOppoCard))  {
                        StringBuilder sb = new StringBuilder();
                        for (int j = i; j < i+num; j++) {
                            if (StringUtil.isBlank(sb.toString())){
                                sb.append(strs[j]);
                            } else {
                                sb.append(","+strs[j]);
                            }
                        }
                        result.add(sb.toString());
                    }
                }
            }
        }
        return result;
    }

    private static String addStr(String s2, String str) {
        if (StringUtil.isBlank(s2)) {
            s2+=str;
        } else {
            s2+=","+str;
        }
        return s2;
    }

    private static Model getBestModel2(int magna, List<Integer> list, Model model, Model myModel, int value, int time) {
        Model bestModel;
        for (int i = 0, len1 = model.a4.size(); i <= len1; i++) {
            for (int j = 0, len2 = model.a3.size(); j <= len2; j++) {
                for (int k = 0, len3 = model.a2.size(); k <= len3; k++) {
                    for (int l = 0, len4 = model.a123.size(); l <= len4; l++) {
                        for (int m = 0, len5 = model.a112233.size(); m <= len5; m++) {
                            for (int n = 0, len6 = model.a111222.size(); n <= len6; n++) {
                                ArrayList<Integer> newlist = new ArrayList<>(list);
                                bestModel = CardTypeTool.getBestModel2(newlist, model, new int[]{i, j, k, l, m, n}, magna);
                                // 加上单牌
                                for (Integer singleCard : newlist) {
                                    bestModel.a1.add(singleCard.toString());
                                }
                                // 计算手数，计算权值
                                int bestTime = CardTypeTool.getTimes(bestModel);
                                if (bestTime < time) {
                                    time = bestTime;
                                    myModel = bestModel;
                                } else if (bestTime == time && CardTypeTool.getCountValues(bestModel) > value) {
                                    value = CardTypeTool.getCountValues(bestModel);
                                    myModel = bestModel;
                                }
                            }
                        }
                    }
                }
            }
        }
        return myModel;
    }

    private static List<Integer> getShowCardsList(Model model) {
        List<Integer> res = new ArrayList<>();
        int len1 = model.a1.size();
        int len2 = model.a2.size();
        int len3 = model.a3.size();
        int len4 = model.a4.size();
        int len5 = model.a123.size();
        int len6 = model.a112233.size();
        int len7 = model.a111222.size();
        // 有单牌出单牌 有双出双 有三出三
        if (len1 > 0) {
            res.add(Integer.parseInt(model.a1.get(len1 - 1)));
            return res;
        }
        if (len2 > 0) {
            res = stringToList(model.a2.get(len2 - 1));
            return res;
        }
        if (len3 > 0) {
            res = stringToList(model.a3.get(len3 - 1));
            return res;
        }
        if (len5 > 0) {
            res = stringToList(model.a123.get(len5 - 1));
            return res;
        }
        if (len6 > 0) {
            res = stringToList(model.a112233.get(len6 - 1));
            return res;
        }
        if (len7 > 0) {
            res = stringToList(model.a111222.get(len7 - 1));
            return res;
        }
        if (len4 > 0) {
            res = stringToList(model.a4.get(len4 - 1));
            return res;
        }
        return res;
    }

    private static Model getBestModel2(ArrayList<Integer> list2, Model model, int[] n, int magna) {
        Model bestModel = new Model();
        List<Integer> magList = getMagList(list2, magna);
        for (int i = 0; i < n[0]; i++) {
            String s = model.a4.get(i);
            List<Integer> reMag = new ArrayList<>();
            if (isExists2(list2, s, magList, reMag)) {
                bestModel.a4.add(s);
                list2.removeAll(getCardsByName(list2, s));
                magList.removeAll(reMag);
                list2.removeAll(reMag);
            }
        }
        for (int i = 0; i < n[1]; i++) {
            String s = model.a3.get(i);
            List<Integer> reMag = new ArrayList<>();
            if (isExists2(list2, s, magList, reMag)) {
                bestModel.a3.add(s);
                list2.removeAll(getCardsByName(list2, s));
                magList.removeAll(reMag);
                list2.removeAll(reMag);
            }
        }
        for (int i = 0; i < n[2]; i++) {
            String s = model.a2.get(i);
            List<Integer> reMag = new ArrayList<>();
            if (isExists2(list2, s, magList, reMag)) {
                bestModel.a2.add(s);
                list2.removeAll(getCardsByName(list2, s));
                magList.removeAll(reMag);
                list2.removeAll(reMag);
            }
        }
        for (int i = 0; i < n[3]; i++) {
            String s = model.a123.get(i);
            List<Integer> reMag = new ArrayList<>();
            if (isExists2(list2, s, magList, reMag)) {
                bestModel.a123.add(s);
                list2.removeAll(getCardsByName(list2, s));
                magList.removeAll(reMag);
                list2.removeAll(reMag);
            }
        }
        for (int i = 0; i < n[4]; i++) {
            String s = model.a112233.get(i);
            List<Integer> reMag = new ArrayList<>();
            if (isExists2(list2, s, magList, reMag)) {
                bestModel.a112233.add(s);
                list2.removeAll(getCardsByName(list2, s));
                magList.removeAll(reMag);
                list2.removeAll(reMag);
            }
        }
        for (int i = 0; i < n[5]; i++) {
            String s = model.a111222.get(i);
            List<Integer> reMag = new ArrayList<>();
            if (isExists2(list2, s, magList, reMag)) {
                bestModel.a111222.add(s);
                list2.removeAll(getCardsByName(list2, s));
                magList.removeAll(reMag);
                list2.removeAll(reMag);
            }
        }
        return bestModel;
    }

    private static boolean isExists2(ArrayList<Integer> newlist, String s, List<Integer> magList, List<Integer> reMag) {
        if (magList.isEmpty()) {
            return false;
        }
        String[] strs = s.split(",");
        int num = 0;
        for (String str : strs) {
            if (str.startsWith("6")) {
                num++;
                reMag.add(Integer.parseInt(str));
                continue;
            }
            if (!newlist.contains(Integer.parseInt(str))) {
                return false;
            }
        }
        return num <= magList.size();
    }

    /**
     * 机器人判断出牌
     *
     * @param curList 自己已有的牌
     * @param oppo    对手出的牌
     * @return 出的牌
     */
    public static List<Integer> getBestAI(List<Integer> curList, List<Integer> oppo) {
        // 从大到小排序
        setOrder(curList);

        List<Integer> showCardslList = new ArrayList<>();
        if (oppo == null || oppo.isEmpty()) {
            showCards(null, showCardslList, curList);
            return showCardslList;
        }

        List<Integer> list = new ArrayList<>(curList);
        Model model = new Model();
        Model modelSingle = new Model();

        setOrder(list);

        getTwo(list, model);
        getThree(list, model);
        getFour(list, model);
        getSingle(list, model);
        get123(list, model);
        get112233(model);
        get111222(model);
        Model model2 = new Model();
        // 补充癞子玩法里的牌型
//        List<List<Integer>> numList = departByNumber(purList);
//        addMagna(list, model, magna, model2, numList);
//        screen(list, model, magna);

        // 去除model里面独立牌型
        CardTypeTool.checkModel(list, model2, modelSingle);
        // 现在分别计算每种可能性的权值,和手数，取最大的那个(注意有些牌型是相关的，组成这个就不能组成其他)
        // 所以组成一种牌型前要判断这种牌型的牌还是否存在
        // 先比较手数再比较权值
        Model bestModel = null, myModel = null;
        int value = 0;
        int time = 99;
        for (int i = 0, len1 = model.a4.size(); i <= len1; i++) {
            for (int j = 0, len2 = model.a3.size(); j <= len2; j++) {
                for (int k = 0, len3 = model.a2.size(); k <= len3; k++) {
                    for (int l = 0, len4 = model.a123.size(); l <= len4; l++) {
                        for (int m = 0, len5 = model.a112233.size(); m <= len5; m++) {
                            for (int n = 0, len6 = model.a111222.size(); n <= len6; n++) {
                                ArrayList<Integer> newlist = new ArrayList<>(list);

                                bestModel = CardTypeTool.getBestModel(newlist, model, new int[]{i, j, k, l, m, n});
                                // 加上独立的牌
                                bestModel.a1.addAll(modelSingle.a1);
                                bestModel.a2.addAll(modelSingle.a2);
                                bestModel.a3.addAll(modelSingle.a3);
                                bestModel.a4.addAll(modelSingle.a4);
                                bestModel.a123.addAll(modelSingle.a123);
                                bestModel.a112233.addAll(modelSingle.a112233);
                                bestModel.a111222.addAll(modelSingle.a111222);
                                // 加上单牌
                                for (Integer singleCard : newlist) {
                                    bestModel.a1.add(singleCard.toString());
                                }
                                // 计算手数，计算权值
                                if (CardTypeTool.getTimes(bestModel) < time) {
                                    time = CardTypeTool.getTimes(bestModel);
                                    myModel = bestModel;
                                } else if (CardTypeTool.getTimes(bestModel) == time && CardTypeTool.getCountValues(bestModel) > value) {
                                    value = CardTypeTool.getCountValues(bestModel);
                                    myModel = bestModel;
                                }
                            }
                        }
                    }
                }
            }
        }
        // 开始出牌

        showCards2(myModel, showCardslList, curList, oppo);
            // showCardslList = canPlay(curList, oppo);

        // 被动出牌
        if (showCardslList == null || showCardslList.size() == 0) {
            return null;
        }
        return showCardslList;
    }

    private static List<List<Integer>> departByNumber(List<Integer> list) {
        setOrder(list);
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            result.add(new ArrayList<Integer>());
        }
        int count = 0;
        int j = 0;
        for (int i = 0, len = list.size(); i < len; i++) {
            if (i + 1 < len && getTrueValue(list.get(i)) == getTrueValue(list.get(i + 1))) {
                count++;
            } else {
                for (int k = j; k < i + 1; ++k) {
                    result.get(count).add(list.get(k));
                }
                j = i + 1;
                count = 0;
            }
        }
        return result;
    }

    private static List<Integer> removeMagna(List<Integer> list, int magna) {
        List<Integer> copy = new ArrayList<>(list);
        for (Integer card : list) {
            if (getTrueValue(card) == magna) {
                copy.remove(card);
            }
        }
        return copy;
    }

    /**
     * 筛选去掉包含癞子牌数量大于拥有癞子牌数量的
     * 去掉不含癞子牌的
     */
    private static void screen(List<Integer> list, Model model, int magna) {
        int magnaNum = getMagnaNum(list, magna);
        List<String> copy = null;
        if (model.a112233 != null) {
            copy = new ArrayList<>(model.a112233);
        }
        if (copy != null) {
            for (String a112233 : copy) {
                int count = 0;
                String[] strs = a112233.split(",");
                for (String str : strs) {
                    if (str.startsWith("6")) {
                        count++;
                    }
                }
                if (count > magnaNum || count == 0) {
                    model.a112233.remove(a112233);
                }
            }
        }

        List<String> copy2 = null;
        if (model.a111222 != null) {
            copy2 = new ArrayList<>(model.a111222);
        }
        if (copy2 != null) {
            for (String a111222 : copy2) {
                int count = 0;
                String[] strs = a111222.split(",");
                for (String str : strs) {
                    if (str.startsWith("6")) {
                        count++;
                    }
                }
                if (count > magnaNum || count == 0) {
                    model.a111222.remove(a111222);
                }
            }
        }
    }

    private static void addMagna(List<Integer> list, Model model, int magna, Model model2, List<List<Integer>> numList) {
        int magnaNum = getMagnaNum(list, magna);
        if (magnaNum > 0) {
            if (magnaNum == 1) {
                // 增加癞子对子
                oneMag(magna, model2, numList);
            } else if (magnaNum == 2) {
                oneMag(magna, model2, numList);
                twoMag(magna, model2, numList);
            } else if (magnaNum == 3) {
                oneMag(magna, model2, numList);
                twoMag(magna, model2, numList);
                threeMag(magna, model2, numList);
            } else if (magnaNum == 4) {
                oneMag(magna, model2, numList);
                twoMag(magna, model2, numList);
                threeMag(magna, model2, numList);
            }
            // 增加癞子顺子
            addMagna123(list, model2, magna, magnaNum);
            // 增加癞子连对
            addMagna112233(list, model, magna, magnaNum, model2, numList);
            // 增加癞子飞机
            addMagna111222(list, model, magna, magnaNum, model2, numList);
        }
    }

    private static void threeMag(int magna, Model model2, List<List<Integer>> numList) {
        List<String> res1 = getMag(magna, numList, 1, 99, 1, 3);
        if (res1 != null) {
            model2.a4.addAll(res1);
        }
    }

    private static void twoMag(int magna, Model model2, List<List<Integer>> numList) {
        List<String> res1 = getMag(magna, numList, 1, 99, 1, 2);
        if (res1 != null) {
            model2.a3.addAll(res1);
        }
        List<String> res2 = getMag(magna, numList, 1, 99, 2, 2);
        if (res2 != null) {
            model2.a4.addAll(res2);
        }
    }

    private static void oneMag(int magna, Model model2, List<List<Integer>> numList) {
        List<String> res = getMag(magna, numList, 1, 99, 1, 1);
        if (res != null) {
            model2.a2.addAll(res);
        }
        List<String> res1 = getMag(magna, numList, 1, 99, 2, 1);
        if (res1 != null) {
            model2.a3.addAll(res1);
        }
        List<String> res2 = getMag(magna, numList, 1, 99, 3, 1);
        if (res2 != null) {
            model2.a4.addAll(res2);
        }
    }

    private static void addMagna111222(List<Integer> list, Model model, int magna, int magnaNum, Model model2, List<List<Integer>> numList) {
        List<String> changeStrListOneMag = new ArrayList<>(model2.a3);
        List<String> temp = new ArrayList<>(model.a3);
        addMagStrModel(model2, magnaNum, temp, changeStrListOneMag, CardType.c111222.getType());
    }

    private static void addMagna112233(List<Integer> list, Model model, int magna, int magnaNum, Model model2, List<List<Integer>> numList) {
        List<String> changeStrListOneMag = new ArrayList<>(model2.a2);
        List<String> changeStrListTwoMag = new ArrayList<>();
        List<String> temp = new ArrayList<>(model.a2);
        if (magnaNum > 1) {
//            List<Integer> valueList = new ArrayList<>();
//            if (!temp.isEmpty()) {
//                for (String str : temp) {
//                    String[] strs = str.split(",");
//                    int value = getTrueValue(Integer.parseInt(strs[0]));
//                    if (!valueList.contains(value)) {
//                        valueList.add(value);
//                    }
//                }
//            }
//            if (!changeStrListOneMag.isEmpty()) {
//                for (String str : changeStrListOneMag) {
//                    String[] strs = str.split(",");
//                    int value = getTrueValue(Integer.parseInt(strs[0]));
//                    if (!valueList.contains(value)) {
//                        valueList.add(value);
//                    }
//                }
//            }
//            List<Integer> changeList = getLeftValueList(valueList);
//            for (Integer value : changeList) {
//                String s = creMagStr(value, 2);
//                s.substring(1);
//                changeStrListTwoMag.add(s);
//            }
        }
        addMagStrModel(model2, magnaNum, temp, changeStrListOneMag, CardType.c1122.getType());
    }

    private static void addMagStrModel(Model model, int magnaNum, List<String> temp, List<String> changeStrList, int type) {
        if (magnaNum == 1) {
            for (int i = 0, len = changeStrList.size(); i < len; ++i) {
                List<String> copy = new ArrayList<>(temp);
                copy.add(changeStrList.get(i));
                addModel(model, type, copy);
            }
        } else if (magnaNum == 2) {
            for (int i = 0, len = changeStrList.size(); i < len; ++i) {
                for (int j = i + 1; j < len; ++j) {
                    List<String> copy = new ArrayList<>(temp);
                    copy.add(changeStrList.get(i));
                    copy.add(changeStrList.get(j));
                    addModel(model, type, copy);
                }
            }
        } else if (magnaNum == 3) {
            for (int i = 0, len = changeStrList.size(); i < len; ++i) {
                for (int j = i + 1; j < len; ++j) {
                    for (int k = j + 1; k < len; ++k) {
                        List<String> copy = new ArrayList<>(temp);
                        copy.add(changeStrList.get(i));
                        copy.add(changeStrList.get(j));
                        copy.add(changeStrList.get(k));
                        addModel(model, type, copy);
                    }
                }
            }
        } else if (magnaNum == 4) {
            for (int i = 0, len = changeStrList.size(); i < len; ++i) {
                for (int j = i + 1; j < len; ++j) {
                    for (int k = j + 1; k < len; ++k) {
                        for (int l = k + 1; l < len; ++l) {
                            List<String> copy = new ArrayList<>(temp);
                            copy.add(changeStrList.get(i));
                            copy.add(changeStrList.get(j));
                            copy.add(changeStrList.get(k));
                            copy.add(changeStrList.get(l));
                            addModel(model, type, copy);
                        }
                    }
                }
            }
        }
    }

    private static void addModel(Model model, int type, List<String> copy) {
        if (type == CardType.c1122.getType()) {
            List<String> res = addLiandui(copy);
            if (!model.a112233.containsAll(res)) {
                model.a112233.addAll(addLiandui(copy));
            }
        } else if (type == CardType.c111222.getType()) {
            List<String> res = addLiandui(copy);
            if (!model.a111222.containsAll(res)) {
                model.a111222.addAll(addLiandui(copy));
            }
        }
    }

    private static List<Integer> getFullValueList() {
        List<Integer> result = new ArrayList<>();
        for (int i = 3; i < 15; i++) {
            result.add(i);
        }
        return result;
    }

    private static void addMagna123(List<Integer> list, Model model, int magna, int magnaNum) {
        // 先要把所有不重复的牌归为一类，防止3带，对子影响
        List<Integer> temp = getNotSameCardList(removeMagna(list, magna));
        List<Integer> leftValueList = getLeftValueList(temp);
        leftValueList.remove((Integer) 15);
        if (magnaNum > leftValueList.size()) {
            magnaNum = leftValueList.size();
        }
        addMagModel(model, magnaNum, temp, leftValueList, CardType.c123.getType());
    }

    private static void addMagnaFour(Model model, int magna, int magnaNum, Model model2, List<List<Integer>> numList) {
        if (magnaNum == 4) {
            model2.a4.add("6" + magna + ",6" + magna + ",6" + magna + ",6" + magna);
        }
        int count1 = model.a3.size();
        int count2 = model.a2.size();
        int count3 = model.a1.size();
        int i = magnaNum > count1 ? count1 : magnaNum;
        magnaNum -= i;
        int j = magnaNum / 2 > count2 ? count2 : magnaNum / 2;
        magnaNum -= j * 2;
        int k = magnaNum / 3 > count3 ? count3 : magnaNum / 3;
        for (int l = 0; l < i; l++) {
            String a3 = model.a3.get(l);
            String[] cards = a3.split(",");
            int card = Short.parseShort(cards[0]);
            String s = a3 + "," + getMagnaCard(card);
            model2.a4.add(s);
        }
        for (int l = 0; l < j; l++) {
            String a2 = model.a2.get(l);
            String[] cards = a2.split(",");
            int card = Short.parseShort(cards[0]);
            String s = a2 + "," + getMagnaCard(card) + "," + getMagnaCard(card);
            model2.a4.add(s);
        }
        for (int l = 0; l < k; l++) {
            String a1 = model.a1.get(l);
            String[] cards = a1.split(",");
            int card = Short.parseShort(cards[0]);
            String s = a1 + "," + getMagnaCard(card) + "," + getMagnaCard(card) + "," + getMagnaCard(card);
            model2.a4.add(s);
        }
    }

    private static int addMagnaThree(Model model, int magna, int magnaNum, Model model2, List<List<Integer>> numList) {
        if (magnaNum == 1) {
            List<Integer> twoList = numList.get(1);
            if (!twoList.isEmpty()) {
                for (int i = 0; i < twoList.size(); i++) {
                    int card = twoList.get(i);
                    if (getTrueValue(card) == magna) {
                        continue;
                    }
                    String s = card + "," + getMagnaCard(card) + "," + getMagnaCard(card);
                    model2.a3.add(s);
                }
            }
        } else if (magnaNum > 1) {

            List<Integer> twoList = numList.get(1);
            List<Integer> oneList = numList.get(0);
            int count1 = twoList.size();
            int count2 = oneList.size();
//            int i = magnaNum > count1 ? count1 : magnaNum;
//            magnaNum -= i;
//            int j = magnaNum / 2 > count2 ? count2 : magnaNum / 2;
//            magnaNum -= j * 2;
            add(model2.a3, twoList, count1);
            add(model2.a3, oneList, count2);
            if (magnaNum > 2) {
                model2.a3.add("6" + magna + ",6" + magna + ",6" + magna);
            }
        }
        return magnaNum;
    }

    private static void add(List<String> res, List<Integer> list, int count1) {
        if (count1 > 0) {
            for (Integer card : list) {
                String s = card + "," + getMagnaCard(card) + "," + getMagnaCard(card);
                res.add(s);
            }
        }
    }

    private static void addMagnaTwo(int magna, List<List<Integer>> numList, Model model2) {
        List<String> res = getMag(magna, numList, 1, 1, 1, 1);
        if (res != null) {
            model2.a2.addAll(res);
        }
    }

    /**
     * @param magna   癞子牌值
     * @param numList 按个数分好的牌集
     * @param sort    排序1由大到小2由小到大
     * @param num     个数
     * @param num2    固定牌个数
     * @param num3    癞子牌个数
     */

    private static List<String> getMag(int magna, List<List<Integer>> numList, int sort, int num, int num2, int num3) {
        List<Integer> list = numList.get(num2 - 1);
        if (list.isEmpty()) {
            return null;
        }
        List<String> result = new ArrayList<>();
        if (sort == 1) {
            setOrder(list);
        } else {
            setOrder2(list);
        }
        if (!list.isEmpty()) {
            for (int j = 0; j + num2 - 1 < list.size(); j++) {
                if (getTrueValue(list.get(j)) == magna) {
                    j = j + num2 - 1;
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                for (int k = j; k < j + num2; k++) {
                    sb.append(list.get(k)).append(",");
                }
                sb.deleteCharAt(sb.lastIndexOf(","));
                sb.append(creMagStr(list.get(j), num3));
                result.add(sb.toString());
                if (result.size() == num) {
                    return result;
                }
                j = j + num2 - 1;
            }
        }
        return result;
    }

    private static String creMagStr(int card, int i) {
        StringBuilder s = new StringBuilder();
        for (int j = 0; j < i; j++) {
            s.append(",").append(600 + getTrueValue(card));
        }
        return s.toString();
    }


    private static List<Integer> getNotSameCardList(List<Integer> list) {
        ArrayList<Integer> list2 = new ArrayList<>(list);
        ArrayList<Integer> temp = new ArrayList<>();
        List<Integer> integers = new Vector<>();
        for (Integer card : list2) {
            if (integers.indexOf(CardTypeTool.getTrueValue(card)) < 0 && CardTypeTool.getColor(card) != 5 && CardTypeTool.getTrueValue(card) != 15) {
                integers.add(CardTypeTool.getTrueValue(card));
                temp.add(card);
            }
        }
        return temp;
    }

    /**
     * 判断牌型
     */
    public static List<CardType> jugdeType(List<Integer> list, int magnaCardValue) {
        List<Integer> cardIds = new ArrayList<>(list);
        List<CardType> typeLists = new ArrayList<>();
        if (magnaCardValue == 0) {
            typeLists.add(jugdeType(cardIds));
            return typeLists;
        } else {
            int len = 0;
            List<Integer> magnaCards = new ArrayList<>();
            // 先确定癞子牌数
            for (int card : cardIds) {
                if (CardTypeTool.getTrueValue(card) == magnaCardValue) {
                    ++len;
                    // 暂时除去癞子牌
                    cardIds.remove(card);
                    magnaCards.add(card);
                }
            }

            if (len == 0) {
                // 没有癞子牌
                typeLists.add(jugdeType(cardIds));
                return typeLists;
            } else {

                if (cardIds.isEmpty()) {
                    // 全是癞子牌
                    CardType cardType = jugdeType(magnaCards);
                    if (cardType == CardType.c4) {
                        cardType = CardType.c666;
                    }
                    typeLists.add(cardType);
                    return typeLists;
                } else {
                    CardType availableType = null;
                    // 如果有其他牌，则癞子牌可以变成该牌
                    switch (len) {
                        case 1:
                            for (int card : list) {
                                List<Integer> copy = new ArrayList<>(cardIds);
                                copy.add(card);
                                availableType = jugdeType(copy);
                                if (availableType != CardType.c0) {
                                    typeLists.add(availableType);
                                }
                            }
                            break;
                        case 2:
                            for (int card : list) {
                                for (int card2 : list) {
                                    List<Integer> copy = new ArrayList<>(cardIds);
                                    copy.add(card);
                                    copy.add(card2);
                                    availableType = jugdeType(copy);
                                    if (availableType != CardType.c0) {
                                        typeLists.add(availableType);
                                    }
                                }
                            }
                            break;
                        case 3:
                            for (int card : list) {
                                for (int card2 : list) {
                                    for (int card3 : list) {
                                        List<Integer> copy = new ArrayList<>(cardIds);
                                        copy.add(card);
                                        copy.add(card2);
                                        copy.add(card3);
                                        availableType = jugdeType(copy);
                                        if (availableType != CardType.c0) {
                                            typeLists.add(availableType);
                                        }
                                    }
                                }
                            }
                            break;
                        case 4:
                            for (int card : list) {
                                for (int card2 : list) {
                                    for (int card3 : list) {
                                        for (int card4 : list) {
                                            List<Integer> copy = new ArrayList<>(cardIds);
                                            copy.add(card);
                                            copy.add(card2);
                                            copy.add(card3);
                                            copy.add(card4);
                                            availableType = jugdeType(copy);
                                            if (availableType != CardType.c0) {
                                                typeLists.add(availableType);
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            return typeLists;
                    }
                    if (typeLists.isEmpty()) {
                        typeLists.add(CardType.c0);
                    }
                    return typeLists;
                }
            }
        }
    }

    /**
     * 将癞子牌转换成正常牌
     *
     * @param magnaCardValue 癞子牌的值
     */
    public static List<Integer> changeCards(List<Integer> list, int magnaCardValue, int action) {
        List<Integer> cardIds = new ArrayList<>(list);
        List<Integer> result = new ArrayList<>(list);
        if (magnaCardValue == 0 && action == CardType.c0.getType()) {
            return result;
        }
        int len = 0;
        // 癞子牌集合
        List<Integer> magnaCards = new ArrayList<>();
        // 先确定癞子牌数
        for (int card : list) {
            if (CardTypeTool.getTrueValue(card) == magnaCardValue) {
                ++len;
                // 除去癞子牌
                cardIds.remove((Integer) card);
                magnaCards.add(card);
            }
        }

        List<Integer> maxCards = null;
        if (len == 0) {
            return result;
        } else {
            if (cardIds.isEmpty()) {
                return result;
            } else {
                // 非癞子牌的数量
                int len2 = cardIds.size();
                // 可能存在的牌型
                CardType availableType = null;
                // 癞子牌可变集合
                List<Integer> selectList = new ArrayList<>();
                List<Integer> valueList = new ArrayList<>();
                // 得到非重复牌集
                for (int card : cardIds) {
                    if (!valueList.contains(getTrueValue(card))) {
                        selectList.add(card);
                        valueList.add(getTrueValue(card));
                    }
                }

                Collections.sort(valueList);
                int minValue = valueList.get(0);
                int maxValue = valueList.get(valueList.size() - 1);

                if (action == 9) {
                    // 顺子
                    for (int i = minValue - 1; i > 2 && i < 16 && i <= maxValue + len; ++i) {
                        if (valueList.contains(i)) {
                            continue;
                        }
                        selectList.add(600 + i);
                    }
                }
                if (action == 10) {
                    // 连对
                    if (cardIds.size() == valueList.size() * 2) {
                        len /= 2;
                        for (int i = minValue - 1; i > 2 && i < 16 && i <= maxValue + len; ++i) {
                            if (valueList.contains(i)) {
                                continue;
                            }
                            selectList.add(600 + i);
                        }
                    } else {
                        for (int i = minValue - len; i > 2 && i < 16 && i < minValue; ++i) {
                            selectList.add(100 + i);
                        }
                        for (int i = maxValue + 1; i > 2 && i < 16 && i <= maxValue + len; ++i) {
                            selectList.add(100 + i);
                        }
                    }
                }

                switch (len) {
                    case 1:
                        for (int i = 3; i < 16; ++i) {
                            selectList.add(600 + i);
                        }
                        for (int card : selectList) {
                            List<Integer> copy = new ArrayList<>(cardIds);
                            copy.add(card);
                            availableType = jugdeType(copy);
                            if (availableType.getType() == action) {
                                if (maxCards == null) {
                                    maxCards = copy;
                                } else {
                                    if (isBigger(copy, maxCards)) {
                                        maxCards = copy;
                                    }
                                }
                            }
                        }
                        break;
                    case 2:
                        for (int card : selectList) {
                            for (int card2 : selectList) {
                                List<Integer> copy = new ArrayList<>(cardIds);
                                copy.add(card);
                                copy.add(card2);
                                availableType = jugdeType(copy);
                                if (availableType.getType() == action) {
                                    if (maxCards == null) {
                                        maxCards = copy;
                                    } else {
                                        if (isBigger(copy, maxCards)) {
                                            maxCards = copy;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case 3:
                        for (int card : selectList) {
                            for (int card2 : selectList) {
                                for (int card3 : selectList) {
                                    List<Integer> copy = new ArrayList<>(cardIds);
                                    copy.add(card);
                                    copy.add(card2);
                                    copy.add(card3);
                                    availableType = jugdeType(copy);
                                    if (availableType.getType() == action) {
                                        if (maxCards == null) {
                                            maxCards = copy;
                                        } else {
                                            if (isBigger(copy, maxCards)) {
                                                maxCards = copy;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case 4:
                        for (int card : selectList) {
                            for (int card2 : selectList) {
                                for (int card3 : selectList) {
                                    for (int card4 : selectList) {
                                        List<Integer> copy = new ArrayList<>(cardIds);
                                        copy.add(card);
                                        copy.add(card2);
                                        copy.add(card3);
                                        copy.add(card4);
                                        availableType = jugdeType(copy);
                                        if (availableType.getType() == action) {
                                            if (maxCards == null) {
                                                maxCards = copy;
                                            } else {
                                                if (isBigger(copy, maxCards)) {
                                                    maxCards = copy;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        return result;
                }
                if (maxCards != null) {
                    result = maxCards;
                }
                return result;
            }
        }
    }

    /**
     * 根据非癞子牌优选出变牌集合
     */
    private static List<Integer> selectChangeList(List<Integer> cardIds, int len) {
        List<Integer> selectList = new ArrayList<>();
        List<Integer> valueList = new ArrayList<>();
        // 得到非重复牌集
        for (int card : cardIds) {
            if (!valueList.contains(getTrueValue(card))) {
                selectList.add(card);
                valueList.add(getTrueValue(card));
            }
        }

        Collections.sort(valueList);
        int minValue = valueList.get(0);
        int maxValue = valueList.get(valueList.size() - 1);

        // if(valueList.size() == cardIds.size()) {
        // // 如果非癞子牌集本身就是非重复牌集
        // for(int i = minValue - 1; i > 2 && i < 16 && i <= maxValue + len;
        // ++i) {
        // selectList.add(100 + i);
        // }
        // } else {
        // for(int i = minValue - len; i > 2 && i < 16 && i < minValue; ++i) {
        // selectList.add(100 + i);
        // }
        // for(int i = maxValue + 1; i > 2 && i < 16 && i <= maxValue + len;
        // ++i) {
        // selectList.add(100 + i);
        // }
        // }
        return selectList;
    }

    /**
     * 判断牌型
     */
    public static CardType jugdeType(List<Integer> cardIds) {
        List<Integer> list = new ArrayList<>(cardIds);
        if (list == null || list.isEmpty()) {
            return CardType.c0;
        }
        // 设定大小顺序
        setOrder(list);
        int len = list.size();
        // 双王,王炸
        if (len == 2 && CardTypeTool.getTrueValue(list.get(0)) == 17 && CardTypeTool.getTrueValue(list.get(1)) == 16) {
            return CardType.c1617;
        }
        // 单牌,对子，3不带，4个一样炸弹
        if (len <= 4) { // 如果第一个和最后个相同，说明全部相同
            if (list.size() > 0 && CardTypeTool.getTrueValue(list.get(0)) == CardTypeTool.getTrueValue(list.get(len - 1))) {
                switch (len) {
                    case 1:
                        return CardType.c1;
                    case 2:
                        return CardType.c2;
                    case 3:
                        return CardType.c3;
                    case 4:
                        return CardType.c4;
                }
            }
            // 当第一个和最后个不同时,3带1
            if (len == 4
                    && ((CardTypeTool.getTrueValue(list.get(0)) == CardTypeTool.getTrueValue(list.get(len - 2))) || CardTypeTool.getTrueValue(list.get(1)) == CardTypeTool.getTrueValue(list
                    .get(len - 1)))) {
                return CardType.c31;
            }

        }
        // 当5张以上时，连字，3带2，飞机，2顺，4带2等等
        if (len >= 4) {// 现在按相同数字最大出现次数
            Card_index card_index = new Card_index();
            for (int i = 0; i < 4; i++) {
                card_index.a[i] = new Vector<Integer>();
            }
            // 求出各种数字出现频率
            CardTypeTool.getMax(card_index, list); // a[0,1,2,3]分别表示重复1,2,3,4次的牌
            // 3带2 -----必含重复3次的牌
            if (card_index.a[2].size() == 1 && len == 5) {
                return CardType.c32;
            }
            // 4带2(单,双)
            if (card_index.a[3].size() == 1 && len == 6) {
                return CardType.c411;
            }
            if (card_index.a[3].size() == 1 && card_index.a[1].size() == 2 && len == 8) {
                return CardType.c422;
            }
            // 顺子,保证不存在王和2
            if (len >= 5 && CardTypeTool.getTrueValue(list.get(0)) < 15 && (card_index.a[0].size() == len)
                    && (CardTypeTool.getTrueValue(list.get(0)) - CardTypeTool.getTrueValue(list.get(len - 1)) == len - 1)) {
                return CardType.c123;
            }
            // 连对,保证不存在王和2
            if (len >= 6 && CardTypeTool.getTrueValue(list.get(0)) < 15 && card_index.a[1].size() == len / 2 && len % 2 == 0 && len / 2 >= 2
                    && (CardTypeTool.getTrueValue(list.get(0)) - CardTypeTool.getTrueValue(list.get(len - 1)) == (len / 2 - 1))) {
                return CardType.c1122;
            }
            // 飞机带n单,n/2对
            List<Integer> threeList = new ArrayList<>();
            threeList.addAll(card_index.a[2]);
            if (!card_index.a[3].isEmpty()) {
                threeList.addAll(card_index.a[3]);
                Collections.sort(threeList);
            }
            threeList.remove((Integer) 15);
            int near = getLianCount(threeList);
            // 飞机
            if (near >= 2 && len == near * 3) {
                return CardType.c111222;
            }
            for (int i = near; i >= 2; --i) {
                if (len == i * 5) {
                    if (i == card_index.a[1].size() + card_index.a[3].size() * 2)
                        return CardType.c1112223344;
                } else if (len == i * 4) {
                    return CardType.c11122234;
                }
            }
        }
        return CardType.c0;
    }

    public static int getLianCount(List<Integer> list) {
        if (list.size() == 1) {
            return 1;
        }
        int maxNear = 0;
        int near = 1;
        for (int i = 0; i < list.size() - 1; i++) {
            int left = list.get(i);
            int right = list.get(i + 1);
            if (right - left == 1) {
                near++;
                if (near > maxNear) {
                    maxNear = near;
                }
            } else {
                near = 1;
            }
        }
        return maxNear;
    }

    /**
     * 返回值
     */
    public static Integer getTrueValue(int card) {
        int i = card % 100;
        return i;
    }

    /**
     * 返回花色
     */
    public static int getColor(int card) {
        return card / 100;
    }

    /**
     * 得到最大相同数,装到card_index中
     *
     * @param card_index 容器
     */
    public static void getMax(Card_index card_index, List<Integer> list) {
        int count[] = new int[17];// 1-16各算一种,王算第16种
        for (int i = 0; i < 17; i++) {
            count[i] = 0;
        }
        for (int i = 0, len = list.size(); i < len; i++) {
            if (CardTypeTool.getColor(list.get(i)) == 5) {
                count[16]++;
            } else {
                count[CardTypeTool.getTrueValue(list.get(i)) - 1]++;
            }
        }
        for (int i = 0; i < 17; i++) {
            switch (count[i]) {
                case 1:
                    card_index.a[0].add(i + 1);
                    break;
                case 2:
                    card_index.a[1].add(i + 1);
                    break;
                case 3:
                    card_index.a[2].add(i + 1);
                    break;
                case 4:
                    card_index.a[3].add(i + 1);
                    break;
            }
        }
    }

    /**
     * 检查牌的是否能出
     *
     * @param c    点选的牌
     * @param oppo 当前最大的牌
     * @return 1能出 0不能
     */
    public static int checkCards(List<Integer> c, List<Integer> oppo) {
        // 找出当前最大的牌是哪个电脑出的,c是点选的牌
        List<Integer> currentlist = oppo;
        CardType cType = CardTypeTool.jugdeType(c);
        CardType cType2 = CardTypeTool.jugdeType(currentlist);
        // 如果张数不同直接过滤
        if (cType != CardType.c4 && (cType != CardType.c32 && c.size() != currentlist.size())) {
            return 0;
        }
        // 比较我的出牌类型
        if (cType != CardType.c4 && cType != cType2) {
            return 0;
        }
        // 比较出的牌是否要大
        // 我是炸弹
        if (cType == CardType.c4) {
            if (c.size() == 2) {
                return 1;
            }
            if (cType2 != CardType.c4) {
                return 1;
            }
        }

        // 单牌,对子,3带,4炸弹
        if (cType == CardType.c1 || cType == CardType.c2 || cType == CardType.c3 || cType == CardType.c4) {
            if (CardTypeTool.getTrueValue(c.get(0)) <= CardTypeTool.getTrueValue(currentlist.get(0))) {
                return 0;
            } else {
                return 1;
            }
        }
        // 顺子,连队，飞机裸
        if (cType == CardType.c123 || cType == CardType.c1122 || cType == CardType.c111222) {
            if (CardTypeTool.getTrueValue(c.get(0)) <= CardTypeTool.getTrueValue(currentlist.get(0))) {
                return 0;
            } else {
                return 1;
            }
        }
        // 按重复多少排序
        // 3带1,3带2 ,飞机带单，双,4带1,2,只需比较第一个就行，独一无二的
        if (cType == CardType.c31 || cType == CardType.c32 || cType == CardType.c411 || cType == CardType.c422 || cType == CardType.c11122234 || cType == CardType.c1112223344) {
            ArrayList<Integer> a1 = CardTypeTool.getOrder2(c); // 我出的牌
            ArrayList<Integer> a2 = CardTypeTool.getOrder2(currentlist);// 当前最大牌
            if (CardTypeTool.getTrueValue(a1.get(0)) < CardTypeTool.getTrueValue(a2.get(0))) {
                return 0;
            }
        }
        return 1;
    }

    // 拆对子
    public static List<String> getTwo(List<Integer> list) {
        List<String> result = new ArrayList<>();
        // 连续2张相同
        for (int i = 0, len = list.size(); i < len; i++) {
            if (i + 1 < len && CardTypeTool.getTrueValue(list.get(i)) == CardTypeTool.getTrueValue(list.get(i + 1))) {
                String s = list.get(i) + ",";
                s += list.get(i + 1);
                result.add(s);
                i = i + 1;
            }
        }
        return result;
    }

    private static int getMagnaNum(List<Integer> list, int magna) {
        int magnaNum = 0;
        for (Integer card : list) {
            if (getTrueValue(card) == magna) {
                magnaNum++;
            }
        }
        return magnaNum;
    }

    /**
     * 获得所有3张相同的组合
     */
    public static List<String> getMaxThreeLian(List<Integer> list) {
        List<String> result = new ArrayList<>();
        // 连续3张相同
        for (int i = 0, len = list.size(); i < len; i++) {
            if (i + 2 < len && CardTypeTool.getTrueValue(list.get(i)) == CardTypeTool.getTrueValue(list.get(i + 2))) {
                String s = list.get(i) + ",";
                s += list.get(i + 1) + ",";
                s += list.get(i + 2);
                result.add(s);
                i += 2;
            }
        }
        return result;
    }

    /**
     * 获得所有4张相同的组合
     */
    public static List<String> getFour(List<Integer> list) {
        List<String> result = new ArrayList<>();
        // 连续3张相同
        for (int i = 0, len = list.size(); i < len; i++) {
            if (i + 3 < len && getTrueValue(list.get(i)) == getTrueValue(list.get(i + 3))) {
                String s = list.get(i) + ",";
                s += list.get(i + 1) + ",";
                s += list.get(i + 2) + ",";
                s += list.get(i + 3);
                result.add(s);
                i += 3;
            }
        }
        return result;
    }

    // 拆炸弹
    public static void getBoomb(List<Integer> list, Model model) {
        if (list.size() < 1) {
            return;
        }
        // 王炸
        if (list.size() >= 2 && CardTypeTool.getTrueValue(list.get(0)) == 17 && CardTypeTool.getTrueValue(list.get(1)) == 16) {
            model.a4.add(list.get(0) + "," + list.get(1)); // 按名字加入
        }
        // 一般的炸弹
        for (int i = 0, len = list.size(); i < len; i++) {
            if (i + 3 < len && CardTypeTool.getTrueValue(list.get(i)) == CardTypeTool.getTrueValue(list.get(i + 3))) {
                String s = list.get(i) + ",";
                s += list.get(i + 1) + ",";
                s += list.get(i + 2) + ",";
                s += list.get(i + 3);
                model.a4.add(s);
                i = i + 3;
            }
        }
    }

    // 拆双顺
    public static List<String> getTwoTwo(List<String> l) {
        // List<String> del = new Vector<String>();// 要删除的Cards
        // 从model里面的对子找
        setStrOrder(l);
        clearSame(l);
        // 连对数大于等于
        if (l.size() < 6) {
            return new ArrayList<>();
        }
        return addLiandui(l);
        // l.removeAll(del);
    }

    private static List<String> addLiandui(List<String> l) {
        setStrOrder(l);
        List<String> result = new ArrayList<>();
        Short s[] = new Short[l.size()];
        for (int i = 0, len = l.size(); i < len; i++) {
            String[] name = l.get(i).split(",");
            s[i] = Short.parseShort(name[0]);
        }
        // s0,1,2,3,4 13,9,8,7,6
        for (int i = 0, len = l.size(); i < len; i++) {
            int k = i;
            for (int j = i; j < len; j++) {
                if (getTrueValue(s[i]) - getTrueValue(s[j]) == j - i) {
                    k = j;
                }
            }
            if (k - i >= 2)// k=4 i=1
            {// 说明从i到k是连队
                String ss = "";
                for (int j = i; j < k; j++) {
                    ss += l.get(j) + ",";
                    // del.add(l.get(j));
                }
                ss += l.get(k);
                result.add(ss);
                // del.add(l.get(k));
                i = k;
            }
        }
        return result;
    }

    private static void clearSame(List<String> l) {
        List<String> copy = new ArrayList<>(l);
        for (int i = 0; i + 1 < copy.size(); i++) {
            String[] strs1 = copy.get(i).split(",");
            String[] strs2 = copy.get(i + 1).split(",");
            if (getTrueValue(Short.parseShort(strs1[0])) == getTrueValue(Short.parseShort(strs2[0]))) {
                l.remove(copy.get(i + 1));
            }
            i = i + 1;
        }
    }

    // 拆双顺
    public static void getTwoTwo(List<Integer> list, Model model, int length) {
        // List<String> del = new Vector<String>();// 要删除的Cards
        // 从model里面的对子找
        List<String> l = model.a2;
        if (l.size() < 2) {
            return;
        }
        Short s[] = new Short[l.size()];
        for (int i = 0, len = l.size(); i < len; i++) {
            String[] name = l.get(i).split(",");
            s[i] = Short.parseShort(name[0]);
        }
        // s0,1,2,3,4 13,9,8,7,6
        List<String> twotwoIndex = calcLian(l, s, length);
        model.a112233.addAll(twotwoIndex);

    }

    public static List<String> calcLian(List<String> l, Short s[], int length) {
        List<String> twotwoIndex = new ArrayList<>();
        for (int i = 0; i < s.length; i++) {
            if (i + length > s.length) {
                break;
            }

            int s1 = 0;
            List<Integer> twotwo = new ArrayList<>();
            int liannum = 0;
            for (int j = 0; j < length; j++) {
                int j_index = i + j;
                int s2 = s[j_index];
                if (getTrueValue(s1) - getTrueValue(s2) == 1) {
                    liannum++;
                } else {
                    liannum = 1;
                    twotwo.clear();
                }
                s1 = s2;
                twotwo.add(j_index);
                if (liannum == length) {
                    StringBuffer sb = new StringBuffer();
                    for (int index : twotwo) {
                        sb.append(l.get(index)).append(",");
                    }
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    twotwoIndex.add(sb.toString());
                    break;
                }
                // twotwoIndex.add(l.get(i));
                // twotwoIndex.add(l.get(i+1));
            }

        }
        return twotwoIndex;
    }

    // 拆飞机
    public static void getPlane(List<Integer> list, Model model, int length) {
        // List<String> del = new Vector<String>();// 要删除的Cards
        // 从model里面的3带找
        List<String> l = model.a3;
        if (l.size() < 2) {
            return;
        }
        Short s[] = new Short[l.size()];
        for (int i = 0, len = l.size(); i < len; i++) {
            String[] name = l.get(i).split(",");
            s[i] = Short.parseShort(name[0]);
        }
        List<String> twotwoIndex = calcLian(l, s, length);
        model.a111222.addAll(twotwoIndex);

        // l.removeAll(del);
    }

    // 拆飞机
    public static List<String> getPlane(List<String> l) {
        // List<String> del = new Vector<String>();// 要删除的Cards
        // 从model里面的3带找
        List<String> result = new ArrayList<>();
        if (l.size() < 2) {
            return new ArrayList<>();
        }
//        setStrOrder(l);
//        clearSame(l);
        return addPlane(l, result);
        // l.removeAll(del);
    }

    private static List<String> addPlane(List<String> l, List<String> result) {
        Short s[] = new Short[l.size()];
        for (int i = 0, len = l.size(); i < len; i++) {
            String[] name = l.get(i).split(",");
            s[i] = Short.parseShort(name[0]);
        }
        for (int i = 0, len = l.size(); i < len; i++) {
            int k = i;
            for (int j = i; j < len; j++) {
                if (getTrueValue(s[i]) - getTrueValue(s[j]) == j - i) {
                    k = j;
                }
            }
            if (k != i) {// 说明从i到k是飞机
                String ss = "";
                for (int j = i; j < k; j++) {
                    ss += l.get(j) + ",";
                    // del.add(l.get(j));
                }
                ss += l.get(k);
                result.add(ss);
                // del.add(l.get(k));
                i = k;
            }
        }
        return result;
    }

    // 拆连子
    public static List<String> get123(List<Integer> list) {
        if (list.size() < 5) {
            return new ArrayList<>();
        }
        // 先要把所有不重复的牌归为一类，防止3带，对子影响
        List<Integer> temp = getNotSameCardList(list);
        CardTypeTool.setOrder(temp);
        return addWuLian(temp);
        // list.removeAll(del);
    }

    public static void addMagModel(Model model, int magnaNum, List<Integer> temp, List<Integer> changeValueList, int type) {
        addMagModel(model, magnaNum, temp, changeValueList, type, null, null);
    }

    public static void addMagModel(Model model, int magnaNum, int type, List<String> temp, List<String> changeValueList) {
        addMagModel(model, magnaNum, null, null, type, temp, changeValueList);
    }


    /**
     * @param temp            固定牌集合
     * @param magnaNum        可变牌的数量
     * @param changeValueList 可变的牌值集合
     * @param type            要加的牌型
     */
    private static void addMagModel(Model model, int magnaNum, List<Integer> temp, List<Integer> changeValueList, int type, List<String> temp2, List<String> changeStrList) {
        if (magnaNum == 1) {
            for (int i = 0, len = changeValueList.size(); i < len; ++i) {
                List<Integer> copy = new ArrayList<>(temp);
                copy.add(600 + changeValueList.get(i));
                addMagModelByType(model, copy, type);
            }
        } else if (magnaNum == 2) {
            for (int i = 0, len = changeValueList.size(); i < len; ++i) {
                for (int j = i + 1; j < len; ++j) {
                    List<Integer> copy = new ArrayList<>(temp);
                    copy.add(600 + changeValueList.get(i));
                    copy.add(600 + changeValueList.get(j));
                    addMagModelByType(model, copy, type);
                }
            }
        } else if (magnaNum == 3) {
            for (int i = 0, len = changeValueList.size(); i < len; ++i) {
                for (int j = i + 1; j < len; ++j) {
                    for (int k = j + 1; k < len; ++k) {
                        List<Integer> copy = new ArrayList<>(temp);
                        copy.add(600 + changeValueList.get(i));
                        copy.add(600 + changeValueList.get(j));
                        copy.add(600 + changeValueList.get(k));
                        addMagModelByType(model, copy, type);
                    }
                }
            }
        } else if (magnaNum == 4) {
            for (int i = 0, len = changeValueList.size(); i < len; ++i) {
                for (int j = i + 1; j < len; ++j) {
                    for (int k = j + 1; k < len; ++k) {
                        for (int l = k + 1; l < len; ++l) {
                            List<Integer> copy = new ArrayList<>(temp);
                            copy.add(getMagnaCard(changeValueList.get(i)));
                            copy.add(getMagnaCard(changeValueList.get(j)));
                            copy.add(getMagnaCard(changeValueList.get(k)));
                            copy.add(getMagnaCard(changeValueList.get(l)));
                            addMagModelByType(model, copy, type);
                        }
                    }
                }
            }
        }
    }

    private static void addMagModelByType(Model model, List<Integer> temp, int type) {
        setOrder(temp);
        if (type == CardType.c123.getType()) {
            model.a123 = addWuLian(temp);
        }
    }

    private static List<String> addWuLian(List<Integer> temp) {
        List<String> result = new ArrayList<>();
        for (int i = 0, len = temp.size(); i < len; i++) {
            int k = i;
            for (int j = i; j < len; j++) {
                if (CardTypeTool.getTrueValue(temp.get(i)) - CardTypeTool.getTrueValue(temp.get(j)) == j - i) {
                    k = j;
                }
            }
            if (k - i >= 4) {
                String s = "";
                for (int j = i; j < k; j++) {
                    s += temp.get(j) + ",";
                }
                s += temp.get(k);
                if (!result.contains(s)) {
                    result.add(s);
                }
                i = k;
            }
        }
        return result;
    }

    private static List<Integer> getLeftValueList(List<Integer> list) {
        List<Integer> valueList = new ArrayList<>();
        for (int i = 3; i < 16; i++) {
            valueList.add(i);
        }
        for (Integer value : list) {
            valueList.remove(value > 100 ? getTrueValue(value) : value);
        }
        return valueList;
    }

    // 拆单牌
    public static void getSingle(List<Integer> list, Model model) {
        for (int i = 0, len = list.size(); i < len; i++) {
            model.a1.add(list.get(i).toString());
        }
        CardTypeTool.delSingle(model.a2, model);
        CardTypeTool.delSingle(model.a3, model);
        CardTypeTool.delSingle(model.a4, model);
        CardTypeTool.delSingle(model.a123, model);
        CardTypeTool.delSingle(model.a112233, model);
        CardTypeTool.delSingle(model.a111222, model);
    }

    // 取单
    public static void delSingle(List<String> list, Model model) {
        for (int i = 0, len = list.size(); i < len; i++) {
            String s[] = list.get(i).split(",");
            for (int j = 0; j < s.length; j++) {
                model.a1.remove(s[j]);
            }
        }
    }

    // 去除独立牌型
    public static void checkModel(List<Integer> list, Model model1, Model modelSingle) {
        // 找出与其他不相关的牌型
        for (int i = 0, len = model1.a2.size(); i < len; i++) {
            int flag = 0;
            // Log.i("mylog","..."+ model1.a2.get(i));
            String s[] = model1.a2.get(i).split(",");
            // flag+=checkModel_1(model1.a2, s);
            flag += checkModel_1(model1.a3, s);
            flag += checkModel_1(model1.a4, s);
            flag += checkModel_1(model1.a112233, s);
            flag += checkModel_1(model1.a111222, s);
            flag += checkModel_1(model1.a123, s);
            // Log.i("mylog", "a2:flag"+flag);
            if (flag == 0) {
                modelSingle.a2.add(model1.a2.get(i));
                list.removeAll(CardTypeTool.getCardsByName(list, model1.a2.get(i)));
            }
        }
        model1.a2.removeAll(modelSingle.a2);
        for (int i = 0, len = model1.a3.size(); i < len; i++) {
            int flag = 0;
            String s[] = model1.a3.get(i).split(",");
            flag += checkModel_1(model1.a2, s);
            // flag+=checkModel_1(model1.a3, s);
            flag += checkModel_1(model1.a4, s);
            flag += checkModel_1(model1.a112233, s);
            flag += checkModel_1(model1.a111222, s);
            flag += checkModel_1(model1.a123, s);
            if (flag == 0) {
                modelSingle.a3.add(model1.a3.get(i));
                list.removeAll(CardTypeTool.getCardsByName(list, model1.a3.get(i)));

            }
        }
        model1.a3.removeAll(modelSingle.a3);
        for (int i = 0, len = model1.a4.size(); i < len; i++) {
            int flag = 0;
            String s[] = model1.a4.get(i).split(",");
            flag += checkModel_1(model1.a2, s);
            flag += checkModel_1(model1.a3, s);
            // flag+=checkModel_1(model1.a4, s);
            flag += checkModel_1(model1.a112233, s);
            flag += checkModel_1(model1.a111222, s);
            flag += checkModel_1(model1.a123, s);
            if (flag == 0) {
                modelSingle.a4.add(model1.a4.get(i));
                list.removeAll(CardTypeTool.getCardsByName(list, model1.a4.get(i)));
            }
        }
        model1.a4.removeAll(modelSingle.a4);
        for (int i = 0, len = model1.a112233.size(); i < len; i++) {
            int flag = 0;
            String s[] = model1.a112233.get(i).split(",");
            flag += checkModel_1(model1.a2, s);
            flag += checkModel_1(model1.a3, s);
            flag += checkModel_1(model1.a4, s);
            // flag+=checkModel_1(model1.a112233, s);
            flag += checkModel_1(model1.a111222, s);
            flag += checkModel_1(model1.a123, s);
            if (flag == 0) {
                modelSingle.a112233.add(model1.a112233.get(i));
                list.removeAll(CardTypeTool.getCardsByName(list, model1.a112233.get(i)));
            }
        }
        model1.a112233.removeAll(modelSingle.a112233);
        for (int i = 0, len = model1.a111222.size(); i < len; i++) {
            int flag = 0;
            String s[] = model1.a111222.get(i).split(",");
            flag += checkModel_1(model1.a2, s);
            flag += checkModel_1(model1.a3, s);
            flag += checkModel_1(model1.a4, s);
            flag += checkModel_1(model1.a112233, s);
            // flag+=checkModel_1(model1.a111222, s);
            flag += checkModel_1(model1.a123, s);
            if (flag == 0) {
                modelSingle.a111222.add(model1.a111222.get(i));
                list.removeAll(CardTypeTool.getCardsByName(list, model1.a111222.get(i)));
            }
        }
        model1.a111222.removeAll(modelSingle.a111222);
        for (int i = 0, len = model1.a123.size(); i < len; i++) {
            int flag = 0;
            String s[] = model1.a123.get(i).split(",");
            flag += checkModel_1(model1.a2, s);
            flag += checkModel_1(model1.a3, s);
            flag += checkModel_1(model1.a4, s);
            flag += checkModel_1(model1.a112233, s);
            flag += checkModel_1(model1.a111222, s);
            // flag+=checkModel_1(model1.a123, s);
            if (flag == 0) {
                modelSingle.a123.add(model1.a123.get(i));
                list.removeAll(CardTypeTool.getCardsByName(list, model1.a123.get(i)));
            }
        }
        model1.a123.removeAll(modelSingle.a123);
    }

    public static int checkModel_1(List<String> list, String[] s) {
        for (int j = 0, len2 = list.size(); j < len2; j++) {
            String ss[] = list.get(j).split(",");
            for (int k = 0; k < ss.length; k++) {
                for (int m = 0; m < s.length; m++) {
                    if (s[m].equals(ss[k])) {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    // 主动出牌
    public static void showCards(Model model, List<Integer> to, List<Integer> from) {
        int size = from.size();
        if (size >= 2){
            int card1=from.get(size-1);
            int card2=from.get(size-2);
            to.add(card1);
            if (card1%100==card2%100){
                to.add(card2);
            }
            if (size >= 3){
                int card3=from.get(size-3);
                if (card1%100==card3%100){
                    to.add(card3);
                }
            }
        }else{
            to.add(from.get(0));
        }

//        List<String> list = new Vector<String>();
//        if (model.a123.size() > 0) {
//            list.add(model.a123.get(model.a123.size() - 1));
//        }
//        // 有单出单 (除开3带，飞机能带的单牌)
//        else if (model.a1.size() > (model.a111222.size() * 2 + model.a3.size()) && CardTypeTool.getValueByName(model.a1.get(model.a1.size() - 1)) < 15) {
//            list.add(model.a1.get(model.a1.size() - 1));
//        } else if (model.a1.size() > (model.a111222.size() * 2 + model.a3.size())) {
//            list.add(model.a1.get(0));
//        }
//        // 有对子出对子 (除开3带，飞机)
//        else if (model.a2.size() > (model.a111222.size() * 2 + model.a3.size()) && CardTypeTool.getValueByName(model.a2.get(model.a2.size() - 1)) < 15) {
//            list.add(model.a2.get(model.a2.size() - 1));
//        }
//        // 有3带就出3带，没有就出光3
//        else if (model.a3.size() > 0 && CardTypeTool.getValueByName(model.a3.get(model.a3.size() - 1)) < 15) {
//            // 3带单,且非关键时刻不能带王，2
//            if (model.a1.size() > 0) {
//                list.add(model.a1.get(model.a1.size() - 1));
//                if (model.a1.size() > 0) {
//                    list.add(model.a1.get(model.a1.size() - 1));
//                }// 3带对
//            } else if (model.a2.size() > 0) {
//                list.add(model.a2.get(model.a2.size() - 1));
//            }
//            list.add(model.a3.get(model.a3.size() - 1));
//        }// 有双顺出双顺
//        else if (model.a112233.size() > 0) {
//            list.add(model.a112233.get(model.a112233.size() - 1));
//        }// 有飞机出飞机
//        else if (model.a111222.size() > 0) {
//            String name[] = model.a111222.get(0).split(",");
//            // 带单
//            if (name.length / 3 <= model.a1.size()) {
//                list.add(model.a111222.get(model.a111222.size() - 1));
//                for (int i = 0; i < name.length / 3; i++) {
//                    list.add(model.a1.get(i));
//                }
//            } else if (name.length / 3 <= model.a2.size())// 带双
//            {
//                list.add(model.a111222.get(model.a111222.size() - 1));
//                for (int i = 0; i < name.length / 3; i++) {
//                    list.add(model.a2.get(i));
//                }
//            }
//
//        } else if (model.a1.size() > (model.a111222.size() * 2 + model.a3.size())) {
//            list.add(model.a1.get(model.a1.size() - 1));
//        } else if (model.a2.size() > (model.a111222.size() * 2 + model.a3.size())) {
//            list.add(model.a2.get(model.a2.size() - 1));
//        } else if (CardTypeTool.getValueByName(model.a3.get(0)) < 15 && model.a3.size() > 0) {
//            // 3带单,且非关键时刻不能带王，2
//            if (model.a1.size() > 0) {
//                list.add(model.a1.get(model.a1.size() - 1));
//            }// 3带对
//            else if (model.a2.size() > 0) {
//                list.add(model.a2.get(model.a2.size() - 1));
//            }
//            list.add(model.a3.get(model.a3.size() - 1));
//        }
//        // 有炸弹出炸弹
//        else if (model.a4.size() > 0) {
//            // 4带2,1
//            int sizea1 = model.a1.size();
//            int sizea2 = model.a2.size();
//            if (sizea1 >= 2) {
//                list.add(model.a1.get(sizea1 - 1));
//                list.add(model.a1.get(sizea1 - 2));
//                list.add(model.a4.get(0));
//
//            } else if (sizea2 >= 2) {
//                list.add(model.a2.get(sizea1 - 1));
//                list.add(model.a2.get(sizea1 - 2));
//                list.add(model.a4.get(0));
//
//            } else {// 直接炸
//                list.add(model.a4.get(0));
//            }
//        }
//        for (String s : list) {
//            to.addAll(CardTypeTool.getCardsByName(from, s));
//        }

    }

    /**
     * @param model
     * @param to    走出的牌
     * @param from  自己已有的牌
     * @param oppo  对手出的牌
     */
    public static void showCards2(Model model, List<Integer> to, List<Integer> from, List<Integer> oppo) {
        // oppo是对手出的牌,from是自己已有的牌,to是要走出的牌
        List<String> list = new Vector<>();// 装要走出的牌的name
        CardType cType = CardTypeTool.jugdeType(oppo);
        // 按重复数排序,这样只需比较第一张牌
        oppo = CardTypeTool.getOrder2(oppo);
        switch (cType) {
            case c1:
                for (int len = model.a1.size(), i = len - 1; i >= 0; i--) {
                    if (CardTypeTool.getValueByName(model.a1.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                        list.add(model.a1.get(i));
                        break;
                    }
                }

                if (list.size() == 0) {
                    for (int i = 0, leni = from.size(); i < leni; i++) {
                        if (CardTypeTool.getTrueValue(from.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                            to.add(from.get(i));
                            return;
                        }
                    }
                }

                break;
            case c2:
                for (int len = model.a2.size(), i = len - 1; i >= 0; i--) {
                    if (CardTypeTool.getValueByName(model.a2.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                        list.add(model.a2.get(i));
                        break;
                    }
                }

                if (list.size() == 0) {
                    for (int len = model.a3.size(), i = len - 1; i >= 0; i--) {

                        if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                            List<Integer> t = CardTypeTool.getCardsByName(from, model.a3.get(i));
                            to.add(t.get(0));
                            to.add(t.get(1));
                            return;
                        }
                    }
                }

                break;
            case c3:

                for (int len = model.a3.size(), i = len - 1; i >= 0; i--) {
                    if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                        list.add(model.a3.get(i));
                        break;
                    }
                }
                break;
            case c31:

                int len1 = model.a3.size();
                int len2 = model.a1.size();
                if (!(len1 < 1 || len2 < 1)) {
                    for (int len = len1, i = len - 1; i >= 0; i--) {
                        if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                            list.add(model.a3.get(i));
                            break;
                        }
                    }
                    if (list.size() > 0) {
                        list.add(model.a1.get(len2 - 1));
                    }
                }
                break;
            case c32:

                len1 = model.a3.size();
                len2 = model.a2.size();
                if (!(len1 < 1 || len2 < 1)) {
                    for (int len = len1, i = len - 1; i >= 0; i--) {
                        if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                            list.add(model.a3.get(i));
                            break;
                        }
                    }
                    if (list.size() > 0) {
                        list.add(model.a2.get(len2 - 1));
                    }
                }
                break;
            case c411:

                len1 = model.a4.size();
                len2 = model.a1.size();
                if (!(len1 < 1 || len2 < 2)) {
                    for (int len = len1, i = len - 1; i >= 0; i--) {
                        if (CardTypeTool.getValueByName(model.a4.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                            list.add(model.a4.get(i));
                            break;
                        }
                    }
                    if (list.size() > 0) {
                        list.add(model.a1.get(len2 - 1));
                        list.add(model.a1.get(len2 - 2));
                    }
                }
                break;
            case c422:

                len1 = model.a4.size();
                len2 = model.a2.size();
                if (!(len1 < 1 || len2 < 2)) {
                    for (int len = len1, i = len - 1; i >= 0; i--) {
                        if (CardTypeTool.getValueByName(model.a4.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                            list.add(model.a4.get(i));
                            break;
                        }
                    }
                    if (list.size() > 0) {
                        list.add(model.a2.get(len2 - 1));
                        list.add(model.a2.get(len2 - 2));
                    }
                }
                break;
            case c123:

                for (int len = model.a123.size(), i = len - 1; i >= 0; i--) {
                    String[] s = model.a123.get(i).split(",");
                    if (s.length == oppo.size() && CardTypeTool.getValueByName(model.a123.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                        list.add(model.a123.get(i));
                        break;
                    }
                }
                break;
            case c1122:

                for (int len = model.a112233.size(), i = len - 1; i >= 0; i--) {
                    String[] s = model.a112233.get(i).split(",");
                    if (s.length == oppo.size() && CardTypeTool.getValueByName(model.a112233.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                        list.add(model.a112233.get(i));
                        break;
                    }
                }
                break;
            case c11122234:
                Card_index card_index_from = new Card_index();
                for (int i = 0; i < 4; i++) {
                    card_index_from.a[i] = new Vector<Integer>();
                }
                // 求出各种数字出现频率
                CardTypeTool.getMax(card_index_from, from); // a[0,1,2,3]分别表示重复1,2,3,4次的牌

                Card_index card_index_oppo = new Card_index();
                for (int i = 0; i < 4; i++) {
                    card_index_oppo.a[i] = new Vector<Integer>();
                }
                // 求出各种数字出现频率
                CardTypeTool.getMax(card_index_oppo, oppo); // a[0,1,2,3]分别表示重复1,2,3,4次的牌
                int card_index_from_size = card_index_from.a[2].size();
                List<Integer> threeList = new ArrayList<>();
                threeList.addAll(card_index_oppo.a[2]);
                threeList.addAll(card_index_oppo.a[3]);

                int card_index_oppo_size = 1;
                for (int i = 0; i < threeList.size(); i++) {
                    if (i + 1 >= threeList.size()) {
                        break;
                    }
                    int left = threeList.get(i);
                    int right = threeList.get(i + 1);
                    if (right - left == 1) {
                        card_index_oppo_size++;
                    }

                }
                // int card_index_oppo_size = card_index_oppo.a[2].size() +
                // card_index_oppo.a[3].size();// 3334444

                CardTypeTool.getMaxThreeLian(from);
                CardTypeTool.getPlane(from, model, card_index_oppo_size);
                if (card_index_from_size >= card_index_oppo_size) {
                    len1 = model.a111222.size();
                    for (int o = len1 - 1; o >= 0; o--) {
                        // String[] s = model.a111222.get(o).split(",");
                        String plane = model.a111222.get(o);
                        if (CardTypeTool.getValueByName(model.a111222.get(o)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                            // 能打的起
                            int disCount = card_index_oppo_size * 3 + card_index_oppo_size * 2;
                            if (from.size() <= disCount) {
                                // 可以一次性出完牌
                                for (int i = 0; i < from.size(); i++) {
                                    list.add(from.get(i) + "");

                                }

                            } else {
                                List<Integer> play = StringUtil.explodeToIntList(plane);
                                for (int value : play) {
                                    list.add(value + "");
                                }
                                // 需要带牌
                                for (int i = 0; i < from.size(); i++) {
                                    if (list.size() >= disCount) {
                                        break;
                                    }
                                    if (!list.contains(from.get(i) + "")) {
                                        list.add(from.get(i) + "");

                                    }

                                }

                            }
                            break;
                        }

                    }
                }
                // len1 = model.a111222.size();
                // len2 = model.a1.size();
                //
                // if (!(len1 < 1 || len2 < 2)) {
                // for (int i = len1 - 1; i >= 0; i--) {
                // String[] s = model.a111222.get(i).split(",");
                // if ((s.length / 3 <= len2) && (s.length * 4 == oppo.size()) &&
                // CardTypeTool.getValueByName(model.a111222.get(i)) >
                // CardTypeTool.getValue(oppo.get(0))) {
                // list.add(model.a111222.get(i));
                // for (int j = 1; j <= s.length / 3; j++) {
                // list.add(model.a1.get(len2 - j));
                // }
                // }
                // }
                // }

                break;
            case c1112223344:

                len1 = model.a111222.size();
                len2 = model.a2.size();

                if (!(len1 < 1 || len2 < 2)) {
                    for (int i = len1 - 1; i >= 0; i--) {
                        String[] s = model.a111222.get(i).split(",");
                        if ((s.length / 3 <= len2) && (s.length * 4 == oppo.size()) && CardTypeTool.getValueByName(model.a111222.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                            list.add(model.a111222.get(i));
                            for (int j = 1; j <= s.length / 3; j++) {
                                list.add(model.a2.get(len2 - j));
                            }
                        }
                    }
                }
                break;
            case c4:
                for (int len = model.a4.size(), i = len - 1; i >= 0; i--) {
                    if (CardTypeTool.getValueByName(model.a4.get(i)) > CardTypeTool.getTrueValue(oppo.get(0))) {
                        list.add(model.a4.get(i));
                        break;
                    }
                }
            default:
                break;
        }
        if (list.size() == 0) {
            if (cType != CardType.c4) {
                if (model.a4.size() > 0) {
                    list.add(model.a4.get(model.a4.size() - 1));
                    for (String s : list) {
                        to.addAll(CardTypeTool.getCardsByName(from, s));
                    }
                    return;
                }
            }
            to = null;
        } else {
            for (String s : list) {
                to.addAll(CardTypeTool.getCardsByName(from, s));
            }
        }
    }

    // 统计各种牌型权值，手数
    public static Model getBestModel(ArrayList<Integer> list2, Model oldModel, int[] n) {
        // a4 a3 a2 a123 a112233 a111222
        Model temp = new Model();
        // 处理炸弹
        for (int i = 0; i < n[0]; i++) {
            String s = oldModel.a4.get(i);
            if (CardTypeTool.isExists(list2, s)) {
                temp.a4.add(s);
                list2.removeAll(CardTypeTool.getCardsByName(list2, s));
            }
        }
        // 3带
        for (int i = 0; i < n[1]; i++) {
            String s = oldModel.a3.get(i);
            if (CardTypeTool.isExists(list2, s)) {
                temp.a3.add(s);
                list2.removeAll(CardTypeTool.getCardsByName(list2, s));
            }
        }
        // 对子
        for (int i = 0; i < n[2]; i++) {
            String s = oldModel.a2.get(i);
            if (CardTypeTool.isExists(list2, s)) {
                temp.a2.add(s);
                list2.removeAll(CardTypeTool.getCardsByName(list2, s));
            }
        }
        // 顺子
        for (int i = 0; i < n[3]; i++) {
            String s = oldModel.a123.get(i);
            if (CardTypeTool.isExists(list2, s)) {
                temp.a123.add(s);
                list2.removeAll(CardTypeTool.getCardsByName(list2, s));
            }
        }
        // 双顺
        for (int i = 0; i < n[4]; i++) {
            String s = oldModel.a112233.get(i);
            if (CardTypeTool.isExists(list2, s)) {
                temp.a112233.add(s);
                list2.removeAll(CardTypeTool.getCardsByName(list2, s));
            }
        }
        // 飞机
        for (int i = 0; i < n[5]; i++) {
            String s = oldModel.a111222.get(i);
            if (CardTypeTool.isExists(list2, s)) {
                temp.a111222.add(s);
                list2.removeAll(CardTypeTool.getCardsByName(list2, s));
            }
        }
        return temp;
    }

    private static void removeMag(ArrayList<Integer> list2, String s, List<Integer> magList, int i) {
        String[] strs = s.split(",");
        for (String str : strs) {
            if (str.startsWith("6")) {
                int card = magList.remove(0);
                list2.remove(card);
            }
        }
    }

    private static List<Integer> getMagList(ArrayList<Integer> list2, int magna) {
        List<Integer> result = new ArrayList<>();
        for (Integer card : list2) {
            if (getTrueValue(card) == magna) {
                result.add(card);
            }
        }
        return result;
    }

    // 通过name返回值
    public static int getValueByName(String ss) {
        String s[] = ss.split(",");
        return getTrueValue(Short.parseShort(s[0]));
    }

    // 按照重复次数排序
    public static ArrayList<Integer> getOrder2(List<Integer> list) {
        ArrayList<Integer> list2 = new ArrayList<>(list);
        ArrayList<Integer> list3 = new ArrayList<>();
        // List<Integer> list4 = new Vector<Integer>();
        int len = list2.size();
        int a[] = new int[20];
        for (int i = 0; i < 20; i++) {
            a[i] = 0;
        }
        for (int i = 0; i < len; i++) {
            a[CardTypeTool.getTrueValue(list2.get(i))]++;
        }
        int max = 0;
        for (int i = 0; i < 20; i++) {
            max = 0;
            for (int j = 19; j >= 0; j--) {
                if (a[j] > a[max]) {
                    max = j;
                }
            }

            for (int k = 0; k < len; k++) {
                if (CardTypeTool.getTrueValue(list2.get(k)) == max) {
                    list3.add(list2.get(k));
                }
            }
            list2.remove(list3);
            a[max] = 0;
        }
        return list3;
    }

    // 计算手数
    public static int getTimes(Model model) {
        int count = 0;
        count += model.a4.size() + model.a3.size();
        count += model.a111222.size() + model.a112233.size() + model.a123.size();
        int temp = 0;
        temp = model.a1.size() + model.a2.size() - model.a3.size() - model.a4.size() * 2;
        for (int i = 0; i < model.a111222.size(); ++i) {
            String[] plane = model.a111222.get(i).split(",");
            temp -= plane.length;
        }
        count += temp > 0 ? temp : 0;
        return count;
    }

    // 计算权值 单1 对子2 带3 炸弹10 飞机7 双顺5 顺子4
    public static int getCountValues(Model model) {
        int count = 0;
        count += model.a1.size() + model.a2.size() * 2 + model.a3.size() * 3;
        count += model.a4.size() * 10 + model.a111222.size() * 7 + model.a112233.size() * 5 + model.a123.size() * 4;
        return count;
    }

    // 通过name得到card
    public static List<Integer> getCardsByName(List<Integer> list, String s) {
        String[] name = s.split(",");
        ArrayList<Integer> temp = new ArrayList<Integer>();
        int c = 0;
        for (int i = 0, len = list.size(); i < len; i++) {
            if (list.get(i) == Short.parseShort(name[c])) {
                temp.add(list.get(i));
                if (c == name.length - 1) {
                    return temp;
                }
                c++;
                i = 0;
            }
        }
        return temp;
    }

    // 判断某牌型还存在list不
    public static Boolean isExists(ArrayList<Integer> list, String s) {
        String name[] = s.split(",");
        int c = 0;
        for (int i = 0, len = list.size(); i < len; i++) {
            if (list.get(i) == Short.parseShort(name[c])) {
                if (c == name.length - 1) {
                    return true;
                }
                c++;
                i = 0;
            }
        }

        return false;
    }

    /**
     * 设定牌的顺序（从大到小）
     */
    public static void setStrOrder(List<String> list) {
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] str1 = o1.split(",");
                String[] str2 = o2.split(",");
                int card1 = Short.parseShort(str1[0]);
                int card2 = Short.parseShort(str2[0]);
                int a1 = CardTypeTool.getColor(card1);// 花色
                int a2 = CardTypeTool.getColor(card2);
                int b1 = CardTypeTool.getTrueValue(card1);// 数值
                int b2 = CardTypeTool.getTrueValue(card2);
                int flag;
                flag = b2 - b1;
                if (flag == 0) {
                    return a2 - a1;
                } else {
                    return flag;
                }
            }
        });
    }

    /**
     * 设定牌的顺序（从小到大）
     */
    public static void setOrder2(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o2, Integer o1) {
                int a1 = CardTypeTool.getColor(o1);// 花色
                int a2 = CardTypeTool.getColor(o2);
                int b1 = CardTypeTool.getTrueValue(o1);// 数值
                int b2 = CardTypeTool.getTrueValue(o2);
                int flag;
                flag = b2 - b1;
                if (flag == 0) {
                    return a2 - a1;
                } else {
                    return flag;
                }
            }
        });
    }

    /**
     * 设定牌的顺序（从大到小）
     */
    public static void setOrder(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
            int a1 = CardTypeTool.getColor(o1);// 花色
            int a2 = CardTypeTool.getColor(o2);
            int b1 = CardTypeTool.getTrueValue(o1);// 数值
            int b2 = CardTypeTool.getTrueValue(o2);
            int flag;
            flag = b2 - b1;
            if (flag == 0) {
                return a2 - a1;
            } else {
                return flag;
            }
            }
        });
    }

    /**
     * 获取拥有4张一个
     */
    public static List<Integer> getBoomCount(List<Integer> cards) {
        setOrder(cards);
        List<Integer> boomCount = new ArrayList<>();
        List<Integer> boomTemp = new ArrayList<>();
        int nowValue = 0;
        for (int card : cards) {
            int value = getTrueValue(card);
            if (nowValue == value) {
                if (!boomTemp.contains(nowValue)) {
                    boomTemp.add(nowValue);

                }
                boomTemp.add(value);
            } else {
                boomTemp.clear();
            }
            nowValue = value;
            if (boomTemp.size() == 4) {
                // 4张一样的牌是炸弹
                boomCount.addAll(boomTemp);
            }

        }
        return boomCount;
    }

    public static void getCards(int value, List<Integer> cards, List<String> addCards, int addNum) {
        int find = 0;
        for (int cardValue : cards) {
            if (addCards.contains(cardValue)) {
                continue;
            }
            if (getTrueValue(cardValue) == value) {
                addCards.add(cardValue + "");
                find++;
            }
            if (find >= addNum) {
                break;
            }
        }
    }

    /**
     * 验证cardIds的牌相对于nowDisCardIds的牌能否出牌
     */
    public static boolean isBigger(List<Integer> cardIds, List<Integer> nowDisCardIds) {
        List<Integer> list1 = new ArrayList<>(cardIds);
        List<Integer> list2 = new ArrayList<>(nowDisCardIds);
        CardType cardType1 = CardTypeTool.jugdeType(list1);
        CardType cardType2 = CardTypeTool.jugdeType(list2);
        int len2 = list2.size();
        setOrder(list1);
        setOrder(list2);
        list1 = getOrder2(list1);
        list2 = getOrder2(list2);

        int maxCardValue1 = list1.get(0) % 100;
        int maxCardValue2 = list2.get(0) % 100;
        // 如果出炸弹
        if (cardType1 == CardType.c1617) {
            return true;
        }
        if (cardType2 == CardType.c1617) {
            return false;
        }
        if (cardType1 == CardType.c4) {
            if (cardType2 == CardType.c4) {
                if (len2 == 2) {
                    return false;
                }
                if (maxCardValue1 > maxCardValue2) {
                    return true;
                }
            } else {
                return true;
            }
        }
        // 如果不出炸弹，必须牌型与上游相同
        // 牌型不相等，则不能算打得起
        return cardType1.getType() == cardType2.getType() && maxCardValue1 > maxCardValue2;
    }
}
