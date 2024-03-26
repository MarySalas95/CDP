package com.comp.complementos.DTO;

import com.comp.complementos.DB.ConsultasFacade;
import com.comp.complementos.DB.Pool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 *
 * @author msalas
 */
public class ActualizarUUIDDTO {

    private Connection BPCSConn = null;

    public int ActualizarUUID(String uuid, String prefijo, String folio, String status, String mensaje, String transaccion) {

        int act = 0;

        try {
            
            System.out.println("Actualizando UUID....");

            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getUUID());
            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ps = BPCSConn.prepareStatement(ConsultasFacade.instance().setUpdateUUID());
                ps.setString(1, prefijo);
                ps.setString(2, folio);
                ps.setString(3, status);
                ps.setString(4, uuid);
                act = ps.executeUpdate();
                System.out.println("UUID " + uuid + " Actualizado, registros=" + act);
            } else {
                ps = BPCSConn.prepareStatement(ConsultasFacade.instance().setInsertUUID());
                ps.setString(1, prefijo);
                ps.setString(2, folio);
                ps.setString(3, uuid);
                ps.setString(4, status);
                act = ps.executeUpdate();
                System.out.println("UUID " + uuid + " Insertado, registros=" + act);
            }

            mensaje = mensaje.replace("02", "");

            if (act > 0) {
                //Actualizar el mensaje de la factura 
                ps = BPCSConn.prepareStatement(ConsultasFacade.instance().setUpdateMSG());
                ps.setString(1, mensaje);
                ps.setString(2, folio);
                ps.setString(3, transaccion);
                ps.executeUpdate();
            }

            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en ActualizarUUIDDTO: " + e.getMessage());
            try {
                BPCSConn.rollback();
                System.out.println("Error en ActualizarUUIDDTO: " + e.getMessage());
            } catch (SQLException ex) {
                Logger.getLogger(ActualizarUUIDDTO.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }

        System.out.println("UUID Actualizado");
        return act;

    }

    private Connection getConnection() throws SQLException {
        return Pool.getConnection();
    }

}
