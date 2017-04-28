/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.jurbunic.web.zrna;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

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

    String poruka;

    public String getPoruka() {
        return poruka;
    }
    /**
     * Creates a new instance of SlanjePoruke
     */
    public SlanjePoruke() {

    }

    public String saljiPoruku() throws NoSuchProviderException {
        try {
            //TODO dodaj ovdje slanje poruke prema primjeru sa predavanja
            Session session = Session.getDefaultInstance(System.getProperties());
            MimeMessage message = new MimeMessage(session);            
            Address fromAddress = new InternetAddress(salje);
            message.setFrom(fromAddress);
            Address[] toAddresses = InternetAddress.parse(prima);
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setSentDate(new Date());
            message.setSubject(predmet);
            message.setText(sadrzaj);
            Transport.send(message);
            poruka = "Uspješno slanje emaila";
        } catch (AddressException ex) {
            poruka = "Neispravna adresa!";
            Logger.getLogger(SlanjePoruke.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            poruka = "Pogreška kod slanja!";
            Logger.getLogger(SlanjePoruke.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "PoslanaPoruka";
    }

    public String promjenaJezika() {
        return "promjenaJezika";
    }

    public String pregledPoruka() {
        return "pregledPoruka";
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
