package com.mayabot.thriftpool;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TProtocolFactory;

import com.mayabot.thriftpool.connpool.ServerList;
import com.mayabot.thriftpool.connpool.ThriftConnectionPool;
import com.mayabot.thriftpool.utils.ClassGenerator;

public class ClientProxyFactory {

	/**
	 * 
	 */
	@SuppressWarnings("rawtypes")
	final static ConcurrentHashMap<Class, Class> classCache = new ConcurrentHashMap<Class, Class>();

	
	/**
	 * Face 客户端可以被重复使用，系统启动后只需要第一次创建该客户端即可。该客户端代理为线程安全
	 * 
	 * @param ifaceClass
	 * @param hosts
	 * @param pool
	 * @param clientFactory
	 * @param protocolProvider
	 * @param transportProvider
	 * @return
	 * @throws Exception
	 */
	public static <F> F makeClient(Class<F> ifaceClass,TServiceClientFactory<?> clientFactory, String hosts,
			ThriftConnectionPool pool, 
			TProtocolFactory protocolProvider,
			TransportProvider transportProvider) {
		try {

			Class<? extends F> clazz = ClientProxyFactory
					.getProxyClass(ifaceClass);

			F faceObject = clazz.newInstance();

			// init
			ClientProxyBase proxy = (ClientProxyBase) faceObject;
			proxy.init(pool, new ServerList(hosts, transportProvider),
					protocolProvider, clientFactory);

			return faceObject;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized static <F> Class<? extends F> getProxyClass(
			Class<F> ifaceClass) {
		if (classCache.containsKey(ifaceClass)) {
			return classCache.get(ifaceClass);
		} else {
			Class<? extends F> c = newProxyClass(ifaceClass);
			classCache.put(ifaceClass, c);
			return c;
		}
	}

	private static <F> Class<? extends F> newProxyClass(Class<F> ifaceClass) {
		ClassGenerator cg = ClassGenerator.newInstance();
		cg.setClassName(ifaceClass.getName() + "Proxy");
		cg.addInterface(ifaceClass);
		cg.setSuperClass(ClientProxyBase.class);

		if (!ifaceClass.isInterface()) {
			return null;
		}

		Method[] methods = ifaceClass.getMethods();
		for (Method method : methods) {
			Class<?> rt = method.getReturnType();
			String mn = method.getName();
			if (Void.TYPE.equals(rt))
				cg.addMethod(
						mn,
						method.getModifiers(),
						rt,
						method.getParameterTypes(),
						method.getExceptionTypes(),
						"	com.mayabot.thriftpool.Tuple tuple = tryGetConnection();\n"
								+ "		try{\n"
								+ "			(("
								+ ifaceClass.getName()
								+ ")clientCache.get(tuple.transport))."
								+ mn
								+ "($$);\n"
								+ "		}finally{\n"
								+ "			connectionPool.returnConnection(tuple.server, tuple.transport);\n"
								+ "		}");
			else
				cg.addMethod(
						mn,
						method.getModifiers(),
						rt,
						method.getParameterTypes(),
						method.getExceptionTypes(),
								"	com.mayabot.thriftpool.Tuple tuple = tryGetConnection();\n"
								+ "		try{\n"
								+ "			return (("
								+ ifaceClass.getName()
								+ ")clientCache.get(tuple.transport))."
								+ mn
								+ "($$);\n"
								+ "		}finally{\n"
								+ "			connectionPool.returnConnection(tuple.server, tuple.transport);\n"
								+ "		}");
		}

		Class<?> clzz = cg.toClass();

		return (Class<? extends F>) clzz;
	}

	// public static void main(String[] args) {
	// Class<Object> clazz = newProxyClass("ClientTest", SegService.Iface.class,
	// SegService.Client.Factory.class);
	// }
}
