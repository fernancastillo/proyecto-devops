package com.citt;

import com.citt.config.DataLoader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SpringbootApiRestApplicationTests {

    @MockBean
    private DataLoader dataLoader;  

    @Test
    void contextLoads() {
    }
}