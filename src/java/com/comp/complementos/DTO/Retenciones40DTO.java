package com.comp.complementos.DTO;

import com.comp.complementos.DAO.Retenciones40DAO;
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
public class Retenciones40DTO extends Retenciones40DAO {

    private Connection BPCSConn = null;

    public Retenciones40DTO(PagoCliente pagoCliente) {
        this.pagoCliente = pagoCliente;
    }

    @Override
    public void fill() {

        //boolean existeRet = false;
        double baseTP = 0, importeTP = 0;

        try {

            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestos());
            ps.setString(1, "RET16");
            ps.setInt(2, pagoCliente.getTransactionProcess());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                //existeRet = true;
                baseTP = baseTP + rs.getDouble("BASETP");
                importeTP = importeTP + (rs.getDouble("BASETDR") * rs.getDouble("TASAOCUOTATDR"));
                setImpuestoRP(rs.getString("IMPUESTOTDR"));
                setTipoFactorRP(rs.getString("TIPOFACTORTDR"));
                setTasaOcuotaRP(rs.getDouble("TASAOCUOTATDR"));
            }

            setBaseRP(baseTP);
            setImporteRP(importeTP);

            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en Retenciones40DTO: " + e.getMessage());
            Logger.getLogger(Retenciones40DTO.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private Connection getConnection() throws SQLException {
        return Pool.getConnection();
    }
}
