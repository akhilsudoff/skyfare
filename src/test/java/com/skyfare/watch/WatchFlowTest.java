package com.skyfare.watch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class WatchFlowTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void registerCreateAndListWatch() throws Exception {
        String creds = "{\"email\":\"flow@test.com\",\"password\":\"password123\"}";
        String registerBody = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(creds))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String token = json.readTree(registerBody).get("token").asText();

        String watch = "{\"origin\":\"adl\",\"destination\":\"syd\",\"departDate\":\"" + LocalDate.now().plusDays(20) + "\",\"targetPrice\":199.00}";
        mvc.perform(post("/api/watches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(watch))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origin").value("ADL"))
                .andExpect(jsonPath("$.destination").value("SYD"));

        mvc.perform(get("/api/watches").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void rejectsUnauthenticatedAccess() throws Exception {
        mvc.perform(get("/api/watches")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidIataCode() throws Exception {
        String registerBody = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"v@test.com\",\"password\":\"password123\"}"))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(registerBody);

        mvc.perform(post("/api/watches")
                        .header("Authorization", "Bearer " + node.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"ADEL\",\"destination\":\"SYD\",\"departDate\":\"2099-01-01\",\"targetPrice\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.origin").exists());
    }
}
