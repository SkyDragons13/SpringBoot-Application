package com.example.rest_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UrlService {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public UrlService(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    public List<String> getAllUrls() {
        List<String> urls = new ArrayList<>();

        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            PathPatternsRequestCondition patternsCondition = info.getPathPatternsCondition();
            if (patternsCondition != null) {
                // Add each pattern individually to the list
                patternsCondition.getPatterns().forEach(url->urls.add(url.toString()));
            }
        }


        return urls;
    }
}
