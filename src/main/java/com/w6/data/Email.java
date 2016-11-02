package com.w6.data;

public class Email {
    private long id;
    private String subject;
    private String  date;
    private String from;
    private String text;
    private Boolean used;
    //todo: add boolean "used"
    
    public Email(long id, String date, String subject, 
             String text, String from, Boolean used) 
    {
        this.id = id;
        this.date = date;
        this.subject = subject;
        this.text = text;
        this.from = from;
        this.used = used;
    }
    
}
