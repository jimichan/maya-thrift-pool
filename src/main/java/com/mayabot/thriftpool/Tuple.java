package com.mayabot.thriftpool;

import org.apache.thrift.transport.TTransport;

import com.mayabot.thriftpool.connpool.ThriftServerAddress;

public class Tuple {
	public TTransport transport;
	public ThriftServerAddress server;

	public Tuple(TTransport transport, ThriftServerAddress server) {
		super();
		this.transport = transport;
		this.server = server;
	}
}