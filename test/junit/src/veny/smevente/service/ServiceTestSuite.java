package veny.smevente.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite runner for all service tests.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 9.12.2010
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    UserServiceTest.class,
    UnitServiceTest.class,
    SmsServiceTest.class,
    AuthorizationTest.class
})
public class ServiceTestSuite { }
