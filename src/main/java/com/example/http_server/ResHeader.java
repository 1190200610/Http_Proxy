package com.example.http_server;

public class ResHeader {
  private String statusCode;
  private String ETag;

  public ResHeader(String statusCode, String ETag) {
    this.statusCode = statusCode;
    this.ETag = ETag;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public String getETag() {
    return ETag;
  }

  @Override
  public String toString() {
    return "ResHeader{" +
      "statusCode='" + statusCode + '\'' +
      ", ETag='" + ETag + '\'' +
      '}';
  }
}
