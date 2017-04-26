/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.zrna;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import org.foi.nwtis.jurbunic.konfiguracije.Konfiguracija;
import org.foi.nwtis.jurbunic.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.jurbunic.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.jurbunic.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.jurbunic.web.kontrole.Izbornik;
import org.foi.nwtis.jurbunic.web.kontrole.Poruka;

/**
 *
 * @author grupa_1
 */
@Named(value = "pregledPoruka")
@RequestScoped
public class PregledPoruka {

    FacesContext ctx;
    Konfiguracija konf;

    String posljuzitelj;
    String korisnik;
    String lozinka;
    String odabranaMapa;
    String traziPoruke;

    Integer ukupanBrojPORUKA = 0;
    Integer ukupanBrojMAPA = 0;

    Integer ukupnoPrikazano = 0;
    Integer pozicijaOd = 0;
    Integer pozicijaDo = 0;

    Store store;

    private String trenutnaStranica;

    public String getTrenutnaStranica() {
        return trenutnaStranica;
    }

    public void setTrenutnaStranica(String trenutnaStranica) {
        this.trenutnaStranica = trenutnaStranica;
    }

    private ArrayList<Poruka> poruke = new ArrayList<>();
    private ArrayList<Izbornik> mape = new ArrayList<>();

    /**
     * Creates a new instance of PregledPoruka
     */
    public PregledPoruka() {
        try {
            ctx = FacesContext.getCurrentInstance();
            dohvatiKonfiguraciju();
            preuzmiMape();
            preuzimPoruke();
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NeispravnaKonfiguracija ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void preuzmiMape() {
        try {
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", konf.dajPostavku("mail.server"));
            //properties.put("mail.smtp.port", konf.dajPostavku("mail.port"));
            //properties.put("mail.smtp.username", konf.dajPostavku("mail.usernameView"));
            //properties.put("mail.smtp.password", konf.dajPostavku("mail.passwordView"));
            Session session = Session.getInstance(properties, null);
            store = session.getStore("imap");
            store.connect("127.0.0.1", 143, "servis@nwtis.nastava.foi.hr", "123456");
            mape.add(new Izbornik(store.getFolder("INBOX").getFullName() + " - " + store.getFolder("INBOX").getMessageCount(), "INBOX"));
            mape.add(new Izbornik(store.getFolder("NWTiS_poruke").getFullName() + " - " + store.getFolder("NWTiS_poruke").getMessageCount(), "NWTiS_poruke"));
            mape.add(new Izbornik(store.getFolder("NWTiS_ostalo").getFullName() + " - " + store.getFolder("NWTiS_ostalo").getMessageCount(), "NWTiS_ostalo"));
            mape.add(new Izbornik(store.getFolder("Spam").getFullName() + " - " + store.getFolder("Spam").getMessageCount(), "Spam"));
            ukupanBrojPORUKA += store.getFolder("Spam").getMessageCount()
                    + store.getFolder("NWTiS_ostalo").getMessageCount()
                    + store.getFolder("NWTiS_poruke").getMessageCount();

        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void preuzimPoruke() {

        //TODO promjeni sa stavrnim preuzimanjem poruka!
        //TODO razmisli o optimiranju preuzimanja poruka!
        if (odabranaMapa == null) {
            return;
        } else {
            try {
                Folder folder = store.getFolder(odabranaMapa);
                folder.open(Folder.READ_ONLY);
                ukupanBrojMAPA = folder.getMessageCount();
                if (trenutnaStranica.isEmpty()) {
                    trenutnaStranica = "0";
                }
                pozicijaOd = ukupanBrojMAPA - Integer.parseInt(konf.dajPostavku("mail.numMessages")) * (Integer.valueOf(trenutnaStranica) + 1);
                pozicijaDo = ukupanBrojMAPA - Integer.parseInt(konf.dajPostavku("mail.numMessages")) * Integer.valueOf(trenutnaStranica);
                if (pozicijaOd < 1) {
                    pozicijaOd = 1;
                }
                if (pozicijaDo < 1) {
                    pozicijaDo += Integer.parseInt(konf.dajPostavku("mail.numMessages"));
                }
                Message[] messages = folder.getMessages(pozicijaOd, pozicijaDo);
                for (int i = 0; i < messages.length; i++) {

                    MimeMessage message = (MimeMessage) messages[i];
                    String primatelji = "";
                    for (int k = 0; k < message.getAllRecipients().length; k++) {
                        primatelji += message.getAllRecipients()[k].toString();
                    }
                    Poruka poruka = new Poruka(message.getContentID(), message.getSentDate(),
                            message.getReceivedDate(), primatelji, message.getSubject(),
                            (String) message.getContent(), message.getContentType());
                    poruke.add(poruka);
                }
                    
            } catch (MessagingException ex) {
                Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IndexOutOfBoundsException ex){
                return;
            }
        
        ukupanBrojMAPA = poruke.size();
    }
}

public String promjenaMape() {
        this.preuzimPoruke();
        return "PromjenaMape";
    }

    public String filtrirajPoruke() {
        System.out.println("Neki tekst: "+traziPoruke);
        this.preuzimPoruke();
        return "FiltrirajPoruke";
    }

    public String prethodnePoruke() {
        if(trenutnaStranica.isEmpty()){
            trenutnaStranica = "0";
        }
        Map<String, String> params = ctx.getExternalContext().getRequestParameterMap();
        String action = params.get("inkrementStranice");
        int test1 = Integer.parseInt(trenutnaStranica);
        test1 -= Integer.parseInt(action);
        trenutnaStranica = String.valueOf(test1);
        this.preuzimPoruke();
        return "prethodnePoruke";
    }

    public String sljedecePoruke() {       
        if(trenutnaStranica.isEmpty()){
            trenutnaStranica = "0";
        }
        Map<String, String> params = ctx.getExternalContext().getRequestParameterMap();
        String action = params.get("inkrementStranice");
        int test1 = Integer.parseInt(trenutnaStranica);
        test1 += Integer.parseInt(action);
        trenutnaStranica = String.valueOf(test1);
        this.preuzimPoruke();
        return "SljedecePoruke";
    }
    
    

    public String promjenaJezika() {
        return "promjenaJezika";
    }

    public String saljiPoruku() {
        return "saljiPoruku";
    }

    public ArrayList<Poruka> getPoruke() {
        return poruke;
    }

    public ArrayList<Izbornik> getMape() {
        return mape;
    }

    public String getTraziPoruke() {
        return traziPoruke;
    }

    public void setTraziPoruke(String traziPoruke) {
        this.traziPoruke = traziPoruke;
    }

    public Integer getUkupanBrojMAPA() {
        return ukupanBrojMAPA;
    }

    public void setUkupanBrojMAPA(Integer ukupanBrojMAPA) {
        this.ukupanBrojMAPA = ukupanBrojMAPA;
    }

    public Integer getUkupanBrojPORUKA() {
        return ukupanBrojPORUKA;
    }

    public void setUkupanBrojPORUKA(Integer ukupanBrojPORUKA) {
        this.ukupanBrojPORUKA = ukupanBrojPORUKA;
    }

    public String getOdabranaMapa() {
        return odabranaMapa;
    }

    public void setOdabranaMapa(String odabranaMapa) {
        this.odabranaMapa = odabranaMapa;
    }

    private void dohvatiKonfiguraciju() throws NemaKonfiguracije, NeispravnaKonfiguracija {
        String datoteka = ctx.getExternalContext().getRealPath("/WEB-INF")
                + File.separator + ctx.getExternalContext().getInitParameter("konfiguracija");
        konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
    }
    
}
