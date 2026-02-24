package com.aruba.zeta.userauth;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires live PostgreSQL — skipped in local/CI builds without DB")
@SpringBootTest
class UserauthApplicationTests {

	@Test
	void contextLoads() {
	}

}
