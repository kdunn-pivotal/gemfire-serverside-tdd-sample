package io.pivotal.tutorial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.FunctionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import io.pivotal.tutorial.db.UpperCaseNameFunction;
import io.pivotal.tutorial.model.Customer;
import org.junit.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest
// Ensure the context is correctly cleaned up between tests
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ServerFunctionTddApplicationTests {

	@Autowired
	Cache cache;

	@Autowired
	Region<String, Customer> customerRegion;

	public List<String> addSomeData(Region<String, Customer> customerRegion) {
		Customer c1 = new Customer("Jane", "Doe", "123 Mane St.", 80219);

		Customer c2 = new Customer("John", "Zoe", "876 Train St.", 80216);

		Customer c3 = new Customer("Hank", "Hill", "500 View Blvd.", 80110);

		customerRegion.put(c1.getId(), c1);

		customerRegion.put(c2.getId(), c3);

		customerRegion.put(c3.getId(), c3);

		List<String> keys = new ArrayList<String>();

		keys.add(c1.getId());
		keys.add(c2.getId());
		keys.add(c3.getId());

		System.out.println(keys.size());

		return keys;
	}

	public void cleanupData(Region<String, Customer> customerRegion, List<String> keys) {
		customerRegion.removeAll(keys);
	}

	@Test
	public void testRegisterFunction() {
		// Put some test data in the region
		List<String> keys = addSomeData(customerRegion);
		Assert.assertEquals(3, keys.size());

		FunctionService.registerFunction(new UpperCaseNameFunction());

		String whatWasRegistered = FunctionService.getRegisteredFunctions().get("UpperCaseNameFunction").toString();
		Assert.assertEquals("io.pivotal.tutorial.db.UpperCaseNameFunction", whatWasRegistered.substring(0, 44));

		// Cleanup the test data
		cleanupData(customerRegion, keys);
		Assert.assertEquals(0, customerRegion.size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteFunction() {
		// Put some test data in the region
		List<String> keys = addSomeData(customerRegion);
		Assert.assertEquals(3, keys.size());

		FunctionService.registerFunction(new UpperCaseNameFunction());
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("keys", keys);

		// Invoke the function for the list of keys
		// Collect a result to ensure synchronous execution
		List<Integer> resultList = (List<Integer>) FunctionService
				.onRegion(customerRegion)
				.withArgs(args)
				.execute("UpperCaseNameFunction")
				.getResult();
		Assert.assertEquals(3, (int)resultList.get(0));
		
		// Validate the function performed the capitalization on the keys		
		for (String k : keys) {
			Customer customerObject = customerRegion.get(k);
			String firstNameUpper = customerObject.getFirstName().toUpperCase();
			String lastNameUpper = customerObject.getLastName().toUpperCase();
			
			Assert.assertEquals(firstNameUpper, customerObject.getFirstName());
			Assert.assertEquals(lastNameUpper, customerObject.getLastName());
		}

		// Cleanup the test data
		cleanupData(customerRegion, keys);
		Assert.assertEquals(0, customerRegion.size());
	}
}
