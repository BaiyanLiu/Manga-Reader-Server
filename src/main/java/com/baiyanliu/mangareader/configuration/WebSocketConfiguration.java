package com.baiyanliu.mangareader.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.core.DefaultLinkRelationProvider;
import org.springframework.hateoas.server.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Component
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private static class HalMessageConverter extends AbstractMessageConverter {

        @Override
        protected boolean supports(@NonNull Class<?> clazz) {
            return EntityModel.class.isAssignableFrom(clazz);
        }

        @Override
        protected Object convertToInternal(@NonNull Object payload, MessageHeaders headers, Object conversionHint) {
            TypeConstrainedMappingJackson2HttpMessageConverter converter = new TypeConstrainedMappingJackson2HttpMessageConverter(EntityModel.class);

            Jackson2HalModule.HalHandlerInstantiator instantiator =
                    new Jackson2HalModule.HalHandlerInstantiator(new DefaultLinkRelationProvider(), CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY);

            ObjectMapper mapper = converter.getObjectMapper();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.registerModule(new Jackson2HalModule());
            mapper.setHandlerInstantiator(instantiator);

            try {
                return mapper.writeValueAsString(payload).getBytes();
            } catch(JsonProcessingException exception) {
                return null;
            }
        }
    }

    public static final String MESSAGE_PREFIX = "/topic";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/events").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(MESSAGE_PREFIX);
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public boolean configureMessageConverters(@NonNull List<MessageConverter> messageConverters) {
        messageConverters.add(new HalMessageConverter());
        return true;
    }
}
