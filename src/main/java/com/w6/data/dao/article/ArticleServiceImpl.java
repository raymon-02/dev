package com.w6.data.dao.article;

import com.w6.data.Article;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private SolrTemplate articleTemplate;


    @Override
    public Article findById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    public List<Article> findAllByEventId(long eventId) {
        return articleRepository.findAllByEventId(eventId);
    }

    @Override
    public List<Article> findAll() {
        List<Article> result = new ArrayList<>();
        articleRepository.findAll().forEach(result::add);

        return result;
    }

    @Override
    public Article save(Article article) {
        if (article.getId() == -1) {
            long totalCount = articleRepository.count();
            article.setId(totalCount + 1);
        }
        return articleRepository.save(article);
    }

    @Override
    public List<Article> findByIdOrMoreLikeThisByText(long id) {
        return executeQuery("id:" + id + " or mlt.fl=text", 20);
    }

    @Override
    public List<Article> findByKeywords(String keywords) {
        return executeQuery(keywords, 200);
    }

    private List<Article> executeQuery(String query, int rows) {
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setRows(rows);
        solrQuery.add("fl", "*,score");

        return articleTemplate.execute(solrClient ->
                articleTemplate.getConverter().read(solrClient.query(solrQuery).getResults(), Article.class));
    }


}
