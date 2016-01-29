package com.mayabot.thriftpool.connpool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.mayabot.thriftpool.TransportProvider;

public class ServerList {

	private static Logger logger = LoggerFactory.getLogger(ServerList.class);

	private CopyOnWriteArrayList<ThriftServerAddress> serverList = new CopyOnWriteArrayList<ThriftServerAddress>();
	private AtomicInteger poinert = new AtomicInteger();

	private long lastcheckTime = System.currentTimeMillis();

	public ServerList(List<String> hosts, TransportProvider transportProvider)
			throws Exception {
		for (String line : hosts) {
			ThriftServerAddress x = new ThriftServerAddress(line,
					transportProvider);
			serverList.add(x);
		}
	}

	public int size() {
		return serverList.size();
	}

	public ServerList(String hosts, TransportProvider transportProvider)
			throws Exception {
		for (String line : Splitter.on(",").omitEmptyStrings().trimResults()
				.splitToList(hosts)) {
			ThriftServerAddress x = new ThriftServerAddress(line,
					transportProvider);
			serverList.add(x);
		}
	}

	private final long step = 1000 * 60;

	public ThriftServerAddress nextServer() throws IllegalStateException {
		final int size = serverList.size();
		if (size == 0) {
			throw new IllegalStateException("no aviable server to use");
		}
		if (size == 1) {
			ThriftServerAddress s = serverList.get(0);
			if (s.isOnline()) {
				return s;
			} else {
				throw new IllegalStateException("no aviable server to use");
			}
		}
		for (int i = 0; i < size; i++) {
			if (i == 1) {
				// 第二次尝试,说明有错误的服务地址存在
				if (System.currentTimeMillis() - lastcheckTime > step) {
					lastcheckTime = System.currentTimeMillis();
					monitorRecover();// 启动一个独立的线程,检查一次
				}
			}
			ThriftServerAddress x = serverList.get(poinert.incrementAndGet()
					% size);
			if (x.isOnline()) {
				return x;
			}
		}
		throw new IllegalStateException("no aviable server to use");
	}

	public void add(ThriftServerAddress info) {
		serverList.add(info);
	};

	/**
	 * 监控一些如果失效的服务器重新上线了的话，那么
	 */
	private void monitorRecover() {
		Thread thread = new Thread() {
			public void run() {
				for (ThriftServerAddress server : serverList) {
					if (!server.isOnline()) {
						logger.debug("start check server list");
						if (testConnect(server)) {
							server.online();
							logger.info("{} can connect it so set it on line",
									server);
						} else {

						}
					}
				}
			}

			private boolean testConnect(ThriftServerAddress server) {
				TSocket t = new TSocket(server.getHost(), server.getPort());
				try {
					t.open();
					boolean open = t.isOpen();
					return open;
				} catch (Throwable e) {
					logger.error("", e);
					return false;
				} finally {
					try {
						t.close();
					} catch (Exception e) {
						logger.warn("", e);
					}
				}
			}
		};

		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@Override
	public String toString() {
		return "ServerList [serverList=" + serverList + "]";
	}

}
