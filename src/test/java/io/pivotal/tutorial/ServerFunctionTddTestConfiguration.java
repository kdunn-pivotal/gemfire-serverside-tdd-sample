package io.pivotal.tutorial;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.pivotal.tutorial.model.Customer;

@Configuration
public class ServerFunctionTddTestConfiguration {
    @Value("${locator-host:localhost}")
    private String locatorHost;

    @Value("${locator-port:20334}")
    private int locatorPort;

    @Value("${cache-server.port:40404}")
    private int port;
    
    @Bean
    public Cache createCache() throws Exception {
        CacheFactory cf = new CacheFactory();
        cf.set("name", "ServerFunctionTdd");
        cf.set("start-locator", locatorHost + 
            "[" + locatorPort + "]");

        cf.setPdxSerializer(new ReflectionBasedAutoSerializer("io.pivotal.tutorial.model.*"));

        Cache c = cf.create();
        CacheServer cs = c.addCacheServer();
        cs.setPort(port);
        cs.start();
        
        return c;
    }
    
	@Bean
	public Region<String, Customer> customerRegion(Cache cache) {

		RegionFactory<String, Customer> rf = cache.createRegionFactory(RegionShortcut.PARTITION);
		
		return rf.create("Customer");
	}
}
