package com.example.http_server;

import java.util.ArrayList;

public interface Data {
  String guideWebMsg = "GET http://jwts.hit.edu.cn/loginLdapQian HTTP/1.1\r\n" +
    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36\r\n" +
    "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\r\n" +
    "Host: jwts.hit.edu.cn\r\n" +
    "Accept-Encoding: gzip, deflate, br\r\n" +
    "Accept-Language: zh-CN,zh;q=0.9\r\n" +
    "Connection: keep-alive\r\n\r\n";

  String[] guideWeb = {"http://zzdirty.info/", "http://www.hit.edu.cn/"};

  String error_404 = "HTTP/1.1 404 Not Found\r\nr" +
    "Content-Length: 1163\r\n" +
    "Connection: keep-alive\r\n" +
    "Content-Type: text/html\r\n" +
    "Keep-Alive: timeout=4\r\n" +
    "Proxy-Connection: keep-alive\r\n" +
    "Server: Microsoft-IIS/7.5\r\n" +
    "X-Powered-By: ASP.NET\r\n\r\n";

}
