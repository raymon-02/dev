package com.w6.email;

import com.sun.mail.pop3.POP3SSLStore;
import com.sun.tools.internal.ws.wsdl.document.jaxws.Exception;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.search.FlagTerm;

class GmailClient {

    private Store store;
    private String username, password;
    private Folder folder;

    public GmailClient(String username, String password, String folderName) {
        this.username = username;
        this.password = password;
        try {
            connect();
            openFolder(folderName);
        } catch (MessagingException e) {
            Logger.getLogger(GmailClient.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void connect() throws MessagingException {

        String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        Properties pop3Props = new Properties();

        pop3Props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
        pop3Props.setProperty("mail.pop3.port", "995");
        pop3Props.setProperty("mail.pop3.socketFactory.port", "995");

        URLName url = new URLName("pop3", "pop.gmail.com", 995, "",
                username, password);

        Session session = Session.getInstance(pop3Props, null);
        store = new POP3SSLStore(session, url);
        store.connect();

    }

    private void openFolder(String folderName) throws MessagingException {
        folder = store.getFolder(folderName);

        if (folder == null) {
            throw new MessagingException("Invalid folder");
        }

        try {
            folder.open(Folder.READ_WRITE);
        } catch (MessagingException ex) {
            folder.open(Folder.READ_ONLY);
        }
    }

    private void closeFolder() throws MessagingException {
        if (folder != null) {
            folder.close(false);
        }
    }

    public int getMessageCount() throws MessagingException {
        if (folder == null) {
            throw new MessagingException("Folder is not open!");
        } else {
            return folder.getMessageCount();
        }
    }


    public Message getMessageById(int id) throws MessagingException {
        if (folder == null) {
            throw new MessagingException("Folder is not open!");
        } else {
            return folder.getMessage(id);
        }
    }

    public int getNewMessageCount() throws MessagingException {
        if (folder == null) {
            throw new MessagingException("Folder is not open!");
        } else {
            return folder.getNewMessageCount();
        }
    }

    public Collection<Message> getUnreadMessages() throws MessagingException {
        if (folder != null) {
            Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            return new ArrayList<>(Arrays.asList(messages));
        }
        return null;
    }

    public void disconnect() throws MessagingException {
        if (folder != null) {
            closeFolder();
        }
        if (store != null) {
            store.close();
        }
    }
}
