package com.w6.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.w6.data.Article;
import com.w6.data.Email;
import com.w6.data.Event;
import com.w6.data.dao.article.ArticleService;
import com.w6.data.dao.email.EmailService;
import com.w6.data.dao.event.EventService;
import com.w6.external_api.Geolocator;
import com.w6.nlp.Parser;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class EndpointController {
    private static final String INPUT_VIEW = "input";
    private static final String W6_VIEW = "w6";
    private static final String QUERY_VIEW = "query";
    private static final String DOCUMENTS_BY_EVENT_VIEW = "articlesOfEvent";
    private static final String REPORT_VIEW = "report";
    private static final String EMAILS_VIEW = "emails";

    @Autowired
    private Parser parser;

    @Autowired
    private Geolocator geolocator;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private EventService eventService;

    @Autowired
    private EmailService emailService;

    private static final Gson gson = new GsonBuilder().create();

    @RequestMapping(value = "post", method = RequestMethod.POST)
    public ModelAndView post(
            @RequestParam("source") String source,
            @RequestParam("title") String title,
            @RequestParam("text") String text
    ) throws IOException {
        Article article = Article.builder()
                .id(-1)
                .source(source)
                .text(text)
                .title(title)
                .eventId(-1)
                .build();
        article.setResponse(gson.toJson(parser.generateResponse(article)));
        article.setLocation(gson.toJson(geolocator.findLocation(article)));
        articleService.save(article);

        return parse(article.getId());
    }

    @RequestMapping(value = "parse", method = RequestMethod.GET)
    public ModelAndView parse(@RequestParam("id") Long id) {
        Article article = articleService.findById(id);
        if (article == null) {
            return new ModelAndView(W6_VIEW);
        }

        ModelAndView modelAndView = new ModelAndView(W6_VIEW);
        modelAndView.addObject("article", gson.toJson(articleService.findById(id)));
        modelAndView.addObject("events", gson.toJson(eventService.guessEvent(article)));
        modelAndView.addObject("id", id);

        return modelAndView;
    }

    @RequestMapping(value = "/update/event", method = RequestMethod.POST)
    public ModelAndView updateEvent(
            @RequestParam("eventId") String id,
            @RequestParam("eventTitle") String title,
            @RequestParam("eventDate") String date,
            @RequestParam("eventDesc") String description,
            @RequestParam("eventReg") String region,
            @RequestParam("eventCountry") String country
    ) {
        Event event = Event.builder()
                .id(Long.parseLong(id))
                .date(date)
                .title(title)
                .description(description)
                .region(region)
                .country(country)
                .build();
        eventService.save(event);

        return displayDocumentsByEvent(Long.parseLong(id));
    }

    @RequestMapping(value = "parse", method = RequestMethod.POST)
    public ModelAndView update(
            @RequestParam("id") long id,
            @RequestParam("event_select") long eventId,
            @RequestParam("title") String title,
            @RequestParam("date") String date,
            @RequestParam("region") String region,
            @RequestParam("country") String country
    ) {
        if (eventId == -1) {
            Event event = Event.builder()
                    .id(-1)
                    .date(date)
                    .title(title)
                    .description("Please provide description")
                    .region(region)
                    .country(country)
                    .build();

            event = eventService.save(event);
            eventId = event.getId();
        }

        Article article = articleService.findById(id);
        article.setEventId(eventId);
        articleService.save(article);

        return parse();
    }

    @RequestMapping(value = "/input", method = RequestMethod.GET)
    public ModelAndView displayInput(@RequestParam(value = "email_id", required = false) Long emailId) {
        ModelAndView modelAndView = new ModelAndView(INPUT_VIEW);
        if (emailId != null) {
            Email email = emailService.findById(emailId + 1);
            if (email != null) {
                modelAndView.addObject("email", gson.toJson(email));
            }
        }

        return modelAndView;
    }

    @RequestMapping(value = "/events/view", method = RequestMethod.GET)
    public ModelAndView displayDocumentsByEvent(@RequestParam("id") long eventId) {
        ModelAndView modelAndView = new ModelAndView(DOCUMENTS_BY_EVENT_VIEW);
        modelAndView.addObject("event", gson.toJson(eventService.findById(eventId)));
        List<Article> articlesByEventId = articleService.findAllByEventId(eventId);
        modelAndView.addObject("docList", gson.toJson(articlesByEventId));

        return modelAndView;
    }

    @RequestMapping(value = "view", method = RequestMethod.GET)
    public ModelAndView parse() {
        List<Article> text = articleService.findAll();
        ModelAndView modelAndView = new ModelAndView(QUERY_VIEW);
        modelAndView.addObject("response", gson.toJson(text));

        return modelAndView;
    }

    @RequestMapping(value = "relevant", method = RequestMethod.GET)
    public ModelAndView relevant() {
        ModelAndView modelAndView = new ModelAndView(QUERY_VIEW);

        List<Article> articles = articleService.findByKeywords(
                "   Involved\n" +
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
        modelAndView.addObject("response", gson.toJson(articles));
        return modelAndView;
    }

    @RequestMapping(value = "emails", method = RequestMethod.GET)
    public ModelAndView emails() {
        ModelAndView modelAndView = new ModelAndView(EMAILS_VIEW);
        modelAndView.addObject("emails", gson.toJson(emailService.findAllByUsedFalse()));

        return modelAndView;
    }

    @RequestMapping(value = "report", method = RequestMethod.GET)
    public ModelAndView report(@RequestParam("month") String month) {
        ModelAndView modelAndView = new ModelAndView(REPORT_VIEW);
        String datePrefix = DateTime.now().getYear() + "-" + month;
        List<Event> eventsInRange = eventService.findByDateStartingWith(datePrefix);
        modelAndView.addObject("events", gson.toJson(eventsInRange));

        List<List<String>> sources = new ArrayList<>();
        for (Event event : eventsInRange) {
            sources.add(
                    articleService.findAllByEventId(event.getId())
                            .stream()
                            .map(Article::getSource)
                            .collect(Collectors.toList())
            );
        }


        modelAndView.addObject("sources", gson.toJson(sources));
        modelAndView.addObject("month", month);

        return modelAndView;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {
        List<Article> articles = articleService.findAll();
        model.addAttribute("articles", gson.toJson(articles));

        return "index";
    }

    @RequestMapping(value = "/gettingstarted", method = RequestMethod.GET)
    public String gettingStarted() {
        return "gettingstarted";
    }
}
