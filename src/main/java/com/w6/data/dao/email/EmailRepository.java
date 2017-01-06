package com.w6.data.dao.email;

import com.w6.data.Email;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends SolrCrudRepository<Email, String> {

    Email findById(long id);

    List<Email> findAllByUsedFalse();
}
