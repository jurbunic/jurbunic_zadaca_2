/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.slusaci;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.jurbunic.konfiguracije.Konfiguracija;
import org.foi.nwtis.jurbunic.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.jurbunic.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.jurbunic.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.jurbunic.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.jurbunic.web.dretve.ObradaPoruka;

/**
 * Web application lifecycle listener.
 *
 * @author grupa_1
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {

    ObradaPoruka op;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String datoteka = sce.getServletContext().getRealPath("/WEB-INF")
                + File.separator + sce.getServletContext().getInitParameter("konfiguracija");

        BP_Konfiguracija bpkonf = new BP_Konfiguracija(datoteka);
        sce.getServletContext().setAttribute("BP_Konfig", bpkonf);
        Konfiguracija konf = null;
        try {
            konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
        sce.getServletContext().setAttribute("Mail_Konfig", konf);
        op = new ObradaPoruka();
        op.setSc(sce.getServletContext());
        op.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (op != null) {
            op.interrupt();
        }
    }
}
