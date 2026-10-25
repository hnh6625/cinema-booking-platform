package com.hnh6625.cinema.auth.controller;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.auth.entity.User;
import com.hnh6625.cinema.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
public class AuthRegisterIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private String registerJson(String email, String phone, String password) {
        return """
                {
                    "email": "%s",
                    "phone": "%s",
                    "password": "%s"
                }
                """.formatted(email, phone, password);
    }
    private ResultActions register(String email, String phone, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson(email, phone, password)));
    }

    @Test
    void register_withValidRequest_returns201AndStoresHashedPassword() throws Exception {
        register("a@x.com", "0901234567", "password123")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("a@x.com"))
                .andExpect(jsonPath("$.phone").value("+84901234567"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        String storedHash = users.get(0).getPasswordHash();
        assertThat(storedHash).isNotEqualTo("password123");
        assertThat(storedHash).startsWith("$2");
    }

    @Test
    void register_withSameEmailInDifferentCase_returns409() throws Exception {
        register("a@x.com", "0901234567", "password123");
        register("A@X.COM", "0912345678", "password123")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email is already registered."));
    }

    @Test
    void register_withSamePhoneInDifferentFormat_returns409() throws Exception {
        register("a@x.com","0901234567", "password123");
        register("b@x.com","+84901234567", "password123")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Phone is already registered."));
    }

    @Test
    void register_withInvalidPhone_returns400() throws Exception {
        register("a@x.com", "abc", "password123")
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withShortPassword_returns400() throws Exception {
        register("a@x.com", "0901234567", "short")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("password")));
    }
}
