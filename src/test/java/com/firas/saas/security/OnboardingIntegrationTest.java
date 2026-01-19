package com.firas.saas.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firas.saas.security.dto.MerchantSignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OnboardingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testFullMerchantFlow() throws Exception {
        // 1. Register
        String uniqueSuffix = "" + System.currentTimeMillis();
        MerchantSignupRequest signup = new MerchantSignupRequest(
                "Store " + uniqueSuffix,
                "slug-" + uniqueSuffix,
                "merchant" + uniqueSuffix + "@test.com",
                "Admin123!",
                "John Merchant"
        );

        String signupResponse = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 2. Login
        String loginBody = "{\"email\":\"" + signup.getEmail() + "\",\"password\":\"Admin123!\"}";
        String jwtResponse = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        String token = objectMapper.readTree(jwtResponse).get("token").asText();

        // 3. Create Category
        String categoryBody = "{\"name\":\"Electronics\",\"slug\":\"elec-" + uniqueSuffix + "\",\"description\":\"desc\"}";
        String catResponse = mockMvc.perform(post("/api/v1/products/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryBody))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        
        long categoryId = objectMapper.readTree(catResponse).get("id").asLong();

        // 4. Create Product
        String productBody = "{\"name\":\"Phone\",\"slug\":\"phone-" + uniqueSuffix + "\",\"description\":\"desc\",\"categoryId\":" + categoryId + ",\"variants\":[{\"name\":\"V1\",\"sku\":\"SKU-" + uniqueSuffix + "\",\"price\":100,\"stockLevel\":10}]}";
        mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productBody))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        // 5. Register Customer
        String customerBody = "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane" + uniqueSuffix + "@test.com\",\"phone\":\"12345\"}";
        mockMvc.perform(post("/api/v1/customers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerBody))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        // 6. Cart and Checkout
        String cartBody = "{\"sku\":\"SKU-" + uniqueSuffix + "\",\"quantity\":1}";
        mockMvc.perform(post("/api/v1/orders/cart")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cartBody))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post("/api/v1/orders/checkout")
                .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }
}
