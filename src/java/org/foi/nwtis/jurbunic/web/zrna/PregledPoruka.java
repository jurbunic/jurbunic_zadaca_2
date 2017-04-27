/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.zrna;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    //----Podaci za konekciju-----
    String posluzitelj;
    String korisnik;
    String lozinka;
    int port;

    //----------------------------
    //-------Varijable za UI------
    String odabranaMapa;
    String traziPoruke;
    Integer ukupanBrojPORUKA = 0;
    Integer ukupanBrojMAPA = 0;
    Integer ukupnoPrikazano = 0;
    Integer pozicijaOd = 0;
    Integer pozicijaDo = 0;
    private String trenutnaStranica;
    //---------------------------
    //---------Mail server-------
    Store store;
    String folderNWTiS;
    String folderOther;

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
            folderNWTiS = konf.dajPostavku("mail.folderNWTiS");
            folderOther = konf.dajPostavku("mail.folderOther");
            preuzmiMape();
            ukupnoPrikazano = Integer.parseInt(konf.dajPostavku("mail.numMessages"));
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
            korisnik = konf.dajPostavku("mail.usernameView");
            lozinka = konf.dajPostavku("mail.passwordView");
            port = Integer.parseInt(konf.dajPostavku("mail.port"));
            posluzitelj = konf.dajPostavku("mail.server");
            Session session = Session.getInstance(properties, null);
            store = session.getStore("imap");
            store.connect(posluzitelj, port, korisnik, lozinka);
            trenutnaStranica = "0";
            if (!store.getFolder(folderNWTiS).exists()) {
                store.getFolder(folderNWTiS).create(Folder.HOLDS_MESSAGES);
            }
            if (!store.getFolder(folderOther).exists()) {
                store.getFolder(folderOther).create(Folder.HOLDS_MESSAGES);
            }
            if (!store.getFolder("Spam").exists()) {
                store.getFolder("Spam").create(Folder.HOLDS_MESSAGES);
            }

            mape.add(new Izbornik(store.getFolder("INBOX").getFullName() + " - " + store.getFolder("INBOX").getMessageCount(), "INBOX"));
            mape.add(new Izbornik(store.getFolder(folderNWTiS).getFullName() + " - " + store.getFolder(folderNWTiS).getMessageCount(), folderNWTiS));
            mape.add(new Izbornik(store.getFolder(folderOther).getFullName() + " - " + store.getFolder(folderOther).getMessageCount(), folderOther));
            mape.add(new Izbornik(store.getFolder("Spam").getFullName() + " - " + store.getFolder("Spam").getMessageCount(), "Spam"));
            ukupanBrojPORUKA += store.getFolder("Spam").getMessageCount()
                    + store.getFolder(folderOther).getMessageCount()
                    + store.getFolder(folderNWTiS).getMessageCount()
                    + store.getFolder("INBOX").getMessageCount();

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
                Collections.reverse(poruke);
            } catch (MessagingException ex) {
                Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IndexOutOfBoundsException ex) {
                return;
            }

            ukupanBrojMAPA = poruke.size();
        }
    }

    public String promjenaMape() {
        trenutnaStranica = "0";
        this.preuzimPoruke();
        return "PromjenaMape";
    }

    public String filtrirajPoruke() {
        //System.out.println("Neki tekst: " + traziPoruke);
        this.preuzimPoruke();
        for (int i = 0; i < poruke.size(); i++) {
            boolean podudaraSe = true;
            Poruka poruka = poruke.get(i);
            String polje = "";
           // String polje =poruka.getId().replace("\\r\\n", ""); 
           // if (polje.compareTo(traziPoruke) == 0) {
           //     podudaraSe = false;
           //     continue;
           // }
            polje = poruka.getSadrzaj().replaceAll("(\\r|\\n)", "");
            if (polje.compareTo(traziPoruke) == 0) {
                podudaraSe = false;
                continue;
            }
            polje = poruka.getSalje().replaceAll("(\\r|\\n)", "");
            if (polje.compareTo(traziPoruke) == 0) {
                podudaraSe = false;
                continue;
            }
            polje = poruka.getVrsta().replaceAll("(\\r|\\n)", "");
            if (polje.compareTo(traziPoruke) == 0) {
                continue;
            }
            if (podudaraSe) {
                poruke.remove(poruka);
                i--;
            }
        }

        return "FiltrirajPoruke";
    }

    public String prethodnePoruke() {
        if (trenutnaStranica.isEmpty()) {
            trenutnaStranica = "0";
            return "prethodnePoruke";
        }
        if (trenutnaStranica.compareTo("0") == 0) {
            this.preuzimPoruke();
            return "prethodnePoruke";
        }
        Map<String, String> params = ctx.getExternalContext().getRequestParameterMap();
        String action = params.get("inkrementStranice");
        int test1 = Integer.parseInt(trenutnaStranica);
        test1 -= Integer.parseInt(action);
        trenutnaStranica = String.valueOf(test1);
        this.preuzimPoruke();
        return "prethodnePoruke";
    }

    public String sljedecePoruke() throws MessagingException {
        if (trenutnaStranica.isEmpty()) {
            trenutnaStranica = "0";
        }if(Integer.parseInt(trenutnaStranica)>(store.getFolder(odabranaMapa).getMessageCount()/ukupnoPrikazano)-1){
            this.preuzimPoruke();
            return "SljedecePoruke";
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
