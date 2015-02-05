package gov.usgs.cida.microservices.messaging;

import gov.usgs.cida.microservices.api.messaging.MicroserviceHandler;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.jodah.lyra.Connections;
import net.jodah.lyra.config.Config;
import net.jodah.lyra.config.RecoveryPolicies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import java.io.Closeable;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author thongsav
 */
public final class MicroserviceMsgservice implements Closeable {

	private static final Logger log = LoggerFactory.getLogger(MicroserviceMsgservice.class);

	private final String host;
	private final String exchange;
	private final String username;
	private final String password;

	private final ConnectionFactory conFactory;
	private final Connection conn;

	private final String serviceName;
	private final Set<Class<? extends MicroserviceHandler>> microserviceHandlers;

	public static MicroserviceMsgservice INSTANCE = null;
	public static String SERVICE_NAME = null;
	public static Set<Class<? extends MicroserviceHandler>> HANDLERS = Collections.EMPTY_SET;

	public static String setServiceName(String name) {
		if (StringUtils.isBlank(SERVICE_NAME)) {
			if (StringUtils.isNotBlank(name)) {
				SERVICE_NAME = name;
				log.info("Set service name: {}", SERVICE_NAME);
			} else {
				log.error("BLANK SERVICE NAME NOT ALLOWED: {}", name);
			}
		} else {
			log.error("SERVICE NAME ALREADY SPECIFIED. NEW: {} OLD: {}", name, SERVICE_NAME);
		}
		return SERVICE_NAME;
	}

	public static Set<Class<? extends MicroserviceHandler>> setHandlers(Set<Class<? extends MicroserviceHandler>> handlers) {
		if (null != handlers && !handlers.isEmpty()) {
			HANDLERS = handlers;
		} else {
			log.error("INVALID HANDLER SET");
		}
		return HANDLERS;
	}

	public static MicroserviceMsgservice getInstance(String serviceName) {
		setServiceName(serviceName);
		MicroserviceMsgservice result = getInstance();
		return result;
	}

	public static MicroserviceMsgservice getInstance() {
		MicroserviceMsgservice result = null;

		if (null != SERVICE_NAME) {
			if (null == INSTANCE) {
				try {
					INSTANCE = new MicroserviceMsgservice();
				} catch (Exception e) {
					log.error("Could not init msg service", e);
				}
			}
			result = INSTANCE;
		} else {
			log.error("SERVICE NAME NOT SPECIFIED!");
		}

		return result;
	}
	
	public MicroserviceMsgservice() throws IOException {
		this("localhost", "amq.headers", "guest", "guest");
	}

	public MicroserviceMsgservice(String host, String exchange, String username, String password) throws IOException {
		this.host = host;
		this.exchange = exchange;
		this.username = username;
		this.password = password;

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(this.host);
		factory.setUsername(this.username);
		factory.setPassword(this.password);

		this.conFactory = factory;
		log.debug("initialized ConnectionFactory");

		Config config = new Config().withRecoveryPolicy(RecoveryPolicies.recoverAlways());
		Connection connection = Connections.create(conFactory, config);
		conn = connection;

		this.serviceName = SERVICE_NAME;
		log.info("THIS IS MY SERVICE NAME: " + this.serviceName);

		this.microserviceHandlers = HANDLERS;
		log.info("I'VE GOT {} HANDLERS", this.microserviceHandlers.size());

		for (Class<? extends MicroserviceHandler> clazz : HANDLERS) {
			String queueName = null;
			try {
				Channel channel = getChannel();
				DeclareOk ack = channel.queueDeclare(serviceName + "." + clazz.getSimpleName(), false, false, true, null);
				queueName = ack.getQueue();
				channel.close();
			} catch (Exception e) {
				log.error("Could not declare queue", e);
			}
			if (null != queueName) {
				try {
					MicroserviceHandler bindingHandler = clazz.newInstance();
					for (Map<String, Object> bindingOptions : bindingHandler.getBindings(serviceName)) {
						Channel channel = getChannel();
						channel.queueBind(queueName, this.exchange, "", bindingOptions);
						channel.close();
					}

					int numberOfConsumers = 3;
					for (int i = 0; i < numberOfConsumers; i++) {
						//new instances just in case someone makes a non-threadsafe handler
						MicroserviceHandler handler = clazz.newInstance();
						Channel channel = getChannel();
						Consumer consumer = new MicroserviceConsumer(channel, handler);
						channel.basicConsume(queueName, true, consumer);
						log.info("Channel {} now listening for {} messages, handled by {}", channel.getChannelNumber(), queueName, clazz.getSimpleName());
					}
				} catch (Exception e) {
					log.error("Could not register consumers", e);
				}
			}
		}

		log.debug("instantiated data syncronizer msg service");
	}

	public Channel getChannel() throws IOException {
		Channel channel = conn.createChannel();
		log.info("init Channel {} of {}", channel.getChannelNumber(), conn.getChannelMax());
		return channel;
	}

	@Override
	public void close() throws IOException {
		log.info("Cleaning Up Message Service");
		this.conn.close(3000);
	}

//	public void sendDataSynced(String authToken, String requestId, String reportType, String stagedDataUrl) throws IOException {
//		Map<String, String> paramsToSend = new HashMap<>();
//		paramsToSend.put("requestId", requestId);
//		paramsToSend.put("authToken", authToken);
//		paramsToSend.put("reportType", reportType);
//		paramsToSend.put("stagedDataUrl", stagedDataUrl);
//
//		String message = new Gson().toJson(paramsToSend, Map.class);
//
//		Channel channel = getChannel();
//		Map<String, Object> headers = new HashMap<>();
//		BasicProperties props = new BasicProperties.Builder().headers(headers).build();
//		channel.basicPublish(exchangeName, "", props, message.getBytes());
//		log.debug("Sent {} for request {}", "dead letter", requestId);
//	}
}
