package com.comp.complementos.DTO;

import com.comp.complementos.DAO.ComplementComposite;
import com.comp.complementos.DAO.DoctoRelacionado40DAO;
import com.complementos.Impuestos;
import com.comp.complementos.DB.ConsultasFacade;
import com.comp.complementos.DB.Pool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author msalas
 */
public class DoctoRelacionado40DTO extends ComplementComposite {

    private Connection BPCSConn = null;

    public DoctoRelacionado40DTO(PagoCliente pagoCliente) {
        this.pagoCliente = pagoCliente;
    }

    @Override
    public void fill() {
        fillDoctosRelacionados();
    }

    public void fillDoctosRelacionados() {

        String mensaje, stBanco = "", stCta = "", stRFC = "";
        boolean existe = false;
        DecimalFormat dec = new DecimalFormat("#.00");

        try {
            BPCSConn = getConnection();
            //Validar que exista el codigo de la trasaccion en la tabla de complementos.
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getTransaccion());
            ps.setInt(1, pagoCliente.getTransactionProcess());

            ResultSet rs = ps.executeQuery();

            existe = rs.next();

            ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getConsultaDoctoRelaciondo());
            ps.setInt(1, pagoCliente.getTransactionProcess());

            rs = ps.executeQuery();

            while (rs.next()) {
                //System.out.println("Validando Docto Relacionado.");
                DoctoRelacionado40DAO documento = new DoctoRelacionado40DAO();

                if (rs.getString("UUID") == null || rs.getString("UUID").trim().equals("")) {
                    documento.setEsUUID(false);
                } else {
                    documento.setLs_idDocumento(rs.getString("UUID"));
                    documento.setEsUUID(true);
                }
                documento.setLs_seriePago(rs.getString("SERIEPAGO"));
                documento.setLi_folioPago(rs.getInt("FOLIOPAGO"));
                documento.setLs_monedaDR(rs.getString("MONEDADR"));

                if (existe == false) {
                    documento.setLi_numParcialidad(rs.getInt("PARCIALIDAD2") + 1);
                } else {
                    documento.setLi_numParcialidad(rs.getInt("PARCIALIDAD2"));
                }

                //Control de parcialidades
                if (rs.getInt("PARCIALIDAD2") == 0 || rs.getInt("PARCIALIDAD2") == 1) {
                    documento.setLd_impSaldoANT(Double.parseDouble(dec.format(rs.getDouble("VFACTURA"))));
                    documento.setLd_impPagado(rs.getDouble("IMPAGO"));
                    documento.setLd_impSaldoInsoluto(Double.parseDouble(dec.format(documento.getLd_impSaldoANT() - documento.getLd_impPagado())));
                } else {
                    PreparedStatement saldo = BPCSConn.prepareStatement(ConsultasFacade.instance().getSaldoAnterior());
                    saldo.setInt(1, documento.getLi_folioPago());
                    saldo.setInt(2, rs.getInt("PARCIALIDAD2") - 1);

                    ResultSet rSaldo = saldo.executeQuery();

                    if (rSaldo.next()) {
                        documento.setLd_impSaldoANT(Double.parseDouble(dec.format(rSaldo.getDouble("CSALD"))));
                    }

                    documento.setLd_impPagado(rs.getDouble("IMPAGO"));
                    documento.setLd_impSaldoInsoluto(Double.parseDouble(dec.format(documento.getLd_impSaldoANT() - documento.getLd_impPagado())));
                }

                documento.setLs_objetoImp(rs.getString("OBJETOIMP"));
                documento.setLs_EquivalenciaDR(rs.getString("EQUIVALENCIADR"));

                mensaje = "";

                switch (rs.getInt("NUMCUEN")) {
                    case 0:
                        mensaje = mensaje + "03";
                        stBanco = "";
                        stCta = "";
                        stRFC = "";
                        break;
                    case 1:
                        ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getConsultaCtasBancariasAct());
                        ps.setInt(1, rs.getInt("CLIENTE"));
                        ResultSet rsCtas = ps.executeQuery();

                        if (rsCtas.next()) {
                            stBanco = rsCtas.getString("BANCO");
                            stCta = rsCtas.getString("ORDENANTE");
                            stRFC = rsCtas.getString("RFC");
                        }
                        break;
                    default:
                        mensaje = mensaje + "01";
                        stBanco = "";
                        stCta = "";
                        stRFC = "";
                }

                if (rs.getString("UUID") == null || rs.getString("UUID").trim().equals("")) {
                    documento.setEsUUID(false);
                } else {
                    documento.setLs_idDocumento(rs.getString("UUID"));
                    documento.setEsUUID(true);
                }

                if (documento.isEsUUID() == false) {
                    mensaje = mensaje + "02";
                }

                //Guardar los conceptos de cada factura
                if (existe == false) {
                    ps = BPCSConn.prepareStatement(ConsultasFacade.instance().setInsertConcepto());
                    ps.setInt(1, documento.getLi_folioPago());
                    ps.setDouble(2, documento.getLd_impSaldoANT());
                    ps.setDouble(3, documento.getLd_impPagado());
                    ps.setDouble(4, documento.getLd_impSaldoInsoluto());
                    ps.setInt(5, documento.getLi_numParcialidad());
                    ps.setInt(6, rs.getInt("SECUENCIA"));
                    ps.setInt(7, pagoCliente.getTransactionProcess());
                    ps.setInt(8, ObtenerFecha());
                    ps.setString(9, stBanco);
                    ps.setString(10, stCta);
                    ps.setString(11, stRFC);
                    ps.setString(12, "NO");
                    ps.setString(13, mensaje);
                    ps.setInt(14, rs.getInt("FECHAPAGO"));
                    ps.executeUpdate();
                } else {
                    ps = BPCSConn.prepareStatement(ConsultasFacade.instance().setUpdateConcepto());
                    ps.setString(1, mensaje);
                    ps.setString(2, stBanco);
                    ps.setString(3, stCta);
                    ps.setString(4, stRFC);
                    ps.setInt(5, pagoCliente.getTransactionProcess());
                    ps.setInt(6, documento.getLi_folioPago());
                    ps.executeUpdate();
                }

                addElement(documento);

                //Calcular los TrasladosDR40DTO
                if (Impuestos.Iva16(pagoCliente, rs.getInt("FOLIOPAGO"))) {
                    addElement(new TrasladosDR40DTO(pagoCliente, "IVA16", rs.getInt("FOLIOPAGO"), rs.getDouble("TCAMBIO")));
                }

                if (Impuestos.Iva00(pagoCliente, rs.getInt("FOLIOPAGO"))) {
                    addElement(new TrasladosDR40DTO(pagoCliente, "IVA00", rs.getInt("FOLIOPAGO"), rs.getDouble("TCAMBIO")));
                }

                if (Impuestos.ret(pagoCliente, rs.getInt("FOLIOPAGO"))) {
                    addElement(new RetencionesDR40DTO(pagoCliente, "RET16", rs.getInt("FOLIOPAGO"), rs.getDouble("TCAMBIO")));
                }

            }

            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en DoctoRelacionado40DTO: " + e.getMessage());
            try {
                BPCSConn.rollback();
                System.out.println("Error en DoctoRelacionado40DTO: " + e.getMessage());
            } catch (SQLException ex) {
                Logger.getLogger(DoctoRelacionado40DTO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private int ObtenerFecha() {

        String dia, mes, annio, fecha;

        Calendar c = Calendar.getInstance();

        dia = Integer.toString(c.get(Calendar.DATE));
        mes = Integer.toString(c.get(Calendar.MONTH) + 1);
        annio = Integer.toString(c.get(Calendar.YEAR));

        if (Integer.parseInt(dia) < 10) {
            dia = "0" + dia;
        }

        if (Integer.parseInt(mes) < 10) {
            mes = "0" + mes;
        }

        fecha = annio + mes + dia;

        //System.out.println("fecha = " + fecha);
        return Integer.parseInt(fecha);

    }

    private Connection getConnection() throws SQLException {
        return Pool.getConnection();
    }

}
