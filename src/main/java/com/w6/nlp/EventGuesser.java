/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.w6.nlp;

import com.w6.data.Article;
import com.w6.data.Event;
import edu.stanford.nlp.ling.tokensregex.types.ValueFunctions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author towelenee
 */
public class EventGuesser {
    @Autowired
    private MySolrClient solrClient;
    public List<Event> guessEvent(Article article) throws SolrServerException, IOException
    {
        List<Event> events = solrClient.getEvents();
        Map<Long, Float> eventsRating = solrClient.getEventsRating(article.id);
        events.sort((Event a, Event b) -> {
            float ratingOfA = 0;
            float ratingOfB = 0;
            if (eventsRating.containsKey(a.id))
            {
                ratingOfA = eventsRating.get(a.id);
            }
            if (eventsRating.containsKey(b.id))
            {
                ratingOfB = eventsRating.get(b.id);
            }
            if (ratingOfA == ratingOfB)
            {
                if (a.id > b.id)
                {
                    return 1;
                }
                else if (a.id < b.id)
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
            if (ratingOfA < ratingOfB)
            {
                return -1;
            }
            else
            {
                return 1;
            }
        });
        return events;        
    }
}
