package com.example.http_server;

public interface Data {
  String guideWebMsg = "GET / HTTP/1.1\n" +
    "Host: zzdirty.info\n" +
    "Connection: keep-alive\n" +
    "Cache-Control: max-age=0\n" +
    "Upgrade-Insecure-Requests: 1\n" +
    "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3760.400 QQBrowser/10.5.4083.400 PostmanRuntime/7.28.3\n" +
    "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n" +
    "Accept-Encoding: gzip, deflate\n" +
    "Accept-Language: zh-CN,zh;q=0.9";
}
