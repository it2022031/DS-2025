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

}