package com.w6.data.dao.email;

import com.w6.data.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private EmailRepository emailRepository;

    @Override
    public Email findById(long id) {
        return emailRepository.findById(id);
    }

    @Override
    public List<Email> findAllByUsedFalse() {
        return null;
    }
}
