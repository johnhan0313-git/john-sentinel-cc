package com.john.sentinel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sentinel 功能测试
 *
 * @author john
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SentinelTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试订单创建接口
     */
    @Test
    public void testCreateOrder() throws Exception {
        mockMvc.perform(post("/api/order/create")
                        .param("userId", "1001")
                        .param("productId", "2001")
                        .param("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.orderId").exists());
    }

    /**
     * 测试订单查询接口
     */
    @Test
    public void testQueryOrder() throws Exception {
        mockMvc.perform(get("/api/order/query/ORD123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    /**
     * 测试用户注册接口
     */
    @Test
    public void testRegisterUser() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").exists());
    }

    /**
     * 测试用户信息查询接口
     */
    @Test
    public void testGetUserInfo() throws Exception {
        mockMvc.perform(get("/api/user/info/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    /**
     * 测试支付处理接口
     */
    @Test
    public void testProcessPayment() throws Exception {
        mockMvc.perform(post("/api/payment/process")
                        .param("orderId", "ORD123456")
                        .param("amount", "99.99")
                        .param("paymentMethod", "ALIPAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").exists());
    }
}
