package one.leftshift.asteria.email.tasks


import one.leftshift.asteria.email.AsteriaEmailExtension
import one.leftshift.asteria.email.AsteriaEmailPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import javax.activation.DataHandler
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

import static javax.mail.Message.RecipientType.TO

class SendEmailTask extends DefaultTask {

    private boolean interruptOnError = false
    private String sender = ""
    private String recipients = ""
    private String subject = ""
    private String content = ""
    private String attachments = ""

    SendEmailTask() {
        group = AsteriaEmailPlugin.GROUP
        description = "Send email."
    }

    @Input
    boolean getInterruptOnError() {
        return interruptOnError
    }

    @Option(option = "interruptOnError", description = "Whether or not to interrupt gradle when an error occurs (default: false)")
    void setInterruptOnError(boolean interruptOnError) {
        this.interruptOnError = interruptOnError
    }

    @Input
    String getSender() {
        return sender
    }

    @Option(option = "sender", description = "Email sender e.g. Foo Bar <foo@bar.at>")
    void setSender(String sender) {
        this.sender = sender
    }

    @Input
    String getRecipients() {
        return recipients
    }

    @Option(option = "recipients", description = "Email recipients e.g. Foo Bar <foo@bar.at>")
    void setRecipients(String recipients) {
        this.recipients = recipients
    }

    @Input
    String getSubject() {
        return subject
    }

    @Option(option = "subject", description = "Subject of the email")
    void setSubject(String subject) {
        this.subject = subject
    }

    @Input
    String getContent() {
        return content
    }

    @Option(option = "content", description = "Content of the email")
    void setContent(String content) {
        this.content = content
    }

    @Input
    String getAttachments() {
        return attachments
    }

    @Option(option = "attachments", description = "Attachments of the email")
    void setAttachments(String attachments) {
        this.attachments = attachments
    }

    @TaskAction
    def sendEmail() {
        def extension = project.rootProject.extensions.findByType(AsteriaEmailExtension)

        Properties emailProperties = new Properties()
        emailProperties.put("mail.smtp.auth", extension.smptAuth)
        emailProperties.put("mail.smtp.starttls.enable", extension.smtpStartTlsEnable)
        emailProperties.put("mail.smtp.host", extension.smtpHost)
        emailProperties.put("mail.smtp.port", extension.smtpPort)
        emailProperties.put("mail.smtp.timeout", extension.smptTimeout)
        emailProperties.put("mail.smtp.connectiontimeout", extension.smptTimeout)
        emailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")

        Session session = Session.getInstance(emailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(extension.smptUser, extension.smptPassword)
            }
        })

        String emailSender = sender ?: extension.sender
        String emailRecipients = recipients ?: extension.recipients
        String emailSubject = subject ?: extension.subject
        String emailContent = content ?: extension.content

        Message message = new MimeMessage(session)
        message.from = new InternetAddress(emailSender)
        message.setRecipients(TO, InternetAddress.parse(emailRecipients))
        message.setSubject(emailSubject)
        MimeBodyPart mimeBodyPart = new MimeBodyPart()
        mimeBodyPart.setContent(emailContent, "text/plain; charset=UTF-8")
        Multipart multipart = new MimeMultipart()
        multipart.addBodyPart(mimeBodyPart)
        if (attachments) {
            attachments.split(",").each { attachmentPath ->
                File file = new File(attachmentPath.trim())
                if (!file.exists()) {
                    logger.warn("Attachment file ${attachmentPath} does not exist.")
                    return false
                }
                String fileExt = null
                int index = file.name.lastIndexOf('.')
                if (index >= 0) {
                    fileExt = file.name.substring(index + 1)
                }
                String mimeType = null
                switch (fileExt) {
                    case "pdf":
                        mimeType = "application/pdf"
                        break
                    case "jpg":
                        mimeType = "image/jpg"
                        break
                    case "svg":
                        mimeType = "image/svg+xml"
                        break
                    case "png":
                        mimeType = "image/png"
                        break
                    case "xml":
                        mimeType = "application/xml"
                        break
                    case "zip":
                        mimeType = "application/zip"
                        break
                    default:
                        throw new RuntimeException("Extension of file ${file.name} not supported")
                }
                MimeBodyPart attachmentPart = new MimeBodyPart()
                attachmentPart.dataHandler = new DataHandler(new ByteArrayDataSource(file.bytes, mimeType))
                attachmentPart.fileName = file.name
                multipart.addBodyPart(attachmentPart)
            }
        }
        message.setContent(multipart)
        project.logger.quiet("Sending email to ${emailRecipients}")
        try {
            Transport.send(message)
        } catch (MessagingException ex) {
            project.logger.error("Failure sending email", ex)
            if (interruptOnError) {
                throw ex
            }
        }
    }
}
