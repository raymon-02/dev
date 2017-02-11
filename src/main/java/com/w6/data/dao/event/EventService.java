package com.w6.data.dao.event;

import com.w6.data.Article;
import com.w6.data.Event;

import java.util.List;

public interface EventService {

    Event findById(long id);

    List<Event> findByDateStartingWith(String datePrefix);

    Event save(Event event);

    List<Event> findAll();

    List<Event> guessEvent(Article article);

}
