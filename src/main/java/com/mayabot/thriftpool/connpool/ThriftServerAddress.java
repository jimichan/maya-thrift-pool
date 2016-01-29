package com.mayabot.thriftpool.connpool;

import java.util.List;

import org.apache.thrift.transport.TTransport;

import com.google.common.base.Splitter;
import com.mayabot.thriftpool.TransportProvider;

public class ThriftServerAddress {

	private static Splitter splitter = Splitter.on(':');

	private final String host;

	private final int port;

	private boolean online = true;
	
	private TransportProvider provider;

	public void offline() {
		online = false;
	}

	public void online() {
		online = true;
	}

	public boolean isOnline() {
		return online;
	}

	/**
	 * <p>
	 * Constructor for ThriftServerInfo.
	 * </p>
	 *
	 * @param hostAndPort
	 */
	public ThriftServerAddress(String hostAndPort,TransportProvider transportProvider) {
		List<String> split = splitter.splitToList(hostAndPort);
		assert split.size() == 2;
		this.host = split.get(0);
		this.port = Integer.parseInt(split.get(1));
		this.provider = transportProvider;
		assert this.provider!=null;
		assert this.port!=0;
		assert this.host!=null;
	}

	public TTransport createTransport() {
		return this.provider.get(host, port);
	}
	
	/**
	 * <p>
	 * Getter for the field <code>host</code>.
	 * </p>
	 *
	 * @return host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * <p>
	 * Getter for the field <code>port</code>.
	 * </p>
	 *
	 * @return port
	 */
	public int getPort() {
		return port;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ThriftServerAddress)) {
			return false;
		}
		ThriftServerAddress other = (ThriftServerAddress) obj;
		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}


	public TransportProvider getProvider() {
		return provider;
	}

	public void setProvider(TransportProvider provider) {
		this.provider = provider;
	}

	@Override
	public String toString() {
		return "ThriftServerAddress [host=" + host + ", port=" + port
				+ ", online=" + online + "]";
	}


}
