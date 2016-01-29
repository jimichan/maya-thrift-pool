package com.mayabot.thriftpool.provider;

import java.io.IOException;

import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TTransport;

import com.mayabot.thriftpool.TransportProvider;

/**
 * Transport for use with async client.
 * @author jimichan
 *
 */
public class NoblockSocketTransportSupport implements TransportProvider {

	public TTransport get(String host, int port) {
		try {
			return new TNonblockingSocket(host, port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
