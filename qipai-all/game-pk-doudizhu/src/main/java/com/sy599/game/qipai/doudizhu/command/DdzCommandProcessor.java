package com.sy599.game.qipai.doudizhu.command;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.doudizhu.bean.DdzPlayer;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.qipai.doudizhu.command.com.DdzComCommand;
import com.sy599.game.qipai.doudizhu.command.play.PlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class DdzCommandProcessor extends AbstractBaseCommandProcessor {
	private static DdzCommandProcessor processor = new DdzCommandProcessor();
	private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<>();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

	static {
		commandMap.put(WebSocketMsgType.cs_com, DdzComCommand.class);
		commandMap.put(WebSocketMsgType.cs_play, PlayCommand.class);
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

	public static DdzCommandProcessor getInstance() {
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
			DdzPlayer pdkPlayer = player.getPlayer(processor);
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
