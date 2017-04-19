/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.kontrole;

import java.util.Date;

/**
 *
 * @author dkermek
 */
public class Poruka {

    private String id;
    private Date vrijemeSlanja;
    private Date vrijemePrijema;
    private String salje;
    private String predmet;
    private String sadrzaj;
    private String vrsta;

    public Poruka(String id, Date vrijemeSlanja, Date vrijemePrijema, String salje, String predmet, String sadrzaj, String vrsta) {
        this.id = id;
        this.vrijemeSlanja = vrijemeSlanja;
        this.vrijemePrijema = vrijemePrijema;
        this.salje = salje;
        this.predmet = predmet;
        this.sadrzaj = sadrzaj;
        this.vrsta = vrsta;
    }

    public String getId() {
        return id;
    }

    public Date getVrijemeSlanja() {
        return vrijemeSlanja;
    }

    public Date getVrijemePrijema() {
        return vrijemePrijema;
    }

    public String getPredmet() {
        return predmet;
    }

    public String getSalje() {
        return salje;
    }

    public String getVrsta() {
        return vrsta;
    }

    public String getSadrzaj() {
        return sadrzaj;
    }

}
