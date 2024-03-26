package com.comp.complementos.DTO;

import com.comp.complementos.DAO.Concepto40DAO;
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
public class Concepto40DTO extends Concepto40DAO {
    
    private Connection BPCSConn = null;

    public Concepto40DTO(PagoCliente pagoCliente) {
        this.pagoCliente = pagoCliente;
    }

    @Override
    public void fill() {

        try {

            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getConsultaConcepto());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                setLs_claveProdServ(rs.getString("CLAVE"));
                setLs_cantidad("1");
                setLs_claveUnidad(rs.getString("UNIDAD"));
                setLs_descripción(rs.getString("DESCRIPCION"));
                setLs_valorUnitario(rs.getString("VALOR"));
                setLs_importe(rs.getString("IMPORTE"));
                setLs_objetoIMP("01");
            }
            
            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en Concepto40DTO: " + e.getMessage());
            Logger.getLogger(Concepto40DTO.class.getName()).log(Level.SEVERE, null, e);
        }

    }
    
    private Connection getConnection() throws SQLException{
        return Pool.getConnection();
    }

}
