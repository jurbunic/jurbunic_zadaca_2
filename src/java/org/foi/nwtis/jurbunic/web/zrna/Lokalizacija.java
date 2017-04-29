/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import org.foi.nwtis.jurbunic.web.kontrole.Izbornik;

/**
 *
 * @author Jurica Bunić
 */
@Named(value = "lokalizator")
@SessionScoped
public class Lokalizacija implements Serializable {

    static final ArrayList<Izbornik> izbornikJezika = new ArrayList<>();
    String odabraniJezik;

    static {        
        izbornikJezika.add(new Izbornik("hrvatski", "hr"));
        izbornikJezika.add(new Izbornik("engleski", "en"));
        izbornikJezika.add(new Izbornik("njemački", "de"));
    }

    /**
     * Creates a new instance of Lokalizacija
     */
    public Lokalizacija() {
    }

    public String getOdabraniJezik() {
        UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
        if (view != null) {
            Locale lokalniJezik = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            odabraniJezik = lokalniJezik.getLanguage();
        }
        return odabraniJezik;
    }

    public void setOdabraniJezik(String odabraniJezik) {
        this.odabraniJezik = odabraniJezik;
        Locale lokalniJezik = new Locale(odabraniJezik);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(lokalniJezik);
    }

    public ArrayList<Izbornik> getIzbornikJezika() {
        return izbornikJezika;
    }

    public Object odaberiJezik() {
        setOdabraniJezik(odabraniJezik);
        return "PromjenaJezika";
    }
    
    public String saljiPoruku(){
        return "saljiPoruku";
    }
    
    public String promjenaJezika(){
        return "promjenaJezika";
    }
    
    public String pregledPoruka(){
        return "pregledPoruka";
    }
}
