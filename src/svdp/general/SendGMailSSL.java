package svdp.general;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendGMailSSL
{
	public static void send( String eMail, String messageTitle, String messageText ) 
	{
        final String username = "biz.decode.dev@gmail.com";
        final String password = "Jizqy8-fiqruv-fakriq";

        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
        Session session = Session.getInstance(prop, new javax.mail.Authenticator() 
        {
            protected PasswordAuthentication getPasswordAuthentication() 
            {
                return new PasswordAuthentication(username, password);
            }
        });

        try 
        {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("biz.decode.dev@gmail.com"));
            message.setRecipients(  Message.RecipientType.TO, InternetAddress.parse(eMail) );
            message.setSubject( messageTitle );
            message.setText( messageText );

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
