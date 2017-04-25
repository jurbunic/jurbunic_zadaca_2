/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.zrna;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    private ArrayList<Poruka> poruke = new ArrayList<>();
    private ArrayList<Izbornik> mape = new ArrayList<>();

    /**
     * Creates a new instance of PregledPoruka
     */
    public PregledPoruka() {
        ctx = FacesContext.getCurrentInstance();
        preuzmiMape();
        preuzimPoruke();
    }

    void preuzmiMape() {
        try {
            dohvatiKonfiguraciju();
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", konf.dajPostavku("mail.server"));
            //properties.put("mail.smtp.port", konf.dajPostavku("mail.port"));
            //properties.put("mail.smtp.username", konf.dajPostavku("mail.usernameView"));
            //properties.put("mail.smtp.password", konf.dajPostavku("mail.passwordView"));
            Session session = Session.getInstance(properties, null);
            store = session.getStore("imap");
            store.connect("127.0.0.1", 143, "servis@nwtis.nastava.foi.hr", "123456");
            mape.add(new Izbornik(store.getFolder("NWTiS_poruke").getFullName(), "NWTiS_poruke"));
            mape.add(new Izbornik(store.getFolder("NWTiS_ostalo").getFullName(), "NWTiS_ostalo"));

        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NeispravnaKonfiguracija ex) {
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
                Message[] messages = folder.getMessages();
                for (int j = 0; j < messages.length; j++) {
                    MimeMessage message = (MimeMessage) messages[j];
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
            }
            ukupanBrojMAPA = poruke.size();
        }
    }
    

    public String promjenaMape() {
        this.preuzimPoruke();
        return "PromjenaMape";
    }

    public String filtrirajPoruke() {
        this.preuzimPoruke();
        return "FiltrirajPoruke";
    }

    public String prethodnePoruke() {
        this.preuzimPoruke();
        return "prethodnePoruke";
    }

    public String sljedecePoruke() {
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
