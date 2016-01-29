package com.mayabot.thriftpool;

import org.apache.thrift.transport.TTransport;

public interface TransportProvider {
	public TTransport get(String host, int port);
}
