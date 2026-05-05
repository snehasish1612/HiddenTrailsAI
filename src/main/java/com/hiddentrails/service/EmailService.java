package com.hiddentrails.service;

import com.hiddentrails.model.Booking;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EmailService — sends transactional emails via SMTP (Gmail or any provider).
 *
 * Config in app.properties:
 *   smtp.host     = smtp.gmail.com
 *   smtp.port     = 587
 *   smtp.username = your@gmail.com
 *   smtp.password = your-app-password
 *   smtp.from     = Hidden Trails AI <noreply@hiddentrails.com>
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    private final String  smtpHost;
    private final String  smtpPort;
    private final String  smtpUser;
    private final String  smtpPass;
    private final String  fromAddress;
    private final boolean enabled;

    public EmailService() {
        try (InputStream in = EmailService.class
                .getClassLoader()
                .getResourceAsStream("app.properties")) {

            Properties props = new Properties();
            if (in != null) props.load(in);

            this.smtpHost    = props.getProperty("smtp.host",     "smtp.gmail.com");
            this.smtpPort    = props.getProperty("smtp.port",     "587");
            this.smtpUser    = props.getProperty("smtp.username", "");
            this.smtpPass    = props.getProperty("smtp.password", "");
            this.fromAddress = props.getProperty("smtp.from",
                               "Hidden Trails AI <noreply@hiddentrails.com>");
            // Disable email if credentials not configured
            this.enabled     = !smtpUser.isBlank() && !smtpPass.isBlank();

        } catch (IOException e) {
            throw new RuntimeException("Cannot load app.properties for EmailService", e);
        }
    }

    /**
     * Sends a booking confirmation email to the user.
     *
     * @param toEmail      recipient email address
     * @param userName     recipient name
     * @param booking      the confirmed Booking object
     * @param destination  trip destination string
     * @param startDate    trip start date string
     * @param endDate      trip end date string
     */
    public void sendBookingConfirmation(String toEmail,
                                        String userName,
                                        Booking booking,
                                        String destination,
                                        String startDate,
                                        String endDate) {
        if (!enabled) {
            LOGGER.warning("Email not configured — skipping confirmation for "
                           + toEmail);
            return;
        }

        try {
            Session session = buildSession();
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail));
            message.setSubject(
                "Booking Confirmed! ✈ " + destination
                + " | " + booking.getConfirmationNo());
            message.setContent(
                buildHtmlBody(userName, booking, destination, startDate, endDate),
                "text/html; charset=utf-8");

            Transport.send(message);
            LOGGER.info("Confirmation email sent to " + toEmail
                        + " for booking " + booking.getConfirmationNo());

        } catch (MessagingException e) {
            // Non-fatal — log but don't throw; booking is already confirmed
            LOGGER.log(Level.WARNING,
                "Failed to send confirmation email to " + toEmail, e);
        }
    }

    // ── Build SMTP Session with TLS ───────────────────────────────
    private Session buildSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            smtpHost);
        props.put("mail.smtp.port",            smtpPort);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPass);
            }
        });
    }

    // ── Build HTML email body ─────────────────────────────────────
    private String buildHtmlBody(String userName, Booking booking,
                                  String destination,
                                  String startDate, String endDate) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <style>
                body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                .container { max-width: 600px; margin: 30px auto; background: #fff;
                             border-radius: 10px; overflow: hidden; }
                .header { background: #1D9E75; padding: 24px; text-align: center; color: #fff; }
                .header h1 { margin: 0; font-size: 22px; }
                .body { padding: 28px 32px; }
                .detail-row { display: flex; justify-content: space-between;
                              padding: 8px 0; border-bottom: 1px solid #eee; }
                .label { color: #666; font-size: 13px; }
                .value { font-weight: bold; color: #222; font-size: 13px; }
                .confirm-box { background: #E1F5EE; border-radius: 8px;
                               padding: 16px; text-align: center; margin: 20px 0; }
                .confirm-no { font-size: 22px; font-weight: bold;
                              color: #085041; letter-spacing: 2px; }
                .footer { background: #f9f9f9; padding: 16px; text-align: center;
                          font-size: 12px; color: #999; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>🏔 Hidden Trails AI</h1>
                  <p style="margin:6px 0 0">Your trip is confirmed!</p>
                </div>
                <div class="body">
                  <p>Dear <strong>%s</strong>,</p>
                  <p>We're excited to confirm your trip to <strong>%s</strong>.
                     Here are your booking details:</p>

                  <div class="confirm-box">
                    <div style="font-size:13px;color:#085041;margin-bottom:6px">
                      Confirmation Number
                    </div>
                    <div class="confirm-no">%s</div>
                  </div>

                  <div class="detail-row">
                    <span class="label">Destination</span>
                    <span class="value">%s</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Travel Dates</span>
                    <span class="value">%s → %s</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Total Amount Paid</span>
                    <span class="value">₹%s</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Payment Method</span>
                    <span class="value">%s</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Booking Status</span>
                    <span class="value" style="color:#1D9E75">✓ Confirmed</span>
                  </div>

                  <p style="margin-top:24px;color:#555;font-size:13px">
                    Log in to your Hidden Trails AI dashboard to view your full
                    day-by-day itinerary, make any changes, or contact support.
                  </p>
                </div>
                <div class="footer">
                  Hidden Trails AI · North Bengal & Sikkim Travel Planner<br>
                  This is an automated email. Please do not reply.
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                userName,
                destination,
                booking.getConfirmationNo(),
                destination,
                startDate, endDate,
                booking.getTotalPrice().toPlainString(),
                booking.getPaymentMethod()
            );
    }
}