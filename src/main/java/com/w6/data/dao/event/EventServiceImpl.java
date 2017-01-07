package com.w6.data.dao.event;

import com.w6.data.Article;
import com.w6.data.Event;
import com.w6.data.dao.article.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private EventRepository eventRepository;

    @Override
    public Event findById(long id) {
        return eventRepository.findById(id);
    }


    @Override
    public List<Event> findByDateStartingWith(String datePrefix) {
        return eventRepository.findByDateStartingWith(datePrefix);
    }


    @Override
    public Event save(Event event) {
        if (event.getId() == -1) {
            long totalCount = eventRepository.count();
            event.setId(totalCount + 1);
        }
        return eventRepository.save(event);
    }

    @Override
    public List<Event> findAll() {
        List<Event> result = new ArrayList<>();

        Iterable<Event> events = eventRepository.findAll();
        for (Event event : events) {
            result.add(event);
        }

        return result;
    }


    @Override
    public List<Event> guessEvent(Article article) {

        List<Article> articles = articleService.findByIdOrMoreLikeThisByText(article.getId());

        Map<Long, Float> rating = new HashMap<>();
        for (Article articleInResponse : articles) {
            Float articleInResponseRating = rating.get(articleInResponse.getEventId());

            if (articleInResponseRating == null) {
                rating.put(articleInResponse.getEventId(), articleInResponse.getScore());
            } else {
                rating.put(articleInResponse.getEventId(), articleInResponse.getScore() + articleInResponseRating);
            }
        }

        List<Event> events = findAll();
        events.sort((eventA, eventB) -> {
            float eventAScore = rating.get(eventA.getId());
            float eventBScore = rating.get(eventB.getId());
            int comparedScore = Float.compare(eventBScore, eventAScore);

            if (comparedScore == 0) {
                return Long.compare(eventA.getId(), eventB.getId());
            }

            return comparedScore;
        });

        return events;
    }

}
