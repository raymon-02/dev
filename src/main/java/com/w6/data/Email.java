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
@SolrDocument(solrCoreName = "email")
public class Email {

    @Field
    private long id;

    @Field
    private String subject;

    @Field
    private String  date;

    @Field
    private String from;

    @Field
    private String text;

    @Field
    private Boolean used;
    //todo: add boolean "used"
}
