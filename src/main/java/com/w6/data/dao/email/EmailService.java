package com.w6.data.dao.email;

import com.w6.data.Email;

import java.util.List;

public interface EmailService {

    Email findById(long id);

    List<Email> findAllByUsedFalse();
}
