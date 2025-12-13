package com.example.demo.Services.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendRegistrationEmail(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject("Καλωσήρθες στην Rental App");
        message.setText(buildBody(username));

        mailSender.send(message);
    }

    private String buildBody(String username) {
        return "Γεια σου " + username + "!\n\n"
                + "Η εγγραφή σου στην εφαρμογή Rental App ολοκληρώθηκε με επιτυχία.\n"
                + "Μπορείς πλέον να συνδεθείς με τα στοιχεία σου.\n\n"
                + "Καλή συνέχεια,\n"
                + "Η ομάδα της Rental App";
    }

    public void sendActivationEmail(String to, String username, String token) {

        String activationLink = "http://localhost:8080/api/auth/activate?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject("Ενεργοποίηση λογαριασμού");
        message.setText(buildActivationBody(username, activationLink));

        mailSender.send(message);
    }

    private String buildActivationBody(String username, String link) {
        return "Γεια σου " + username + "!\n\n"
                + "Για να ενεργοποιήσεις τον λογαριασμό σου στην Rental App, "
                + "πάτησε στο παρακάτω link:\n\n"
                + link + "\n\n"
                + "Το link ισχύει για 24 ώρες.\n\n"
                + "Αν δεν έκανες εσύ την εγγραφή, αγνόησε αυτό το email.\n\n"
                + "Η ομάδα της Rental App";
    }
}