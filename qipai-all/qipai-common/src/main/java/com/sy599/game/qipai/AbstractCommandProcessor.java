package com.sy599.game.qipai;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.Map;

public abstract class AbstractCommandProcessor<T extends Player> extends AbstractBaseCommandProcessor<T> {

    public abstract Map<Short, Class<? extends BaseCommand<T>>> loadCommands();

    public void process(T player, MessageUnit message) {
        try {
            Class<? extends BaseCommand<T>> cls = loadCommands().get(message.getMsgType());
            if (cls == null) {
                LogUtil.msgLog.warn("command is not exists:msgType={},processor={},userId={},playerClass={}"
                        , message.getMsgType(), getClass().getName(), player == null ? "null" : player.getUserId(), player == null ? "unknown" : player.getClass().getName());
                return;
            }
            BaseCommand<T> action = ObjectUtil.newInstance(cls);
            action.setPlayer(player);
            action.execute(player, message);
        } catch (Exception e) {
            LogUtil.errorLog.error("socket err: userId:" + player.getUserId() + ",msgType:" + message.getMsgType() + ",processor:" + getClass().getName() + ",errorMsg:" + e.getMessage(), e);
        }
    }
}
