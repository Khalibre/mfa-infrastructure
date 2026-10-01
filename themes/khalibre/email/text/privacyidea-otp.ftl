<#ftl output_format="plainText">
${msg("otpEmailBrand")} — ${msg("otpEmailSecurityLabel")}

${msg("otpEmailHello", user.firstname?has_content?then(user.firstname, user.username))}

${msg("otpEmailHeading")}

    ${msg("otpEmailCodeLabel")}: ${otp}

${msg("otpEmailExpiry", expiryMinutes)}

${msg("otpEmailNotice")}

${msg("otpEmailFooter", realmName)}
