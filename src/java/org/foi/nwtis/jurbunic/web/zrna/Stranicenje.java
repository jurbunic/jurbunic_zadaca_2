/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.zrna;

/**
 *
 * @author Jurica Bunić
 */
public class Stranicenje {
    
    private static Stranicenje stranicenje;
    
    private int brojStavkaPoStrinici;
    
    private int pozicijaOd=0;
    private int pozicijaDo=0;
    
    private Stranicenje(int brojStavkaPoStrinici){
        this.brojStavkaPoStrinici = brojStavkaPoStrinici;
    }
    
    public static Stranicenje getInstance(int brojStavkaPoStrinici){
        if(stranicenje==null){
            stranicenje = new Stranicenje(brojStavkaPoStrinici);
        }
        return stranicenje;
    }
    
    public int getPozicijaOdNaprijed() {
        pozicijaOd = pozicijaDo;
        return pozicijaOd;
    }

    public int getPozicijaDoNaprijed() {
        pozicijaDo += brojStavkaPoStrinici;
        return pozicijaDo;
    }
    
    public int getPozicijaOdNatrag(){
        pozicijaOd = pozicijaDo - brojStavkaPoStrinici;
        return pozicijaOd;
    }
    
    public int getPozicijaDoNatrag(){
        pozicijaDo = pozicijaDo - brojStavkaPoStrinici;
        return pozicijaDo; 
    }

}
