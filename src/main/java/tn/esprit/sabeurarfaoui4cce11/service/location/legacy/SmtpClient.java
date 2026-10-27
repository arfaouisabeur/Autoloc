package tn.esprit.sabeurarfaoui4cce11.service.location.legacy;

/**
 * Client SMTP legacy — fourni pour l'Atelier 6.
 * Simule l'envoi d'un e-mail.
 */
public class SmtpClient {

    public void send(String to, String subject, String body) {
        // Simulation : dans un vrai projet, appel à JavaMail ou Spring Mail
        System.out.println("[SMTP] Envoi à " + to
                + " | Sujet : " + subject
                + " | Corps : " + body);
    }
}
