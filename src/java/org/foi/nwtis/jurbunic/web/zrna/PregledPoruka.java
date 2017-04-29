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
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.mail.Address;
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
 * @author Jurica Bunić
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
    private int stranicaUIod = 1;
    private int stranicaUIdo = 1;
    private boolean naprijedGumb=false;
    private boolean natragGumb=false;
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

    /**
     * Metoda služi za preuzimanje mapa sa mail servera.
     * Metoda se spaja na mail server sa parametrima definiranima u konfiguracijskoj
     * datoteci. Nakon spajanja provjerava se postoje li mape za spojenog korisnika.
     * Ako ne postoje, kreiraju se, te izračunava ukupan broj poruka za korisnika.
     */
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
    /**
     * Metoda služi za preuzimanje poruka sa mail servera.
     * Dohvać se odabrana mapa te se računaju pozicije od - do koje će se preuzeti poruke
     * Nakon što se poruka dohvati, informacije unutar dohvaćene poruke se spremaju u objekte
     * klase Poruka, te zatim u listu
     */
    void preuzimPoruke() {

        if (odabranaMapa == null) {
            odabranaMapa = mape.get(0).getVrijednost();
        } else {
            try {
                Folder folder = store.getFolder(odabranaMapa);
                folder.open(Folder.READ_ONLY);
                ukupanBrojMAPA = folder.getMessageCount();
                if (trenutnaStranica.isEmpty()) {
                    trenutnaStranica = "0";
                }
                pozicijaOd = ukupanBrojMAPA - Integer.parseInt(konf.dajPostavku("mail.numMessages")) * (Integer.valueOf(trenutnaStranica) + 1) + 1;
                pozicijaDo = ukupanBrojMAPA - Integer.parseInt(konf.dajPostavku("mail.numMessages")) * Integer.valueOf(trenutnaStranica);

                if (pozicijaOd < 1) {
                    pozicijaOd = 1;
                }
                if (pozicijaDo < 1) {
                    pozicijaDo += Integer.parseInt(konf.dajPostavku("mail.numMessages"));
                }
                stranicaUIdo = ukupanBrojMAPA / Integer.parseInt(konf.dajPostavku("mail.numMessages"));
                if (ukupanBrojMAPA % Integer.parseInt(konf.dajPostavku("mail.numMessages")) != 0) {
                    stranicaUIdo++;
                }
                if(stranicaUIdo==0){
                    stranicaUIdo=1;
                }
                Message[] messages = folder.getMessages(pozicijaOd, pozicijaDo);
                for (int i = 0; i < messages.length; i++) {
                    MimeMessage message = (MimeMessage) messages[i];
                    Address[] posiljatelj = message.getFrom();
                    String salje = "";
                    for (int k = 0; k < posiljatelj.length; k++) {
                        salje += posiljatelj[k].toString();
                    }
                    String tijelo = "";
                    if (message.isMimeType("text/*")) {
                        tijelo = (String) message.getContent();
                    } else {
                        tijelo = "Tijelo nije u text formatu!";
                    }
                    Poruka poruka = new Poruka(message.getMessageID(), message.getSentDate(),
                            message.getReceivedDate(), salje, message.getSubject(),
                            tijelo, message.getContentType());
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
            if(stranicaUIod == 1){
               natragGumb = true;
            }
            if(stranicaUIdo <= stranicaUIod){
                naprijedGumb = true;
            }
            ukupanBrojMAPA = poruke.size();
        }
    }

    public String promjenaMape() {
        trenutnaStranica = "0";
        this.preuzimPoruke();
        return "PromjenaMape";
    }

    /**
     * Metoda prvo preuzima poruke, te nakon preuzimanja, nad preuzetim porukama
     * provodi pretraživanje nad sadržajem
     * @return "FiltrirajPoruke"
     */
    public String filtrirajPoruke() {
        this.preuzimPoruke();
        for (int i = 0; i < poruke.size(); i++) {
            boolean podudaraSe = false;
            Poruka poruka = poruke.get(i);
            String polje = poruka.getSadrzaj().replaceAll("(\\r|\\n)", "");
            if (polje.contains(traziPoruke)) {
                podudaraSe = true;
            }
            if (!podudaraSe) {
                poruke.remove(poruka);
                i--;
            }
        }

        return "FiltrirajPoruke";
    }
    /**
     * Metoda služi za dohvaćanje poruka na prethodnoj stranici ( ako se je moguće
     * prebaciti na prethodnu stranicu ) 
     * @return 
     */
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
        stranicaUIod = test1 + 1;
        this.preuzimPoruke();
        return "prethodnePoruke";
    }
    /**
     * Metoda se koristi kod dohvaćanja poruka na sljedećoj stranici (ako je moguće).
     * Ako je ukupno poruka u mapi, podijeljeno sa brojem poruka koje se smiju prikazati nije cijeli broj
     * tada se koristi prvi slućaj, a inače se izvršava else;
     * @return
     * @throws MessagingException 
     */
    public String sljedecePoruke() throws MessagingException {
        if (trenutnaStranica.isEmpty()) {
            trenutnaStranica = "0";
        }
        if (store.getFolder(odabranaMapa).getMessageCount() % ukupnoPrikazano != 0) {
            if (Integer.parseInt(trenutnaStranica) > (store.getFolder(odabranaMapa).getMessageCount() / ukupnoPrikazano) - 1) {
                stranicaUIod = Integer.parseInt(trenutnaStranica) + 1;
                this.preuzimPoruke();
                return "SljedecePoruke";
            }
            stranicenje(); 
        } else {
            if (Integer.parseInt(trenutnaStranica) >= (store.getFolder(odabranaMapa).getMessageCount() / ukupnoPrikazano) - 1) {
                stranicaUIod = Integer.parseInt(trenutnaStranica) + 1;
                this.preuzimPoruke();
                return "SljedecePoruke";
            }
            stranicenje(); 
        }

        return "SljedecePoruke";
    }
    /**
     * Metoda koja se koristi kod prebacivanja na sljedeću stranicu
     * Dohvaća se parametar koji predstavlja inkrement ( +1) te se zatim dohvaća
     * vrijednost sa stranice (trenutnaStranica - hidden input na stranici) koja
     * se zatim povećava za 1, te se zatim preuzimaju poruke sa novim indeksima.
     */
    private void stranicenje() {
        Map<String, String> params = ctx.getExternalContext().getRequestParameterMap();
        String action = params.get("inkrementStranice");
        int test1 = Integer.parseInt(trenutnaStranica);
        test1 += Integer.parseInt(action);
        trenutnaStranica = String.valueOf(test1);
        stranicaUIod = test1 + 1;

        this.preuzimPoruke();
        if (stranicaUIod > stranicaUIdo) {
            stranicaUIod--;
        }
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

    public int getStranicaUIod() {
        return stranicaUIod;
    }

    public int getStranicaUIdo() {
        return stranicaUIdo;
    }

    public void setPozicijaOd(Integer pozicijaOd) {
        this.pozicijaOd = pozicijaOd;
    }

    public void setPozicijaDo(Integer pozicijaDo) {
        this.pozicijaDo = pozicijaDo;
    }

    public boolean isNaprijedGumb() {
        return naprijedGumb;
    }

    public void setNaprijedGumb(boolean naprijedGumb) {
        this.naprijedGumb = naprijedGumb;
    }

    public boolean isNatragGumb() {
        return natragGumb;
    }

    public void setNatragGumb(boolean natragGumb) {
        this.natragGumb = natragGumb;
    }
    
    /**
     * Dohvaćanje konfiguracije
     * 
     * @throws NemaKonfiguracije
     * @throws NeispravnaKonfiguracija 
     */
    private void dohvatiKonfiguraciju() throws NemaKonfiguracije, NeispravnaKonfiguracija {
        String datoteka = ctx.getExternalContext().getRealPath("/WEB-INF")
                + File.separator + ctx.getExternalContext().getInitParameter("konfiguracija");
        konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
    }

}
