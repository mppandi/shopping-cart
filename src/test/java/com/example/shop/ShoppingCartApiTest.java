package com.example.shop;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end through the real filter chain, controllers, services and the H2 database. */
@SpringBootTest
@AutoConfigureMockMvc
class ShoppingCartApiTest {

    @Autowired
    MockMvc mvc;

    private String registerNewUser() throws Exception {
        String username = "user-" + UUID.randomUUID().toString().substring(0, 8);
        String body = mvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }

    private void addToCart(String auth, int productId, int quantity) throws Exception {
        mvc.perform(post("/api/cart/items").header("Authorization", auth).contentType(APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk());
    }

    @Test
    void products_arePublic() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void cart_withoutToken_isUnauthorized() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
    }

    @Test
    void cart_withGarbageToken_isUnauthorized() throws Exception {
        mvc.perform(get("/api/cart").header("Authorization", "Bearer not.a.token")).andExpect(status().isUnauthorized());
    }

    @Test
    void login_withWrongPassword_isUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addToCart_thenCheckoutWithCard_createsPaidOrder_andEmptiesCart() throws Exception {
        String auth = registerNewUser();
        addToCart(auth, 2, 3); // 3 x 19.99

        mvc.perform(get("/api/cart").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.total").value(59.97));

        mvc.perform(post("/api/orders/checkout").header("Authorization", auth).contentType(APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"CARD\",\"paymentDetails\":{\"cardToken\":\"tok_ok\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.total").value(59.97));

        mvc.perform(get("/api/cart").header("Authorization", auth))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void declinedPayment_returns402_andKeepsTheCart() throws Exception {
        String auth = registerNewUser();
        addToCart(auth, 1, 1); // 899.00 is above the demo wallet balance

        mvc.perform(post("/api/orders/checkout").header("Authorization", auth).contentType(APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"WALLET\",\"paymentDetails\":{\"walletId\":\"w-1\"}}"))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.status").value("PAYMENT_FAILED"));

        mvc.perform(get("/api/cart").header("Authorization", auth))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void upiPayment_isAwaitingConfirmation() throws Exception {
        String auth = registerNewUser();
        addToCart(auth, 3, 1);

        mvc.perform(post("/api/orders/checkout").header("Authorization", auth).contentType(APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"UPI\",\"paymentDetails\":{\"vpa\":\"alice@bank\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AWAITING_CONFIRMATION"));
    }

    @Test
    void checkout_withEmptyCart_isConflict() throws Exception {
        String auth = registerNewUser();

        mvc.perform(post("/api/orders/checkout").header("Authorization", auth).contentType(APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"CARD\",\"paymentDetails\":{\"cardToken\":\"tok_ok\"}}"))
                .andExpect(status().isConflict());
    }

    @Test
    void checkout_withUnsupportedMethod_isBadRequest() throws Exception {
        String auth = registerNewUser();
        addToCart(auth, 2, 1);

        mvc.perform(post("/api/orders/checkout").header("Authorization", auth).contentType(APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"BITCOIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anotherUser_cannotSeeMyOrder() throws Exception {
        String alice = registerNewUser();
        addToCart(alice, 2, 1);
        String body = mvc.perform(post("/api/orders/checkout").header("Authorization", alice).contentType(APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"CARD\",\"paymentDetails\":{\"cardToken\":\"tok_ok\"}}"))
                .andReturn().getResponse().getContentAsString();
        int orderId = JsonPath.read(body, "$.id");

        String bob = registerNewUser();
        mvc.perform(get("/api/orders/" + orderId).header("Authorization", bob)).andExpect(status().isNotFound());
        mvc.perform(get("/api/orders/" + orderId).header("Authorization", alice)).andExpect(status().isOk());
    }

    @Test
    void removeItem_updatesTheCart() throws Exception {
        String auth = registerNewUser();
        addToCart(auth, 2, 1);
        addToCart(auth, 3, 1);

        mvc.perform(delete("/api/cart/items/2").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(3));
    }

    @Test
    void addUnknownProduct_isNotFound() throws Exception {
        String auth = registerNewUser();

        mvc.perform(post("/api/cart/items").header("Authorization", auth).contentType(APPLICATION_JSON)
                        .content("{\"productId\":999,\"quantity\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void paymentMethods_listComesFromTheRegistry() throws Exception {
        String auth = registerNewUser();

        mvc.perform(get("/api/payment-methods").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("CARD"));
    }
}
