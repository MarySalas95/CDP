package com.comp.complementos.DTO;

import com.comp.complementos.DAO.Receptor40DAO;
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
public class Receptor40DTO extends Receptor40DAO {
    
    private Connection BPCSConn = null;

    public Receptor40DTO(PagoCliente pagoCliente) {
        this.pagoCliente = pagoCliente;
    }

    @Override
    public void fill() {

        try {

            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getConsultaReceptor());
            ps.setInt(1, pagoCliente.getTransactionProcess());

            ResultSet rs = ps.executeQuery();

            //Obtenemos parte de la informacion del Nodo [RECEPTOR]
            if (rs.next()) {
                setLs_noCliente(rs.getString("NOCLIENTE"));
                if (rs.getString("TIP").trim().equals("SP")) {
                    setLs_rfcR(rs.getString(2)); //"RFDR"
                    setLs_nombreR(rs.getString("NOMBRER"));
                    setLs_email1(rs.getString("EMAIL1"));
                    setLs_email2(rs.getString("EMAIL2"));
                    setLs_email3(rs.getString("EMAIL3"));
                    setLs_numRegIdTrib(rs.getString("NUMREGIDTRIB"));
                    setLs_domicilioFiscalReceptor(rs.getString("DOMICILIOFISCALRECEPTOR"));
                    setLs_regimenFiscalReceptor(rs.getString("REGIMENFISCALRECEPTOR"));
                } else {
                    //Validacion para determinar si es Facturaje Financiero FF, es decir se recibe el pago a nombre de otro cliente.
                    ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getFinanciera());
                    ps.setString(1, rs.getString("NOCLIENTE"));

                    rs = ps.executeQuery();
                    if (rs.next()) {
                        setLs_rfcR(rs.getString("RFC"));
                        setLs_nombreR(rs.getString("RAZONSOCIAL"));
                        setLs_email1(rs.getString("EMAIL1"));
                        setLs_email2(rs.getString("EMAIL2"));
                        setLs_email3(rs.getString("EMAIL3"));
                        setLs_numRegIdTrib(rs.getString("NIT"));
                        setLs_domicilioFiscalReceptor(rs.getString("DOMICILIOFISCALRECEPTOR"));
                        setLs_regimenFiscalReceptor(rs.getString("REGIMENFISCALRECEPTOR"));
                    }
                }

            }
            
            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en Receptor40DTO: " + e.getMessage());
            Logger.getLogger(Receptor40DTO.class.getName()).log(Level.SEVERE, null, e);
        }

    }
    
    private Connection getConnection() throws SQLException{
        return Pool.getConnection();
    }

}
