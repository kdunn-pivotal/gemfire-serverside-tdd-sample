package io.pivotal.tutorial.db;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.pivotal.tutorial.model.Customer;

public class UpperCaseNameFunction implements Function, Declarable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(UpperCaseNameFunction.class);

	@Override
	public void init(Properties arg0) {
		// TODO Auto-generated method stub

	}
	
	private Customer upperCaseFields(Customer c) {
		
		Customer cPrime = new Customer();
		
		cPrime.setId(c.getId());
		
		cPrime.setFirstName(c.getFirstName().toUpperCase());
		
		cPrime.setLastName(c.getLastName().toUpperCase());
		
		cPrime.setAddress(c.getAddress());
		
		cPrime.setZipCode(c.getZipCode());
		
		return cPrime;
	}

	@Override
	public void execute(FunctionContext context) {
		if (!(context instanceof RegionFunctionContext)) {
			throw new FunctionException(
					"This is a data aware function, and has to be called using FunctionService.onRegion.");
		}
		RegionFunctionContext regionFunctionContext = (RegionFunctionContext) context;

		Map<String, Object> arguments = (Map<String, Object>) regionFunctionContext.getArguments();

		LOG.debug("Executing server-side UPPERCASE function");
		
		// Get a handle on the server cache object
		Cache cache = CacheFactory.getAnyInstance();

		// Get handles on the live and export region objects
		Region<String, Customer> customerRegion = cache.getRegion("Customer");
		
		LOG.debug(arguments.values().toString());

		List<String> keysToModify = (List<String>) arguments.get("keys");
		
		LOG.debug(keysToModify.toString());

		Integer numProcessedObjects = 0;
		for (Iterator<String> iter = keysToModify.iterator(); iter.hasNext();) {
			String thisKey = (String) iter.next();
			
			customerRegion.put(thisKey, upperCaseFields(customerRegion.get(thisKey)));
			
			LOG.debug(customerRegion.get(thisKey).getFirstName());
			
			++numProcessedObjects;
		}
		
		context.getResultSender().lastResult(numProcessedObjects);
	}

	@Override
	public String getId() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean hasResult() {
		return true;
	}

	@Override
	public boolean isHA() {
		return false;
	}

	@Override
	public boolean optimizeForWrite() {
		return true;
	}

}
