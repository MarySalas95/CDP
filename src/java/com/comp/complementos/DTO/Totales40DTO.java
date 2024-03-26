package com.comp.complementos.DTO;

import com.comp.complementos.DAO.Totales40DAO;
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
public class Totales40DTO extends Totales40DAO {

    private Connection BPCSConn = null;

    public Totales40DTO(PagoCliente pagoCliente) {

        try {

            DecimalFormat dec = new DecimalFormat("#.00");
            DecimalFormat dect = new DecimalFormat("#.000000");
            double montoTotPagos = 0, valIVA0 = 0, iva0Fact = 0, porcentaje = 0, pagado = 0, baseTP = 0, impuestos = 0, iva00 = 0, tcFact = 0, tc = 0;

            BPCSConn = getConnection();
            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getPagCam());
            ps.setInt(1, pagoCliente.getTransactionProcess());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                montoTotPagos = Double.parseDouble(dec.format(rs.getDouble("MONTO") * rs.getDouble("TCAMBIO")));
                setLd_montoTotalPagos(montoTotPagos);

                tc = Double.parseDouble(dect.format(rs.getDouble("TCAMBIO")));
            }

            //Complementos con IVA16 - IVA0
            ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestos());
            ps.setString(1, "IVA16");
            ps.setInt(2, pagoCliente.getTransactionProcess());

            rs = ps.executeQuery();

            while (rs.next()) {

                if (tc == 1) {
                    valIVA0 = (rs.getDouble("VFACTURA") - rs.getDouble("BASETDR"));
                } else {
                    valIVA0 = (rs.getDouble("VFACTURA") - (rs.getDouble("BASETP") / 1.16)); //MJSV 19102023
                }

                //Caso de que se liquide la cuenta en un solo pago.
                if (valIVA0 == 0) {
                    baseTP = baseTP + (rs.getDouble("BASETP"));
                    impuestos = impuestos + (baseTP * 0.16);
                    iva00 = 0;
                } else {
                    if (rs.getString("IMPUESTO").trim().equals("IVA16")) {
                        if (tc == 1) { //MJSV 19102023
                            iva0Fact = (rs.getDouble("VFACTURA") - Double.parseDouble(dec.format(rs.getDouble("BASETDR") + rs.getDouble("IMPORTETDR"))));
                            porcentaje = 100 - (iva0Fact * 100 / rs.getDouble("VFACTURA"));
                            pagado = rs.getDouble("BASETP") * (porcentaje / 100);
                            baseTP = baseTP + (pagado / 1.16);
                            impuestos = impuestos + (pagado / 1.16) * .16;
                            iva00 = iva00 + (rs.getDouble("BASETP") - pagado);
                        } else { //MJSV 19102023
                            iva0Fact = (rs.getDouble("VFACTURA") - ((rs.getDouble("BASETP") / 1.16) + (rs.getDouble("BASETP") / 1.16) * .16));
                            porcentaje = 100 - (iva0Fact * 100 / rs.getDouble("VFACTURA"));
                            pagado = rs.getDouble("BASETP") * (porcentaje / 100);
                            baseTP = baseTP + (pagado / 1.16);
                            impuestos = impuestos + (pagado / 1.16) * .16;
                            iva00 = iva00 + (rs.getDouble("BASETP") - pagado);
                        }
                    }
                }
            }

            //Complementos con IVA16 - IVA0
            ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestos());
            ps.setString(1, "IVA00");
            ps.setInt(2, pagoCliente.getTransactionProcess());

            rs = ps.executeQuery();

            while (rs.next()) {

                tcFact = rs.getDouble("BASETDR") / rs.getDouble("VFACTURA");
                if (tc == 1) {
                    valIVA0 = (rs.getDouble("VFACTURA") - rs.getDouble("BASETDR"));
                } else {
                    //tcFact = Double.parseDouble(dectc.format(tcFact));
                    valIVA0 = (rs.getDouble("VFACTURA") * tcFact) - (rs.getDouble("BASETDR") + rs.getDouble("IMPORTETDR"));
                    valIVA0 = Double.parseDouble(dec.format(valIVA0));
                }

                if (valIVA0 <= 0) {
                    iva00 = iva00 + rs.getDouble("BASETP");

                } else {

                    if (rs.getString("IMPUESTO").trim().equals("IVA00")) {

                        iva0Fact = (rs.getDouble("VFACTURA") * tc) - (rs.getDouble("BASETDR") + rs.getDouble("IMPORTETDR"));
                        porcentaje = 100 - (iva0Fact * 100 / rs.getDouble("VFACTURA"));

                        if (porcentaje >= 100) {
                            pagado = rs.getDouble("BASETP") * (0 / 100);
                        } else {
                            pagado = rs.getDouble("BASETP") * (porcentaje / 100);
                        }

                        baseTP = baseTP + (pagado / 1.16);
                        impuestos = impuestos + (pagado / 1.16) * .16;
                        iva00 = getLd_montoTotalPagos();

                    }
                }

            }

            if (iva00 == 0) {
                if (tc == 1) {
                    if (getLd_montoTotalPagos() / 1.16 == baseTP) {
                        setLd_totalTrasladosBaseIVA0(0.0);
                    }
                }
            }

            if (iva00 == 0) {

                ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestos());
                ps.setString(1, "IVA16");
                ps.setInt(2, pagoCliente.getTransactionProcess());

                rs = ps.executeQuery();
                pagado = 0;

                while (rs.next()) {
                    iva00 = rs.getDouble("VFACTURA") - (rs.getDouble("BASETDR") + rs.getDouble("IMPORTETDR"));
                    porcentaje = 100 - (iva00 / rs.getDouble("VFACTURA"));
                    pagado = pagado + rs.getDouble("BASETP") * (porcentaje / 100);
                }

                //MJSV 10112023 ADECUACION PARA VALIDAR RETENCIONES 
                if (porcentaje >= 100) {
                    iva00 = 0;
                } else {

                    ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getPagCam());
                    ps.setInt(1, pagoCliente.getTransactionProcess());

                    rs = ps.executeQuery();

                    if (rs.next()) {
                        iva00 = rs.getDouble("MONTO") - Double.parseDouble(dec.format(pagado));
                    }
                }

            }

            //Truncar Decimales
            if (tc == 1) {
                baseTP = Double.parseDouble(dec.format(baseTP));
                impuestos = Double.parseDouble(dec.format(impuestos));
                iva00 = Double.parseDouble(dec.format(iva00));
            } else {
                baseTP = Double.parseDouble(dec.format(baseTP * tc));
                impuestos = Double.parseDouble(dec.format(impuestos * tc));
                iva00 = Double.parseDouble(dec.format(iva00 * tc));
            }

            setLd_totalTrasladosBaseIVA16(baseTP);
            setLd_totalTrasladosImpuestoIVA16(impuestos);
            setLd_totalTrasladosBaseIVA0(iva00);
            setLd_totalTrasladosImpuestoIVA0(0);

            //Control de Retenciones 
            ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getImpuestos());
            ps.setString(1, "RET16");
            ps.setInt(2, pagoCliente.getTransactionProcess());

            rs = ps.executeQuery();
            baseTP = 0;

            while (rs.next()) {
                baseTP = baseTP + rs.getDouble("BASETP");
            }

            ps.close();
            BPCSConn.close();

            if (baseTP != 0) {
                setLd_totalRetencionesIVA(baseTP * .16);
            } else {
                setLd_totalRetencionesIVA(0);
            }

            //Otras retenciones que no aplican
            setLs_totalRetencionesISR("0");
            setLs_totalRetencionesIEPS("0");
            setLs_totalTrasladosBaseIVA8("0");
            setLs_totalTrasladosBaseIVAexento("0");

        } catch (SQLException e) {
            System.out.println("Error en Totales40DAO: " + e.getMessage());
            Logger.getLogger(Totales40DAO.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private Connection getConnection() throws SQLException {
        return Pool.getConnection();
    }
}
