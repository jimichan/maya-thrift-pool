package com.mayabot.thriftpool;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

public interface TProtocolProvider {
	TProtocol get(TTransport transport);
}
