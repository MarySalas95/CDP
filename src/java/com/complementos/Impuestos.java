package com.complementos;

//import com.comp.complementos.DB.ConexionDB;
import com.comp.complementos.DB.ConsultasFacade;
import com.comp.complementos.DB.Pool;
import com.comp.complementos.DTO.PagoCliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author msalas
 */
public class Impuestos {

    public static boolean Iva16(PagoCliente pagoCliente, int factura) {

        try {

            boolean esIva16 = false;

            Connection BPCSConn = null;
            BPCSConn = Pool.getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestosBB());
            ps.setString(1, "IVA16");
            ps.setInt(2, pagoCliente.getTransactionProcess());
            ps.setInt(3, factura);

            ResultSet rs = ps.executeQuery();

            esIva16 = rs.next();

            ps.close();
            BPCSConn.close();

            return esIva16;

        } catch (SQLException e) {
            System.out.println("Error en Impuestos Iva16: " + e.getMessage());
            Logger.getLogger(Impuestos.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }

    }

    public static boolean Iva00(PagoCliente pagoCliente, int factura) {

        boolean esiva16 = false, esiva0 = false;

        try {

            Connection BPCSConn = null;
            BPCSConn = Pool.getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestosBB());
            ps.setString(1, "IVA16");
            ps.setInt(2, pagoCliente.getTransactionProcess());
            ps.setInt(3, factura);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                esiva16 = true;
            }

            if (esiva16) {
                esiva0 = false;
            } else {

                ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestosIVA00());
                ps.setInt(1, factura);

                rs = ps.executeQuery();

                while (rs.next()) {
                    esiva0 = !rs.getString("IVA").trim().equals("IVA16");
                }
            }

            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en Impuestos Iva00: " + e.getMessage());
            Logger.getLogger(Impuestos.class.getName()).log(Level.SEVERE, null, e);
        }

        return esiva0;
    }

    public static boolean ret(PagoCliente pagoCliente, int factura) {

        boolean esRet = false;

        try {

            Connection BPCSConn = null;
            BPCSConn = Pool.getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestosBB());
            ps.setString(1, "RET16");
            ps.setInt(2, pagoCliente.getTransactionProcess());
            ps.setInt(3, factura);

            ResultSet rs = ps.executeQuery();

            esRet = rs.next();

            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en Impuestos ret: " + e.getMessage());
            Logger.getLogger(Impuestos.class.getName()).log(Level.SEVERE, null, e);
        }

        return esRet;

    }

    public static boolean retP40(PagoCliente pagoCliente) {

        boolean esRet = false;

        try {

            Connection BPCSConn = null;
            BPCSConn = Pool.getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestos());
            ps.setString(1, "RET16");
            ps.setInt(2, pagoCliente.getTransactionProcess());

            ResultSet rs = ps.executeQuery();

            esRet = rs.next();

            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en Impuestos retP40: " + e.getMessage());
            Logger.getLogger(Impuestos.class.getName()).log(Level.SEVERE, null, e);
        }

        return esRet;

    }

}
