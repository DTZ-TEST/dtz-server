package com.sy599.game.qipai.bbtz.command;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.qipai.bbtz.bean.BbtzPlayer;
import com.sy599.game.qipai.bbtz.command.com.ComCommand;
import com.sy599.game.qipai.bbtz.command.play.PlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class BbtzCommandProcessor extends AbstractBaseCommandProcessor {
	private static BbtzCommandProcessor processor = new BbtzCommandProcessor();
	private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<>();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

	static {
		commandMap.put(WebSocketMsgType.cs_play, PlayCommand.class);
		commandMap.put(WebSocketMsgType.cs_com, ComCommand.class);

		try {
			for (Short type : commandMap.keySet()) {
				Class<? extends BaseCommand> cl = commandMap.get(type);
				BaseCommand action = ObjectUtil.newInstance(cl);
				Map<Class<?>, Short> msgTypeMap = action.getMsgTypeMap();
				if (msgTypeMap != null && !msgTypeMap.isEmpty()) {
					for (Class<?> msgClass : msgTypeMap.keySet()) {
						if (msgClassToMsgTypeMap.containsKey(msgClass)) {
							throw new Exception("msgClassToMsgTypeMap err!!!!");

						} else {
							msgClassToMsgTypeMap.put(msgClass, msgTypeMap.get(msgClass));

						}
					}
				}
			}

		} catch (Exception e) {
			LogUtil.e("SocketAcitonProcessor err:", e);

		}

	}

	public static BbtzCommandProcessor getInstance() {
		return processor;
	}

	public short getMsgType(Class<?> clazz) {
		if (msgClassToMsgTypeMap.containsKey(clazz)) {
			return msgClassToMsgTypeMap.get(clazz);
		}
		return 0;
	}

	@Override
	public void process(Player player, MessageUnit message) {
		int code = 0;
		try {
			BbtzPlayer pdkPlayer = player.getPlayer(processor);
			BaseCommand action = ObjectUtil.newInstance(commandMap.get(message.getMsgType()));
			action.setPlayer(player);
			action.execute(pdkPlayer, message);
		} catch (Exception e) {
			LogUtil.e("socket err: " + player.getUserId() + " " + message.getMsgType() + " " + LogUtil.printlnLog(message.getMessage()), e);
			code = -1;
		} finally {
			if (code != 0) {
			}
		}
	}

}
