package com.w6.data.dao.article;

import com.w6.data.Article;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.DefaultQueryParser;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
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
        SimpleQuery simpleQuery = new SimpleQuery("id:" + id + " or mlt.fl=text");
        simpleQuery.setRows(20);
        simpleQuery.addProjectionOnField("*");
        simpleQuery.addProjectionOnField("score");
        DefaultQueryParser defaultQueryParser = new DefaultQueryParser();
        SolrQuery solrQuery = defaultQueryParser.doConstructSolrQuery(simpleQuery);

        return articleTemplate.execute(solrClient ->
                articleTemplate.getConverter().read(solrClient.query(solrQuery).getResults(), Article.class));
    }


}
