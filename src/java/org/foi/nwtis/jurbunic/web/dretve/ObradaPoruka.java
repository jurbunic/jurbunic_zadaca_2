/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.dretve;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.ServletContext;
import org.foi.nwtis.jurbunic.konfiguracije.Konfiguracija;

/**
 *
 * @author grupa_1
 */
public class ObradaPoruka extends Thread {

    private ServletContext sc = null;
    private boolean prekidObrade = false;

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
        Session session;
        Store store;
        Folder folder;
        Message[] messages;
        // TODO dodati ostale parametre!
        int redniBrojCiklusa = 0;
        while (!prekidObrade) {
            try {
                //----Citanje poruka----//
                // Start the session
                java.util.Properties properties = System.getProperties();
                properties.put("mail.smtp.host", server);
                session = Session.getInstance(properties, null);

                // Connect to the store
                store = session.getStore("imap");
                store.connect(server, korisnik, lozinka);

                // Open the INBOX folder
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);

                messages = folder.getMessages();
                for (int i = 0; i < messages.length; ++i) {
                    
                    // TODO dovršiti čitanje, obradu i prebacivanje u mape
                }
                redniBrojCiklusa++;
                System.out.println("ObradaPoruka" + redniBrojCiklusa);
                //!---Citanje poruka---!//
                sleep(trajanjeCiklusa * 1000 - trajanjeObrade);
            } catch (InterruptedException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchProviderException ex) {
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

}
