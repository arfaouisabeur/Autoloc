package tn.esprit.sabeurarfaoui4cce11.service.location.legacy;

/**
 * Passerelle SMS legacy — fournie pour l'Atelier 6.
 * Simule l'envoi d'un SMS.
 */
public class SmsGateway {

    public void send(String phone, String message) {
        // Simulation : dans un vrai projet, appel à une API SMS (Twilio, Nexmo…)
        System.out.println("[SMS] Envoi au " + phone
                + " | Message : " + message);
    }
}
