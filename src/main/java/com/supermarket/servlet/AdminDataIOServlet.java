package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.AuditLogDAO;
import com.supermarket.dao.CategoryDAO;
import com.supermarket.dao.OrderDAO;
import com.supermarket.dao.ProductDAO;
import com.supermarket.bean.Product;
import com.supermarket.bean.Order;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.List;

@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
public class AdminDataIOServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String action = request.getParameter("action");

        if ("exportProducts".equals(action)) {
            exportProducts(response);
            new AuditLogDAO().log(user.getUserId(), user.getUsername(),
                "导出商品数据", "products", null, request.getRemoteAddr());

        } else if ("exportOrders".equals(action)) {
            exportOrders(response);
            new AuditLogDAO().log(user.getUserId(), user.getUsername(),
                "导出订单数据", "orders", null, request.getRemoteAddr());

        } else {
            request.getRequestDispatcher("/admin/dataIO.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            response.sendRedirect("adminDataIO?msg=nofile");
            return;
        }

        int success = 0, fail = 0;
        try (InputStream is = filePart.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            ProductDAO productDAO = new ProductDAO();
            CategoryDAO categoryDAO = new CategoryDAO();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Product p = new Product();
                    p.setProductName(getCellStr(row, 0));
                    p.setCategoryId(getCategoryId(categoryDAO, getCellStr(row, 1)));
                    p.setPrice(Double.parseDouble(getCellStr(row, 2)));
                    p.setStock((int) Double.parseDouble(getCellStr(row, 3)));
                    p.setUnit(getCellStr(row, 4));
                    p.setSupplier(getCellStr(row, 5));
                    p.setStatus("active");
                    if (productDAO.addProduct(p)) success++; else fail++;
                } catch (Exception e) { fail++; }
            }
        } catch (Exception e) {
            response.sendRedirect("adminDataIO?msg=error");
            return;
        }

        new AuditLogDAO().log(user.getUserId(), user.getUsername(),
            "导入商品数据", "products", "成功:" + success + " 失败:" + fail, request.getRemoteAddr());
        response.sendRedirect("adminDataIO?msg=ok&success=" + success + "&fail=" + fail);
    }

    private void exportProducts(HttpServletResponse response) throws IOException {
        List<Product> products = new ProductDAO().searchProducts(null, null, null);
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("商品数据");
        String[] headers = {"ID", "商品名称", "分类", "价格", "库存", "单位", "供应商", "状态"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
        int rowNum = 1;
        for (Product p : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getProductId());
            row.createCell(1).setCellValue(p.getProductName());
            row.createCell(2).setCellValue(p.getCategoryName() != null ? p.getCategoryName() : "");
            row.createCell(3).setCellValue(p.getPrice());
            row.createCell(4).setCellValue(p.getStock());
            row.createCell(5).setCellValue(p.getUnit() != null ? p.getUnit() : "");
            row.createCell(6).setCellValue(p.getSupplier() != null ? p.getSupplier() : "");
            row.createCell(7).setCellValue(p.getStatus());
        }
        writeExcel(response, wb, "products.xlsx");
    }

    private void exportOrders(HttpServletResponse response) throws IOException {
        List<Order> orders = new OrderDAO().searchAllOrders(null, null);
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("订单数据");
        String[] headers = {"订单号", "用户", "总金额", "状态", "支付方式", "下单时间"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
        int rowNum = 1;
        for (Order o : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(o.getOrderId());
            row.createCell(1).setCellValue(o.getUsername() != null ? o.getUsername() : "");
            row.createCell(2).setCellValue(o.getTotalAmount());
            row.createCell(3).setCellValue(o.getOrderStatus());
            row.createCell(4).setCellValue(o.getPaymentMethod() != null ? o.getPaymentMethod() : "");
            row.createCell(5).setCellValue(o.getOrderTime() != null ? o.getOrderTime().toString() : "");
        }
        writeExcel(response, wb, "orders.xlsx");
    }

    private void writeExcel(HttpServletResponse response, Workbook wb, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        wb.write(response.getOutputStream());
        wb.close();
    }

    private String getCellStr(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return cell.getCellType() == CellType.NUMERIC
            ? String.valueOf((long) cell.getNumericCellValue())
            : cell.getStringCellValue().trim();
    }

    private int getCategoryId(CategoryDAO dao, String name) {
        return dao.getAllCategories().stream()
            .filter(c -> c.getCategoryName().equals(name))
            .findFirst().map(c -> c.getCategoryId()).orElse(1);
    }
}
