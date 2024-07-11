package com.credibanco.bancinc.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CustomerService customerService;

        private Customer customer;

        @BeforeEach
        void setup() {
                customer = Customer.builder()
                                .id(3L)
                                .name("David")
                                .lastName("Alfonso")
                                .email("jdavid.alfonso@gmail.com")
                                .build();
        }

        @Test
        void testSaveCustomer() throws Exception {
                // > Given
                given(customerService.saveCustomer(customer)).willAnswer((invocation) -> {
                        ResponseDTO responseDTO = new ResponseDTO(HttpStatus.CREATED.value(), customer);
                        return responseDTO;
                });

                // > When
                ResultActions response = mockMvc.perform(post("/customer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(customer)));

                // > Then
                response.andDo(print())
                                .andExpect(status().isCreated());
        }

        @Test
        void testCustomerList() throws Exception {
                // > Given
                List<Customer> customerList = new ArrayList<Customer>();
                customerList.add(customer);
                customerList.add(
                                Customer.builder()
                                                .name("Nicolas")
                                                .lastName("Bejarano")
                                                .email("nico.bejarano@gmail.com")
                                                .build());

                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), customerList);

                given(customerService.listCustomers()).willReturn(ResponseEntity.ok(responseDTO));

                // > When
                ResultActions response = mockMvc.perform(get("/customer"));

                // > Then
                response.andExpect(status().isOk())
                                .andDo(print())
                                .andExpect(jsonPath("$.size()", is(customerList.size())));
        }

        @Test
        void testGetDataById() throws Exception {
                // >Given
                long customerID = 3L;

                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), customer);

                given(customerService.getCustomerById(customerID)).willReturn(ResponseEntity.ok(responseDTO));

                // >When
                ResultActions response = mockMvc.perform(get("/customer/{customerID}", customerID));

                // >Then
                response.andExpect(status().isOk())
                                .andDo(print())
                                .andExpect(jsonPath("$.data.name", is(customer.getName())))
                                .andExpect(jsonPath("$.data.lastName", is(customer.getLastName())))
                                .andExpect(jsonPath("$.data.email", is(customer.getEmail())));
        }

        @Test
        void testGetDataByEmail() throws Exception {
                // >Given
                String customerEmail = "jdavid.alfonso@gmail.com";

                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), customer);

                given(customerService.getCustomerByEmail(customerEmail)).willReturn(ResponseEntity.ok(responseDTO));

                // >When
                ResultActions response = mockMvc.perform(get("/customer/find")
                                .param("email", customerEmail)
                                .contentType(MediaType.APPLICATION_JSON));

                // >Then
                response.andExpect(status().isOk())
                                .andDo(print())
                                .andExpect(jsonPath("$.data.name", is(customer.getName())))
                                .andExpect(jsonPath("$.data.lastName", is(customer.getLastName())))
                                .andExpect(jsonPath("$.data.email", is(customer.getEmail())));
        }

        @Test
        void testDeleteCustomer() throws Exception {
                // > Given
                Long customerID = 3L;

                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), "Eliminado con exito");
                given(customerService.deleteCustomer(customerID)).willReturn(ResponseEntity.ok(responseDTO));

                // > When
                ResultActions response = mockMvc.perform(delete("/customer/{customerID}", customerID)
                                .contentType(MediaType.APPLICATION_JSON));

                // > Then
                response.andExpect(status().isOk())
                                .andDo(print())
                                .andExpect(jsonPath("$.statusCode", is(HttpStatus.OK.value())))
                                .andExpect(jsonPath("$.data", is("Eliminado con exito")));
        }

}
