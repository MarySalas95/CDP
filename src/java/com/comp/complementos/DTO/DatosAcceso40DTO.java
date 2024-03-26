package com.comp.complementos.DTO;

import com.comp.complementos.DAO.DatosAcceso40DAO;
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
public class DatosAcceso40DTO extends DatosAcceso40DAO {
    
    private Connection BPCSConn = null;

    @Override
    public void fill() {

        try {
            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getAcceso());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                setLs_usuario(rs.getString("DATO1"));
                setLs_password(rs.getString("DATO2"));
            }
            
            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en DatosAcceso40DTO: " + e.getMessage());
            Logger.getLogger(DatosAcceso40DTO.class.getName()).log(Level.SEVERE, null, e);
        }

    }
    
    private Connection getConnection() throws SQLException{
        return Pool.getConnection();
    }

}
