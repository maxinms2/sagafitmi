package com.sagafitmi.ecommerce.config;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

    @Bean
    public Module localDateTimeModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new StdDeserializer<LocalDateTime>(LocalDateTime.class) {
            private static final long serialVersionUID = 1L;

            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getText();
                if (text == null || text.isBlank()) {
                    return null;
                }

                // Try common ISO LocalDateTime first (e.g. 2025-11-26T00:00:00)
                try {
                    return LocalDateTime.parse(text);
                } catch (DateTimeParseException ex1) {
                    // If it's a date-only string (yyyy-MM-dd), convert to start of day
                    try {
                        LocalDate ld = LocalDate.parse(text);
                        return ld.atStartOfDay();
                    } catch (DateTimeParseException ex2) {
                        // Try OffsetDateTime with zone offset (e.g. 2025-11-28T13:40:01.165-06:00)
                        try {
                            OffsetDateTime odt = OffsetDateTime.parse(text);
                            return odt.toLocalDateTime();
                        } catch (DateTimeParseException ex3) {
                            throw JsonMappingException.from(p, "Cannot parse LocalDateTime from '" + text + "'", ex3);
                        }
                    }
                } catch (DateTimeException dex) {
                    throw JsonMappingException.from(p, "Cannot parse LocalDateTime from '" + text + "'", dex);
                }
            }
        });

        return module;
    }

}
