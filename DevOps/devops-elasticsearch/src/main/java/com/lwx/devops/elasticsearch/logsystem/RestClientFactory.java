/**
 * 
 */
package com.lwx.devops.elasticsearch.logsystem;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author fengql
 * @date 2019年5月17日 下午3:56:51
 * 
 */
public class RestClientFactory {
	private static Logger logger = LoggerFactory.getLogger(RestClientFactory.class);

	private static final RestClientFactory factory = new RestClientFactory();

	private static RestHighLevelClient client;

	private static int retryInterval = 1000;
	
	private RestClientFactory() {}

	public static RestHighLevelClient buildClient(String indexerServers) {
		if(client == null) {
			createClient(indexerServers);
		}
		return client;
	}
	
	public static synchronized RestHighLevelClient createClient(String indexerServers) {
		logger.info("集群地址：" + indexerServers);
		String[] servers = indexerServers.split(",");
		HttpHost[] hosts = new HttpHost[servers.length];
		for (int i=0; i < servers.length ; i++) {
			String server = servers[i];
			String [] splits = server.split(":");
			String ip = splits[0];
			String port = splits[1];
			HttpHost host = new HttpHost(ip, Integer.parseInt(port), "http");
			hosts[i] = host;
		}
		while (true) {
			try {
				client = new RestHighLevelClient(
						RestClient.builder(hosts));
				break;
			} catch (Exception e) {
				logger.info("------------------------创建索引客户端失败-----------------------------");
				try {
					Thread.sleep(retryInterval);// 间隔1秒后重连
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		return client;
	}
	
	public static RestClientFactory getInstance() {
		return factory;
	}
	
	
	

}
