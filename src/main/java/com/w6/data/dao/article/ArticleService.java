package com.w6.data.dao.article;


import com.w6.data.Article;

import java.util.List;

public interface ArticleService {

    Article findById(Long id);

    List<Article> findAllByEventId(long eventId);

    List<Article> findAll();

    Article save(Article article);

    List<Article> findByIdOrMoreLikeThisByText(long id);
}
