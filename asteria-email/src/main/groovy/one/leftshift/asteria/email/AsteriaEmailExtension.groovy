package one.leftshift.asteria.email

class AsteriaEmailExtension {

    Boolean smptAuth = true
    Boolean smtpStartTlsEnable = true
    String smtpHost = ""
    Integer smtpPort = 465
    String smptTimeout = 15000
    String smptUser = ""
    String smptPassword = ""

    String sender = ""
    String recipients
    String subject
    String content
}
