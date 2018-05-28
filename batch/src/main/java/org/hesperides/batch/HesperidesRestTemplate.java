//package org.hesperides.batch;
//
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.converter.json.GsonHttpMessageConverter;
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//public class HesperidesRestTemplate {
//    RestTemplate restTemplate;
//    public HesperidesRestTemplate(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//
//        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters().stream()
//                .filter(httpMessageConverter -> !(httpMessageConverter instanceof MappingJackson2HttpMessageConverter))
//                .collect(Collectors.toList());
//
//        converters.add(new GsonHttpMessageConverter());
//        restTemplate.setMessageConverters(converters);
//    }
//}a
