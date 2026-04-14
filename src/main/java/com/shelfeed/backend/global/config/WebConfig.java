package com.shelfeed.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 알라딘 API는 Content-Type: text/javascript 로 JSON을 반환 → Jackson이 처리할 수 있도록 추가
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN,
                new MediaType("text", "javascript"),
                new MediaType("application", "javascript"),
                MediaType.TEXT_HTML
        ));
        restTemplate.getMessageConverters().add(0, converter);

        return restTemplate;
    }
}
