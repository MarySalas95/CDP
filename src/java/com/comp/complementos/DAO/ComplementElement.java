package com.comp.complementos.DAO;

import com.comp.complementos.DTO.PagoCliente;

/**
 *
 * @author msalas
 */
public abstract class ComplementElement {
    
    protected PagoCliente pagoCliente;
    
    public abstract void fill();
    public abstract String createTXT();
    
}
