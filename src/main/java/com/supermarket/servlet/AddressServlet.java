package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.AddressDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AddressServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) { response.sendRedirect(request.getContextPath() + "/login.jsp"); return; }

        String action = request.getParameter("action");
        AddressDAO dao = new AddressDAO();

        if ("add".equals(action)) {
            String receiver = request.getParameter("receiver");
            String phone    = request.getParameter("phone");
            String detail   = request.getParameter("detail");
            dao.add(user.getUserId(), receiver, phone, detail);
            response.sendRedirect(request.getContextPath() + "/user/address.jsp?msg=added");

        } else if ("delete".equals(action)) {
            int addressId = Integer.parseInt(request.getParameter("addressId"));
            dao.delete(addressId, user.getUserId());
            response.sendRedirect(request.getContextPath() + "/user/address.jsp?msg=deleted");

        } else if ("setDefault".equals(action)) {
            int addressId = Integer.parseInt(request.getParameter("addressId"));
            dao.setDefault(addressId, user.getUserId());
            response.sendRedirect(request.getContextPath() + "/user/address.jsp");
        }
    }
}
