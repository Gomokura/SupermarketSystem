package com.supermarket.servlet.admin;

import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** InventoryServlet - 库存管理 */
public class InventoryServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        json(req, resp, new java.util.HashMap<>());
    }
}
