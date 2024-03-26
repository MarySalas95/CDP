package com.complementos;

import com.comp.complementos.DB.ConsultasFacade;
import com.comp.complementos.DB.Pool;
import com.comp.complementos.DTO.PagoCliente;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author msalas
 */
public class ConsultarCDP {

    private Connection BPCSConn = null;

    public void sendComplementos(int FechaI, int FechaF) {

        try {

            PagoCliente pago = new PagoCliente();
            BPCSConn = getConnection();

            System.out.println("Llamando a sendComplementos....");

            PreparedStatement ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getComplementos());
            ps.setInt(1, FechaI);
            ps.setInt(2, FechaF);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                if (rs.getInt("TRANSACCION") == pago.getTransactionProcess()) {

                } else {
                    System.out.println("Transaccion: " + rs.getInt("TRANSACCION"));
                    
                    pago.setTransactionProcess(rs.getInt("TRANSACCION"));

                    ComplementoPagoBuilder builder = new ComplementoPago20Builder(pago);
                    ComplementoPago complemento = builder.buildComplemento();
                    complemento.fill();

                    //Validar Estado del CDP
                    ps = BPCSConn.prepareStatement(ConsultasFacade.instance().getEdoCDP());
                    ps.setInt(1, pago.getTransactionProcess());

                    ResultSet rsEdo = ps.executeQuery();

                    if (rsEdo.next()) {
                        if (rsEdo.getString("CESTD").trim().equals("SI")) {
                            //Documento ya timbrado
                        } else {
                            if (rsEdo.getString("CTEXT").trim().equals("")) {
                                try {
                                    String contenido = complemento.createTXT();

                                    String nomArchivo = "S" + rsEdo.getString("CFOLIO") + ".txt";
                                    File archivo = new File(nomArchivo);
                                    if (!archivo.exists()) {
                                        archivo.createNewFile();
                                    }
                                    FileWriter fw = new FileWriter(archivo);
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    bw.write(contenido);
                                    bw.close();

                                    //Actualizar estado del TXT
                                    ps = BPCSConn.prepareStatement(ConsultasFacade.instance().setUpdateEdoTXT());
                                    ps.setString(1, "SI");
                                    ps.setInt(2, pago.getTransactionProcess());
                                    ps.executeUpdate();

                                    ftp(archivo, nomArchivo, "192.168.50.21", "BPCS", "B3llot@2022");
                                    archivo.delete();

                                } catch (IOException | SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }

            }

            ps.close();
            BPCSConn.close();
            System.out.println("Termina llamada sendComplementos");

        } catch (SQLException e) {
            System.out.println("Error en SendComplementos");
            try {
                BPCSConn.rollback();
                System.out.println("Error en Consultas: " + e.getMessage());
            } catch (SQLException ex) {
                Logger.getLogger(ConsultarCDP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void ftp(File archivo, String nomArchivo, String servidor, String user, String pass) {

        FTPClient ftpClient = new FTPClient();

        try {

            System.out.println("Creando TXT " + nomArchivo);
            ftpClient.connect(servidor, 21);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            File firstLocalFile = archivo;
            String firstRemoteFile = nomArchivo;
            InputStream inputStream = new FileInputStream(firstLocalFile);
            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();

        } catch (IOException ex) {
            System.out.println("Error en FTP = " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return Pool.getConnection();
    }

}
