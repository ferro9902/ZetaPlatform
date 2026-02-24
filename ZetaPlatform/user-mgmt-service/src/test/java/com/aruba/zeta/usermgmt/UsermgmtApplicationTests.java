package com.aruba.zeta.usermgmt;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires live PostgreSQL — skipped in local/CI builds without DB")
@SpringBootTest
class UsermgmtApplicationTests {

	@Test
	void contextLoads() {
	}

}
