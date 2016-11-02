package com.w6.config;

import com.google.code.geocoder.Geocoder;
import com.w6.external_api.Geolocator;
import com.w6.nlp.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@Configuration
@EnableScheduling
public class AppContext {

    @Bean
    public MySolrClient mySolrClient() {
        return new MySolrClient();
    }


    @Bean
    public LexicalizedParser lp() {
        return LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
    }

    @Bean
    public ViolentVerbsParser violentVerbsParser() throws IOException {
        return new ViolentVerbsParser(lp());
    }

    @Bean
    public WeaponsParser weaponsParser() throws IOException {
        return new WeaponsParser(lp());
    }

    @Bean
    public CountryParser countryParser() throws IOException {
        return new CountryParser();
    }

    @Bean
    public TokenizerFactory<CoreLabel> tokenizerFactory() {
        return PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
    }

    @Bean
    public Parser parser() throws IOException {
        return new Parser();
    }
    
    @Bean
    public Geolocator geolator()
    {
        return new Geolocator();
    }

    @Bean
    public Geocoder geocoder()
    {
        return new Geocoder();
    }
    
    @Bean
    public EventGuesser eventGuesser()
    {
        return new EventGuesser();
    }
}
