package com.sagafitmi.ecommerce.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sagafitmi.ecommerce.repository.AllowedOriginRepository;
import com.sagafitmi.ecommerce.service.AllowedOriginService;

@Service
public class AllowedOriginServiceImpl implements AllowedOriginService {

    private final AllowedOriginRepository repository;

    public AllowedOriginServiceImpl(AllowedOriginRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<String> getAllowedOrigins() {
        return repository.findByEnabledTrue()
                .stream()
                .map(a -> a.getOrigin())
                .collect(Collectors.toList());
    }
}
