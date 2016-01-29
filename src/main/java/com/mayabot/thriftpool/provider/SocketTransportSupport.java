package com.mayabot.thriftpool.provider;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.mayabot.thriftpool.TransportProvider;

public class SocketTransportSupport implements TransportProvider {

	public TTransport get(String host, int port) {
		return new TSocket(host, port);
	}

}
