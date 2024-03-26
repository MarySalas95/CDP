package com.comp.complementos.DTO;

import com.comp.complementos.DAO.Cfdi40DAO;
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
public class Cfdi40DTO extends Cfdi40DAO {
    
    private Connection BPCSConn = null;

    public Cfdi40DTO(PagoCliente pagoCliente) {
        this.pagoCliente = pagoCliente;
    }

    @Override
    public void fill() {

        try {
            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getCFD40());

            ResultSet rs = ps.executeQuery();

            //Obtenemos parte de la informacion del Nodo [CFD40]
            while (rs.next()) {
                switch (rs.getString("ETIQUETA").replace(" ", "")) {
                    case "SERIE":
                        setLs_serie(rs.getString("VALOR"));
                        break;
                    case "SUBTOTAL":
                        setLs_subtotal(rs.getString("VALOR"));
                        break;
                    case "DESCUENT":
                        setLs_descuento(rs.getString("VALOR"));
                        break;
                    case "MONEDA":
                        setLs_moneda(rs.getString("VALOR"));
                        break;
                    case "TOTAL":
                        setLs_total(rs.getString("VALOR"));
                        break;
                    case "TIPOCOMP":
                        setLs_tipoComprobante(rs.getString("VALOR"));
                        break;
                    case "LUGAREXP":
                        setLs_lugarExpedicion(rs.getString("VALOR"));
                        break;
                    case "EXPORTAC":
                        setLs_exportacion(rs.getString("VALOR"));
                        break;
                }
            }
            
            ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getFolio());
            ps.setInt(1, pagoCliente.getTransactionProcess());
            rs = ps.executeQuery();

            //Obtenemos el folio del complemento.
            if (rs.next()) {
                setLi_folio(rs.getInt("CFOLIO"));
            } else {
                //Si no encontramos resultado con esa transaccion, entonces debemos insertarlo en automatico nos dará un Folio del CDP.
                ps = BPCSConn.prepareStatement(ConsultasFacade.instance().setInsertFolio());
                ps.setInt(1, pagoCliente.getTransactionProcess());

                ps.executeUpdate();

                //Ejecutamos nuevamente la consulta del Folio del CDP
                ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getFolio());
                ps.setInt(1, pagoCliente.getTransactionProcess());
                rs = ps.executeQuery();

                if (rs.next()) {
                    //Guardamos el Folio de CDP
                    setLi_folio(rs.getInt("CFOLIO"));
                }

            }
            
            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en Cfdi40DTO: " + e.getMessage());
            try {
                BPCSConn.rollback();
                System.out.println("Error en Cfdi40DTO: " + e.getMessage());
            } catch (SQLException ex) {
                Logger.getLogger(Cfdi40DTO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    private Connection getConnection() throws SQLException{
        return Pool.getConnection();
    }

}
