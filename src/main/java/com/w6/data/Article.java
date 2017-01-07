package com.w6.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SolrDocument(solrCoreName = "core")
public class Article {

    @Field
    private long id;

    @Field
    private String source;

    @Field
    private String text;

    @Field
    private String title;

    @Field
    private String response;

    @Field
    private long eventId;

    @Field
    public String location;

    @Indexed(value = "score", readonly = true)
    private Float score;
}
