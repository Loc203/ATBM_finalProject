package controller.auth;

import Utils.ValidationUtils;
import dao.EmailDAO;
import dao.LoggingLogin;
import dao.UserDAO;
import model.Account;
import service.PasswordResetService;
import service.UserService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/login"})
public class LoginController extends HttpServlet {
    @Inject
    private UserService userService;
    EmailDAO emailDAO = new EmailDAO();
    String emailAccount = "";
    PasswordResetService passwordResetService = new PasswordResetService(emailDAO, emailAccount);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LoggingLogin loggingLogin = new LoggingLogin();
        String ipRemote = request.getRemoteAddr();
        int count = loggingLogin.countLogging_Login(ipRemote);
        System.out.print(count);
        request.getRequestDispatcher("/view/login.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        emailAccount = email;
        String password = request.getParameter("password");
        UserDAO userDAO = new UserDAO();
        int isActive = userDAO.checkIsActive(email);
        List<String> error = new ArrayList<>();
//        String country = request.getParameter("myAddress");

        if (!ValidationUtils.isValidEmail(email)) {
            error.add("Email không hợp lệ ");
        }
        if (!ValidationUtils.isValidPassword(password)) {
            error.add("Nhập mật khẩu ít nhất 1 chữ hoa, 1 chữ thường, 1 kí tự đặc biệt @,#,% ... , 1 số");
        }
        Account user = userService.findUserByEmailAndPassword(email, password);
        LoggingLogin loggingLogin = new LoggingLogin();
        String ipUser = request.getRemoteAddr();
        String country = loggingLogin.getCountryFromIP(ipUser);
        if (error.isEmpty()){
            int count = loggingLogin.countLogging_Login(ipUser);
            if (count >= 5) {
                request.setAttribute("notLogin", "Ip của bạn đã bị band do đăng nhập sai quá nhiều lần, hãy quay lại sau 30 phút");
                request.getRequestDispatcher("/view/login.jsp").forward(request, response);
                return;
            }
            if (user == null) {
                request.setAttribute("error", "Email hoặc mật khẩu không đúng");
                request.getRequestDispatcher("/view/login.jsp").forward(request, response);
                System.out.print(count);

            } else if (user.getIs_active() == 0) {
                passwordResetService.createAndSendResetCode(emailAccount);
                request.getSession().setAttribute("userEmailRegister", email);
                response.sendRedirect("/view/enter_Code_Register.jsp");
            } else if (userDAO.banUser(email) == 1) {
                request.setAttribute("error", "Tài khoản của bạn đã bị khóa");
                request.getRequestDispatcher("/view/login.jsp").forward(request, response);
        } else {
                userDAO.updateIPAndCountry(ipUser, country, email);
                request.getSession().setAttribute("account", user);
                request.getSession().setAttribute("nameAccount", user.getFirst_name() + " " + user.getLast_name());
                request.getSession().setAttribute("userEmailLogin", user.getEmail());
                if ("admin".equals(user.getRole())) {
                    request.getSession().setAttribute("message", "Đăng nhập thành công");
                    request.getSession().setAttribute("status", true);
                    response.sendRedirect(request.getContextPath() + "/admin/revenue-statistics");
                }else if ("manage".equals(user.getRole())) {
                    request.getSession().setAttribute("message", "Đăng nhập thành công");
                    request.getSession().setAttribute("status", true);
                    response.sendRedirect(request.getContextPath() + "/admin/revenue-statistics");
                } else {
                        request.getSession().setAttribute("message", "Đăng nhập thành công");
                    request.getSession().setAttribute("status", true);
                    response.sendRedirect(request.getContextPath() + "/home");
                }
            }
        }else{
            int count = loggingLogin.countLogging_Login(ipUser);
            if (count >= 5) {
                request.setAttribute("notLogin", "IP của bạn đã bị band do đăng nhập sai quá nhiều lần, hãy quay lại sau 30 phút");
                request.setAttribute("status", false); // Đảm bảo status phản ánh tình trạng này
                request.setAttribute("errors", error);
                request.getRequestDispatcher("/view/login.jsp").forward(request, response);
            }else{
                request.setAttribute("errors", error);
                request.getRequestDispatcher("/view/login.jsp").forward(request, response);
            }
            System.out.print(count);
        }
        LoggingLogin.Logging_Login(request, email, UserDAO.checkLogin(email, password));
    }

}
