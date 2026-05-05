package com.supermarket.servlet.admin;

import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/** SupplierManageServlet - 供应商管理 */
public class SupplierManageServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        json(req, resp, new ArrayList<>());
    }
}
