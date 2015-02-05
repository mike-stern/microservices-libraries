package gov.usgs.cida.microservices.messaging;

import gov.usgs.cida.microservices.api.messaging.MicroserviceHandler;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dmsibley
 */
public class MicroserviceConsumer extends DefaultConsumer  {
	private static final Logger log = LoggerFactory.getLogger(MicroserviceConsumer.class);

	private final Channel channel;
	private final MicroserviceHandler handler;
	
	public MicroserviceConsumer(Channel channel, MicroserviceHandler handler) {
		super(channel);
		this.channel = channel;
		this.handler = handler;
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
		long deliveryTag = envelope.getDeliveryTag();
		boolean isRedeliver = envelope.isRedeliver();
		log.trace("Received message {}, isRedeliver: {}", deliveryTag, isRedeliver);
		
		String message = StringUtils.toEncodedString(body, Charset.forName("utf-8"));
		log.trace(message);
		
		Map<String, String> params = new Gson().fromJson(message, Map.class);
		
		this.handler.handle(params);

		channel.basicAck(deliveryTag, false);
	}
}
