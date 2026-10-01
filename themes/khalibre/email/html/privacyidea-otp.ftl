<#import "template.ftl" as layout>
<@layout.emailLayout>
<table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f9;padding:32px 12px;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">
  <tr>
    <td align="center">
      <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="max-width:520px;background-color:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #d7dae2;box-shadow:0 2px 8px rgba(16,42,67,0.06);">
        <tr>
          <td style="background-color:#2b3072;padding:20px 28px;">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0">
              <tr>
                <td style="font-size:18px;font-weight:600;color:#ffffff;letter-spacing:0.2px;">${msg("otpEmailBrand")}</td>
                <td align="right" style="font-size:12px;color:#ffffff;">${msg("otpEmailSecurityLabel")}</td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
          <td style="padding:28px 28px 8px 28px;">
            <p style="margin:0 0 6px 0;font-size:15px;color:#5b6478;">${msg("otpEmailHello", user.firstname?has_content?then(user.firstname, user.username))}</p>
            <p style="margin:0;font-size:20px;font-weight:600;color:#14162b;line-height:1.35;">${msg("otpEmailHeading")}</p>
          </td>
        </tr>
        <tr>
          <td style="padding:20px 28px 0 28px;">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#eef0fb;border:1px solid #b9bfee;border-radius:10px;">
              <tr>
                <td align="center" style="padding:20px 12px;">
                  <div style="font-size:11px;font-weight:600;letter-spacing:1.4px;color:#5b6478;text-transform:uppercase;padding-bottom:10px;">${msg("otpEmailCodeLabel")}</div>
                  <div style="font-size:38px;font-weight:700;letter-spacing:10px;color:#2b3072;font-family:'SF Mono',Menlo,Consolas,monospace;line-height:1.1;">${otp}</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
          <td style="padding:20px 28px 0 28px;">
            <p style="margin:0;font-size:14px;color:#5b6478;line-height:1.55;">${msg("otpEmailExpiry", expiryMinutes)}</p>
          </td>
        </tr>
        <tr>
          <td style="padding:24px 28px 28px 28px;">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="border-top:1px solid #d7dae2;">
              <tr>
                <td style="padding-top:18px;font-size:12px;color:#8b94a7;line-height:1.6;">${msg("otpEmailNotice")}</td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
      <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="max-width:520px;">
        <tr>
          <td align="center" style="padding-top:14px;font-size:11px;color:#8b94a7;">${msg("otpEmailFooter", realmName)}</td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</@layout.emailLayout>
