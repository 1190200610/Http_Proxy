package com.example.http_server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


public class MainVerticle extends AbstractVerticle {

  static HashMap<NetSocket, NetSocket> map1 = new HashMap<>();
  static HashMap<NetSocket, NetSocket> map2 = new HashMap<>();
  static HashMap<URL, Buffer> map_url_buffer = new HashMap<>();
  static HashMap<URL, String> map_url_ETag = new HashMap<>();


  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    NetServer server = vertx.createNetServer();

    server.connectHandler(socket -> {
      socket.handler(buffer -> {
        String message = buffer.toString();
        System.out.println("数据的长度是" + message.length());
        try {
          sendHttpRequest(vertx, message, socket);
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      });

      socket.closeHandler(close -> {
        System.out.println("客户端已断开连接");
      });
    });

    server.listen(5555, res -> {
      if (res.succeeded()) {
        System.out.println("服务器启动成功");
      }
    });
  }

  public static String parseHttpHeader(String reqMsg) {
    String[] msg = reqMsg.split("\n");
    int length = msg.length;
    String[] info = msg[0].split(" ");
    return info[1];
  }

  public static ResHeader parseResHeader(Buffer buffer) {
    String resMsg = buffer.toString();
    String[] msg = resMsg.split("\n");
    String statusCode = "";
    String ETag = "";
    if (!msg[0].split(" ")[0].equals("HTTP/1.1")) {
      return null;
    }
    statusCode = msg[0].split(" ")[1];
    int count = 0;
    while (true) {
      String[] msg_split = msg[count].split(":");
      if (msg_split[0].equals("ETag")) {
        ETag = msg_split[1].substring(1);
        break;
      }
      count++;
    }
    return new ResHeader(statusCode, ETag);
  }

  public static String addEtag(URL url, String message) {
      String m = message.substring(0, message.length() - 2);
      return m + "If-None-Match: " + map_url_ETag.get(url) + "\r\n";
  }


  public static void sendHttpRequest(Vertx vertx, String message, NetSocket clientSocket) throws MalformedURLException {
    NetClient client = vertx.createNetClient();
    String u = parseHttpHeader(message);
    URL url = new URL(u);
    // 与目的主机连接
    client.connect(url.getDefaultPort(), url.getHost(), res -> {
      if (res.succeeded()) {
        System.out.println("服务器已连接成功");
        NetSocket socket = res.result();
        map1.put(clientSocket, socket);
        map2.put(socket, clientSocket);
        String msg = message;
        if (map_url_ETag.get(url) != null) {
          msg = addEtag(url, message);
        }

        // 将客户端获得的请求发给目的主机
        socket.write(msg);
        System.out.println(msg);

        NetSocket client_socket = map2.get(socket);
        socket.handler(buffer -> {
          // 获得响应头的状态码和ETag
          ResHeader resHeader = parseResHeader(buffer);
          if (resHeader != null) {
            // 进行缓存
            if (resHeader.getStatusCode().equals("200")) {
              map_url_buffer.put(url, buffer);
              map_url_ETag.put(url, resHeader.getETag());
              client_socket.write(buffer);
            } else if (resHeader.getStatusCode().equals("304")) {
              client_socket.write(map_url_buffer.get(url));
            }
          } else {
            map_url_buffer.get(url).appendBuffer(buffer);
            client_socket.write(buffer);
          }
        });
      } else {
        System.out.println("服务器连接失败" + res.cause().getMessage());
      }
    });
  }


}
