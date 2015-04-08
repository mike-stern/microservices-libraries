package gov.usgs.cida.microservices.messaging;

import gov.usgs.cida.microservices.api.messaging.MicroserviceHandler;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dmsibley
 * @author thongsav
 */
public class MicroserviceConsumer extends DefaultConsumer  {
	private static final Logger log = LoggerFactory.getLogger(MicroserviceConsumer.class);

	private final Channel channel;
	private final MicroserviceHandler handler;
	private final MicroserviceMsgservice msgService;
	
	public MicroserviceConsumer(Channel channel, MicroserviceHandler handler, MicroserviceMsgservice msgService) {
		super(channel);
		this.channel = channel;
		this.handler = handler;
		this.msgService = msgService;
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
		log.trace("Handling Delivery");
		long deliveryTag = envelope.getDeliveryTag();
		boolean isRedeliver = envelope.isRedeliver();
		log.trace("Consumer {} on Channel {} received message {}, isRedeliver: {}", 
				this.getClass().getSimpleName(), this.channel.getChannelNumber(), deliveryTag, isRedeliver);
		
		log.trace("Message headers: {}", new Gson().toJson(properties.getHeaders(), Map.class));
		
		try {
			this.handler.handle(properties.getHeaders(), body);

			//TODO turn off autoack?
			//For error handlers, we probably want autoack off so that there can be multiple error handlers
//			channel.basicAck(deliveryTag, false);
		} catch (Exception e) {
			log.error("Error while handling message", e);
			msgService.postError(
					getStringFromHeaders(properties.getHeaders(), "requestId"),
					getStringFromHeaders(properties.getHeaders(), "serviceRequestId"), 
					e);
		}
	}

	private String getStringFromHeaders(Map<String, Object> params, String key) {
		return params.get(key) != null ? params.get(key).toString() : null;
	}
}
