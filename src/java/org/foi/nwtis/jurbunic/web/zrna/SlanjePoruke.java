/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author grupa_1
 */
@Named(value = "slanjePoruke")
@RequestScoped
public class SlanjePoruke {
    String posluzitelj;
    String salje;
    String prima;
    String predmet;
    String sadrzaj;
    /**
     * Creates a new instance of SlanjePoruke
     */
    public SlanjePoruke() {
    }

    public String getSalje() {
        return salje;
    }

    public void setSalje(String salje) {
        this.salje = salje;
    }

    public String getPrima() {
        return prima;
    }

    public String saljiPoruku(){
        //TODO tu smo stali
        return "PoslanaPoruka";
    }
    
    public void setPrima(String prima) {
        this.prima = prima;
    }
    public String getPredmet() {
        return predmet;
    }
    public void setPredmet(String predmet) {
        this.predmet = predmet;
    }
    public String getSadrzaj() {
        return sadrzaj;
    }
    public void setSadrzaj(String sadrzaj) {
        this.sadrzaj = sadrzaj;
    }
    
    
}
