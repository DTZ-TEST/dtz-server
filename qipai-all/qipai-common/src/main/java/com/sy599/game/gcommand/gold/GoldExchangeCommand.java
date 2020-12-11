package com.sy599.game.gcommand.gold;

import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SystemCommonInfoType;
import com.sy599.game.db.bean.GoodsItem;
import com.sy599.game.db.bean.UserExtend;
import com.sy599.game.db.dao.ItemExchangeDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.SystemCommonInfoManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.staticdata.StaticDataManager;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoldExchangeCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

        if (player.getMyExtend().isGroupMatch()){
            player.writeErrMsg("正在为您匹配房间，请不要进行其他操作");
            return;
        }

        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> ints = req.getParamsList();
        if (ints.size() < 2) {
            return;
        }

        int kind = ints.get(0);

        int type = ints.get(1);

        if (kind == 0) {
            //0钻石兑换积分1积分兑换钻石2钻石兑换体力
            Map<Integer, GoodsItem> map = StaticDataManager.loadGoodsItems(type);

            if (map == null || map.size()==0) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_213));
                return;
            }
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), type, date + " 00:00:00", date + " 23:59:59");

            player.writeComMessage(WebSocketMsgType.res_code_gold_exchange_items, kind, type, String.valueOf(sum), type==0?"10000000":"50000", JacksonUtil.writeValueAsString(map.values()));
        } else if (kind == 1) {
            if (ints.size() < 3) {
                return;
            }
            int id = ints.get(2);

            if (id <= 0) {
                return;
            }
            GoodsItem goodsItem = StaticDataManager.loadGoodsItem(type, id);
            if (goodsItem == null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_214));
                return;
            }

            if (type == 0) {//钻石兑换积分
                long val = Math.round(goodsItem.getAmount() * (goodsItem.getDiscount() * 1.0 / 100));
                if (val>0&&player.loadAllCards() < val) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                    return;
                }

                if(player.getGoldPlayer()==null||player.loadAllGolds()<=0){
                    player.loadGoldPlayer(true);
                }

                int value = (int) Math.round((goodsItem.getCount() * goodsItem.getRatio() * 1.0 / 100) + goodsItem.getGive());
                player.changeCards(0, -val, true, 0, false, CardSourceType.cardExchangeGold);
                player.changeGold(value, 0, false, 0, true);
                LogUtil.msgLog.info("diamond 2 gold:userId=" + player.getUserId() + "itemId=" + goodsItem.getId() + ",count=" + val + ",value=" + value);

                Date currentDate=new Date();
                Map<String,Object> map = new HashMap<>();
                map.put("userId",String.valueOf(player.getUserId()));
                map.put("itemType",goodsItem.getType());
                map.put("itemId",goodsItem.getId());
                map.put("itemName",goodsItem.getName());
                map.put("itemAmount",val);
                map.put("itemCount",value);
                map.put("itemGive",goodsItem.getGive());
                map.put("itemMsg",JacksonUtil.writeValueAsString(goodsItem));
                map.put("createdTime",currentDate);
                ItemExchangeDao.getInstance().save(map);

                String date = new SimpleDateFormat("yyyy-MM-dd").format(currentDate);
                int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), type, date + " 00:00:00", date + " 23:59:59");

                player.writeComMessage(WebSocketMsgType.res_code_gold_exchange_items, kind, type, 1,String.valueOf(sum),"10000000");
            } else if (type == 1) {//积分兑换钻石
                int rest = NumberUtils.toInt(SystemCommonInfoManager.getInstance().getSystemCommonInfo(SystemCommonInfoType.goldGive).getContent(),Integer.parseInt(SystemCommonInfoType.goldGive.getContent()));
                int val = (int) Math.round(goodsItem.getAmount() * (goodsItem.getDiscount() * 1.0 / 100));
                if (player.loadAllGolds() < val) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_215));
                    return;
                }else if (player.loadAllGolds()-val < rest){
                    player.writeErrMsg(LangMsg.code_216, rest);
                    return;
                }
                int value = (int) Math.round((goodsItem.getCount() * goodsItem.getRatio() * 1.0 / 100) + goodsItem.getGive());

                player.changeCards(value, 0, true, 0, false, CardSourceType.goldExchangeCard);
                player.changeGold(0, -val, false, 0, true);
                LogUtil.msgLog.info("gold 2 diamond:userId=" + player.getUserId() + ",count=" + val + ",value=" + value);

                Date currentDate=new Date();
                Map<String,Object> map = new HashMap<>();
                map.put("userId",String.valueOf(player.getUserId()));
                map.put("itemType",goodsItem.getType());
                map.put("itemId",goodsItem.getId());
                map.put("itemName",goodsItem.getName());
                map.put("itemAmount",val);
                map.put("itemCount",value);
                map.put("itemGive",goodsItem.getGive());
                map.put("itemMsg",JacksonUtil.writeValueAsString(goodsItem));
                map.put("createdTime",currentDate);
                ItemExchangeDao.getInstance().save(map);

                String date = new SimpleDateFormat("yyyy-MM-dd").format(currentDate);
                int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), type, date + " 00:00:00", date + " 23:59:59");

                player.writeComMessage(WebSocketMsgType.res_code_gold_exchange_items, kind, type, 1,String.valueOf(sum),"50000");
            }else if (type == 2) {//钻石兑换体力
                long val = Math.round(goodsItem.getAmount() * (goodsItem.getDiscount() * 1.0 / 100));
                if (val>0&&player.loadAllCards() < val) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                    return;
                }

                Date currentDate=new Date();

                int value = (int) Math.round((goodsItem.getCount() * goodsItem.getRatio() * 1.0 / 100) + goodsItem.getGive());
                player.changeCards(0, -val, true, 0, false, CardSourceType.cardExchangeTili);
                UserExtend userExtend = new UserExtend();
                userExtend.setUserId(String.valueOf(player.getUserId()));
                userExtend.setCreatedTime(currentDate);
                userExtend.setModifiedTime(currentDate);
                userExtend.setMsgDesc("体力");
                userExtend.setMsgKey(UserResourceType.TILI.name());
                userExtend.setMsgType(UserResourceType.TILI.getType());
                userExtend.setMsgState("1");
                userExtend.setMsgValue(String.valueOf(value));
                UserDao.getInstance().saveOrUpdateUserExtend(userExtend);

                player.changeTili(value);
                player.writeUserResourceMessage(UserResourceType.TILI,value,player.getUserTili());

                LogUtil.msgLog.info("diamond 2 tili:userId=" + player.getUserId() + "itemId=" + goodsItem.getId() + ",count=" + val + ",value=" + value);

                Map<String,Object> map = new HashMap<>();
                map.put("userId",String.valueOf(player.getUserId()));
                map.put("itemType",goodsItem.getType());
                map.put("itemId",goodsItem.getId());
                map.put("itemName",goodsItem.getName());
                map.put("itemAmount",val);
                map.put("itemCount",value);
                map.put("itemGive",goodsItem.getGive());
                map.put("itemMsg",JacksonUtil.writeValueAsString(goodsItem));
                map.put("createdTime",currentDate);
                ItemExchangeDao.getInstance().save(map);

                String date = new SimpleDateFormat("yyyy-MM-dd").format(currentDate);
                int sum = ItemExchangeDao.getInstance().sumItemExchange(player.getUserId(), type, date + " 00:00:00", date + " 23:59:59");

                player.writeComMessage(WebSocketMsgType.res_code_gold_exchange_items, kind, type, 1,String.valueOf(sum),"10000");
            }
        }
    }

    @Override
    public void setMsgTypeMap() {
    }

}
