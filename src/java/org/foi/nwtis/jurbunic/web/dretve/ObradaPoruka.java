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
    private Message[] messages;

    private ArrayList<Message> zaNWTiS_poruke = new ArrayList<>();
    private ArrayList<Message> zaNWTiS_ostalo = new ArrayList<>();

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
                if (folder.hasNewMessages()) {
                    messages = folder.getMessages();                
                    for (int i = 0; i < messages.length; ++i) {
                        MimeMessage message = (MimeMessage) messages[i];
                        if (message.getSubject().compareTo("NWTiS_poruke") == 0) {
                            Message[] poruka = new Message[1];
                            poruka[0] = messages[i];
                            store.getFolder("NWTiS_poruke").appendMessages(poruka);
                            //zaNWTiS_poruke.add(messages[i]);
                        }
                        if (message.getSubject().compareTo("NWTiS_ostalo") == 0) {
                            //zaNWTiS_ostalo.add(messages[i]);
                        }
                        ContentType ct = new ContentType(message.getContentType());
                        // TODO dovršiti čitanje, obradu i prebacivanje u mape
                    }
                }

                //System.out.println(zaNWTiS_poruke.size());
                //Message[] porukeNWTiS_poruke = new Message[zaNWTiS_poruke.size()];
                //porukeNWTiS_poruke = zaNWTiS_poruke.toArray(porukeNWTiS_poruke);
                //for(Message m:porukeNWTiS_poruke){
                //    System.out.println(m.getSubject());
                //}
                //folder = store.getFolder("NWTiS_poruke");
                //folder.appendMessages(porukeNWTiS_poruke);
                redniBrojCiklusa++;
                //System.out.println("Broj u NWTiS_poruke:" + folder.getFullName()+":::"+folder.getMessageCount());
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

        if (!store.getFolder("NWTiS_poruke").exists()) {
            folder = store.getFolder("NWTiS_poruke");
            folder.create(Folder.HOLDS_MESSAGES);
        }
        if (!store.getFolder("NWTiS_ostalo").exists()) {
            folder = store.getFolder("NWTiS_ostalo");
            folder.create(Folder.HOLDS_MESSAGES);
        }
    }

}
