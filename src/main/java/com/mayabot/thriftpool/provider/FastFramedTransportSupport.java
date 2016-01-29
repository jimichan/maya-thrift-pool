package com.mayabot.thriftpool.provider;

import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.mayabot.thriftpool.TransportProvider;

public class FastFramedTransportSupport implements TransportProvider {

	public TTransport get(String host, int port) {
		return new TFastFramedTransport(new TSocket(host, port));
	}

}
