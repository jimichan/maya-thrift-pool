package com.mayabot.thriftpool;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mayabot.thriftpool.connpool.ServerList;
import com.mayabot.thriftpool.connpool.ThriftConnectionPool;
import com.mayabot.thriftpool.connpool.ThriftServerAddress;

public abstract class ClientProxyBase {

	protected ThriftConnectionPool connectionPool;
	protected ServerList serverList;
	protected TProtocolFactory protocolProvider;
	protected org.apache.thrift.TServiceClientFactory<?> clientFactory;

	protected LoadingCache<TTransport, TServiceClient> clientCache = CacheBuilder
			.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES)
			.build(new CacheLoader<TTransport, TServiceClient>() {
				@Override
				public TServiceClient load(TTransport transport)
						throws Exception {
					TServiceClient client = clientFactory
							.getClient(protocolProvider.getProtocol(transport));
					return client;
				}
			});

	private Method pingMethod;
	private long lastPingtime;

	public void init(ThriftConnectionPool connectionPool,
			ServerList serverList, TProtocolFactory protocolProvider,
			TServiceClientFactory<?> clientFactory) {
		this.connectionPool = connectionPool;
		this.serverList = serverList;
		this.protocolProvider = protocolProvider;
		this.clientFactory = clientFactory;

		try {
			// public boolean ping();
			Object client = clientFactory.getClient(null);
			Method m = client.getClass().getMethod("ping", null);
			if (m != null && m.isAccessible()
					&& m.getReturnType().equals(Boolean.TYPE)) {
				pingMethod = m;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private byte hasPingMethod = 0;// 0未知 1没有 2有

	public Tuple tryGetConnection() {
		try {
			ThriftServerAddress server = serverList.nextServer();
			TTransport transport = connectionPool.getConnection(server);

			

			return new Tuple(transport, server);
		} catch (Exception e) {
			// retry n-1 times
			int size = serverList.size() - 1;

			if (size >= 1) {

				for (int i = 0; i < size; i++) {
					try {
						ThriftServerAddress server = serverList.nextServer();
						TTransport t = connectionPool.getConnection(server);
						return new Tuple(t, server);
					} catch (Exception e1) {
					}
				}

				throw new RuntimeException(e);

			} else {
				throw new RuntimeException(e);
			}
		}
	}

	public ThriftConnectionPool getConnectionPool() {
		return connectionPool;
	}

	public void setConnectionPool(ThriftConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
	}

	public ServerList getServerList() {
		return serverList;
	}

	public void setServerList(ServerList servers) {
		this.serverList = servers;
	}

	public org.apache.thrift.TServiceClientFactory<?> getClientFactory() {
		return clientFactory;
	}

	public void setClientFactory(
			org.apache.thrift.TServiceClientFactory<?> clientFactory) {
		this.clientFactory = clientFactory;
	}

	public TProtocolFactory getProtocolProvider() {
		return protocolProvider;
	}

	public void setProtocolProvider(TProtocolFactory protocolProvider) {
		this.protocolProvider = protocolProvider;
	}
}
