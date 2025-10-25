import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ForgotPassword")
public class ForgotPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        // Get email from request
        String email = request.getParameter("email");

        if (email == null || email.isEmpty()) {
            out.print("{\"error\": \"Email is required\"}");
            out.flush();
            return;
        }

        // Generate a random code
        String verificationCode = generateRandomCode();

        
        boolean emailSent = sendEmail(email, verificationCode);

        if (emailSent) {
            out.print("{\"code\": \"" + verificationCode + "\"}");
        } else {
            out.print("{\"error\": \"Failed to send email\"}");
        }

        out.flush();
    }

    // Generate a random code
    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); 
        return String.valueOf(code);
    }

    // Send an email with code
    private boolean sendEmail(String recipientEmail, String code) {
        final String senderEmail = "pasinduchamika2b@gmail.com"; 
        final String senderPassword = "sarl uwpw uzqw ouro"; 

        // Email properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Password Reset Code");
            message.setText("Your password reset code is: " + code);

            // Send email
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
