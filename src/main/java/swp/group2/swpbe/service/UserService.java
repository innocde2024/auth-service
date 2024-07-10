package swp.group2.swpbe.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import swp.group2.swpbe.constant.Gender;
import swp.group2.swpbe.constant.MailType;
import swp.group2.swpbe.constant.State;
import swp.group2.swpbe.dto.ChangePasswordDTO;
import swp.group2.swpbe.dto.LoginDTO;
import swp.group2.swpbe.dto.LoginSocialDTO;
import swp.group2.swpbe.dto.ResetPasswordDTO;
import swp.group2.swpbe.dto.SignupDTO;
import swp.group2.swpbe.dto.UpdatePasswordDTO;
import swp.group2.swpbe.dto.UpdateProfileDTO;
import swp.group2.swpbe.entities.User;
import swp.group2.swpbe.exception.ApiRequestException;
import swp.group2.swpbe.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JavaMailSender emailSender;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, JavaMailSender emailSender, JwtService jwtService) {
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.jwtService = jwtService;
    }

    int strength = 10;
    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(strength, new SecureRandom());

    @Value("${allow.origin}")
    private String allowedOrigins;

    public User signup(SignupDTO user) {
        String fullName = user.getFullName();
        String email = user.getEmail().toLowerCase();
        String password = bCryptPasswordEncoder.encode(user.getPassword());
        if (userRepository.findByEmail(email) != null) {
            throw new ApiRequestException("Email already exist", HttpStatus.BAD_REQUEST);
        }
        try {
            String htmlContent = this.getMailTemplate("Tap the button below to confirm your email address",
                    "Verify", email, MailType.VERIFY);
            this.sendMail(email, "Verify email", htmlContent);
        } catch (Exception e) {
            throw new ApiRequestException("Failed to send mail", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User newUser = userRepository.save(new User(fullName, email, password, State.PENDING));
        newUser.setPassword("");
        return newUser;
    }

    public void reverify(String email) {
        try {
            String htmlContent = this.getMailTemplate("Tap the button below to confirm your email address",
                    "Verify", email, MailType.VERIFY);
            this.sendMail(email, "Verify email", htmlContent);
        } catch (Exception e) {
            throw new ApiRequestException("Failed to send mail", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void sendMail(
            String email, String subject, String html) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("huyhkds170542@fpt.edu.vn");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(html, true);

        emailSender.send(message);

    }

    public User updateVerifyEmail(String token) {
        String email = "";
        try {
            email = jwtService.verifyVToken(token);
        } catch (Exception e) {
            throw new ApiRequestException("Invalid token", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findByEmail(email);
        user.setState(State.ACTIVE);
        user.setUpdatedAt(new Date());
        return userRepository.save(user);
    }

    public void forgotPassword(ResetPasswordDTO body) {
        String email = body.getEmail().toLowerCase();
        String html = this.getMailTemplate("Click here to reset password", "Reset password", email,
                MailType.FORGOT_PASSWORD);
        try {
            this.sendMail(email, "Reset password", html);
        } catch (Exception e) {
            throw new ApiRequestException("Can't send email", HttpStatus.BAD_REQUEST);
        }
    }

    public void changePassword(ChangePasswordDTO body) {
        String email = "";
        try {
            email = jwtService.verifyToken(body.getToken()).getEmail();
        } catch (Exception e) {
            throw new ApiRequestException("Invalid token!", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ApiRequestException("User not found!", HttpStatus.BAD_REQUEST);
        }
        user.setUpdatedAt(new Date());
        user.setPassword(bCryptPasswordEncoder.encode(body.getNewPassword()));
        userRepository.save(user);

    }

    public User saveSocialUser(LoginSocialDTO user) {
        User userExist = userRepository.findByEmail(user.getEmail());
        if (userExist != null) {
            if (userExist.getState() == State.BLOCKED) {
                throw new ApiRequestException("Your account is blocked",
                        HttpStatus.BAD_REQUEST);
            }
            return userExist;
        }
        User newUser = new User(user.getName(), user.getEmail().toLowerCase(), null, State.ACTIVE);
        newUser.setAvatarUrl(user.getPicture());
        return userRepository.save(newUser);
    }

    public User login(LoginDTO body) {
        String email = body.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ApiRequestException("Email not found", HttpStatus.BAD_REQUEST);
        }
        if (user.getState().equals(State.PENDING)) {
            throw new ApiRequestException("not_verify_yet", HttpStatus.BAD_REQUEST);
        }
        if (user.getState().equals(State.BLOCKED)) {
            throw new ApiRequestException("Your account is blocked", HttpStatus.BAD_REQUEST);
        }
        String password = body.getPassword();
        boolean isCorrectPassword = bCryptPasswordEncoder.matches(password, user.getPassword());
        if (!isCorrectPassword) {
            throw new ApiRequestException("Wrong password", HttpStatus.BAD_REQUEST);
        }
        return user;
    }

    public User getUserProfile(String id) {
        return userRepository.findById(Integer.parseInt(id));

    }

    public void updatePassword(UpdatePasswordDTO body, String userId) {
        String newPassword = body.getNewPassword();
        String oldPassword = body.getOldPassword();
        User user = userRepository.findById(Integer.parseInt(userId));
        if (user == null) {
            throw new ApiRequestException("User not found", HttpStatus.BAD_REQUEST);
        }
        if (!bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ApiRequestException("old password wrong", HttpStatus.BAD_REQUEST);
        }
        user.setUpdatedAt(new Date());
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);

    }

    public void updateAvatar(String url, String userId) {
        User user = userRepository.findById(Integer.parseInt(userId));
        user.setAvatarUrl(url);
        user.setUpdatedAt(new Date());
        userRepository.save(user);
    }

    public void updateProfile(UpdateProfileDTO body, String userId) {
        String fullName = body.getFullName();
        String about = body.getAbout();
        Date dob = body.getDob();
        Gender gender = body.getGender();
        User user = userRepository.findById(Integer.parseInt(userId));
        user.setFullName(fullName);
        user.setAbout(about);
        user.setDob(dob);
        user.setGender(gender);
        userRepository.save(user);

    }

    public String getMailTemplate(String message, String buttonTitle, String email, MailType type) {
        String token;
        String url;
        try {
            token = URLEncoder.encode(jwtService.generateVerifyToken(email), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new ApiRequestException("Server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        switch (type) {
            case VERIFY:
                // url = allowedOrigins + ":8080/verify?token=" + token;
                url = "http://3.27.151.233:8080/auth/verify?token=" + token;
                break;
            case FORGOT_PASSWORD:
                url = allowedOrigins + "/reset-password?token=" + token;
                break;
            default:
                throw new ApiRequestException("Invalid mail type", HttpStatus.BAD_REQUEST);
        }
        return " <!DOCTYPE html>\n" + //
                "<html>\n" + //
                "<head>\n" + //
                "\n" + //
                "  <meta charset=\"utf-8\">\n" + //
                "  <meta http-equiv=\"x-ua-compatible\" content=\"ie=edge\">\n" + //
                "  <title>Email Confirmation</title>\n" + //
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" + //
                "  <style type=\"text/css\">\n" + //
                "  /**\n" + //
                "   * Google webfonts. Recommended to include the .woff version for cross-client compatibility.\n" + //
                "   */\n" + //
                "  @media screen {\n" + //
                "    @font-face {\n" + //
                "      font-family: 'Source Sans Pro';\n" + //
                "      font-style: normal;\n" + //
                "      font-weight: 400;\n" + //
                "      src: local('Source Sans Pro Regular'), local('SourceSansPro-Regular'), url(https://fonts.gstatic.com/s/sourcesanspro/v10/ODelI1aHBYDBqgeIAH2zlBM0YzuT7MdOe03otPbuUS0.woff) format('woff');\n"
                + //
                "    }\n" + //
                "    @font-face {\n" + //
                "      font-family: 'Source Sans Pro';\n" + //
                "      font-style: normal;\n" + //
                "      font-weight: 700;\n" + //
                "      src: local('Source Sans Pro Bold'), local('SourceSansPro-Bold'), url(https://fonts.gstatic.com/s/sourcesanspro/v10/toadOcfmlt9b38dHJxOBGFkQc6VGVFSmCnC_l7QZG60.woff) format('woff');\n"
                + //
                "    }\n" + //
                "  }\n" + //
                "  /**\n" + //
                "   * Avoid browser level font resizing.\n" + //
                "   * 1. Windows Mobile\n" + //
                "   * 2. iOS / OSX\n" + //
                "   */\n" + //
                "  body,\n" + //
                "  table,\n" + //
                "  td,\n" + //
                "  a {\n" + //
                "    -ms-text-size-adjust: 100%; /* 1 */\n" + //
                "    -webkit-text-size-adjust: 100%; /* 2 */\n" + //
                "  }\n" + //
                "  /**\n" + //
                "   * Remove extra space added to tables and cells in Outlook.\n" + //
                "   */\n" + //
                "  table,\n" + //
                "  td {\n" + //
                "    mso-table-rspace: 0pt;\n" + //
                "    mso-table-lspace: 0pt;\n" + //
                "  }\n" + //
                "  /**\n" + //
                "   * Better fluid images in Internet Explorer.\n" + //
                "   */\n" + //
                "  img {\n" + //
                "    -ms-interpolation-mode: bicubic;\n" + //
                "  }\n" + //
                "  /**\n" + //
                "   * Remove blue links for iOS devices.\n" + //
                "   */\n" + //
                "  a[x-apple-data-detectors] {\n" + //
                "    font-family: inherit !important;\n" + //
                "    font-size: inherit !important;\n" + //
                "    font-weight: inherit !important;\n" + //
                "    line-height: inherit !important;\n" + //
                "    color: inherit !important;\n" + //
                "    text-decoration: none !important;\n" + //
                "  }\n" + //
                "  /**\n" + //
                "   * Fix centering issues in Android 4.4.\n" + //
                "   */\n" + //
                "  div[style*=\"margin: 16px 0;\"] {\n" + //
                "    margin: 0 !important;\n" + //
                "  }\n" + //
                "  body {\n" + //
                "    width: 100% !important;\n" + //
                "    height: 100% !important;\n" + //
                "    padding: 0 !important;\n" + //
                "    margin: 0 !important;\n" + //
                "  }\n" + //
                "  /**\n" + //
                "   * Collapse table borders to avoid space between cells.\n" + //
                "   */\n" + //
                "  table {\n" + //
                "    border-collapse: collapse !important;\n" + //
                "  }\n" + //
                "  a {\n" + //
                "    color: #1a82e2;\n" + //
                "  }\n" + //
                "  img {\n" + //
                "    height: auto;\n" + //
                "    line-height: 100%;\n" + //
                "    text-decoration: none;\n" + //
                "    border: 0;\n" + //
                "    outline: none;\n" + //
                "  }\n" + //
                "  </style>\n" + //
                "\n" + //
                "</head>\n" + //
                "<body style=\"background-color: #e9ecef;\">\n" + //
                "\n" + //
                "  <!-- start preheader -->\n" + //
                "  <div class=\"preheader\" style=\"display: none; max-width: 0; max-height: 0; overflow: hidden; font-size: 1px; line-height: 1px; color: #fff; opacity: 0;\">\n"
                + //
                "    A preheader is the short summary text that follows the subject line when an email is viewed in the inbox.\n"
                + //
                "  </div>\n" + //
                "  <!-- end preheader -->\n" + //
                "\n" + //
                "  <!-- start body -->\n" + //
                "  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" + //
                "\n" + //
                "    <!-- start logo -->\n" + //
                "    <tr>\n" + //
                "      <td align=\"center\" bgcolor=\"#e9ecef\">\n" + //
                "        <!--[if (gte mso 9)|(IE)]>\n" + //
                "        <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\">\n" + //
                "        <tr>\n" + //
                "        <td align=\"center\" valign=\"top\" width=\"600\">\n" + //
                "        <![endif]-->\n" + //
                "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n"
                + //
                "          <tr>\n" + //
                "            <td align=\"center\" valign=\"top\" style=\"padding: 36px 24px;\">\n" + //
                "              <a href=\"https://www.blogdesire.com\" target=\"_blank\" style=\"display: inline-block;\">\n"
                + //
                "                <img src=\"https://www.blogdesire.com/wp-content/uploads/2019/07/blogdesire-1.png\" alt=\"Logo\" border=\"0\" width=\"48\" style=\"display: block; width: 48px; max-width: 48px; min-width: 48px;\">\n"
                + //
                "              </a>\n" + //
                "            </td>\n" + //
                "          </tr>\n" + //
                "        </table>\n" + //
                "        <!--[if (gte mso 9)|(IE)]>\n" + //
                "        </td>\n" + //
                "        </tr>\n" + //
                "        </table>\n" + //
                "        <![endif]-->\n" + //
                "      </td>\n" + //
                "    </tr>\n" + //
                "    <!-- end logo -->\n" + //
                "\n" + //
                "    <!-- start hero -->\n" + //
                "    <tr>\n" + //
                "      <td align=\"center\" bgcolor=\"#e9ecef\">\n" + //
                "        <!--[if (gte mso 9)|(IE)]>\n" + //
                "        <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\">\n" + //
                "        <tr>\n" + //
                "        <td align=\"center\" valign=\"top\" width=\"600\">\n" + //
                "        <![endif]-->\n" + //
                "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n"
                + //
                "          <tr>\n" + //
                "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 36px 24px 0; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; border-top: 3px solid #d4dadf;\">\n"
                + //
                "              <h1 style=\"margin: 0; font-size: 32px; font-weight: 700; letter-spacing: -1px; line-height: 48px;\">Confirm Your Email Address</h1>\n"
                + //
                "            </td>\n" + //
                "          </tr>\n" + //
                "        </table>\n" + //
                "        <!--[if (gte mso 9)|(IE)]>\n" + //
                "        </td>\n" + //
                "        </tr>\n" + //
                "        </table>\n" + //
                "        <![endif]-->\n" + //
                "      </td>\n" + //
                "    </tr>\n" + //
                "    <!-- end hero -->\n" + //
                "\n" + //
                "    <!-- start copy block -->\n" + //
                "    <tr>\n" + //
                "      <td align=\"center\" bgcolor=\"#e9ecef\">\n" + //
                "        <!--[if (gte mso 9)|(IE)]>\n" + //
                "        <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\">\n" + //
                "        <tr>\n" + //
                "        <td align=\"center\" valign=\"top\" width=\"600\">\n" + //
                "        <![endif]-->\n" + //
                "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n"
                + //
                "\n" + //
                "          <!-- start copy -->\n" + //
                "          <tr>\n" + //
                "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; line-height: 24px;\">\n"
                + //
                "              <p style=\"margin: 0;\">" + message + "</p>\n" + //
                "            </td>\n" + //
                "          </tr>\n" + //
                "          <!-- end copy -->\n" + //
                "\n" + //
                "          <!-- start button -->\n" + //
                "          <tr>\n" + //
                "            <td align=\"left\" bgcolor=\"#ffffff\">\n" + //
                "              <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" + //
                "                <tr>\n" + //
                "                  <td align=\"center\" bgcolor=\"#ffffff\" style=\"padding: 12px;\">\n" + //
                "                    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" + //
                "                      <tr>\n" + //
                "                        <td align=\"center\" bgcolor=\"#1a82e2\" style=\"border-radius: 6px;\">\n" + //
                "                          <a href=" + url
                + "  target=\"_blank\" style=\"display: inline-block; padding: 16px 36px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; border-radius: 6px;\">"
                + buttonTitle + "</a>\n" + //
                "                        </td>\n" + //
                "                      </tr>\n" + //
                "                    </table>\n" + //
                "                  </td>\n" + //
                "                </tr>\n" + //
                "              </table>\n" + //
                "            </td>\n" + //
                "          </tr>\n" + //
                "          <!-- end button -->\n" + //
                "\n" + //
                "          <!-- start copy -->\n" + //
                "          <tr>\n" + //
                "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; line-height: 24px;\">\n"
                + //
                "              <p style=\"margin: 0;\">This verify wil be expired in 5 minutes. If you can't verify, please login with your account we will send you a verify email again</p>\n"
                + //
                "            </td>\n" + //
                "          </tr>\n" + //
                "          <!-- end copy -->\n" + //
                "\n" + //
                "          <!-- start copy -->\n" + //
                "          <tr>\n" + //
                "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; line-height: 24px; border-bottom: 3px solid #d4dadf\">\n"
                + //
                "              <p style=\"margin: 0;\">Cheers,<br> Paste</p>\n" + //
                "            </td>\n" + //
                "          </tr>\n" + //
                "          <!-- end copy -->\n" + //
                "\n" + //
                "        </table>\n" + //
                "        <!--[if (gte mso 9)|(IE)]>\n" + //
                "        </td>\n" + //
                "        </tr>\n" + //
                "        </table>\n" + //
                "        <![endif]-->\n" + //
                "      </td>\n" + //
                "    </tr>\n" + //
                "    <!-- end copy block -->\n" + //
                "\n" + //
                "    <!-- start footer -->\n" + //
                "    <tr>\n" + //
                "      <td align=\"center\" bgcolor=\"#e9ecef\" style=\"padding: 24px;\">\n" + //
                "        <!--[if (gte mso 9)|(IE)]>\n" + //
                "        <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\">\n" + //
                "        <tr>\n" + //
                "        <td align=\"center\" valign=\"top\" width=\"600\">\n" + //
                "        <![endif]-->\n" + //
                "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n"
                + //
                "\n" + //
                "          <!-- start permission -->\n" + //
                "          <tr>\n" + //
                "            <td align=\"center\" bgcolor=\"#e9ecef\" style=\"padding: 12px 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 14px; line-height: 20px; color: #666;\">\n"
                + //
                "              <p style=\"margin: 0;\">You received this email because we received a request at FU records need. We need to verify your email. If you didn't request you can safely delete this email.</p>\n"
                + //
                "            </td>\n" + //
                "          </tr>\n" + //
                "          <!-- end permission -->\n" + //
                "\n" + //
                "          <!-- start unsubscribe -->\n" + //
                "          <!-- end unsubscribe -->\n" + //
                "\n" + //
                "        </table>\n" + //
                "        <!--[if (gte mso 9)|(IE)]>\n" + //
                "        </td>\n" + //
                "        </tr>\n" + //
                "        </table>\n" + //
                "        <![endif]-->\n" + //
                "      </td>\n" + //
                "    </tr>\n" + //
                "    <!-- end footer -->\n" + //
                "\n" + //
                "  </table>\n" + //
                "  <!-- end body -->\n" + //
                "\n" + //
                "</body>\n" + //
                "</html>";
    }
}
