package com.comp.complementos.DTO;

import com.comp.complementos.DAO.Pago40DAO;
import com.comp.complementos.DB.ConsultasFacade;
import com.comp.complementos.DB.Pool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author msalas
 */
public class Pago40DTO extends Pago40DAO {

    private Connection BPCSConn = null;

    public Pago40DTO(PagoCliente pagoCliente) {
        this.pagoCliente = pagoCliente;
    }

    @Override
    public void fill() {

        int contador = 0, conteo = 0;
        double montoPag = 0;
        DecimalFormat dec = new DecimalFormat("#.00");

        try {
            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getConsultaPago());
            ps.setInt(1, pagoCliente.getTransactionProcess());

            ResultSet rs = ps.executeQuery();
            ResultSet ff, cb;

            while (rs.next()) {

                setLs_fechaPago(rs.getString("FECHA"));
                setLs_formaPago(rs.getString("CODIGOSAT"));
                setLs_monedaP(rs.getString("MONEDA"));
                setLd_tipoCambioP(rs.getDouble("TCAMBIO"));
                montoPag = montoPag + rs.getDouble("MONTO"); //Se suma el importe de pagos aplicados a este complemento.
                setLs_numOperacion(rs.getString("NUMOPE"));

                if (rs.getString("TIP").trim().equals("FF")) {
                    //Validacion para determinar si es Facturaje Financiero FF, es decir se recibe el pago a nombre de otro cliente.
                    ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getFinanciera());
                    ps.setString(1, rs.getString("CLIENTE"));

                    ff = ps.executeQuery();

                    if (ff.next()) {
                        setLs_nomBancoOrdext(ff.getString("BANKE").trim());
                        setLs_ctaOrdenante(ff.getString("CUENTA").trim());
                        setLs_rfcemisorctaord(ff.getString("RFCB").trim());
                    } else {
                        setLs_nomBancoOrdext("");
                        setLs_ctaOrdenante("");
                        setLs_rfcemisorctaord("");
                    }

                } else {

                    ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getConsultaCtasBancariasAct());
                    ps.setString(1, rs.getString("CLIENTE"));

                    cb = ps.executeQuery();

                    if (conteo == 0) {

                        while (cb.next()) {
                            contador++;
                            if (contador == 1) {
                                setLs_nomBancoOrdext(cb.getString("BANCO").trim());
                                setLs_ctaOrdenante(cb.getString("ORDENANTE").trim());
                                setLs_rfcemisorctaord(cb.getString("RFC").trim());
                            } else {
                                setLs_nomBancoOrdext("");
                                setLs_ctaOrdenante("");
                                setLs_rfcemisorctaord("");
                            }
                        }
                        conteo = 1;
                    }

                }
                setLs_ctaBeneficiario(rs.getString("CUENTAB"));
                setLs_rfcEmisorctaben(rs.getString("RFCB"));
            }

            ps.close();
            BPCSConn.close();

            montoPag = Double.parseDouble(dec.format(montoPag));
            setLd_monto(montoPag);

        } catch (SQLException e) {
            System.out.println("Error en Pago40DTO: " + e.getMessage());
            Logger.getLogger(Pago40DTO.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private Connection getConnection() throws SQLException {
        return Pool.getConnection();
    }

}
