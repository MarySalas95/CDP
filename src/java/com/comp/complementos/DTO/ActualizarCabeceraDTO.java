package com.comp.complementos.DTO;

import com.comp.complementos.DAO.cabeceraDAO;
import com.comp.complementos.DAO.ctasBancariasDAO;
import com.comp.complementos.DAO.detalleDAO;
import com.comp.complementos.DB.ConsultasFacade;
import com.comp.complementos.DB.Pool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author msalas
 */
public class ActualizarCabeceraDTO {

    private Connection BPCSConn = null;

    public cabeceraDAO ActualizarCabecera(int fechaI, int fechaF) {

        cabeceraDAO cab = new cabeceraDAO();
        List<cabeceraDAO> cabecera = new ArrayList<>();
        List<detalleDAO> detalle = new ArrayList<>();
        List<ctasBancariasDAO> cuentas = new ArrayList<>();

        try {
            
            System.out.println("Actualizando Cabecera....");

            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getCabeceraCDP());
            ps.setInt(1, fechaI);
            ps.setInt(2, fechaF);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                cabeceraDAO jsonCab = new cabeceraDAO();
                jsonCab.setFecha(rs.getInt("FECHA"));
                jsonCab.setCliente(rs.getInt("CLIENTE"));
                jsonCab.setLsCliente(rs.getString("NOMBRE"));
                jsonCab.setFolio(rs.getInt("FOLIO"));
                jsonCab.setTransaccion(rs.getInt("TRANSACCION"));
                jsonCab.setLsBanco(rs.getString("BANCO"));
                jsonCab.setLsCuenta(rs.getString("CUENTA"));
                jsonCab.setLsRFCBanco(rs.getString("RFC"));
                jsonCab.setLsTXT(rs.getString("TXT"));

                //Descomponer mensaje
                if (rs.getString("MENSAJE") == null) {
                    jsonCab.setLsMensaje("");
                } else {
                    jsonCab.setLsMensaje(rs.getString("MENSAJE").trim());

                    String mensajeR = rs.getString("MENSAJE").trim();
                    String codigo;
                    int sep = 0, ini = mensajeR.length();

                    while (sep < ini) {
                        codigo = mensajeR.substring(sep, sep + 2);
                        sep = sep + 2;

                        if (codigo.equals("01")) {
                            jsonCab.setLsMensaje1(codigo);
                            ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getConsultaCtasBancariasAct());
                            ps.setInt(1, rs.getInt("CLIENTE"));

                            ResultSet rsc = ps.executeQuery();
                            while (rsc.next()) {
                                ctasBancariasDAO ctas = new ctasBancariasDAO();
                                ctas.setBanco(rsc.getString("BANCO").trim());
                                ctas.setCtaOrdenante(rsc.getString("ORDENANTE").trim());
                                ctas.setRFC(rsc.getString("RFC").trim());
                                ctas.setCliente(rsc.getString("B7CUST"));
                                cuentas.add(ctas);
                            }
                            jsonCab.setCtasBancarias(cuentas);
                        }

                        if (codigo.equals("03")) {
                            jsonCab.setLsMensaje3(codigo);
                        }
                    }

                }

                cabecera.add(jsonCab);

                ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getDetalleCDP());
                ps.setInt(1, fechaI);
                ps.setInt(2, fechaF);
                ps.setInt(3, rs.getInt("TRANSACCION"));

                ResultSet rsd = ps.executeQuery();

                while (rsd.next()) {
                    detalleDAO jsonDet = new detalleDAO();
                    jsonDet.setTransaccion(rsd.getInt("CTRAN"));
                    jsonDet.setNumFactura(rsd.getInt("FACTURA"));
                    jsonDet.setParcialidad(rsd.getInt("PARCIALIDAD"));
                    jsonDet.setLdSaldoAnterior(rsd.getDouble("SALDO_ANTERIOR"));
                    jsonDet.setLdMontoPagado(rsd.getDouble("MONTO_PAGO"));
                    jsonDet.setLdNvoSaldo(rsd.getDouble("NUEVO_SALDO"));
                    jsonDet.setLsMoneda(rsd.getString("MONEDA"));
                    //jsonDet.setLsMensaje(rsd.getString("MENSAJE"));
                    jsonDet.setLsPrefijo(rsd.getString("PREFIJO"));

                    if (rsd.getString("MENSAJE") == null) {

                    } else {
                        jsonDet.setLsMensaje(rsd.getString("MENSAJE").trim());
                        //Descomponer mensaje
                        String mensaje = rsd.getString("MENSAJE").trim();
                        String codigo;
                        int sep = 0, ini = mensaje.length();

                        while (sep < ini) {
                            codigo = mensaje.substring(sep, sep + 2);
                            sep = sep + 2;

                            if (codigo.equals("02")) {
                                jsonDet.setLsMensaje2(codigo);
                            }

                        }

                        detalle.add(jsonDet);
                    }

                }

            }

            cab.setCabecera(cabecera);
            cab.setDetalle(detalle);
            cab.setCtasBancarias(cuentas);

            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en ActualizarCabeceraDTO: " + e.getMessage());
            Logger.getLogger(ActualizarCabeceraDTO.class.getName()).log(Level.SEVERE, null, e);
        }

        System.out.println("Cabecera actualizada");
        return cab;
    }

    private Connection getConnection() throws SQLException {
        return Pool.getConnection();
    }

}
