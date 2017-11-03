package org.some.thing.rx.loader;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import lombok.SneakyThrows;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.URI;
import java.util.Map;

public class ClientFactory {

  @SneakyThrows
  public static HttpClientRequest<ByteBuf, ByteBuf> create(String address, boolean ignoreSSl, Map<String,String> headers) {
    final URI uri = URI.create(address.startsWith("http") ? address :  "http://"+address);
    final int port  = uri.getPort() < 0 ? 80 : uri.getPort();

    HttpClient<ByteBuf, ByteBuf> client = HttpClient.newClient(uri.getHost(), port);
    if(uri.getScheme().equalsIgnoreCase("https")) {
      if (ignoreSSl)
        client = client.unsafeSecure();
      else
        client = client.secure(defaultSSLEngineForClient(uri.getHost(), uri.getPort()));
    }

     HttpClientRequest<ByteBuf, ByteBuf> resultedClient = client.createGet(uri.getPath() + "?" + uri.getQuery());
    if(headers != null) {
      for(Map.Entry<String,String> header: headers.entrySet()) {
        resultedClient = resultedClient.addHeader(header.getKey(), header.getValue());
      }
    }
    return resultedClient;
  }

  @SneakyThrows
  private static SSLEngine defaultSSLEngineForClient(String host, int port) {
    SSLContext sslCtx = SSLContext.getDefault();
    SSLEngine sslEngine = sslCtx.createSSLEngine(host, port);
    sslEngine.setUseClientMode(true);
    return sslEngine;
  }
}
