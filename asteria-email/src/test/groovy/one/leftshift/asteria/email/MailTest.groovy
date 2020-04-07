/*
 * Copyright (c) 2016-2019, Leftshift One
 * __________________
 * [2019] Leftshift One
 * All Rights Reserved.
 * NOTICE:  All information contained herein is, and remains
 * the property of Leftshift One and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Leftshift One
 * and its suppliers and may be covered by Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Leftshift One.
 */

package one.leftshift.asteria.email

import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

import static javax.mail.Message.RecipientType.TO

/**
 * Usage information: You are required to set the @link AsteriaEmailExtension
 * settings to make use of this test.
 */
class MailTest {

    static void main(String[] args) {
        def extension = new AsteriaEmailExtension()
        def recipients = ""
        def subject = "Test"
        def content = "Test"

        Properties emailProperties = new Properties()
        emailProperties.put("mail.debug", true)
        emailProperties.put("mail.smtp.auth", extension.smptAuth)
        emailProperties.put("mail.smtp.starttls.enable", extension.smtpStartTlsEnable)
        emailProperties.put("mail.smtp.host", extension.smtpHost)
        emailProperties.put("mail.smtp.port", extension.smtpPort)
        emailProperties.put("mail.smtp.timeout", 15000)
        emailProperties.put("mail.smtp.connectiontimeout", 15000)
        emailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")

        Session session = Session.getInstance(emailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(extension.smptUser, extension.smptPassword)
            }
        })

        String emailRecipients = recipients ?: extension.recipients
        String emailSubject = subject ?: extension.subject
        String emailContent = content ?: extension.content

        Message message = new MimeMessage(session)
        message.from = new InternetAddress(extension.sender)
        message.setRecipients(TO, InternetAddress.parse(emailRecipients))
        message.setSubject(emailSubject)
        MimeBodyPart mimeBodyPart = new MimeBodyPart()
        mimeBodyPart.setContent(emailContent, "text/plain; charset=UTF-8")
        Multipart multipart = new MimeMultipart()
        multipart.addBodyPart(mimeBodyPart)
        message.setContent(multipart)

        println "Sending email to ${recipients}"
        Transport.send(message)
    }
}
