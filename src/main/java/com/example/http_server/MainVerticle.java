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

  static HashMap<NetSocket, NetSocket> map_s_t = new HashMap<>();
  static HashMap<NetSocket, NetSocket> map_t_s = new HashMap<>();
  static HashMap<URL, Buffer> map_url_buffer = new HashMap<>();
  static HashMap<URL, String> map_url_ETag = new HashMap<>();


  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    NetServer server = vertx.createNetServer();

    server.connectHandler(socket -> {
      // 用户过滤
//      userFilter(socket);
      socket.handler(buffer -> {
        String message = buffer.toString();
        System.out.println(message);
        System.out.println("数据的长度是" + message.length());
        // 网站过滤
        if (!webFilter(message, socket)) {
          // 网站引导
          if (webGuide(message)) {
            message = Data.guideWebMsg;
          }
          {
            try {
              sendHttpRequest(vertx, message, socket);
            } catch (MalformedURLException e) {
              e.printStackTrace();
            }
          }
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

  // 网站引导
  public static boolean webGuide(String message) {
    String url = parseHttpHeader(message);
    for (int i = 0; i < Data.guideWeb.length; i++) {
      if (url.equals(Data.guideWeb[i])) {
        return true;
      }
    }
    return false;
  }

  // 网站过滤
  public static boolean webFilter(String message, NetSocket socket) {
    String url = parseHttpHeader(message);
    String checkHttps = url.substring(0, 4);
    if (!checkHttps.equals("http")) {
      socket.write(Data.error_404);
      return true;
    }
    return false;
  }


  // 用户过滤
  public static void userFilter(NetSocket socket) {
    if (socket.remoteAddress().host().equals("127.0.0.1")) {
      socket.close();
    }
  }

  // 解析头部url
  public static String parseHttpHeader(String reqMsg) {
    String[] msg = reqMsg.split("\n");
    String[] info = msg[0].split(" ");
    if (info[0].equals("CONNECT")) {
      return info[1].split(":")[0];
    } else {
      return info[1];
    }
  }

  // 读取头部的ETag与状态码
  public static ResHeader parseResHeader(Buffer buffer) {
    String resMsg = buffer.toString();
    String[] msg = resMsg.split("\n");
    String statusCode = "";
    String ETag = "";
    int length = msg.length;
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
      if (count >= length) {
        break;
      }
    }
    return new ResHeader(statusCode, ETag);
  }

  // 在报文段头部添加ETag
  public static String addEtag(URL url, String message) {
    String m = message.substring(0, message.length() - 2);
    return m + "If-None-Match: " + map_url_ETag.get(url) + "\n" + "\r\n";
  }

  // 创建客户端发送报文
  public static void sendHttpRequest(Vertx vertx, String message, NetSocket srv_clt_socket) throws MalformedURLException {
    NetClient client = vertx.createNetClient();
    String u = parseHttpHeader(message);
    URL url = new URL(u);
    // 与目的主机连接
    client.connect(url.getDefaultPort(), url.getHost(), res -> {
      if (res.succeeded()) {
        System.out.println("服务器已连接成功");
        NetSocket socket = res.result();
        map_s_t.put(srv_clt_socket, socket);
        map_t_s.put(socket, srv_clt_socket);
        String msg = message;
        if (map_url_ETag.get(url) != null) {
          msg = addEtag(url, message);
        }

        // 将客户端获得的请求发给目的主机
        socket.write(msg);
        System.out.println(msg);

        socket.handler(buffer -> {
          // 获得响应头的状态码和ETag
          NetSocket client_socket = map_t_s.get(socket);
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
