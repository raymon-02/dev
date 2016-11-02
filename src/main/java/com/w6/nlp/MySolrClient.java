package com.w6.nlp;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.w6.data.Article;
import com.w6.data.Email;
import com.w6.data.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;


public class MySolrClient {

    final private String url = "http://localhost:8983/solr/core";
    final private String urlEvents = "http://localhost:8983/solr/events";
    final private String urlEmails = "http://localhost:8983/solr/emails";
    final private SolrClient clientSolr;
    final private SolrClient clientSolrEvent;
    final private SolrClient clientSolrEmail;


    public MySolrClient() {
        clientSolr = new HttpSolrClient(url);
        clientSolrEvent = new HttpSolrClient(urlEvents);
        clientSolrEmail = new HttpSolrClient(urlEmails);
    }

    public void uploadDataToSolr(
            Article article)
            throws IOException, SolrServerException {
        if (article.id == -1) {
            article.id = getNumberOfDocuments() + 1;
        }
        clientSolr.deleteByQuery("id:" + article.id);
        clientSolr.commit();
        clientSolr.add(createDocument(article));
        clientSolr.commit();
    }

    public long uploadEventToSolr(
            Event event)
            throws IOException, SolrServerException {
        event.id = getNumberOfEvents() + 1;
        clientSolrEvent.add(createEvent(event));
        clientSolrEvent.commit();
        return event.id;
    }

    public void updateEventInSolr(
            Event event)
            throws IOException, SolrServerException {
        clientSolrEvent.deleteByQuery("id:" + event.id);
        clientSolrEvent.commit();
        clientSolrEvent.add(createEvent(event));
        clientSolrEvent.commit();
    }

    public Article getDocumentById(long id) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        query.setQuery("id:" + id);

        QueryResponse response = clientSolr.query(query);

        SolrDocumentList listOfDocuments = response.getResults();
        if (!listOfDocuments.isEmpty()) {
            SolrDocument document = listOfDocuments.get(0);
            if (!listOfDocuments.isEmpty()) {
                return parseArticle(document);
            }
        }

        return null;
    }

    public void setEventIdToArticle(long documentId, long eventId)
            throws SolrServerException, IOException {
        Article article = getDocumentById(documentId);
        article.setEventId(eventId);
        clientSolr.add(createDocument(article));
    }

    public void updateDocument(Article article)
            throws IOException, SolrServerException {
        clientSolr.deleteByQuery("id:" + article.id);
        clientSolr.commit();
        clientSolr.add(createDocument(article));
        clientSolr.commit();
    }

    private long getNumberOfDocuments() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        query.setQuery("*");

        QueryResponse response = clientSolr.query(query);

        SolrDocumentList listOfDocuments = response.getResults();
        return listOfDocuments.getNumFound();
    }

    public ArrayList<Article> getDocuments(String keywords) throws SolrServerException, IOException
    {
        ArrayList<Article> listOfDocuments = new ArrayList<>();
        SolrQuery query = new SolrQuery(keywords);
        query.setRows(200);
        query.add("fl", "*,score");
        QueryResponse response = clientSolr.query(query);
        for (final SolrDocument solrDocument : response.getResults())
        {
            listOfDocuments.add(parseArticle(solrDocument));
        }
        return listOfDocuments;
    }

    public Map<Long, Float> getEventsRating(long articleId) throws SolrServerException, IOException
    {
        SolrQuery query = new SolrQuery("id:" + articleId + "or mlt.fl=text");
        query.setRows(20);
        query.add("fl", "*,score");
        Map<Long, Float> map = new HashMap<Long, Float>();
        QueryResponse response = clientSolr.query(query);
        for (final SolrDocument solrDocument : response.getResults())
        {
            Long eventId = (Long) solrDocument.getFirstValue("eventId");
            Float score = (Float) solrDocument.getFirstValue("score");
            System.err.println(eventId);
            System.err.println(score);
            if (map.containsKey(eventId))
            {
                Float newScore = map.get(eventId) + score;
                map.put(eventId, newScore);
            }
            else
            {
                map.put(eventId, score);
            }

        }
        return map;
    }
    private SolrInputDocument createDocument(
            Article article
    ) throws IOException {
        SolrInputDocument newDocument = new SolrInputDocument();
        newDocument.addField("id", article.id);
        newDocument.addField("title", article.title);
        newDocument.addField("sourse", article.sourse);
        newDocument.addField("text", article.text);
        newDocument.addField("response", article.response);
        newDocument.addField("eventId", article.eventId);
        if (article.location != null)
        {
            newDocument.addField("location", article.location);
        }
        
        return newDocument;
    }


    private SolrInputDocument createEvent(
            Event article
    ) throws IOException {
        SolrInputDocument newDocument = new SolrInputDocument();

        newDocument.addField("id", article.id);
        newDocument.addField("title", article.title);
        newDocument.addField("date", article.date);
        newDocument.addField("description", article.description);
        newDocument.addField("country", article.country);
        newDocument.addField("region", article.region);

        return newDocument;
    }

    public void updateEventInSolr(
            Event event, long id
    ) throws SolrServerException, IOException {
        clientSolrEvent.add(createEvent(event));
        clientSolrEvent.commit();
    }

    private long getNumberOfEvents() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        query.setQuery("*");

        QueryResponse response = clientSolrEvent.query(query);

        SolrDocumentList listOfDocuments = response.getResults();
        return listOfDocuments.getNumFound();
    }

    private Article parseArticle(final SolrDocument document)
    {
        Article article = new Article(
                Long.parseLong(document.getFirstValue("id").toString()),
                document.getFirstValue("sourse").toString(),
                document.getFirstValue("text").toString(),
                document.getFirstValue("title").toString(),
                document.getFirstValue("response").toString(),
                (long) document.getFirstValue("eventId")
        );
        if (document.keySet().contains("location"))
        {
            article.location = document.getFirstValue("location").toString();
        }
        return article;
    }

    public Event getEventById(long id) throws SolrServerException, IOException {

        SolrQuery query = new SolrQuery();
        query.setQuery("id:" + id);

        QueryResponse response = clientSolrEvent.query(query);

        SolrDocumentList listOfDocuments = response.getResults();
        SolrDocument document = new SolrDocument();
        if (!listOfDocuments.isEmpty()) {
            document = listOfDocuments.get(0);
            return new Event(
                    id,
                    document.getFirstValue("date").toString(),
                    document.getFirstValue("title").toString(),
                    document.getFirstValue("description").toString(),
                    document.getFirstValue("region") == null ? "" : document.getFirstValue("region").toString(),
                    document.getFirstValue("country") == null ? "" : document.getFirstValue("country").toString()
            );
        }

        return null;
    }

    private List<Event> getEventsFromSolrDocumentList(SolrDocumentList list) {
        List<Event> events = new ArrayList<>();

        list.forEach((document) -> events.add(
                new Event(
                        Long.parseLong(document.getFieldValue("id").toString()),
                        document.getFieldValue("description").toString(),
                        document.getFieldValue("title").toString(),
                        document.getFieldValue("date").toString(),
                        document.getFieldValue("region").toString(),
                        document.getFieldValue("country").toString()
                )
        ));

        return events;
    }

    public List<Article> getArticlesByEventId(long eventId)
            throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        query.setQuery("eventId:" + eventId);
        QueryResponse response = clientSolr.query(query);

        SolrDocumentList listOfDocuments = response.getResults();
        return listOfDocuments.stream().map(document -> parseArticle(document)).collect(Collectors.toList());
    }

    public ArrayList<Event> getEvents() throws SolrServerException, IOException {
        ArrayList<Event> events = new ArrayList<>();
        long numberOfEvents = getNumberOfEvents();

        for (long documentId = 1; documentId <= numberOfEvents; ++documentId) {
            events.add(getEventById(documentId));
        }
        return events;
    }

    public Email getEmailById(long id) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        query.setQuery("id:" + id);

        QueryResponse response = clientSolrEmail.query(query);

        SolrDocumentList listOfDocuments = response.getResults();
        Email email = null;
        if (!listOfDocuments.isEmpty()) {
            SolrDocument document = listOfDocuments.get(0);
            email = new Email(
                    Long.parseLong(document.getFirstValue("id").toString()),
                    document.getFirstValue("date").toString(),
                    document.getFirstValue("subject").toString(),
                    document.getFirstValue("text").toString(),
                    document.getFirstValue("from").toString(),
                    false
            );

        }
        return email;
    }


    public List<Email> getAllNewEmails() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        query.setQuery("used:false");

        QueryResponse response = clientSolrEmail.query(query);

        SolrDocumentList listOfDocuments = response.getResults();
        List<Email> emails = new ArrayList<>();
        if (!listOfDocuments.isEmpty()) {
            listOfDocuments.forEach((document) -> {
                emails.add(
                        new Email(
                                Long.parseLong(document.getFirstValue("id").toString()),
                                document.getFirstValue("date").toString(),
                                document.getFirstValue("subject").toString(),
                                document.getFirstValue("text").toString(),
                                document.getFirstValue("from").toString(),
                                false
                        ));
            });

        }
        return emails;
    }

    public ArrayList<Event> getEventsInRange(String startDate, String endDate) throws SolrServerException, IOException {
        ArrayList<Event> events = new ArrayList<>();
        long numberOfEvents = getNumberOfEvents();

        for (long documentId = 1; documentId <= numberOfEvents; ++documentId) {
            Event event = getEventById(documentId);
            if (event.date.compareTo(startDate) >= 0 && event.date.compareTo(endDate) <= 0) {
                events.add(event);
            }
        }
        events.sort((a, b) -> a.date.compareTo(b.date));
        return events;
    }
}
