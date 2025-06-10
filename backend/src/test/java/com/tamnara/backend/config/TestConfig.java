package com.tamnara.backend.config;

import com.tamnara.backend.global.config.JpaConfig;
import com.tamnara.backend.global.config.QuerydslConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({JpaConfig.class, QuerydslConfig.class})
public class TestConfig {
}
