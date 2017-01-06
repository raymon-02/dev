package com.w6.data.dao.article;

import com.w6.data.Article;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends SolrCrudRepository<Article, String> {

    Article findById(Long id);

    List<Article> findAllByEventId(long eventId);
}
