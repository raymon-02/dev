package com.w6.data.dao.event;

import com.w6.data.Event;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends SolrCrudRepository<Event, String> {

    Event findById(long id);

    List<Event> findByDateStartingWith(String datePrefix);

}
