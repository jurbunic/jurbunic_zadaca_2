/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.dretve;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import org.foi.nwtis.jurbunic.konfiguracije.Konfiguracija;

/**
 *
 * @author grupa_1
 */
public class ObradaPoruka extends Thread {

    private ServletContext sc = null;
    private boolean prekidObrade = false;

    private Session session;
    private Store store;
    private Folder folder;
    private ArrayList<Folder> folders = new ArrayList<>();
    private Message[] messages;

    @Override
    public void interrupt() {
        prekidObrade = true;
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        Konfiguracija konf = (Konfiguracija) sc.getAttribute("Mail_Konfig");
        String server = konf.dajPostavku("mail.server");
        String port = konf.dajPostavku("mail.port");
        String korisnik = konf.dajPostavku("mail.usernameThread");
        String lozinka = konf.dajPostavku("mail.passwordThread");
        int trajanjeCiklusa = Integer.parseInt(konf.dajPostavku("mail.timeSecThread"));
        int trajanjeObrade = 0;
        // TODO odredi trajanje
        try {
            // TODO dodati ostale parametre!
            spajanje(server, port, korisnik, lozinka);
        } catch (MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        int redniBrojCiklusa = 0;
        while (!prekidObrade) {
            try {
                //----Citanje poruka----//                         
                // Open the INBOX folder

                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);
                messages = folder.getMessages();
                for (int i = 0; i < messages.length; ++i) {
                    MimeMessage message = (MimeMessage) messages[i];
                    if(message.getSubject().compareTo("NWTiS_poruke")==1){
                        folders.get(1).open(Folder.READ_WRITE);
                        
                    }
                    ContentType ct = new ContentType(message.getContentType());
                    // TODO dovršiti čitanje, obradu i prebacivanje u mape
                }
                redniBrojCiklusa++;
                System.out.println("ObradaPoruka" + redniBrojCiklusa);
                //!---Citanje poruka---!//
                sleep(trajanjeCiklusa * 1000 - trajanjeObrade);
            } catch (InterruptedException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MessagingException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    public void setSc(ServletContext sc) {
        this.sc = sc;
    }

    private void spajanje(String server, String port, String korisnik, String lozinka) throws MessagingException {
        // Start the session
        java.util.Properties properties = System.getProperties();
        properties.put("mail.smtp.host", server);
        session = Session.getInstance(properties, null);

        // Connect to the store
        store = session.getStore("imap");
        store.connect(server, korisnik, lozinka);
        
        Folder folder = store.getFolder("NWTiS_poruke");
        if(!folder.exists()){
            folder.create(Folder.HOLDS_MESSAGES);
        }
        System.out.println(folder.getFullName());
        
    }

}
