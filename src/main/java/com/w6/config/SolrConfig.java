package com.w6.config;


import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.SolrClientFactory;
import org.springframework.data.solr.server.support.MulticoreSolrClientFactory;

import javax.annotation.Resource;

@Configuration
@EnableSolrRepositories(basePackages = "com.w6.data", multicoreSupport = true)
@PropertySource("classpath:application.properties")
public class SolrConfig {

    @Resource
    private Environment environment;

//    @Bean
//    public SolrClient solrClient() {
//        return new HttpSolrClient(environment.getProperty("solr.server.uri"));
//    }

    @Bean
    public SolrClientFactory solrClientFactory() {
        return new MulticoreSolrClientFactory(new HttpSolrClient(environment.getProperty("solr.server.uri")));
    }

//    @Bean
//    public SolrTemplate articleTemplate() {
//        return new SolrTemplate(new HttpSolrClient(environment.getProperty("solr.server.uri")), "core");
//    }

    @Bean
    public SolrTemplate articleTemplate() {
        SolrTemplate solrTemplate = new SolrTemplate(solrClientFactory());
        solrTemplate.setSolrCore("core");

        return solrTemplate;
    }

}
