package veny.smevente;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import veny.smevente.client.utils.ClientTextUtilsTest;
import veny.smevente.service.EventServiceTest;
import veny.smevente.service.GeneralTest;
import veny.smevente.service.UnitServiceTest;
import veny.smevente.service.UserServiceTest;

/**
 * Aggregator of all unit tests.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 24.8.2016
 */
@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({ GeneralTest.class, UserServiceTest.class, UnitServiceTest.class, EventServiceTest.class, ClientTextUtilsTest.class })
public class Suite {
}
