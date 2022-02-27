package kr.mcsv.client.websocket;

import com.neovisionaries.ws.client.*;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.Main;
import kr.mcsv.client.authorization.MCSVAuthorization;
import kr.mcsv.client.core.MCSVCore;
import kr.mcsv.client.server.MCSVServer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class MCSVWebsocketSession {
    WebSocket ws;
    WebSocketAdapter adapter = null;

    int retryTimeout = 60;
    static int retryTimeoutMax = 60;

    boolean preventReconnect = false;

    private MCSVServer server;
    public MCSVWebsocketSession(MCSVServer server) {
        this.server = server;
    }

    public void setPreventReconnect(boolean preventReconnect) {
        this.preventReconnect = preventReconnect;
    }

    public WebSocket connect() throws IOException, InvalidRefreshTokenException, WebSocketException {
        if (this.isConnected()) return null;
        
        // if prevent reconnect is activated, do not reconnect.
        if (this.ws != null && !this.isConnected()) {
            if (this.preventReconnect) {
                return null;
            }
        }

        WebSocket ws;

        WebSocketFactory factory = new WebSocketFactory();

        URI wsURI = URI.create("wss://api.mcsv.kr/v1/servers/"+this.server.getServerId()+"/ws/server");
        factory.setServerName(wsURI.getHost());

        ws = factory.createSocket(wsURI);

        if (this.adapter == null) {
            this.adapter = new MCSVWebsocketListener(this);
        }

        ws.addHeader("Authorization", "Bearer "+ Main.core.authorization.getAccessToken());
        ws.addListener(this.adapter);
        ws.setPingInterval(20 * 1000);

        ws.connect();
        this.ws = ws;

        return this.ws;
    }

    public void retryConnect() {

    }

    public void disconnect() {
        ws.disconnect();
        this.preventReconnect = true;
    }

    public boolean isConnected() {
        return this.ws != null && this.ws.isOpen();
    }

    public void sendMessage(String content) {
        ws.sendText(content);
    }

    public void sendMessage(JSONObject json) {
        this.sendMessage(json.toJSONString());
    }
}
