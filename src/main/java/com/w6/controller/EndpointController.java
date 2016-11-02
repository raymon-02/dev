package com.w6.controller;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderStatus;
import static com.google.code.geocoder.model.GeocoderStatus.OK;
import com.google.code.geocoder.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.w6.data.Article;
import com.w6.data.Email;
import com.w6.data.Event;
import com.w6.data.Response;
import com.w6.external_api.Geolocator;
import com.w6.nlp.EventGuesser;
import com.w6.nlp.Parser;
import com.w6.nlp.MySolrClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class EndpointController {
    private static final String INPUT_VIEW = "input";
    private static final String W6_VIEW = "w6";
    private static final String UPLOAD_VIEW = "upload";
    private static final String QUERY_VIEW = "query";
    private static final String DOCUMENTS_BY_EVENT_VIEW = "articlesOfEvent";
    private static final String REPORT_VIEW = "report";
    private static final String EMAILS_VIEW = "emails";

    @Autowired
    private Parser parser;

    @Autowired
    protected MySolrClient solrClient;
    
    @Autowired
    private Geolocator geolocator;

    @Autowired
    private EventGuesser eventGuesser;


    private static final Gson gson = new GsonBuilder().create();
    
    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public ModelAndView post(
            @RequestParam("sourse") String sourse,
            @RequestParam("title") String title,           
            @RequestParam("text") String text
            ) throws IOException, SolrServerException
    {
        Article article = new Article(new Long(-1), sourse, text, title, 
                "",
                -1
        );
        article.response = gson.toJson(parser.generateResponse(article));
        solrClient.uploadDataToSolr(article);
        return parse(article.id);
    }

    @RequestMapping(value = "/parse", method = RequestMethod.GET)
    public ModelAndView parse(@RequestParam("id") Long docId) throws IOException
    {
        Article text;
        try { 
            text = solrClient.getDocumentById(docId);
            if (text == null) {
                return new ModelAndView(W6_VIEW);
            }
            
            ModelAndView modelAndView = new ModelAndView(W6_VIEW);
            Article article = solrClient.getDocumentById(docId);
            modelAndView.addObject("article", gson.toJson(article));
            modelAndView.addObject("events", gson.toJson(eventGuesser.guessEvent(article)));
            modelAndView.addObject("id", docId);
            
            return modelAndView;
        } catch (SolrServerException ex) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, ex);
            return new ModelAndView(W6_VIEW);
        }
        
    }
    
    @RequestMapping(value = "/update/event", method = RequestMethod.POST)
    public ModelAndView  updateEvent(
            @RequestParam("eventId") String id,    
            @RequestParam("eventTitle") String title ,                
            @RequestParam("eventDate") String date,
            @RequestParam("eventDesc") String description,
            @RequestParam("eventReg") String region,
            @RequestParam("eventCountry") String country
    ) throws IOException
    {
        Event event = new Event(Long.parseLong(id), date, title, description, region, country);
        try {
            solrClient.updateEventInSolr(event);
        } catch (SolrServerException ex) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return displayDocumentsByEvent(Long.parseLong(id));
    }
    
    @RequestMapping(value = "/parse", method = RequestMethod.POST)
    public ModelAndView update(
            @RequestParam("id") long docId,    
            @RequestParam("event_select") long eventId,                
            @RequestParam("title") String title,
            @RequestParam("date") String date,
            @RequestParam("region") String region,
            @RequestParam("country") String country            
    ) throws IOException, SolrServerException
    {
        if (eventId == -1)
        {
            Event event = new Event(
                    -1,
                    date,
                    title,
                    "Please provide description",
                    region,
                    country                    
            );
            eventId = solrClient.uploadEventToSolr(event);
        }
        
        Article document = solrClient.getDocumentById(docId);
        document.eventId = eventId;
        solrClient.uploadDataToSolr(document);
        return parse();
        
    }

    @RequestMapping(value = "/input", method = RequestMethod.GET)
    public ModelAndView displayInput(@RequestParam(value = "email_id", required = false) Long emailId) throws IOException
    {
        try {
            ModelAndView modelAndView = new ModelAndView(INPUT_VIEW);
            if (emailId != null)
            {
                Email email = solrClient.getEmailById(emailId+1);
                if (email != null)
                {
                    modelAndView.addObject("email", gson.toJson(email));
                }
            }
            return modelAndView;
        } catch (SolrServerException e) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, e);
            return new ModelAndView(INPUT_VIEW);
        }

    }
    
    @RequestMapping(value = "/events/view", method = RequestMethod.GET)
    public ModelAndView displayDocumentsByEvent(@RequestParam("id") long docId) 
            throws IOException
    {
        ModelAndView modelAndView = new ModelAndView(DOCUMENTS_BY_EVENT_VIEW);
        try {
           modelAndView.addObject("event", gson.toJson(solrClient.getEventById(docId))); 
           modelAndView.addObject("docList", gson.toJson(solrClient.getArticlesByEventId(docId)));
        } catch (SolrServerException e) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return modelAndView;
    }
    
    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public ModelAndView parse() throws IOException
    {
        List<Article> text;
        try { 
            text = solrClient.getDocuments("*:*");
            ModelAndView modelAndView = new ModelAndView(QUERY_VIEW);
            modelAndView.addObject("response", gson.toJson(text));
            return modelAndView;
        } catch (SolrServerException ex) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, ex);
            return new ModelAndView(W6_VIEW);
        }
    }
    @RequestMapping(value = "/relevant", method = RequestMethod.GET)
    public ModelAndView relevant() throws IOException
    {
        ModelAndView modelAndView = new ModelAndView(QUERY_VIEW);
        ArrayList<Article> documents;
        try {
            documents = solrClient.getDocuments("   Involved\n" +
"   Incident\n" +
"   Staff\n" +
"   IMC\n" +
"   aid\n" +
"   Office\n" +
"    security\n" +
"    NGO\n" +
"    killed\n" +
"    Afghanistan\n" +
"    Deaths\n" +
"    police\n" +
"    workers\n" +
"    Afghan\n" +
"    Taliban\n" +
"    Vehicle\n" +
"    international\n" +
"    government\n" +
"    humanitarian\n" +
"    Security\n" +
"    UN\n" +
"    Incidents\n" +
"    Pakistan\n" +
"    kidnapped\n" +
"    armed\n" +
"    hospital\n" +
"    Injuries\n" +
"    WFP\n" +
"    CMT \n" +
"    HQ\n" +
"    AOG\n" +
"    Evacuation\n" +
"    Relocation\n" +
"    Weapon\n" +
"    Violation\n" +
"    Property\n" +
"    Equipment\n" +
"    members\n" +
"    military\n" +
"    officials\n" +
"    Chad\n" +
"    foreign\n" +
"    suicide\n" +
"    Darfur\n" +
"    NGOs\n" +
"    MSF\n" +
"   ICRC\n" +
"  U.N.\n" +
"    injured");
            modelAndView.addObject("response", gson.toJson(documents));
            return modelAndView;
        } catch (SolrServerException ex) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, ex);
            return new ModelAndView(W6_VIEW);
        }
    }

    @RequestMapping(value = "/emails", method = RequestMethod.GET)
    public ModelAndView emails() throws IOException
    {
        ModelAndView modelAndView = new ModelAndView(EMAILS_VIEW);
        try {
            modelAndView.addObject("emails", gson.toJson(solrClient.getAllNewEmails()));
        } catch (SolrServerException ex) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView map() throws IOException, SolrServerException
    {
        ModelAndView modelAndView = new ModelAndView("index");

        ArrayList<Article> articles = solrClient.getDocuments("*:*");
        modelAndView.addObject("articles", gson.toJson(articles));
        return modelAndView;

    }

    @RequestMapping(value = "/report", method = RequestMethod.GET)
    public ModelAndView report(@RequestParam("month") String month) throws IOException, SolrServerException
    {
        ModelAndView modelAndView = new ModelAndView(REPORT_VIEW);
        ArrayList<Event> eventsInRange = solrClient.getEventsInRange(month.concat("-01"), month.concat("-31"));
        modelAndView.addObject("events", gson.toJson(eventsInRange));
        ArrayList<String[] > sourses = new ArrayList<>();
        
        for (Event event: eventsInRange)
        {
            sourses.add(
                    solrClient.getArticlesByEventId(event.id)
                            .stream()
                            .map(article -> article.sourse)
                            .toArray(size -> new String[size])
            );
        }
            

        modelAndView.addObject("sourses", gson.toJson(sourses));
        modelAndView.addObject("month", month);
        
        return modelAndView;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logout (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }
}
