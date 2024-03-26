package com.comp.complementos.DTO;

import com.comp.complementos.DAO.RetencionesDR40DAO;
import com.comp.complementos.DB.ConsultasFacade;
import com.comp.complementos.DB.Pool;
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
public class RetencionesDR40DTO extends RetencionesDR40DAO {

    private final String impuesto;
    private final int factura;
    private final double tcFact;
    
    private Connection BPCSConn = null;

    public RetencionesDR40DTO(PagoCliente pagoCliente, String impuesto, int factura, double tcFact) {
        this.pagoCliente = pagoCliente;
        this.impuesto = impuesto;
        this.factura = factura;
        this.tcFact = tcFact;

        try {

            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestosDR());
            ps.setString(1, impuesto);
            ps.setInt(2, pagoCliente.getTransactionProcess());
            ps.setInt(3, factura);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                setBaseDR(rs.getDouble("BASETP"));
                setImpuestoDR(rs.getString("IMPUESTOTDR"));
                setTipoFactorDR(rs.getString("TIPOFACTORTDR"));
                setTasaOcuotaDR(rs.getDouble("TASAOCUOTATDR"));
                setImporteDR((rs.getDouble("BASETDR") * rs.getDouble("TASAOCUOTATDR")));
            }

            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en RetencionesDR40DTO: " + e.getMessage());
            Logger.getLogger(RetencionesDR40DTO.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private Connection getConnection() throws SQLException {
        return Pool.getConnection();
    }
}
