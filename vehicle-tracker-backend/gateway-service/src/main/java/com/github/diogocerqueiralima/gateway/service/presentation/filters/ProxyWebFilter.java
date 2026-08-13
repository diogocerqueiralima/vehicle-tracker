package com.github.diogocerqueiralima.gateway.service.presentation.filters;

import com.github.diogocerqueiralima.gateway.service.presentation.config.GatewayProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Forwards requests that passed authentication and rate limiting to the resolved downstream
 * service, streaming the request body upstream and the upstream response back to the caller.
 * This is the last filter in the chain.
 */
@Component
@Order(ProxyWebFilter.ORDER)
public class ProxyWebFilter implements WebFilter {

    public static final int ORDER = Ordered.LOWEST_PRECEDENCE;

    // Hop-by-hop headers must not be forwarded as-is between gateway and upstream (RFC 7230 6.1).
    private static final Iterable<String> HOP_BY_HOP_HEADERS = List.of(
            HttpHeaders.CONNECTION, HttpHeaders.HOST, "Keep-Alive", "Transfer-Encoding", "Upgrade"
    );

    private final WebClient webClient;

    public ProxyWebFilter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        GatewayProperties.Route route =
                (GatewayProperties.Route) exchange.getAttributes().get(RouteResolutionWebFilter.ROUTE_ATTRIBUTE);

        String targetUri = route.uri() + exchange.getRequest().getURI().getRawPath()
                + (exchange.getRequest().getURI().getRawQuery() == null
                        ? "" : "?" + exchange.getRequest().getURI().getRawQuery());

        return webClient.method(exchange.getRequest().getMethod())
                .uri(targetUri)
                .headers(headers -> {
                    headers.addAll(exchange.getRequest().getHeaders());
                    HOP_BY_HOP_HEADERS.forEach(headers::remove);
                })
                .body((outputMessage, context) -> outputMessage.writeWith(exchange.getRequest().getBody()))
                .exchangeToMono(response -> forward(exchange, response));
    }

    private Mono<Void> forward(ServerWebExchange exchange, ClientResponse response) {
        exchange.getResponse().setStatusCode(response.statusCode());
        exchange.getResponse().getHeaders().addAll(response.headers().asHttpHeaders());
        HOP_BY_HOP_HEADERS.forEach(exchange.getResponse().getHeaders()::remove);
        return exchange.getResponse().writeWith(response.bodyToFlux(DataBuffer.class));
    }

}
