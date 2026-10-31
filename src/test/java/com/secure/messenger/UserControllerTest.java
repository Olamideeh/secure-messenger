package com.secure.messenger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerUserShouldReturnCreated() throws Exception {

        String requestBody = """
                {
                    "phoneNumber": "+2348012345678",
                    "region": "NG"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.phoneNumber").value("+2348012345678"))
                .andExpect(jsonPath("$.phoneVerified").value(false))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void registerUserShouldReturnConflictWhenPhoneNumberAlreadyExists() throws Exception {

        String requestBody = """
                {
                    "phoneNumber": "+2348012345678",
                    "region": "NG"
                }
                """;

        // First registration should succeed
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        // Second registration with the same number should fail
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Phone number already exists!"));
    }

    @Test
    void registerUserShouldReturnBadRequestWhenPhoneNumberIsBlank() throws Exception {

        String requestBody = """
            {
                "phoneNumber": "",
                "region": "NG"
            }
            """;

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Phone number is required"));
    }

    @Test
    void registerUserShouldReturnBadRequestWhenPhoneNumberIsInvalid() throws Exception {

        String requestBody = """
            {
                "phoneNumber": "123",
                "region": "NG"
            }
            """;

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid phone number"));
    }
    @Test
    void registerUserShouldReturnBadRequestWhenRegionIsMissing() throws Exception {

        String requestBody = """
            {
                "phoneNumber": "+2348012345678",
                "region": ""
            }
            """;

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Country region is required"));
    }
}