/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.dretve;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import org.foi.nwtis.jurbunic.konfiguracije.Konfiguracija;
import org.foi.nwtis.jurbunic.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.jurbunic.web.zrna.SlanjePoruke;

/**
 *
 * @author Jurica Bunić
 */
public class ObradaPoruka extends Thread {

    //-----------REGEKSI----------------
    final String regexADD = "^ADD IoT ([0-9]{1,6}) \\\"([^\\\\s]+)\\\"GPS: ([0-9]{1,3}.[0-9]{6}),([0-9]{1,3}.[0-9]{6});";
    final String regexTEMP = "^TEMP IoT ([0-9]{1,6}) T: ([0-9]{4})[.]([0-9]{2})[.]([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2}) C: ([0-9]{1,2})[.][0-9];$";
    final String regexEVENT = "^EVENT IoT ([0-9]{1,6}) T: ([0-9]{4})[.]([0-9]{2})[.]([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2}) F: ([0-9]{1,2});$";
    //----------------------------------
    //-----------STATISTIKA-------------
    static int brojStatistike;
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
    BP_Konfiguracija bpkonf;
    private Connection veza;

    private Session session;
    private Store store;
    private Folder folder;
    private Message[] messages;
    private String folderNWTiS;
    private String folderOther;

    private Konfiguracija konf;
    @Override
    public void interrupt() {
        prekidObrade = true;
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        konf = (Konfiguracija) sc.getAttribute("Mail_Konfig");
        bpkonf = (BP_Konfiguracija) sc.getAttribute("BP_Konfig");
        String server = konf.dajPostavku("mail.server");
        String port = konf.dajPostavku("mail.port");
        String korisnik = konf.dajPostavku("mail.usernameThread");
        String lozinka = konf.dajPostavku("mail.passwordThread");
        folderNWTiS = konf.dajPostavku("mail.folderNWTiS");
        folderOther = konf.dajPostavku("mail.folderOther");
        trajanjeCiklusa = Long.parseLong(konf.dajPostavku("mail.timeSecThread"));
        trajanjeObrade = 0l;
        try {
            spajanjeMail(server, port, korisnik, lozinka);
        } catch (MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        spajanjeBaza();
        redniBrojCiklusa = 0;
        while (!prekidObrade) {
            
            try {
                Long pocetak = System.currentTimeMillis();
                // Open the INBOX folder
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_WRITE);
                brojPoruka = 0;
                brojIzvrsenihEVENT = 0;
                brojMjerenihTEMP = 0;
                brojPogresaka = 0;
                brojdodanihIOT = 0;
                //----Citanje poruka----//                         
                if (folder.hasNewMessages()) {                 
                    messages = folder.getMessages();
                    for (int i = 0; i < messages.length; ++i) {
                        MimeMessage message = (MimeMessage) messages[i];
                        brojPoruka++;
                        if (message.getSubject().compareTo(folderNWTiS) == 0) {
                            ContentType ct = new ContentType(message.getContentType());
                            if (ct.getBaseType().compareTo("TEXT/PLAIN") == 0) {
                                if (obradaNaredbe(message.getContent().toString())) {
                                    System.out.println("Dobro");
                                    Message[] poruka = new Message[1];
                                    poruka[0] = messages[i];
                                    store.getFolder(folderNWTiS).appendMessages(poruka);

                                } else {
                                    brojPogresaka++;
                                    System.out.println("Nije dobro");
                                    Message[] poruka = new Message[1];
                                    poruka[0] = messages[i];
                                    store.getFolder("Spam").appendMessages(poruka);
                                }
                            }
                        } else {
                            Message[] poruka = new Message[1];
                            poruka[0] = messages[i];
                            //folder.setFlags(poruka, new Flags(Flag.SEEN), true);
                            store.getFolder(folderOther).appendMessages(poruka);
                        }
                        messages[i].setFlag(Flag.DELETED, true);
                        // TODO dovršiti čitanje, obradu i prebacivanje u mape
                    }
                }
                redniBrojCiklusa++;
                System.out.println("ObradaPoruka" + redniBrojCiklusa);

                trajanjeObrade = System.currentTimeMillis() - pocetak;
                folder.close(true);
                
                //Statistika
                SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy hh.mm.ss.SSS");
                long zavrsetak = System.currentTimeMillis();
                String poc = formater.format(new Date(pocetak));
                String krj = formater.format(new Date(zavrsetak));
                String statistika = "";
                StringBuilder sb = new StringBuilder();
                sb.append("\n")
                        .append("Obrada započela u: ").append(poc).append("\n")
                        .append("Obrada završila u: ").append(krj).append("\n")
                        .append("\n")
                        .append("Trajanje obrade u ms: ").append(trajanjeObrade).append("\n")
                        .append("Broj poruka: ").append(brojPoruka).append("\n")
                        .append("Broj dodanih IOT: ").append(brojdodanihIOT).append("\n")
                        .append("Broj mjerenih TEMP: ").append(brojMjerenihTEMP).append("\n")
                        .append("Broj izvršenih EVENT: ").append(brojIzvrsenihEVENT).append("\n")
                        .append("Broj pogrešaka: ").append(brojPogresaka).append("\n");             
                statistika = sb.toString();
                saljiStatistiku(statistika);
                long preostaloSpavanje = trajanjeCiklusa * 1000 - trajanjeObrade;
                if(preostaloSpavanje < 0){
                    preostaloSpavanje *= -1;
                    preostaloSpavanje = (preostaloSpavanje % 1000);
                }
                sleep(preostaloSpavanje);
            } catch (InterruptedException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MessagingException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * Funkcija saljiStatistiku prima jedan parametar koji predstavlja tijelo email poruke.
     * Poruka se šalje sa adrese koja je definirana u konfiguraciji. Sadržaj poruke se ispisuje
     * u konzolu nakon slanja poruke
     * @param statistika 
     */
    
    private void saljiStatistiku(String statistika){
        try {
            Session session = Session.getDefaultInstance(System.getProperties());
            MimeMessage message = new MimeMessage(session);            
            Address fromAddress = new InternetAddress(konf.dajPostavku("mail.usernameThread"));
            message.setFrom(fromAddress);
            Address[] toAddresses = InternetAddress.parse(konf.dajPostavku("mail.usernameStatistics"));
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setSentDate(new Date());
            brojStatistike++;
            message.setSubject(konf.dajPostavku("mail.subjectStatistics")+" - "+NumberFormat.getNumberInstance(Locale.GERMANY).format(brojStatistike));
            message.setText(statistika);
            Transport.send(message);
            System.out.println(statistika);
        } catch (AddressException ex) {
            Logger.getLogger(SlanjePoruke.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(SlanjePoruke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    public void setSc(ServletContext sc) {
        this.sc = sc;
    }

    /**
     * Metoda spajanjeBaza služi za spajanje na bazu podataka koja je definirana u konfiguracijskoj
     * datoteci. 
     */
    private void spajanjeBaza() {
        try {
            Class.forName(bpkonf.getDriverDatabase());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            veza = DriverManager.getConnection(
                    bpkonf.getServerDatabase() + bpkonf.getUserDatabase(),
                    bpkonf.getUserUsername(),
                    bpkonf.getUserPassword());
        } catch (SQLException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * spajanjeMail se koristi kod povezivanja na mail server. Prima četiri parametra
     * Uz spajanje na mail server, provjerava se postoje li mape koje su definirane u
     * konfiguracijskoj datoteci
     * 
     * @param server - adresa mail poslužitelja
     * @param port - port na mail poslužitelju
     * @param korisnik - korisničko ime korisnika koji se prijavljuje na server
     * @param lozinka lozinka korisnika koji se prijavljuje na server
     * @throws MessagingException 
     */
    private void spajanjeMail(String server, String port, String korisnik, String lozinka) throws MessagingException {
        // Start the session
        java.util.Properties properties = System.getProperties();
        properties.put("mail.smtp.host", server);
        session = Session.getInstance(properties, null);
        // Connect to the store
        store = session.getStore("imap");
        store.connect(server,Integer.parseInt(port), korisnik, lozinka);
        if (!store.getFolder(folderNWTiS).exists()) {
            folder = store.getFolder(folderNWTiS);
            folder.create(Folder.HOLDS_MESSAGES);
        }
        if (!store.getFolder(folderOther).exists()) {
            folder = store.getFolder(folderOther);
            folder.create(Folder.HOLDS_MESSAGES);
        }
        if (!store.getFolder("Spam").exists()) {
            folder = store.getFolder("Spam");
            folder.create(Folder.HOLDS_MESSAGES);
        }
    }

    /**
     * Funkcija prima naredbu i provjerava ispravnost naredbe prema definiranim 
     * regexima. Ukoliko je naredba ispravna provjerava se preko funkcije bazaUpit(sqlUpit)
     * dali postoji zadani IoT uređaj u bazi. Ako je sve ispravno u bazu se upisuje novi redak
     * 
     * @param naredba naredba koja je dobivena u email poruci
     * @return true - ako je naredba ispravna, false - ako naredba nije ispravna
     */
    private boolean obradaNaredbe(String naredba) {
        String cistaNaredba = naredba.replaceAll("(\\r|\\n)", "");
        Pattern pattern = Pattern.compile(regexADD);
        Matcher m = pattern.matcher(cistaNaredba);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (m.matches()) {
            Date datumKreiranja = new Date();
            String sqlUpit = "SELECT id FROM uredaji WHERE id=" + m.group(1);
            if (bazaUpit(sqlUpit)) {
                String sqlUnos = "INSERT INTO uredaji (id,naziv,latitude,longitude,status,vrijeme_promjene,vrijeme_kreiranja) VALUES "
                        + "(" + m.group(1) + ",'" + m.group(2) + "'," + Float.parseFloat(m.group(3)) + "," + Float.parseFloat(m.group(4)) + ","
                        + 0 + ",'" + sdf.format(datumKreiranja) + "','" + sdf.format(datumKreiranja) + "')";
                unosUBazu(sqlUnos);
                brojdodanihIOT++;
                return true;
            } else {
                return false;
            }
        }
        pattern = Pattern.compile(regexTEMP);
        m = pattern.matcher(cistaNaredba);
        if (m.matches()) {
            String sqlUpit = "SELECT id FROM uredaji WHERE id=" + m.group(1);
            if (!bazaUpit(sqlUpit)) {
                String datumMjerenja = m.group(2)+"."+m.group(3)+"."+m.group(4)+" "
                                      +m.group(5)+":"+m.group(6)+":"+m.group(7);
                String datumZapisa = sdf.format(new Date());
                String sqlUnos = "INSERT INTO temperature(id,temp,vrijeme_mjerenja,vrijeme_kreiranja) VALUES "
                        + "(" + m.group(1) + ","+Float.parseFloat(m.group(8))+",'"+datumMjerenja+"','"+datumZapisa+"')";
                unosUBazu(sqlUnos);
                String sqlUnos1 = "UPDATE uredaji SET vrijeme_promjene='"+datumZapisa+"' WHERE id="+m.group(1);
                unosUBazu(sqlUnos1);
                brojMjerenihTEMP++;
                return true;
            }else{
                return false;
            }
        }
        pattern = Pattern.compile(regexEVENT);
        m = pattern.matcher(cistaNaredba);
        if (m.matches()) {
            String sqlUpit = "SELECT id FROM uredaji WHERE id=" + m.group(1);
            if(!bazaUpit(sqlUpit)){
                String datumDogadaja = m.group(2)+"."+m.group(3)+"."+m.group(4)+" "
                                      +m.group(5)+":"+m.group(6)+":"+m.group(7);
                String datumZapisa = sdf.format(new Date());
                String sqlUnos = "INSERT INTO dogadaji(id,vrsta,vrijeme_izvrsavanja,vrijeme_kreiranja) VALUES "
                        + "("+Integer.parseInt(m.group(1))+","+Integer.parseInt(m.group(8))+",'"+datumDogadaja+"','"+datumZapisa+"')";
                unosUBazu(sqlUnos);
                String sqlUnos1 = "UPDATE uredaji SET vrijeme_promjene='"+datumZapisa+"' WHERE id="+m.group(1);
                unosUBazu(sqlUnos1);
                brojIzvrsenihEVENT++;
                return true;
            }
            else{
                return false;
            }
        }
        return false;
    }
    /**
     * funkcija služi za provjeru vraća li zadani SQL upit barem jedan redak. Ako
     * vraća tada funkcija vraća vrijednost false, a ako upit ne vraća ni jedan 
     * redak tada funkcija vraća true
     * @param sqlNaredba SQL naredba (SELECT) 
     * @return 
     */
    private boolean bazaUpit(String sqlNaredba) {
        try {
            Statement naredba = veza.createStatement();
            ResultSet odgovor = naredba.executeQuery(sqlNaredba);
            if (!odgovor.next()) {
                odgovor.beforeFirst();
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Metoda služi za unos podataka u bazu podataka. 
     * @param sqlNaredba SQL naredba (INSERT, UPDATE, DELETE)
     */
    private void unosUBazu(String sqlNaredba) {
        try {
            Statement naredba = veza.createStatement();
            naredba.executeUpdate(sqlNaredba);
        } catch (SQLException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
