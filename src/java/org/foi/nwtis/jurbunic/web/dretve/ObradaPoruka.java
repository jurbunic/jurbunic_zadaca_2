/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.dretve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
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

    //-----------REGEKSI----------------
    final String regexADD = "^ADD IoT ([1-6]) \"([^\\s]+)\" GPS: ([0-9]{1,3}.[0-9]{6}), ([0-9]{1,3}.[0-9]{6});$";
    final String regexTEMP = "^TEMP IoT ([1-6]) T: ([0-9]{4})[.]([0-9]{2})[.]([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2}) C:([0-9]{1,2})[.][0-9];$";
    final String regexEVENT = "^EVENT IoT ([1-6]) T: ([0-9]{4})[.]([0-9]{2})[.]([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2}) F:([0-9]{1,2});$";
    //----------------------------------
    //-----------STATISTIKA-------------
    static Long obradaZapocela;
    static Long obradaZavrsila;
    static Long trajanjeObrade;
    static Long trajanjeCiklusa;
    static int redniBrojCiklusa;
    static int brojPoruka;
    static int brojdodanihIOT;
    static int brojMjerenihTEMP;
    static int brojIzvrsenihEVENT;
    static int brojPogresaka;
    //----------------------------------
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
        trajanjeCiklusa = Long.parseLong(konf.dajPostavku("mail.timeSecThread"));
        trajanjeObrade = 0l;
        // TODO odredi trajanje
        try {
            // TODO dodati ostale parametre!
            spajanje(server, port, korisnik, lozinka);
        } catch (MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        redniBrojCiklusa = 0;
        while (!prekidObrade) {

            try {
                Long pocetak = System.currentTimeMillis();
                // Open the INBOX folder
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_WRITE);
                //----Citanje poruka----//                         
                if (folder.hasNewMessages()) {
                    brojPoruka = 0;
                    messages = folder.getMessages();
                    for (int i = 0; i < messages.length; ++i) {
                        MimeMessage message = (MimeMessage) messages[i];
                        brojPoruka++;
                        if (message.getSubject().compareTo("NWTiS_poruke") == 0) {
                            ContentType ct = new ContentType(message.getContentType());
                            if (ct.getBaseType().compareTo("TEXT/PLAIN") == 0) {
                                if (obradaNaredbe(message.getContent().toString())) {
                                    System.out.println("Dobro");
                                    Message[] poruka = new Message[1];
                                    poruka[0] = messages[i];
                                    folder.setFlags(poruka, new Flags(Flag.SEEN), true);
                                    store.getFolder("NWTiS_poruke").appendMessages(poruka);
                                } else {
                                    brojPogresaka++;
                                    System.out.println("Nije dobro");
                                }
                            }
                        } else if (message.getSubject().compareTo("NWTiS_ostalo") == 0) {
                            Message[] poruka = new Message[1];
                            poruka[0] = messages[i];
                            folder.setFlags(messages, new Flags(Flag.SEEN), prekidObrade);
                        } else {
                            System.out.println("U spam!");
                        }
                        messages[i].setFlag(Flag.DELETED, true);
                        // TODO dovršiti čitanje, obradu i prebacivanje u mape
                    }
                }
                redniBrojCiklusa++;
                System.out.println("ObradaPoruka" + redniBrojCiklusa);
                //!---Citanje poruka---!//
                trajanjeObrade = System.currentTimeMillis() - pocetak;
                //TODO ovdje ide slanje maila statistike!
                folder.close(true);
                sleep(trajanjeCiklusa * 1000 - trajanjeObrade);
            } catch (InterruptedException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MessagingException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
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

    private boolean obradaNaredbe(String naredba) {
        String cistaNaredba = naredba.replaceAll("(\\r|\\n)", "");
        Pattern pattern = Pattern.compile(regexADD);
        Matcher m = pattern.matcher(cistaNaredba);
        if (m.matches()) {
            brojdodanihIOT++;
            return true;
        }
        pattern = Pattern.compile(regexTEMP);
        m = pattern.matcher(cistaNaredba);
        if (m.matches()) {
            brojMjerenihTEMP++;
            return true;
        }
        pattern = Pattern.compile(regexEVENT);
        m = pattern.matcher(cistaNaredba);
        if (m.matches()) {
            brojIzvrsenihEVENT++;
            return true;
        }
        return false;
    }

}
