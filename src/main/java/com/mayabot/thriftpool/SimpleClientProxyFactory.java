package com.mayabot.thriftpool;

import org.apache.thrift.TServiceClientFactory;

import com.mayabot.thriftpool.connpool.ThriftConnectionPool;
import com.mayabot.thriftpool.provider.FramedTransportSupport;
import com.mayabot.thriftpool.provider.SocketTransportSupport;

public class SimpleClientProxyFactory {

	public static <F> F makeClient(Class<F> ifaceClass,
			TServiceClientFactory<?> clientFactory, String hosts,
			ThriftConnectionPool pool, TProtocolProvider protocolProvider) {
		return ClientProxyFactory.makeClient(ifaceClass, clientFactory, hosts,
				pool, protocolProvider, new SocketTransportSupport());
	}

	public static <F> F makeClientFramedTransport(Class<F> ifaceClass,
			TServiceClientFactory<?> clientFactory, String hosts,
			ThriftConnectionPool pool, TProtocolProvider protocolProvider) {
		return ClientProxyFactory.makeClient(ifaceClass, clientFactory, hosts,
				pool, protocolProvider, new FramedTransportSupport());
	}
	
	

}
