package com.w6.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.SolrDocument;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SolrDocument(solrCoreName = "events")
public class Event {
    @Field
    private long id;

    //todo: make it date
    @Field
    private String date;

    @Field
    private String title;

    @Field
    private String description;

    @Field
    private String region;

    @Field
    private String country;
}
