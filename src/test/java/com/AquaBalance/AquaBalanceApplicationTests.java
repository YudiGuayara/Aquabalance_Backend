package com.AquaBalance;

import com.AquaBalance.notifications.application.ports.out.NotificacionEmailPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class AquaBalanceApplicationTests {

	@MockBean
	private NotificacionEmailPort notificacionEmailPort;

	@Test
	void contextLoads() {
	}
}