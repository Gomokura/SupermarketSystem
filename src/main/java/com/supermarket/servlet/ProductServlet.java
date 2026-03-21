package com.supermarket.servlet;

import com.supermarket.bean.Product;
import com.supermarket.dao.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class ProductServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        ProductDAO productDAO = new ProductDAO();

        if ("search".equals(action)) {
            String keyword = request.getParameter("keyword");
            String categoryId = request.getParameter("categoryId");
            String orderBy = request.getParameter("orderBy");

            Integer catId = null;
            if (categoryId != null && !categoryId.isEmpty()) {
                catId = Integer.parseInt(categoryId);
            }

            List<Product> products = productDAO.searchProducts(keyword, catId, orderBy);
            request.setAttribute("products", products);
            request.getRequestDispatcher("/admin/productList.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        ProductDAO productDAO = new ProductDAO();

        if ("add".equals(action)) {
            Product product = new Product();
            product.setProductName(request.getParameter("productName"));
            product.setCategoryId(Integer.parseInt(request.getParameter("categoryId")));
            product.setPrice(Double.parseDouble(request.getParameter("price")));
            product.setStock(Integer.parseInt(request.getParameter("stock")));
            product.setUnit(request.getParameter("unit"));
            product.setSupplier(request.getParameter("supplier"));
            product.setStatus("active");

            if (productDAO.addProduct(product)) {
                response.sendRedirect(request.getContextPath() + "/product?action=search");
            }
        } else if ("update".equals(action)) {
            Product product = new Product();
            product.setProductId(Integer.parseInt(request.getParameter("productId")));
            product.setProductName(request.getParameter("productName"));
            product.setCategoryId(Integer.parseInt(request.getParameter("categoryId")));
            product.setPrice(Double.parseDouble(request.getParameter("price")));
            product.setStock(Integer.parseInt(request.getParameter("stock")));
            product.setUnit(request.getParameter("unit"));
            product.setSupplier(request.getParameter("supplier"));
            product.setStatus(request.getParameter("status"));

            if (productDAO.updateProduct(product)) {
                response.sendRedirect(request.getContextPath() + "/product?action=search");
            }
        } else if ("delete".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            productDAO.deleteProduct(productId);
            response.sendRedirect(request.getContextPath() + "/product?action=search");
        }
    }
}
