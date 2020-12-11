package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Column;
import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;

@Table(alias = "server_config")
public class ServerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String host;
    @Column(alias = "chathost")
    private String chatHost;
    private String intranet;
    private String gameType;
    private String matchType;
    private Integer onlineCount;
    private String extend;
    private Integer serverType;
    public Integer getServerType() {
		return serverType;
	}

	public void setServerType(Integer serverType) {
		this.serverType = serverType;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getChatHost() {
        return chatHost;
    }

    public void setChatHost(String chatHost) {
        this.chatHost = chatHost;
    }

    public String getIntranet() {
        return intranet;
    }

    public void setIntranet(String intranet) {
        this.intranet = intranet;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public Integer getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(Integer onlineCount) {
        this.onlineCount = onlineCount;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
}
