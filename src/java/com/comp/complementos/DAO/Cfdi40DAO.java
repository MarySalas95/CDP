package com.comp.complementos.DAO;

/**
 *
 * @author msalas
 */
public class Cfdi40DAO extends ComplementElement {

    private int li_folio;
    private String ls_serie;
    private String ls_subtotal;
    private String ls_descuento;
    private String ls_moneda;
    private String ls_total;
    private String ls_tipoComprobante;
    private String ls_lugarExpedicion;
    private String ls_exportacion;
    //public String ls_fecha, ls_tipoCambio, ls_metodoPago, ls_mensajePDF; //Adicionales

    /**
     * @return the li_folio
     */
    public int getLi_folio() {
        return li_folio;
    }

    /**
     * @param li_folio the li_folio to set
     */
    public void setLi_folio(int li_folio) {
        this.li_folio = li_folio;
    }

    /**
     * @return the ls_serie
     */
    public String getLs_serie() {
        return ls_serie;
    }

    /**
     * @param ls_serie the ls_serie to set
     */
    public void setLs_serie(String ls_serie) {
        this.ls_serie = ls_serie;
    }

    /**
     * @return the ls_subtotal
     */
    public String getLs_subtotal() {
        return ls_subtotal;
    }

    /**
     * @param ls_subtotal the ls_subtotal to set
     */
    public void setLs_subtotal(String ls_subtotal) {
        this.ls_subtotal = ls_subtotal;
    }

    /**
     * @return the ls_descuento
     */
    public String getLs_descuento() {
        return ls_descuento;
    }

    /**
     * @param ls_descuento the ls_descuento to set
     */
    public void setLs_descuento(String ls_descuento) {
        this.ls_descuento = ls_descuento;
    }

    /**
     * @return the ls_moneda
     */
    public String getLs_moneda() {
        return ls_moneda;
    }

    /**
     * @param ls_moneda the ls_moneda to set
     */
    public void setLs_moneda(String ls_moneda) {
        this.ls_moneda = ls_moneda;
    }

    /**
     * @return the ls_total
     */
    public String getLs_total() {
        return ls_total;
    }

    /**
     * @param ls_total the ls_total to set
     */
    public void setLs_total(String ls_total) {
        this.ls_total = ls_total;
    }

    /**
     * @return the ls_tipoComprobante
     */
    public String getLs_tipoComprobante() {
        return ls_tipoComprobante;
    }

    /**
     * @param ls_tipoComprobante the ls_tipoComprobante to set
     */
    public void setLs_tipoComprobante(String ls_tipoComprobante) {
        this.ls_tipoComprobante = ls_tipoComprobante;
    }

    /**
     * @return the ls_lugarExpedicion
     */
    public String getLs_lugarExpedicion() {
        return ls_lugarExpedicion;
    }

    /**
     * @param ls_lugarExpedicion the ls_lugarExpedicion to set
     */
    public void setLs_lugarExpedicion(String ls_lugarExpedicion) {
        this.ls_lugarExpedicion = ls_lugarExpedicion;
    }

    /**
     * @return the ls_exportacion
     */
    public String getLs_exportacion() {
        return ls_exportacion;
    }

    /**
     * @param ls_exportacion the ls_exportacion to set
     */
    public void setLs_exportacion(String ls_exportacion) {
        this.ls_exportacion = ls_exportacion;
    }

    @Override
    public String createTXT() {

        String txt = "";

        txt += "[CFD40]\r\n";
        txt += "FOLIO;" + getLi_folio() + "\r\n";
        //txt += "FECHA;"+getFecha()+"\r\n";
        txt += "SERIE;" + getLs_serie() + "\r\n";
        txt += "SUBTOTAL;" + getLs_subtotal() + "\r\n";
        txt += "DESCUENTO;" + getLs_descuento() + "\r\n";
        txt += "MONEDA;" + getLs_moneda() + "\r\n";
        //txt += "TIPOCAMBIO;" + getTipoCambio() + "\r\n";
        txt += "TOTAL;" + getLs_total() + "\r\n";
        //txt += "METODOPAGO;" + getMetodoPago() + "\r\n";
        txt += "TIPODECOMPROBANTE;" + getLs_tipoComprobante() + "\r\n";
        txt += "LUGAREXPEDICION;" + getLs_lugarExpedicion() + "\r\n";
        //txt += "MENSAJEPDF;"+getMensajePDF()+"\r\n";
        txt += "EXPORTACION;" + getLs_exportacion() + "\r\n\n";

        return txt;

    }
    
    @Override
    public void fill(){
        
    }

}
