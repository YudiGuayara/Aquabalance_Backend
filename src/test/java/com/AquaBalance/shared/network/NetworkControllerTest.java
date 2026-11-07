package com.AquaBalance.shared.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("NetworkController - Pruebas Unitarias")
class NetworkControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new NetworkController()).build();
    }

    @Test
    @DisplayName("GET /api/network/ping responde 200 con status ok y sin caché")
    void ping_respondeOk() throws Exception {
        mockMvc.perform(get("/api/network/ping"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.timestamp").isNumber())
                .andExpect(jsonPath("$.clientIp").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/network/ping usa la IP de X-Forwarded-For cuando existe")
    void ping_usaXForwardedFor() throws Exception {
        mockMvc.perform(get("/api/network/ping")
                        .header("X-Forwarded-For", "190.25.10.5, 10.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientIp").value("190.25.10.5"));
    }
}