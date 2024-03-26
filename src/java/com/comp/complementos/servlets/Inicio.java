package com.comp.complementos.servlets;

import com.comp.complementos.DAO.cabeceraDAO;
import com.complementos.ConsultarCDP;
import com.comp.complementos.DTO.ActualizarCabeceraDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author msalas
 */
@WebServlet(name = "Inicio", urlPatterns = {"/Inicio"})
public class Inicio extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Inicio</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Inicio at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //processRequest(request, response);
        HttpSession misession = (HttpSession) request.getSession();

        String fechaInicio = request.getParameter("fechaInicio").replace("-", "");
        String fechaFin = request.getParameter("fechaFin").replace("-", "");
        int fechaI = Integer.parseInt(fechaInicio);
        int fechaF = Integer.parseInt(fechaFin);
        fechaInicio = request.getParameter("fechaInicio");
        fechaFin = request.getParameter("fechaFin");
        
        System.out.println("Aplicando filtro de CDP Del: " + fechaInicio + " al " + fechaFin);

        //Formatear Fecha
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d1, d2;

        //Consultar los Complementos por rango de fechas.
        ConsultarCDP consulta = new ConsultarCDP();
        consulta.sendComplementos(fechaI, fechaF);
        
        try {
            d1 = sdf.parse(fechaInicio);
            d2 = sdf.parse(fechaFin);

            cabeceraDAO cabecera;
            ActualizarCabeceraDTO cab = new ActualizarCabeceraDTO();

            cabecera = cab.ActualizarCabecera(fechaI, fechaF);

            misession.setAttribute("cabecera", cabecera.getCabecera());
            misession.setAttribute("detalle", cabecera.getDetalle());
            misession.setAttribute("bancos", cabecera.getCtasBancarias());
            misession.setAttribute("fechaI", d1);
            misession.setAttribute("fechaF", d2);
            response.sendRedirect("Index.jsp");

        } catch (ParseException ex) {
            Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
