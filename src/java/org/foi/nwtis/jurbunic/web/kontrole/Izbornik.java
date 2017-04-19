/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.kontrole;

/**
 *
 * @author grupa_1
 */
public class Izbornik {
    String labela;
    String vrijednost;

    public Izbornik(String labela, String vrijednost) {
        this.labela = labela;
        this.vrijednost = vrijednost;
    } 
    
    public String getLabela() {
        return labela;
    }
    public void setLabela(String labela) {
        this.labela = labela;
    }
    public String getVrijednost() {
        return vrijednost;
    }
    public void setVrijednost(String vrijednost) {
        this.vrijednost = vrijednost;
    }
}
