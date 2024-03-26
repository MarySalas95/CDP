package com.comp.complementos.DTO;

import com.comp.complementos.DB.ConsultasFacade;
import com.comp.complementos.DB.Pool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 *
 * @author msalas
 */
public class ActualizarCtasBancariasDTO {

    private Connection BPCSConn = null;

    public int ActualizarCtas(String rfc, String banco, String cuenta, String transaccion, String mensaje) {

        int act = 0;
        String msg = mensaje.replace("01", "");

        try {

            System.out.println("Actualizando Ctas. Bancarias....");
            
            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().setUpdateCtasBancarias());
            ps.setString(1, banco);
            ps.setString(2, cuenta);
            ps.setString(3, rfc);
            ps.setString(4, msg);
            ps.setString(5, transaccion);

            act = ps.executeUpdate();

            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en ActualizarCtasBancariasDTO: " + e.getMessage());
            try{
                BPCSConn.rollback();
                System.out.println("Error en ActualizarCtasBancariasDTO: " + e.getMessage());
            }catch(SQLException ex){
                Logger.getLogger(ActualizarCtasBancariasDTO.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }

        System.out.println("Ctas. Bancarias Actualizadas");
        return act;

    }

    private Connection getConnection() throws SQLException {
        return Pool.getConnection();
    }

}
