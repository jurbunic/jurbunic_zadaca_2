/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.zrna;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import org.foi.nwtis.jurbunic.konfiguracije.Konfiguracija;
import org.foi.nwtis.jurbunic.web.kontrole.Izbornik;
import org.foi.nwtis.jurbunic.web.kontrole.Poruka;

/**
 *
 * @author grupa_1
 */
@Named(value = "pregledPoruka")
@RequestScoped
public class PregledPoruka {

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
        preuzimPoruke();
        preuzmiMape();
    }

    void preuzmiMape() {
        try {
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", "127.0.0.1");
            Session session = Session.getInstance(properties, null);
            store = session.getStore("imap");
            store.connect("127.0.0.1", 143, "servis@nwtis.nastava.foi.hr", "123456");
            store.getFolder("NWTiS_poruke").getFullName();
            mape.add(new Izbornik(store.getFolder("NWTiS_poruke").getFullName(),"NWTiS_poruke"));
            mape.add(new Izbornik(store.getFolder("NWTiS_ostalo").getFullName(),"NWTiS_ostalo"));
            
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void preuzimPoruke() {

        //TODO promjeni sa stavrnim preuzimanjem poruka!
        //TODO razmisli o optimiranju preuzimanja poruka!
        poruke.clear();
        for(int i=0;i<mape.size();i++){
            if(mape.get(i).getVrijednost().compareTo("NWTiS_poruke")==0){
                
            }
        }
        int i = 0;
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        ukupanBrojMAPA = poruke.size();

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

}
