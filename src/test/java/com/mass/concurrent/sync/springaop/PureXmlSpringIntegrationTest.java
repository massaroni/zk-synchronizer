package com.mass.concurrent.sync.springaop;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-app-context.xml" })
public class PureXmlSpringIntegrationTest extends SpringIntegrationTest {

}
