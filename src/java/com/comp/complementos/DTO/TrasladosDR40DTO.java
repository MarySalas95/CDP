package com.comp.complementos.DTO;

import com.comp.complementos.DAO.TrasladosDR40DAO;
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
public class TrasladosDR40DTO extends TrasladosDR40DAO {

    private final String impuesto;
    private final int factura;
    private final double tcFact;
    private Connection BPCSConn = null;

    public TrasladosDR40DTO(PagoCliente pagoCliente, String impuesto, int factura, double tcFact) {
        this.impuesto = impuesto;
        this.factura = factura;
        this.tcFact = tcFact;

        DecimalFormat dec = new DecimalFormat("#.000000");
        DecimalFormat deci = new DecimalFormat("#.00");
        DecimalFormat dectc = new DecimalFormat("#.0000000");
        double valIVA0 = 0, iva0Fact, porcentaje = 0, pagado = 0, baseTP = 0, impuestos = 0, iva00 = 0, tcFact1 = 0;
        int existe = 0;

        try {

            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getFacturaIva());
            ps.setString(1, impuesto);
            ps.setInt(2, pagoCliente.getTransactionProcess());
            ps.setInt(3, factura);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                existe = 1;

                if (tcFact == 1) {
                    valIVA0 = (rs.getDouble("VFACTURA") - rs.getDouble("BASETDR"));
                } else {
                    valIVA0 = (rs.getDouble("VFACTURA") - (rs.getDouble("BASETP") / 1.16)); //MJSV 19102023
                }

                if (impuesto.equals("IVA16")) {
                    //Caso de que se liquide la cuenta en un solo pago.
                    if (valIVA0 == 0) {
                        baseTP = baseTP + (rs.getDouble("BASETP"));
                        impuestos = impuestos + (baseTP * 0.16);
                        iva00 = 0;
                    } else {
                        if (tcFact == 1) {
                            if (rs.getString("IMPUESTO").trim().equals("IVA16")) {
                                iva0Fact = (rs.getDouble("VFACTURA") - Double.parseDouble(deci.format((rs.getDouble("BASETDR") + rs.getDouble("IMPORTETDR")))));
                                porcentaje = 100 - (iva0Fact * 100 / rs.getDouble("VFACTURA"));
                                pagado = rs.getDouble("BASETP") * (porcentaje / 100);
                                baseTP = baseTP + (pagado / 1.16);
                                impuestos = impuestos + (pagado / 1.16) * .16;
                                iva00 = iva00 + (rs.getDouble("BASETP") - pagado);
                            }
                        } else {
                            iva0Fact = (rs.getDouble("VFACTURA") - ((rs.getDouble("BASETP") / 1.16) + (rs.getDouble("BASETP") / 1.16) * .16));
                            porcentaje = 100 - (iva0Fact * 100 / rs.getDouble("VFACTURA"));
                            pagado = rs.getDouble("BASETP") * (porcentaje / 100);
                            baseTP = baseTP + (pagado / 1.16);
                            impuestos = impuestos + (pagado / 1.16) * .16;
                            iva00 = iva00 + (rs.getDouble("BASETP") - pagado);
                        }
                    }
                } else {
                    if (rs.getString("IMPUESTO").trim().equals("IVA00") || rs.getString("IMPUESTO").trim().equals("")) {

                        tcFact1 = rs.getDouble("BASETDR") / rs.getDouble("VFACTURA");
                        if (tcFact == 1) {
                            valIVA0 = (rs.getDouble("VFACTURA") - rs.getDouble("BASETDR"));
                        } else {
                            tcFact1 = Double.parseDouble(dectc.format(tcFact1));
                            valIVA0 = (rs.getDouble("VFACTURA") * tcFact1) - (rs.getDouble("BASETDR") + rs.getDouble("IMPORTETDR"));
                            valIVA0 = Double.parseDouble(deci.format(valIVA0));
                        }

                        if (valIVA0 <= 0) {
                            iva00 = iva00 + rs.getDouble("BASETP");
                        } else {

                            iva0Fact = (rs.getDouble("VFACTURA") * tcFact1) - (rs.getDouble("BASETDR") + rs.getDouble("IMPORTETDR"));
                            porcentaje = 100 - (iva0Fact * 100 / rs.getDouble("VFACTURA"));

                            if (porcentaje == 100) {
                                pagado = rs.getDouble("BASETP") * (0 / 100);
                            } else {
                                pagado = rs.getDouble("BASETP") * (porcentaje / 100);
                            }

                            baseTP = baseTP + (pagado / 1.16);
                            impuestos = impuestos + (pagado / 1.16) * .16;
                            iva00 = 0 + (baseTP - pagado);
                        }
                    }
                }

            }

            if (existe == 0) {
                ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestosIVA00T());
                ps.setInt(1, pagoCliente.getTransactionProcess());
                ps.setInt(2, pagoCliente.getTransactionProcess());
                ps.setInt(3, factura);

                rs = ps.executeQuery();

                while (rs.next()) {
                    iva00 = iva00 + rs.getDouble("IMPAGO");
                }
            }

            if (baseTP > 0) {
                setLd_baseTDR(Double.parseDouble(dec.format(baseTP)));
                setLs_impuestoTDR("002");
                setLs_tipoFactorTDR("TASA");
                setLs_tasaOcuotaTDR("0.16");
                setLd_importeTDR(Double.parseDouble(dec.format(impuestos)));
            }

            iva00 = Double.parseDouble(dec.format(iva00));

            if (iva00 > 0.0) {
                setLd_baseTDR(iva00);
                setLs_impuestoTDR("002");
                setLs_tipoFactorTDR("TASA");
                setLs_tasaOcuotaTDR("0.0");
                setLd_importeTDR(0);
            }

            ps.close();
            BPCSConn.close();

        } catch (SQLException e) {
            System.out.println("Error en TrasladosDR40DTO: " + e.getMessage());
            Logger.getLogger(TrasladosDR40DTO.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private Connection getConnection() throws SQLException {
        return Pool.getConnection();
    }
}
