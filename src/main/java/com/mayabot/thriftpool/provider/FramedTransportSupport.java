package com.mayabot.thriftpool.provider;

import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.mayabot.thriftpool.TransportProvider;

public class FramedTransportSupport implements TransportProvider {
	
	public TTransport get(String host, int port) {
		return new TFramedTransport(new TSocket(host, port));
	}

}
