package io.pivotal.tutorial;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.pivotal.tutorial.model.Customer;

@Profile("prod")
@Configuration
public class ServerFunctionTddConfiguration {

	@Value("${locator-host:localhost}")
	private String locatorHost;

	@Value("${locator-port:10334}")
	private int locatorPort;

	@Bean
	public ClientCache createClientCache() {
		ClientCacheFactory ccf = new ClientCacheFactory();

		ccf.addPoolLocator(locatorHost, locatorPort);

		// ccf.setPdxSerializer(new
		// ReflectionBasedAutoSerializer("io.pivotal.tutorial.model"));

		return ccf.create();
	}

	@Bean
	public Region<String, Customer> customerRegion(ClientCache cache) {

		ClientRegionFactory<String, Customer> crf = cache.createClientRegionFactory(ClientRegionShortcut.PROXY);

		return crf.create("Customer");
	}

}
