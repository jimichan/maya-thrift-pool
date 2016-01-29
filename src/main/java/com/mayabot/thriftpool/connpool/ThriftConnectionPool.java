package com.mayabot.thriftpool.connpool;

import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.transport.TTransport;

public class ThriftConnectionPool {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ThriftConnectionPool.class);

	private final GenericKeyedObjectPool<ThriftServerAddress, TTransport> pool;

	public final static GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();

	// Default config
	static {
		config.setMaxTotal(100);

		config.setMaxTotalPerKey(100);
		config.setMaxIdlePerKey(100);
		config.setMinIdlePerKey(100);

		config.setTestOnBorrow(true);
		config.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(5));
		config.setSoftMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(5));
		config.setJmxEnabled(false);
	}

	/**
	 * <p>
	 * Constructor for DefaultThriftConnectionPoolImpl.
	 * </p>
	 *
	 * @param config
	 *            a
	 *            {@link org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig}
	 *            object.
	 * @param transportProvider
	 *            a {@link java.util.function.Function} object.
	 */
	public ThriftConnectionPool(GenericKeyedObjectPoolConfig config) {
		pool = new GenericKeyedObjectPool<>(new ThriftConnectionFactory(),
				config);
	}

	public static final class ThriftConnectionFactory implements
			KeyedPooledObjectFactory<ThriftServerAddress, TTransport> {

		@Override
		public PooledObject<TTransport> makeObject(ThriftServerAddress server)
				throws Exception {
			TTransport transport = null;
			try {
				transport = server.createTransport();
				transport.open();

				DefaultPooledObject<TTransport> result = new DefaultPooledObject<>(
						transport);

				logger.trace("make new thrift connection:{}", server);
				return result;
			} catch (Exception e) {
				logger.error("make transport for" + server, e);
				server.offline();// 标记服务位下线
				// 抛出异常
				throw e;
			}
		}

		@Override
		public void destroyObject(ThriftServerAddress info,
				PooledObject<TTransport> p) throws Exception {
			TTransport transport = p.getObject();
			if (transport != null && transport.isOpen()) {
				transport.close();
				logger.trace("close thrift connection:{}", info);
			}
		}

		@Override
		public boolean validateObject(ThriftServerAddress info,
				PooledObject<TTransport> p) {
			try {
				// System.out.println(p.getObject()
				// +" open "+p.getObject().isOpen());
				return p.getObject().isOpen();
			} catch (Throwable e) {
				logger.error("fail to validate tsocket:{}", info, e);
				return false;
			}
		}

		@Override
		public void activateObject(ThriftServerAddress info,
				PooledObject<TTransport> p) throws Exception {
			// do nothing
		}

		@Override
		public void passivateObject(ThriftServerAddress info,
				PooledObject<TTransport> p) throws Exception {
			// do nothing
		}

	}
	public static class Item{
		TTransport transport;
	}

	public TTransport getConnection(ThriftServerAddress thriftServerInfo) {
		try {
			return pool.borrowObject(thriftServerInfo);
		} catch (Exception e) {
			logger.error("fail to get connection for {}", thriftServerInfo, e);
			throw new RuntimeException(e);
		}
	}

	public TTransport getConnection(ThriftServerAddress thriftServerInfo,
			long borrowMaxWaitMillis) {
		try {
			return pool.borrowObject(thriftServerInfo, borrowMaxWaitMillis);
		} catch (Exception e) {
			logger.error("fail to get connection for {}", thriftServerInfo, e);
			throw new RuntimeException(e);
		}
	}

	public void returnConnection(ThriftServerAddress thriftServerInfo,
			TTransport transport) {
		pool.returnObject(thriftServerInfo, transport);
	}

	public void clear() {
		pool.clear();
	}

	public void clear(ThriftServerAddress key) {
		pool.clear(key);
	}

	public void close() {
		pool.close();
	}

	public void clearOldest() {
		pool.clearOldest();
	}

	public GenericKeyedObjectPool<ThriftServerAddress, TTransport> getPool() {
		return pool;
	}

}
